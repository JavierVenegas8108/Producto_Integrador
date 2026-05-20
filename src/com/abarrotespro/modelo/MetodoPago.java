package com.abarrotespro.modelo;

/**
 * Forma de pago aceptada en caja y ventas.
 */
public enum MetodoPago {
    EFECTIVO,
    TARJETA,
    TRANSFERENCIA;

    /** Etiqueta legible para ticket e interfaz. */
    public String getEtiqueta() {
        return switch (this) {
            case EFECTIVO -> "Efectivo";
            case TARJETA -> "Tarjeta";
            case TRANSFERENCIA -> "Transferencia";
        };
    }
}
