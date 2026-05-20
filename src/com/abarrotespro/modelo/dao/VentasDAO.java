package com.abarrotespro.modelo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.abarrotespro.modelo.LineaVenta;
import com.abarrotespro.modelo.MetodoPago;
import com.abarrotespro.modelo.Producto;
import com.abarrotespro.modelo.TipoMovimientoCaja;
import com.abarrotespro.modelo.Venta;

import config.Conexion;

/**
 * Acceso a datos de ventas, surtido de inventario y movimientos de caja.
 * Usa PreparedStatement y transacciones para garantizar consistencia.
 */
public class VentasDAO {

    /**
     * Suma unidades al stock_actual del producto (surtido de mercancia).
     *
     * @return filas afectadas
     */
    public int actualizarStock(int productoId, int cantidad) throws SQLException {
        String sql = "UPDATE productos SET stock_actual = stock_actual + ? WHERE id = ?";
        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, productoId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Registra un egreso en movimientos_caja.
     * Nota: la tabla cajas solo tiene id/estado; no se actualizan montos ahi por ahora.
     */
    public void registrarEgreso(int cajaId, double monto, String concepto) throws SQLException {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto del egreso debe ser positivo");
        }
        String insertMovimiento = """
                INSERT INTO movimientos_caja (caja_id, tipo, monto, concepto, fecha_hora)
                VALUES (?, 'EGRESO', ?, ?, ?)
                """;

        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement psMov = con.prepareStatement(insertMovimiento)) {
            psMov.setInt(1, cajaId);
            psMov.setDouble(2, monto);
            psMov.setString(3, concepto);
            psMov.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            psMov.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Registra el ingreso de una venta en movimientos_caja (opcional, no interrumpe el cobro).
     * Totales en tabla cajas (ingresos/egresos/total_caja) deshabilitados: esa tabla aun no los tiene.
     */
    public void registrarIngresoVenta(int cajaId, double monto, MetodoPago metodoPago, int ventaId) {
        if (monto <= 0) {
            return;
        }
        String concepto = "Venta #" + ventaId + " - " + metodoPago.getEtiqueta();
        String insertMovimiento = """
                INSERT INTO movimientos_caja (caja_id, tipo, monto, concepto, fecha_hora)
                VALUES (?, 'INGRESO', ?, ?, ?)
                """;

        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement psMov = con.prepareStatement(insertMovimiento)) {
            psMov.setInt(1, cajaId);
            psMov.setDouble(2, monto);
            psMov.setString(3, concepto);
            psMov.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            psMov.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            // No relanzar: la venta ya quedo en ventas/detalle_ventas; el movimiento de caja es complementario.
        }
    }

    /**
     * Persiste la venta, sus detalles y descuenta stock de cada producto vendido.
     *
     * @return id generado de la venta en MySQL
     */
    public int finalizarVenta(int cajaId, Venta venta, MetodoPago metodoPago,
            double montoRecibido, double cambio) throws SQLException {
        return registrarVenta(cajaId, venta, metodoPago, montoRecibido, cambio);
    }

