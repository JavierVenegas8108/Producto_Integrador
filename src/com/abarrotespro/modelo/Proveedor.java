package com.abarrotespro.modelo;

/**
 * Representa un proveedor del negocio.
 */
public class Proveedor {

    private int id;
    private String razonSocial;
    private String nombreContacto;
    private String telefono;
    private String correo;
    private String direccion;
    private String diasVisita;
    private boolean activo;

    public Proveedor() {
        this.activo = true;
    }

    public Proveedor(int id, String razonSocial, String nombreContacto, String telefono,
            String correo, String direccion, String diasVisita, boolean activo) {
        this.id = id;
        this.razonSocial = razonSocial;
        this.nombreContacto = nombreContacto;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
        this.diasVisita = diasVisita;
        this.activo = activo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getNombreContacto() {
        return nombreContacto;
    }

    public void setNombreContacto(String nombreContacto) {
        this.nombreContacto = nombreContacto;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDiasVisita() {
        return diasVisita;
    }

    public void setDiasVisita(String diasVisita) {
        this.diasVisita = diasVisita;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getEstadoTexto() {
        return activo ? "Activo" : "Inactivo";
    }
}
