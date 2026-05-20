package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestiona la conexion JDBC con MySQL para Abarrotes Pro.
 * Ajuste URL, usuario y contrasena segun su entorno local.
 */
public final class Conexion {

    private static final String URL =
            "jdbc:mysql://localhost:3306/abarrotes_pro?useSSL=false&serverTimezone=America/Mexico_City";
    private static final String USUARIO = "root";
    private static final String CONTRASENA = "1234";

    private Conexion() {
    }

    /**
     * Obtiene una conexion nueva. El llamador debe cerrarla en un try-with-resources.
     */
    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }

    /**
     * Verifica que el servidor y la base de datos respondan.
     */
    public static boolean probarConexion() {
        try (Connection con = obtenerConexion()) {
            return con != null && !con.isClosed();
        } catch (SQLException e) {
            System.err.println("Error de conexion MySQL: " + e.getMessage());
            return false;
        }
    }
}
