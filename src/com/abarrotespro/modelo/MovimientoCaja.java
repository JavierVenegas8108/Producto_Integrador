package com.abarrotespro.modelo;

import java.time.LocalDateTime;

/**
 * POJO alineado con la tabla movimientos_caja de MySQL.
 * Campos: id, caja_id, tipo, monto, concepto, fecha.
 */
public class MovimientoCaja {

    private int id;
    private int cajaId;
    private TipoMovimientoCaja tipo;
    private double monto;
    private String concepto;
    private LocalDateTime fecha;
    /** Uso interno de la capa de negocio (no persiste en MySQL). */
    private MetodoPago metodoPago;

    public MovimientoCaja() {
        this.fecha = LocalDateTime.now();
    }

    /** Constructor para persistencia en MySQL. */
    public MovimientoCaja(int id, int cajaId, TipoMovimientoCaja tipo, double monto,
            String concepto, LocalDateTime fecha) {
        this.id = id;
        this.cajaId = cajaId;
        this.tipo = tipo;
        this.monto = monto;
        this.concepto = concepto;
        this.fecha = fecha != null ? fecha : LocalDateTime.now();
    }

    /** Compatibilidad con el libro de caja en memoria. */
    public MovimientoCaja(int id, TipoMovimientoCaja tipo, double monto,
            MetodoPago metodoPago, String concepto, LocalDateTime timestamp) {
        this(id, 0, tipo, monto, concepto, timestamp);
        this.metodoPago = metodoPago;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCajaId() {
        return cajaId;
    }

    public void setCajaId(int cajaId) {
        this.cajaId = cajaId;
    }

    public TipoMovimientoCaja getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimientoCaja tipo) {
        this.tipo = tipo;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    /** Alias usado por la capa de caja en memoria. */
    public LocalDateTime getTimestamp() {
        return fecha;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }
}
