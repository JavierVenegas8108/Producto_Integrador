package com.abarrotespro.modelo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.abarrotespro.modelo.Producto;

import config.Conexion;

/**
 * Migra el catalogo generado desde src/img hacia MySQL cuando la tabla productos esta vacia.
 */
public final class ProductosMigracion {

    private ProductosMigracion() {
    }

    /**
     * Si la tabla productos no tiene filas, inserta el catalogo en memoria.
     *
     * @return cantidad de productos insertados (0 si la tabla ya tenia datos)
     */
    public static int migrarSiVacio(List<Producto> catalogo) throws SQLException {
        if (catalogo == null || catalogo.isEmpty()) {
            return 0;
        }
        if (!tablaProductosVacia()) {
            return 0;
        }

        asegurarColumnaImagen();

        String sql = """
                INSERT INTO productos (codigo_barras, nombre, precio_venta, stock_actual, stock_minimo, imagen)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            con.setAutoCommit(false);
            try {
                for (Producto p : catalogo) {
                    ps.setString(1, generarCodigoBarras(p));
                    ps.setString(2, p.getNombre());
                    ps.setDouble(3, p.getPrecioVenta());
                    ps.setInt(4, p.getStockActual());
                    ps.setInt(5, Math.max(0, p.getStockMinimo()));
                    ps.setString(6, p.getRutaImagen());
                    ps.addBatch();
                }
                int[] resultados = ps.executeBatch();
                con.commit();
                return resultados.length;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    /** Indica si no hay productos registrados en MySQL. */
    public static boolean tablaProductosVacia() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM productos";
        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total") == 0;
            }
            return true;
        }
    }

    /** Carga todos los productos de MySQL con sus ids reales para ventas y stock. */
    public static List<Producto> cargarDesdeBaseDatos() throws SQLException {
        asegurarColumnaImagen();

        String sql = """
                SELECT id, codigo_barras, nombre, precio_venta, stock_actual, stock_minimo, imagen
                FROM productos
                ORDER BY id
                """;

        List<Producto> lista = new ArrayList<>();
        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Producto p = new Producto(
                        rs.getInt("id"),
                        rs.getString("codigo_barras"),
                        rs.getString("nombre"),
                        rs.getDouble("precio_venta"),
                        rs.getInt("stock_actual"),
                        rs.getInt("stock_minimo"));
                String imagen = rs.getString("imagen");
                if (imagen != null && !imagen.isBlank()) {
                    p.setRutaImagen(imagen);
                }
                p.setPrecioCompra(rs.getDouble("precio_venta"));
                lista.add(p);
            }
        }
        return lista;
    }

    /**
     * Crea la columna imagen si la tabla fue creada con el script anterior sin ese campo.
     */
    private static void asegurarColumnaImagen() throws SQLException {
        String alter = "ALTER TABLE productos ADD COLUMN imagen VARCHAR(255) NULL";
        try (Connection con = Conexion.obtenerConexion();
                Statement st = con.createStatement()) {
            st.execute(alter);
        } catch (SQLException e) {
            // 1060 = Duplicate column name (MySQL)
            if (e.getErrorCode() != 1060) {
                throw e;
            }
        }
    }

    private static String generarCodigoBarras(Producto producto) {
        String ruta = producto.getRutaImagen();
        if (ruta != null && !ruta.isBlank()) {
            String archivo = ruta.substring(ruta.lastIndexOf('/') + 1);
            int punto = archivo.lastIndexOf('.');
            return punto > 0 ? archivo.substring(0, punto) : archivo;
        }
        return "PROD-" + producto.getId();
    }
}
