package com.abarrotespro.modelo;

import java.time.LocalDateTime;

/**
 * Registro atomico de entrada o salida de dinero en caja.
 */
public class MovimientoCaja {

    private final int id;
    private final TipoMovimientoCaja tipo;
    private final double monto;
    private final MetodoPago metodoPago;
    private final String concepto;
    private final LocalDateTime timestamp;

    public MovimientoCaja(int id, TipoMovimientoCaja tipo, double monto,
            MetodoPago metodoPago, String concepto, LocalDateTime timestamp) {
        this.id = id;
        this.tipo = tipo;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.concepto = concepto;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public TipoMovimientoCaja getTipo() {
        return tipo;
    }

    public double getMonto() {
        return monto;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public String getConcepto() {
        return concepto;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
