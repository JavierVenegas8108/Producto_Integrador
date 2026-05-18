package com.abarrotespro.modelo;

/**
 * Representa un producto del inventario del punto de venta.
 */
public class Producto {

    private int id;
    private String nombre;
    private double precio;
    private int stock;
    private String categoria;
    private String emoji;
    private int stockMinimo;
    
    public Producto(int id, String nombre, double precio, int stock, String categoria, String emoji) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
        this.stockMinimo = stockMinimo;
        this.emoji = emoji;
    }
    
    public Producto(String nombre, double precio, int stock, int stockMinimo, String emoji) {
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.stockMinimo = stockMinimo; 
        this.emoji = emoji;
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

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
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
    /** Reduce el stock si hay unidades disponibles. */
    public boolean reducirStock(int cantidad) {
        if (cantidad <= 0 || stock < cantidad) {
            return false;
        }
        stock -= cantidad;
        return true;
    }

    /** Aumenta el stock del producto. */
    public void aumentarStock(int cantidad) {
        if (cantidad > 0) {
            stock += cantidad;
        }
    }

    public boolean tieneStock() {
        return stock > 0;
    }
}
