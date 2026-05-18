package com.abarrotespro.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Registro de un corte de caja realizado al cerrar el dia.
 */
public class Corte {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private double monto;
    private LocalDateTime fechaHora;
    private String usuarioNombre;

    public Corte(double monto, String usuarioNombre) {
        this(monto, usuarioNombre, LocalDateTime.now());
    }

    public Corte(double monto, String usuarioNombre, LocalDateTime fechaHora) {
        this.monto = monto;
        this.fechaHora = fechaHora;
        this.usuarioNombre = usuarioNombre;
    }

    public double getMonto() {
        return monto;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public String getFechaFormateada() {
        return fechaHora.format(FORMATO);
    }
}
