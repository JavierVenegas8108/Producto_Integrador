package com.abarrotespro.excepcion;

/**
 * Se lanza cuando el efectivo recibido (billetes acumulados) no cubre el total a cobrar.
 */
public class DineroInsuficienteException extends Exception {

    private final double montoRequerido;
    private final double montoRecibido;

    public DineroInsuficienteException(double montoRequerido, double montoRecibido) {
        super(String.format(
                "Dinero insuficiente: se requieren $%.2f y solo hay $%.2f en efectivo recibido.",
                montoRequerido, montoRecibido));
        this.montoRequerido = montoRequerido;
        this.montoRecibido = montoRecibido;
    }

    public double getMontoRequerido() {
        return montoRequerido;
    }

    public double getMontoRecibido() {
        return montoRecibido;
    }
}
