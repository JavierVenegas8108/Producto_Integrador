package com.abarrotespro.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Estado financiero de la caja registradora y libro de movimientos.
 */
public class Caja {

    private double fondoInicial;
    private double ingresosEfectivo;
    private double ingresosTarjeta;
    private double ingresosTransferencia;
    private double egresosEfectivo;
    private final List<MovimientoCaja> movimientos;
    private int contadorMovimientos;
    private boolean abierta;

    public Caja() {
        movimientos = new ArrayList<>();
        contadorMovimientos = 0;
        abierta = false;
    }

    public void abrirCaja(double fondoInicial) {
        if (fondoInicial < 0) {
            throw new IllegalArgumentException("El fondo inicial no puede ser negativo");
        }
        reiniciarTotales();
        this.fondoInicial = fondoInicial;
        this.abierta = true;
        registrarMovimiento(TipoMovimientoCaja.INGRESO, fondoInicial, MetodoPago.EFECTIVO,
                "Apertura de caja");
    }

    private void reiniciarTotales() {
        fondoInicial = 0;
        ingresosEfectivo = 0;
        ingresosTarjeta = 0;
        ingresosTransferencia = 0;
        egresosEfectivo = 0;
        movimientos.clear();
        contadorMovimientos = 0;
    }

    public void registrarIngreso(double monto, MetodoPago metodoPago, String concepto) {
        validarCajaAbierta();
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto de ingreso debe ser positivo");
        }
        switch (metodoPago) {
            case EFECTIVO -> ingresosEfectivo += monto;
            case TARJETA -> ingresosTarjeta += monto;
            case TRANSFERENCIA -> ingresosTransferencia += monto;
        }
        registrarMovimiento(TipoMovimientoCaja.INGRESO, monto, metodoPago, concepto);
    }

    public void registrarEgreso(double monto, MetodoPago metodoPago, String concepto) {
        validarCajaAbierta();
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto de egreso debe ser positivo");
        }
        if (metodoPago == MetodoPago.EFECTIVO) {
            egresosEfectivo += monto;
        }
        registrarMovimiento(TipoMovimientoCaja.EGRESO, monto, metodoPago, concepto);
    }

    private void registrarMovimiento(TipoMovimientoCaja tipo, double monto,
            MetodoPago metodoPago, String concepto) {
        contadorMovimientos++;
        movimientos.add(new MovimientoCaja(
                contadorMovimientos, tipo, monto, metodoPago, concepto, LocalDateTime.now()));
    }

    private void validarCajaAbierta() {
        if (!abierta) {
            throw new IllegalStateException("La caja no esta abierta");
        }
    }

    /** Fondo inicial + ingresos en efectivo - egresos en efectivo. */
    public double calcularEfectivoEsperado() {
        return fondoInicial + ingresosEfectivo - egresosEfectivo;
    }

    public double calcularVentasHoy(List<Venta> ventasPagadas) {
        return ventasPagadas.stream()
                .filter(v -> v.getEstado() == EstadoVenta.PAGADA)
                .mapToDouble(Venta::getMontoCobrable)
                .sum();
    }

    public double calcularPromedioTicket(List<Venta> ventasPagadas) {
        long pagadas = ventasPagadas.stream()
                .filter(v -> v.getEstado() == EstadoVenta.PAGADA)
                .count();
        if (pagadas == 0) {
            return 0;
        }
        return calcularVentasHoy(ventasPagadas) / pagadas;
    }

    public double calcularUtilidadHoy(List<Venta> ventasPagadas) {
        return ventasPagadas.stream()
                .filter(v -> v.getEstado() == EstadoVenta.PAGADA)
                .mapToDouble(Venta::getUtilidad)
                .sum();
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

    public List<MovimientoCaja> getMovimientos() {
        return Collections.unmodifiableList(movimientos);
    }

    public boolean isAbierta() {
        return abierta;
    }

    /** Restaura totales desde persistencia (sin registrar movimiento de apertura). */
    public void restaurarEstado(double fondoInicial, double ingresosEfectivo,
            double ingresosTarjeta, double ingresosTransferencia, double egresosEfectivo) {
        reiniciarTotales();
        this.fondoInicial = fondoInicial;
        this.ingresosEfectivo = ingresosEfectivo;
        this.ingresosTarjeta = ingresosTarjeta;
        this.ingresosTransferencia = ingresosTransferencia;
        this.egresosEfectivo = egresosEfectivo;
        this.abierta = true;
    }

    /** Reinicia la caja al cerrar el dia operativo. */
    public void cerrarDiaOperativo() {
        reiniciarTotales();
        abierta = false;
    }

    /** Revierte un ingreso por venta (devolucion). */
    public void revertirIngreso(double monto, MetodoPago metodoPago) {
        if (monto <= 0) {
            return;
        }
        switch (metodoPago) {
            case EFECTIVO -> ingresosEfectivo = Math.max(0, ingresosEfectivo - monto);
            case TARJETA -> ingresosTarjeta = Math.max(0, ingresosTarjeta - monto);
            case TRANSFERENCIA -> ingresosTransferencia = Math.max(0, ingresosTransferencia - monto);
        }
    }
}
