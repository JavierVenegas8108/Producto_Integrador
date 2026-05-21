package com.abarrotespro.modelo.dao;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.abarrotespro.modelo.Producto;

import config.Conexion;

/**
 * Acceso a datos de la tabla productos en MySQL.
 */
public class ProductosDAO {

    /**
     * Registra un producto nuevo en la base de datos.
     *
     * @return id generado (AUTO_INCREMENT)
     */
    public int registrarProducto(String codigoBarras, String nombre, String imagen,
            double precioVenta, int stockActual, int stockMinimo) throws SQLException {

        String sql = """
                INSERT INTO productos (codigo_barras, nombre, imagen, precio_venta, stock_actual, stock_minimo)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = Conexion.obtenerConexion();
            ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, codigoBarras);
            ps.setString(2, nombre);
            ps.setString(3, imagen);
            ps.setDouble(4, precioVenta);
            ps.setInt(5, stockActual);
            ps.setInt(6, stockMinimo);

            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (!rs.next()) {
                throw new SQLException("No se obtuvo el id del producto insertado");
            }
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            cerrar(rs);
            cerrar(ps);
            cerrar(con);
        }
    }

    /** Registra un producto a partir del modelo en memoria. */
    public int registrarProducto(Producto producto) throws SQLException {
        String codigo = producto.getCodigoBarras();
        if (codigo == null || codigo.isBlank()) {
            codigo = generarCodigoBarras(producto);
            producto.setCodigoBarras(codigo);
        }
        return registrarProducto(
                codigo,
                producto.getNombre(),
                nombreArchivoImagen(producto.getRutaImagen()),
                producto.getPrecioVenta(),
                producto.getStockActual(),
                Math.max(0, producto.getStockMinimo()));
    }

    /** Alias de compatibilidad. */
    public int insertarProducto(Producto producto) throws SQLException {
        return registrarProducto(producto);
    }

    /**
     * Actualiza todos los campos editables de un producto existente.
     *
     * @return filas afectadas (1 si se actualizo correctamente)
     */
    public int actualizarProducto(String codigoBarras, String nombre, String imagen,
            double precioVenta, int stockActual, int stockMinimo, int id) throws SQLException {

        String sql = """
                UPDATE productos
                SET codigo_barras = ?, nombre = ?, imagen = ?, precio_venta = ?,
                    stock_actual = ?, stock_minimo = ?
                WHERE id = ?
                """;

        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = Conexion.obtenerConexion();
            ps = con.prepareStatement(sql);

            ps.setString(1, codigoBarras);
            ps.setString(2, nombre);
            ps.setString(3, imagen);
            ps.setDouble(4, precioVenta);
            ps.setInt(5, stockActual);
            ps.setInt(6, stockMinimo);
            ps.setInt(7, id);

            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            cerrar(ps);
            cerrar(con);
        }
    }

    /** Actualiza un producto a partir del modelo en memoria. */
    public int actualizarProducto(Producto producto) throws SQLException {
        if (producto.getId() <= 0) {
            throw new IllegalArgumentException("El producto debe tener un id valido para actualizar");
        }
        String codigo = producto.getCodigoBarras();
        if (codigo == null || codigo.isBlank()) {
            codigo = generarCodigoBarras(producto);
            producto.setCodigoBarras(codigo);
        }
        return actualizarProducto(
                codigo,
                producto.getNombre(),
                nombreArchivoImagen(producto.getRutaImagen()),
                producto.getPrecioVenta(),
                producto.getStockActual(),
                Math.max(0, producto.getStockMinimo()),
                producto.getId());
    }

    /** Alias del metodo de edicion. */
    public int modificarProducto(Producto producto) throws SQLException {
        return actualizarProducto(producto);
    }

    /** Carga el catalogo completo desde MySQL. */
    public List<Producto> listarTodos() throws SQLException {
        String sql = """
                SELECT id, codigo_barras, nombre, imagen, precio_venta, stock_actual, stock_minimo
                FROM productos
                ORDER BY id
                """;

        List<Producto> lista = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = Conexion.obtenerConexion();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

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
                    p.setRutaImagen(rutaClasspathDesdeNombreArchivo(imagen));
                }
                p.setPrecioCompra(rs.getDouble("precio_venta"));
                lista.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            cerrar(rs);
            cerrar(ps);
            cerrar(con);
        }
        return lista;
    }

    public boolean tablaVacia() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM productos";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = Conexion.obtenerConexion();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total") == 0;
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            cerrar(rs);
            cerrar(ps);
            cerrar(con);
        }
    }

    public static String generarCodigoBarras(Producto producto) {
        String desdeImagen = nombreArchivoImagen(producto.getRutaImagen());
        if (desdeImagen != null && !desdeImagen.isBlank()) {
            int punto = desdeImagen.lastIndexOf('.');
            return punto > 0 ? desdeImagen.substring(0, punto) : desdeImagen;
        }
        String base = producto.getNombre() != null
                ? producto.getNombre().trim().toUpperCase(Locale.ROOT).replace(' ', '-')
                : "PROD";
        return base + "-" + System.currentTimeMillis();
    }

    /** Guarda en BD solo el nombre del archivo (ej. zucaritas.png). */
    public static String nombreArchivoImagen(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            return null;
        }
        String normalizada = ruta.replace('\\', '/');
        if (normalizada.startsWith("/img/")) {
            return normalizada.substring("/img/".length());
        }
        return Paths.get(normalizada).getFileName().toString();
    }

    public static String rutaClasspathDesdeNombreArchivo(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            return null;
        }
        if (nombreArchivo.startsWith("/img/")) {
            return nombreArchivo;
        }
        return "/img/" + nombreArchivo;
    }

    private static void cerrar(AutoCloseable recurso) {
        if (recurso != null) {
            try {
                recurso.close();
            } catch (Exception ignored) {
            }
        }
    }
}
