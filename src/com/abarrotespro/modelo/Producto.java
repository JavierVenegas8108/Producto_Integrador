package com.abarrotespro.modelo;

/**
 * Representa un producto del inventario del punto de venta.
 */
public class Producto {

    private int id;
    private String nombre;
    private double precioCompra;
    private double precioVenta;
    private int stock;
    private String categoria;
    private String emoji;
    private int stockMinimo;
    private String rutaImagen;
    private int idProveedor;

    public Producto(int id, String nombre, double precioVenta, int stock, String categoria, String emoji) {
        this(id, nombre, precioVenta, precioVenta, stock, categoria, emoji, 0, 0, null);
    }

    public Producto(int id, String nombre, double precioCompra, double precioVenta,
            int stock, String categoria, String emoji, int stockMinimo, int idProveedor, String rutaImagen) {
        this.id = id;
        this.nombre = nombre;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.stock = stock;
        this.categoria = categoria;
        this.emoji = emoji;
        this.stockMinimo = stockMinimo;
        this.idProveedor = idProveedor;
        this.rutaImagen = rutaImagen;
    }

    public Producto(String nombre, double precioVenta, int stock, int stockMinimo, String emoji) {
        this.nombre = nombre;
        this.precioCompra = precioVenta;
        this.precioVenta = precioVenta;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.emoji = emoji;
    }

    /** Compatibilidad: precio de venta al publico. */
    public double getPrecio() {
        return precioVenta;
    }

    public void setPrecio(double precio) {
        this.precioVenta = precio;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(double precioCompra) {
        this.precioCompra = precioCompra;
    }

    public double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public void setRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public boolean reducirStock(int cantidad) {
        if (cantidad <= 0 || stock < cantidad) {
            return false;
        }
        stock -= cantidad;
        return true;
    }

    public void aumentarStock(int cantidad) {
        if (cantidad > 0) {
            stock += cantidad;
        }
    }

    public boolean tieneStock() {
        return stock > 0;
    }

    /** Nivel de alerta de stock para la tabla de inventario. */
    public NivelAlertaStock getNivelAlertaStock() {
        if (stockMinimo <= 0) {
            return stock <= 0 ? NivelAlertaStock.CRITICO : NivelAlertaStock.NORMAL;
        }
        if (stock <= stockMinimo) {
            return NivelAlertaStock.CRITICO;
        }
        int umbralPreventivo = stockMinimo + Math.max(2, stockMinimo / 2);
        if (stock <= umbralPreventivo) {
            return NivelAlertaStock.PREVENTIVO;
        }
        return NivelAlertaStock.NORMAL;
    }

    public enum NivelAlertaStock {
        NORMAL, PREVENTIVO, CRITICO
    }
}
