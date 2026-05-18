package com.abarrotespro.modelo;

/**
 * Linea individual dentro de un ticket de venta.
 */
public class LineaVenta {

    private Producto producto;
    private int cantidad;

    public LineaVenta(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public void aumentarCantidad(int unidades) {
        cantidad += unidades;
    }

    public double getSubtotal() {
        return producto.getPrecio() * cantidad;
    }
}
