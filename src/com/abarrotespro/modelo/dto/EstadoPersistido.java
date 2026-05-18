package com.abarrotespro.modelo.dto;

import java.util.ArrayList;
import java.util.List;

import com.abarrotespro.modelo.Corte;
import com.abarrotespro.modelo.Producto;
import com.abarrotespro.modelo.Proveedor;
import com.abarrotespro.modelo.Venta;

/**
 * DTO con el estado completo del punto de venta para persistencia en disco.
 */
public class EstadoPersistido {

    private final List<Producto> productos;
    private final List<Venta> ventas;
    private final List<Corte> cortes;
    private final List<Proveedor> proveedores;
    private final double totalEnCaja;
    private final double entradasManuales;
    private final int contadorVentas;
    private final int contadorProductos;
    private final int contadorCortes;
    private final int contadorProveedores;

    public EstadoPersistido(List<Producto> productos, List<Venta> ventas, List<Corte> cortes,
            List<Proveedor> proveedores, double totalEnCaja, double entradasManuales,
            int contadorVentas, int contadorProductos, int contadorCortes, int contadorProveedores) {
        this.productos = new ArrayList<>(productos);
        this.ventas = new ArrayList<>(ventas);
        this.cortes = new ArrayList<>(cortes);
        this.proveedores = new ArrayList<>(proveedores);
        this.totalEnCaja = totalEnCaja;
        this.entradasManuales = entradasManuales;
        this.contadorVentas = contadorVentas;
        this.contadorProductos = contadorProductos;
        this.contadorCortes = contadorCortes;
        this.contadorProveedores = contadorProveedores;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public List<Venta> getVentas() {
        return ventas;
    }

    public List<Corte> getCortes() {
        return cortes;
    }

    public double getTotalEnCaja() {
        return totalEnCaja;
    }

    public double getEntradasManuales() {
        return entradasManuales;
    }

    public int getContadorVentas() {
        return contadorVentas;
    }

    public int getContadorProductos() {
        return contadorProductos;
    }

    public int getContadorCortes() {
        return contadorCortes;
    }

    public List<Proveedor> getProveedores() {
        return proveedores;
    }

    public int getContadorProveedores() {
        return contadorProveedores;
    }
}
