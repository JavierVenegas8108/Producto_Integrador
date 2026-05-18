package com.abarrotespro.modelo;

/**
 * Usuario del sistema con credenciales de acceso.
 */
public class Usuario {

    private String nombreUsuario;
    private String contrasena;
    private String nombreCompleto;
    private String iniciales;

    public Usuario(String nombreUsuario, String contrasena, String nombreCompleto, String iniciales) {
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.nombreCompleto = nombreCompleto;
        this.iniciales = iniciales;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getIniciales() {
        return iniciales;
    }

    /** Valida credenciales de inicio de sesion. */
    public boolean validarAcceso(String usuario, String contrasenaIngresada) {
        return this.nombreUsuario.equalsIgnoreCase(usuario)
                && this.contrasena.equals(contrasenaIngresada);
    }
}
