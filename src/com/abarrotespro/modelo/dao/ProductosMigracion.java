package com.abarrotespro.modelo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.abarrotespro.modelo.Producto;

import config.Conexion;

/**
 * Migra el catalogo generado desde src/img hacia MySQL cuando la tabla productos esta vacia.
 */
public final class ProductosMigracion {

    private ProductosMigracion() {
    }

    public static int migrarSiVacio(List<Producto> catalogo) throws SQLException {
        if (catalogo == null || catalogo.isEmpty()) {
            return 0;
        }
        if (!tablaProductosVacia()) {
            return 0;
        }

        String sql = """
                INSERT INTO productos (codigo_barras, nombre, imagen, precio_venta, stock_actual, stock_minimo)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = Conexion.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            con.setAutoCommit(false);
            try {
                for (Producto p : catalogo) {
                    ps.setString(1, ProductosDAO.generarCodigoBarras(p));
                    ps.setString(2, p.getNombre());
                    ps.setString(3, ProductosDAO.nombreArchivoImagen(p.getRutaImagen()));
                    ps.setDouble(4, p.getPrecioVenta());
                    ps.setInt(5, p.getStockActual());
                    ps.setInt(6, Math.max(0, p.getStockMinimo()));
                    ps.addBatch();
                }
                int[] resultados = ps.executeBatch();
                con.commit();
                return resultados.length;
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public static boolean tablaProductosVacia() throws SQLException {
        return new ProductosDAO().tablaVacia();
    }

    public static List<Producto> cargarDesdeBaseDatos() throws SQLException {
        return new ProductosDAO().listarTodos();
    }
}
