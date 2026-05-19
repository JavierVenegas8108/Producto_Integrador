package com.abarrotespro.modelo.dto;

public class FilaReporteVenta {

    private final String fechaVenta;
    private final String horaVenta;
    private final int numeroTicket;
    private final String producto;
    private final int cantidad;
    private final double precioUnitario;
    private final double importeLinea;
    private final double totalTicket;
    private final String usuario;

    public FilaReporteVenta(String fechaVenta, String horaVenta, int numeroTicket, String producto,
            int cantidad, double precioUnitario, double importeLinea, double totalTicket, String usuario) {
        this.fechaVenta = fechaVenta;
        this.horaVenta = horaVenta;
        this.numeroTicket = numeroTicket;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.importeLinea = importeLinea;
        this.totalTicket = totalTicket;
        this.usuario = usuario;
    }

    public String getFechaVenta() {
        return fechaVenta;
    }

    public String getHoraVenta() {
        return horaVenta;
    }

    public int getNumeroTicket() {
        return numeroTicket;
    }

    public String getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public double getImporteLinea() {
        return importeLinea;
    }

    public double getTotalTicket() {
        return totalTicket;
    }

    public String getUsuario() {
        return usuario;
    }
}
