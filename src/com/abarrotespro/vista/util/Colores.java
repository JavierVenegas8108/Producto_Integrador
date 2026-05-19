package com.abarrotespro.vista.util;

import java.awt.Color;

/**
 * Paleta de colores del sistema Abarrotes Pro.
 */
public final class Colores {

    private static boolean modoOscuro;

    public static Color FONDO_APP = new Color(245, 247, 250);
    public static Color FONDO_LOGIN = new Color(236, 240, 245);
    public static Color BLANCO = Color.WHITE;
    public static Color AZUL_PRIMARIO = new Color(37, 99, 235);
    public static Color AZUL_OSCURO = new Color(30, 64, 175);
    public static Color AZUL_CLARO = new Color(219, 234, 254);
    public static Color GRIS_TEXTO = new Color(100, 116, 139);
    public static Color GRIS_BORDE = new Color(226, 232, 240);
    public static Color GRIS_SIDEBAR = new Color(248, 250, 252);
    public static Color AMARILLO = new Color(234, 179, 8);
    public static Color AMARILLO_CLARO = new Color(254, 243, 199);
    public static Color VERDE = new Color(34, 197, 94);
    public static Color VERDE_CLARO = new Color(220, 252, 231);
    public static Color ROJO = new Color(239, 68, 68);
    public static Color ROJO_OSCURO = new Color(185, 28, 28);
    public static Color NEGRO_TEXTO = new Color(30, 41, 59);
    public static Color SIDEBAR_ACTIVO = new Color(239, 246, 255);
    public static Color FONDO_SIDEBAR = Color.WHITE;
    public static Color FONDO_BARRA = Color.WHITE;
    public static Color FONDO_TARJETA = Color.WHITE;
    public static Color FONDO_LINEA_TICKET = new Color(248, 250, 252);

    private static final Color[] CLARO = {
            new Color(245, 247, 250), new Color(236, 240, 245), Color.WHITE,
            new Color(37, 99, 235), new Color(30, 64, 175), new Color(219, 234, 254),
            new Color(100, 116, 139), new Color(226, 232, 240), new Color(248, 250, 252),
            new Color(34, 197, 94), new Color(220, 252, 231),
            new Color(239, 68, 68), new Color(185, 28, 28),
            new Color(30, 41, 59), new Color(239, 246, 255),
            Color.WHITE, Color.WHITE, Color.WHITE, new Color(248, 250, 252)
    };

    private Colores() {
    }

    public static boolean esModoOscuro() {
        return modoOscuro;
    }

    public static void establecerModoOscuro(boolean oscuro) {
        modoOscuro = oscuro;
        Color[] p = oscuro ? paletaOscura() : CLARO;
        FONDO_APP = p[0];
        FONDO_LOGIN = p[1];
        BLANCO = p[2];
        AZUL_PRIMARIO = p[3];
        AZUL_OSCURO = p[4];
        AZUL_CLARO = p[5];
        GRIS_TEXTO = p[6];
        GRIS_BORDE = p[7];
        GRIS_SIDEBAR = p[8];
        VERDE = p[9];
        VERDE_CLARO = p[10];
        ROJO = p[11];
        ROJO_OSCURO = p[12];
        NEGRO_TEXTO = p[13];
        SIDEBAR_ACTIVO = p[14];
        FONDO_SIDEBAR = p[15];
        FONDO_BARRA = p[16];
        FONDO_TARJETA = p[17];
        FONDO_LINEA_TICKET = p[18];
    }

    private static Color[] paletaOscura() {
        return new Color[]{
                new Color(30, 41, 59),
                new Color(15, 23, 42),
                new Color(51, 65, 85),
                new Color(96, 165, 250),
                new Color(59, 130, 246),
                new Color(30, 58, 95),
                new Color(148, 163, 184),
                new Color(71, 85, 105),
                new Color(51, 65, 85),
                new Color(74, 222, 128),
                new Color(22, 78, 55),
                new Color(248, 113, 113),
                new Color(220, 38, 38),
                new Color(241, 245, 249),
                new Color(30, 58, 95),
                new Color(30, 41, 59),
                new Color(30, 41, 59),
                new Color(51, 65, 85),
                new Color(51, 65, 85)
        };
    }
}
