package com.abarrotespro.vista.util;

/**
 * Formato visual de identificadores para tablas (no usar en exportacion de reportes).
 */
public final class FormatoIdUtil {

    private static final int DIGITOS_ID = 5;

    private FormatoIdUtil() {
    }

    /** Formato visual con ceros a la izquierda (ej. 00001). */
    public static String formatearIdVisual(int id) {
        return String.format("%0" + DIGITOS_ID + "d", id);
    }
}
