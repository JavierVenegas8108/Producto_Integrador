package com.abarrotespro.controlador;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import com.abarrotespro.excepcion.DineroInsuficienteException;
import com.abarrotespro.modelo.EstadoVenta;
import com.abarrotespro.modelo.Inventario;
import com.abarrotespro.modelo.MetodoPago;
import com.abarrotespro.modelo.Producto;
import com.abarrotespro.modelo.Venta;
import com.abarrotespro.modelo.dao.VentasDAO;
import com.abarrotespro.modelo.dao.VentasDAO.DatosTicketVenta;

/**
 * Controlador MVC: enlaza las vistas Swing con {@link VentasDAO}.
 * Gestiona cobro, surtido, egresos de caja y generacion de ticket.
 */
public class VentasController {

    private static final Set<Integer> DENOMINACIONES_VALIDAS =
            Set.of(20, 50, 100, 200, 500, 1000);

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("es-MX"));
    private static final DateTimeFormatter HORA =
            DateTimeFormatter.ofPattern("HH:mm:ss", Locale.forLanguageTag("es-MX"));

    private final VentasDAO ventasDAO;
    private final Inventario inventario;

    private int cajaIdActual;
    private double montoAcumuladoEfectivo;

    /** Ultima venta cobrada (datos de pago para el ticket). */
    private Venta ultimaVentaCobrada;
    private MetodoPago ultimoMetodoPago;
    private double ultimoMontoRecibido;
    private double ultimoCambio;
    private int ultimaVentaIdDb;

    public VentasController(Inventario inventario) {
        this.ventasDAO = new VentasDAO();
        this.inventario = inventario;
        this.cajaIdActual = 1;
        this.montoAcumuladoEfectivo = 0;
        cargarCajaAbierta();
    }

    /** Intenta leer la caja abierta desde MySQL; si falla, usa id 1 por defecto. */
    public void cargarCajaAbierta() {
        try {
            Optional<Integer> id = ventasDAO.obtenerCajaAbiertaId();
            id.ifPresent(valor -> cajaIdActual = valor);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setCajaIdActual(int cajaId) {
        this.cajaIdActual = cajaId;
    }

    public int getCajaIdActual() {
        return cajaIdActual;
    }

    // --- Billetes y efectivo (dialogo de cobro) ---

    public void agregarBillete(int denominacion) {
        if (!DENOMINACIONES_VALIDAS.contains(denominacion)) {
            throw new IllegalArgumentException("Denominacion no valida: " + denominacion);
        }
        montoAcumuladoEfectivo += denominacion;
    }

    public void reiniciarAcumuladorEfectivo() {
        montoAcumuladoEfectivo = 0;
    }

    public void establecerMontoEfectivo(double monto) {
        montoAcumuladoEfectivo = Math.max(0, monto);
    }

    public double getMontoAcumuladoEfectivo() {
        return montoAcumuladoEfectivo;
    }

    /**
     * Valida el cobro segun metodo de pago y persiste la venta en MySQL.
     *
     * @return cambio entregado (solo efectivo)
     */
    public double confirmarCobro(Venta venta, MetodoPago metodoPago) throws DineroInsuficienteException, SQLException {
        validarVentaPendiente(venta);

        double total = venta.getTotal();
        double cambio = 0;
        double recibido = total;

        if (metodoPago == MetodoPago.EFECTIVO) {
            if (montoAcumuladoEfectivo < total) {
                throw new DineroInsuficienteException(total, montoAcumuladoEfectivo);
            }
            cambio = montoAcumuladoEfectivo - total;
            recibido = montoAcumuladoEfectivo;
            reiniciarAcumuladorEfectivo();
        }

        int ventaId = ventasDAO.registrarVenta(cajaIdActual, venta, metodoPago, recibido, cambio);
        ventasDAO.registrarIngresoVenta(cajaIdActual, total, metodoPago, ventaId);

        inventario.aplicarSalidaPorVenta(venta);

        venta.setMetodoPago(metodoPago);
        venta.setMontoRecibido(recibido);
        venta.setCambio(cambio);
        venta.setEstado(EstadoVenta.PAGADA);

        ultimaVentaCobrada = venta;
        ultimoMetodoPago = metodoPago;
        ultimoMontoRecibido = recibido;
        ultimoCambio = cambio;
        ultimaVentaIdDb = ventaId;

        return cambio;
    }

    /**
     * Surtido de inventario: suma stock en BD.
     *
     * @param pagarConCajaEfectivo si true, registra egreso en caja por el costo del surtido
     */
    public void ejecutarSurtido(int productoId, int cantidad, double costoTotal,
            boolean pagarConCajaEfectivo) throws SQLException {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }

        int filas = ventasDAO.actualizarStock(productoId, cantidad);
        if (filas == 0) {
            throw new SQLException("No se encontro el producto con id " + productoId);
        }

        inventario.buscarPorId(productoId).ifPresent(p -> p.aumentarStock(cantidad));

        if (pagarConCajaEfectivo && costoTotal > 0) {
            String concepto = "Surtido producto #" + productoId;
            ventasDAO.registrarEgreso(cajaIdActual, costoTotal, concepto);
        }
    }

    /**
     * Genera en consola el ticket de la ultima venta cobrada.
     * Efectivo: muestra "Pago con" y "Su cambio". Tarjeta/Transferencia: los omite.
     */
    public void imprimirTicketUltimaVenta() {
        if (ultimaVentaCobrada != null) {
            imprimirTicketConsola(ultimaVentaCobrada, ultimoMetodoPago, ultimoMontoRecibido, ultimoCambio, ultimaVentaIdDb);
            return;
        }
        try {
            Optional<DatosTicketVenta> datos = ventasDAO.consultarUltimaVenta();
            if (datos.isEmpty()) {
                System.out.println("No hay ventas registradas para imprimir ticket.");
                return;
            }
            imprimirTicketDesdeBd(datos.get());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void imprimirTicketConsola(Venta venta, MetodoPago metodo, double recibido,
            double cambio, int ventaIdDb) {
        System.out.println(generarContenidoTicket(
                ventaIdDb > 0 ? ventaIdDb : venta.getId(),
                venta.getFechaHora(),
                venta.getUsuarioNombre(),
                venta.getLineas().stream()
                        .map(l -> new VentasDAO.LineaTicket(
                                l.getProducto().getNombre(),
                                l.getCantidad(),
                                l.getPrecioVentaUnitario(),
                                l.getSubtotal()))
                        .toList(),
                venta.getCantidadTotalArticulos(),
                venta.getSubtotal(),
                venta.getMontoIva(),
                venta.getDescuento(),
                venta.getTotal(),
                metodo,
                recibido,
                cambio));
    }

    private void imprimirTicketDesdeBd(DatosTicketVenta datos) {
        MetodoPago metodo = parsearMetodoPago(datos.metodoPago());
        System.out.println(generarContenidoTicket(
                datos.ventaId(),
                datos.fecha(),
                "Cajero",
                datos.lineas(),
                datos.lineas().stream().mapToInt(VentasDAO.LineaTicket::cantidad).sum(),
                datos.lineas().stream().mapToDouble(VentasDAO.LineaTicket::subtotal).sum(),
                0,
                0,
                datos.total(),
                metodo,
                datos.montoRecibido(),
                datos.cambio()));
    }

    private static MetodoPago parsearMetodoPago(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return MetodoPago.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Arma el texto del ticket segun el formato de Ticket.jpeg.
     */
    public String generarContenidoTicket(int idTransaccion, java.time.LocalDateTime fechaHora,
            String atendio, java.util.List<VentasDAO.LineaTicket> lineas,
            int cantidadArticulos, double subtotal, double iva, double descuento, double total,
            MetodoPago metodoPago, double pagoCon, double cambio) {

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("           ABARROTES PRO\n");
        sb.append("========================================\n");
        sb.append("Fecha:              ").append(fechaHora.format(FECHA)).append("\n");
        sb.append("Hora:               ").append(fechaHora.format(HORA)).append("\n");
        sb.append("ID Transaccion:     ").append(idTransaccion).append("\n");
        sb.append("Atendio:            ").append(atendio).append("\n");
        sb.append("----------------------------------------\n");
        sb.append("DETALLE DE COMPRA\n");
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-22s %4s %8s %10s%n", "Producto", "Cant", "P.Unit", "Subtotal"));
        sb.append("----------------------------------------\n");

        for (VentasDAO.LineaTicket linea : lineas) {
            String nombre = truncar(linea.nombre(), 22);
            sb.append(String.format(Locale.US, "%-22s %4d %8.2f %10.2f%n",
                    nombre, linea.cantidad(), linea.precioUnitario(), linea.subtotal()));
        }

        sb.append("----------------------------------------\n");
        sb.append(String.format(Locale.US, "%-28s %12d%n", "Cantidad total articulos:", cantidadArticulos));
        sb.append(String.format(Locale.US, "%-28s %12.2f%n", "Subtotal:", subtotal));
        if (iva > 0) {
            sb.append(String.format(Locale.US, "%-28s %12.2f%n", "IVA (16%):", iva));
        }
        if (descuento > 0) {
            sb.append(String.format(Locale.US, "%-28s %12.2f%n", "Descuento:", descuento));
        }
        sb.append("----------------------------------------\n");
        sb.append(String.format(Locale.US, "%-28s %12.2f%n", "TOTAL FINAL:", total));

        if (metodoPago != null) {
            sb.append(String.format("%-28s %12s%n", "Metodo de Pago:", metodoPago.getEtiqueta()));
            if (metodoPago == MetodoPago.EFECTIVO) {
                sb.append(String.format(Locale.US, "%-28s $%11.2f%n", "Pago con:", pagoCon));
                sb.append(String.format(Locale.US, "%-28s $%11.2f%n", "Su cambio:", cambio));
            }
        }

        sb.append("========================================\n");
        sb.append("   Gracias por su preferencia!\n");
        sb.append("        Vuelva pronto.\n");
        sb.append("========================================\n");
        return sb.toString();
    }

    private void validarVentaPendiente(Venta venta) {
        if (venta.getEstado() != EstadoVenta.PENDIENTE) {
            throw new IllegalStateException("La venta ya fue procesada");
        }
        if (venta.estaVacia()) {
            throw new IllegalStateException("No hay productos en la venta");
        }
    }

    private static String truncar(String texto, int max) {
        if (texto == null) {
            return "";
        }
        if (texto.length() <= max) {
            return texto;
        }
        return texto.substring(0, max - 1) + ".";
    }

    public Venta getUltimaVentaCobrada() {
        return ultimaVentaCobrada;
    }
}
