package com.abarrotespro.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una venta o ticket activo o cerrado.
 */
public class Venta {

    /** Tasa de IVA aplicada al subtotal (16% Mexico). */
    public static final double TASA_IVA = 0.16;

    private int id;
    private List<LineaVenta> lineas;
    private LocalDateTime fechaHora;
    private String usuarioNombre;
    private boolean cerrada;
    private double descuento;

    public Venta(int id, String usuarioNombre) {
        this.id = id;
        this.usuarioNombre = usuarioNombre;
        this.lineas = new ArrayList<>();
        this.fechaHora = LocalDateTime.now();
        this.cerrada = false;
    }

    public int getId() {
        return id;
    }

    public List<LineaVenta> getLineas() {
        return lineas;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public boolean isCerrada() {
        return cerrada;
    }

    public void setCerrada(boolean cerrada) {
        this.cerrada = cerrada;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    /** Agrega un producto al ticket o incrementa su cantidad. */
    public void agregarProducto(Producto producto, int cantidad) {
        agregarLinea(new LineaVenta(producto, cantidad));
    }

    public void agregarLinea(LineaVenta linea) {
        for (LineaVenta existente : lineas) {
            if (existente.getProducto().getId() == linea.getProducto().getId()) {
                existente.aumentarCantidad(linea.getCantidad());
                return;
            }
        }
        lineas.add(linea);
    }

    public double getUtilidad() {
        return lineas.stream().mapToDouble(LineaVenta::getUtilidad).sum();
    }

    public double getCostoTotal() {
        return lineas.stream().mapToDouble(LineaVenta::getCostoTotal).sum();
    }

    public void eliminarLinea(int indice) {
        if (indice >= 0 && indice < lineas.size()) {
            lineas.remove(indice);
        }
    }

    /** Suma de importes de linea sin impuestos. */
    public double getSubtotal() {
        return lineas.stream().mapToDouble(LineaVenta::getSubtotal).sum();
    }

    public double getMontoIva() {
        return getSubtotal() * TASA_IVA;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = Math.max(0, descuento);
    }

    /** Total final: subtotal + IVA - descuento. */
    public double getTotal() {
        return getSubtotal() + getMontoIva() - descuento;
    }

    public int getCantidadTotalArticulos() {
        return lineas.stream().mapToInt(LineaVenta::getCantidad).sum();
    }

    public boolean estaVacia() {
        return lineas.isEmpty();
    }

    public void limpiar() {
        lineas.clear();
    }
}
