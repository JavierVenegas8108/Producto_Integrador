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

    /**
     * Interpreta texto de busqueda como ID numerico (ej. 1, 00001).
     * @return el ID o null si no es un valor numerico valido
     */
    public static Integer parsearIdBusqueda(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String limpio = texto.trim();
        if (!limpio.matches("\\d+")) {
            return null;
        }
        try {
            return Integer.parseInt(limpio);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
