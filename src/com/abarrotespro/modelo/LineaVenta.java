package com.abarrotespro.modelo;

/**
 * Linea individual dentro de un ticket de venta.
 */
public class LineaVenta {

    private Producto producto;
    private int cantidad;
    private final double precioVentaUnitario;
    private final double precioCompraUnitario;

    public LineaVenta(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioVentaUnitario = producto.getPrecioVenta();
        this.precioCompraUnitario = producto.getPrecioCompra();
    }

    public LineaVenta(Producto producto, int cantidad, double precioVentaUnitario, double precioCompraUnitario) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioVentaUnitario = precioVentaUnitario;
        this.precioCompraUnitario = precioCompraUnitario;
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

    public double getPrecioVentaUnitario() {
        return precioVentaUnitario;
    }

    public double getPrecioCompraUnitario() {
        return precioCompraUnitario;
    }

    public double getSubtotal() {
        return precioVentaUnitario * cantidad;
    }

    public double getCostoTotal() {
        return precioCompraUnitario * cantidad;
    }

    public double getUtilidad() {
        return getSubtotal() - getCostoTotal();
    }
}
