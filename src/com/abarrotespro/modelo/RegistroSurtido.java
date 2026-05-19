package com.abarrotespro.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Registro de una operacion de surtido de inventario.
 */
public class RegistroSurtido {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final LocalDateTime fechaHora;
    private final int productoId;
    private final String nombreProducto;
    private final int cantidad;
    private final double precioCompra;
    private final int proveedorId;
    private final String nombreProveedor;

    public RegistroSurtido(LocalDateTime fechaHora, int productoId, String nombreProducto,
            int cantidad, double precioCompra, int proveedorId, String nombreProveedor) {
        this.fechaHora = fechaHora;
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioCompra = precioCompra;
        this.proveedorId = proveedorId;
        this.nombreProveedor = nombreProveedor != null ? nombreProveedor : "—";
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getFechaFormateada() {
        return fechaHora.format(FORMATO);
    }

    public int getProductoId() {
        return productoId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioCompra() {
        return precioCompra;
    }

    public int getProveedorId() {
        return proveedorId;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public double getCostoTotal() {
        return precioCompra * cantidad;
    }
}
