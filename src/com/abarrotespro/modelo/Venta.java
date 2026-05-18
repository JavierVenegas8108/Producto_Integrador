package com.abarrotespro.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una venta o ticket activo o cerrado.
 */
public class Venta {

    private int id;
    private List<LineaVenta> lineas;
    private LocalDateTime fechaHora;
    private String usuarioNombre;
    private boolean cerrada;

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
        for (LineaVenta linea : lineas) {
            if (linea.getProducto().getId() == producto.getId()) {
                linea.aumentarCantidad(cantidad);
                return;
            }
        }
        lineas.add(new LineaVenta(producto, cantidad));
    }

    public void eliminarLinea(int indice) {
        if (indice >= 0 && indice < lineas.size()) {
            lineas.remove(indice);
        }
    }

    public double getTotal() {
        return lineas.stream().mapToDouble(LineaVenta::getSubtotal).sum();
    }

    public boolean estaVacia() {
        return lineas.isEmpty();
    }

    public void limpiar() {
        lineas.clear();
    }
}
