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
    private final double fondoInicial;
    private final double ingresosEfectivo;
    private final double ingresosTarjeta;
    private final double ingresosTransferencia;
    private final double egresosEfectivo;
    private final boolean cajaAbierta;

    public EstadoPersistido(List<Producto> productos, List<Venta> ventas, List<Corte> cortes,
            List<Proveedor> proveedores, double totalEnCaja, double entradasManuales,
            int contadorVentas, int contadorProductos, int contadorCortes, int contadorProveedores) {
        this(productos, ventas, cortes, proveedores, totalEnCaja, entradasManuales,
                contadorVentas, contadorProductos, contadorCortes, contadorProveedores,
                totalEnCaja, 0, 0, 0, 0, true);
    }

    public EstadoPersistido(List<Producto> productos, List<Venta> ventas, List<Corte> cortes,
            List<Proveedor> proveedores, double totalEnCaja, double entradasManuales,
            int contadorVentas, int contadorProductos, int contadorCortes, int contadorProveedores,
            double fondoInicial, double ingresosEfectivo, double ingresosTarjeta,
            double ingresosTransferencia, double egresosEfectivo, boolean cajaAbierta) {
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
        this.fondoInicial = fondoInicial;
        this.ingresosEfectivo = ingresosEfectivo;
        this.ingresosTarjeta = ingresosTarjeta;
        this.ingresosTransferencia = ingresosTransferencia;
        this.egresosEfectivo = egresosEfectivo;
        this.cajaAbierta = cajaAbierta;
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

    public double getFondoInicial() {
        return fondoInicial;
    }

    public double getIngresosEfectivo() {
        return ingresosEfectivo;
    }

    public double getIngresosTarjeta() {
        return ingresosTarjeta;
    }

    public double getIngresosTransferencia() {
        return ingresosTransferencia;
    }

    public double getEgresosEfectivo() {
        return egresosEfectivo;
    }

    public boolean isCajaAbierta() {
        return cajaAbierta;
    }
}