    /**
     * Registra el cobro en ventas y detalle_ventas (columnas segun esquema real de MySQL).
     */
    public int registrarVenta(int cajaId, Venta venta, MetodoPago metodoPago,
            double montoRecibido, double cambio) throws SQLException {
        if (venta == null || venta.estaVacia()) {
            throw new IllegalArgumentException("La venta no tiene productos");
        }

        String insertVenta = """
                INSERT INTO ventas (caja_id, fecha_hora, total, metodo_pago, monto_recibido, cambio)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        String insertDetalle = """
                INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio_unitario)
                VALUES (?, ?, ?, ?)
                """;
        String updateStock = """
                UPDATE productos
                SET stock_actual = stock_actual - ?
                WHERE id = ? AND stock_actual >= ?
                """;

        double total = venta.getTotal();
        LocalDateTime fechaHora = venta.getFechaHora() != null
                ? venta.getFechaHora() : LocalDateTime.now();

        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                int ventaId;
                try (PreparedStatement psVenta = con.prepareStatement(insertVenta, Statement.RETURN_GENERATED_KEYS)) {
                    psVenta.setInt(1, cajaId);
                    psVenta.setTimestamp(2, Timestamp.valueOf(fechaHora));
                    psVenta.setDouble(3, total);
                    psVenta.setString(4, metodoPago.name());
                    psVenta.setDouble(5, montoRecibido);
                    psVenta.setDouble(6, cambio);
                    psVenta.executeUpdate();

                    try (ResultSet rs = psVenta.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("No se obtuvo el id de la venta insertada");
                        }
                        ventaId = rs.getInt(1);
                    }
                }

                try (PreparedStatement psDetalle = con.prepareStatement(insertDetalle);
                        PreparedStatement psStock = con.prepareStatement(updateStock)) {
                    for (LineaVenta linea : venta.getLineas()) {
                        Producto producto = linea.getProducto();
                        int cantidad = linea.getCantidad();
                        double precioUnitario = linea.getPrecioVentaUnitario();

                        psDetalle.setInt(1, ventaId);
                        psDetalle.setInt(2, producto.getId());
                        psDetalle.setInt(3, cantidad);
                        psDetalle.setDouble(4, precioUnitario);
                        psDetalle.addBatch();

                        psStock.setInt(1, cantidad);
                        psStock.setInt(2, producto.getId());
                        psStock.setInt(3, cantidad);
                        psStock.addBatch();
                    }
                    psDetalle.executeBatch();
                    int[] resultadosStock = psStock.executeBatch();
                    for (int filas : resultadosStock) {
                        if (filas == 0) {
                            throw new SQLException("Stock insuficiente para completar la venta");
                        }
                    }
                }

                con.commit();
                return ventaId;
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Obtiene el id de la caja con estado abierto (1 = abierta).
     */
    public Optional<Integer> obtenerCajaAbiertaId() throws SQLException {
        String sql = "SELECT id FROM cajas WHERE estado = 1 ORDER BY id DESC LIMIT 1";
        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(rs.getInt("id"));
            }
            return Optional.empty();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Consulta la ultima venta registrada con su detalle (para ticket).
     */
    public Optional<DatosTicketVenta> consultarUltimaVenta() throws SQLException {
        String sqlVenta = """
                SELECT v.id, v.caja_id, v.total, v.fecha_hora,
                       v.metodo_pago, v.monto_recibido, v.cambio
                FROM ventas v
                ORDER BY v.id DESC
                LIMIT 1
                """;
        String sqlDetalle = """
                SELECT d.cantidad, d.precio_unitario, p.nombre
                FROM detalle_ventas d
                INNER JOIN productos p ON p.id = d.producto_id
                WHERE d.venta_id = ?
                """;

        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement psVenta = con.prepareStatement(sqlVenta);
                ResultSet rsVenta = psVenta.executeQuery()) {

            if (!rsVenta.next()) {
                return Optional.empty();
            }

            int ventaId = rsVenta.getInt("id");
            int cajaId = rsVenta.getInt("caja_id");
            double total = rsVenta.getDouble("total");
            LocalDateTime fecha = rsVenta.getTimestamp("fecha_hora").toLocalDateTime();
            String metodoPago = rsVenta.getString("metodo_pago");
            double montoRecibido = rsVenta.getDouble("monto_recibido");
            double cambio = rsVenta.getDouble("cambio");

            List<LineaTicket> lineas = new ArrayList<>();
            try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle)) {
                psDetalle.setInt(1, ventaId);
                try (ResultSet rsDetalle = psDetalle.executeQuery()) {
                    while (rsDetalle.next()) {
                        int cantidad = rsDetalle.getInt("cantidad");
                        double precioUnitario = rsDetalle.getDouble("precio_unitario");
                        double subtotal = cantidad * precioUnitario;
                        lineas.add(new LineaTicket(
                                rsDetalle.getString("nombre"),
                                cantidad,
                                precioUnitario,
                                subtotal));
                    }
                }
            }
            return Optional.of(new DatosTicketVenta(
                    ventaId, cajaId, total, fecha, lineas, metodoPago, montoRecibido, cambio));
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /** Datos minimos para imprimir ticket desde la ultima venta en BD. */
    public record DatosTicketVenta(
            int ventaId,
            int cajaId,
            double total,
            LocalDateTime fecha,
            List<LineaTicket> lineas,
            String metodoPago,
            double montoRecibido,
            double cambio) {
    }

    public record LineaTicket(String nombre, int cantidad, double precioUnitario, double subtotal) {
    }
}
