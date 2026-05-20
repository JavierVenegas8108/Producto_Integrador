package com.abarrotespro.vista.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.JTableHeader;

/**
 * Aplica el tema claro/oscuro recorriendo el arbol de componentes Swing.
 */
public final class TemaUi {

    public static final String PROP_FONDO = "tema.fondo";
    public static final String PROP_SUBTITULO = "tema.subtitulo";

    private TemaUi() {
    }

    public static void aplicarModoOscuro(boolean oscuro) {
        Colores.establecerModoOscuro(oscuro);
    }

    public static void recorrer(Component raiz) {
        if (raiz == null) {
            return;
        }
        if (raiz instanceof JComponent jc) {
            aplicarAComponente(jc);
        }
        if (raiz instanceof Container cont) {
            for (Component hijo : cont.getComponents()) {
                recorrer(hijo);
            }
        }
    }

    private static void aplicarAComponente(JComponent comp) {
        if (comp instanceof JPanel panel) {
            if (Boolean.TRUE.equals(panel.getClientProperty("tema.decorado"))) {
                panel.repaint();
            } else if (panel.isOpaque()) {
                panel.setBackground(colorFondoPanel(panel));
            }
        } else if (comp instanceof JLabel lbl) {
            aplicarEtiqueta(lbl);
        } else if (comp instanceof JTextField || comp instanceof JPasswordField) {
            comp.setBackground(Colores.BLANCO);
            comp.setForeground(Colores.NEGRO_TEXTO);
            if (comp.getBorder() instanceof LineBorder) {
                comp.setBorder(javax.swing.BorderFactory.createLineBorder(Colores.GRIS_BORDE, 1, true));
            }
        } else if (comp instanceof JTextArea area) {
            area.setBackground(Colores.BLANCO);
            area.setForeground(Colores.NEGRO_TEXTO);
        } else if (comp instanceof JScrollPane scroll) {
            scroll.getViewport().setBackground(Colores.FONDO_APP);
            scroll.setBackground(Colores.FONDO_APP);
        } else if (comp instanceof JTable tabla) {
            tabla.setBackground(Colores.BLANCO);
            tabla.setForeground(Colores.NEGRO_TEXTO);
            tabla.setGridColor(Colores.GRIS_BORDE);
            JTableHeader header = tabla.getTableHeader();
            if (header != null) {
                header.setBackground(Colores.GRIS_SIDEBAR);
                header.setForeground(Colores.NEGRO_TEXTO);
            }
        } else if (comp instanceof JList<?> lista) {
            lista.setBackground(Colores.BLANCO);
            lista.setForeground(Colores.NEGRO_TEXTO);
            lista.setSelectionBackground(Colores.SIDEBAR_ACTIVO);
            lista.setSelectionForeground(Colores.NEGRO_TEXTO);
        } else if (comp instanceof JButton btn && btn.isContentAreaFilled()
                && !"nav".equals(btn.getClientProperty(PROP_FONDO))) {
            btn.setBackground(Colores.GRIS_SIDEBAR);
            btn.setForeground(Colores.NEGRO_TEXTO);
        }

        if (comp.getBorder() instanceof MatteBorder) {
            comp.setBorder(javax.swing.BorderFactory.createMatteBorder(
                    0, 0, 0, 1, Colores.GRIS_BORDE));
        }
    }

    private static void aplicarEtiqueta(JLabel lbl) {
        if ("iconoNav".equals(lbl.getName())) {
            return;
        }
        Color fg = lbl.getForeground();
        if (fg == Colores.AZUL_PRIMARIO || fg == Colores.VERDE || fg == Colores.ROJO) {
            return;
        }
        if (Boolean.TRUE.equals(lbl.getClientProperty(PROP_SUBTITULO))) {
            lbl.setForeground(Colores.GRIS_TEXTO);
        } else {
            lbl.setForeground(Colores.NEGRO_TEXTO);
        }
    }

    public static Color colorFondoPanel(JPanel panel) {
        Object rol = panel.getClientProperty(PROP_FONDO);
        if (rol instanceof String nombreRol) {
            return switch (nombreRol) {
                case "sidebar" -> Colores.FONDO_SIDEBAR;
                case "barra" -> Colores.FONDO_BARRA;
                case "contenido" -> Colores.FONDO_APP;
                case "area" -> Colores.FONDO_APP;
                default -> Colores.FONDO_APP;
            };
        }
        if (Colores.esModoOscuro()) {
            return Colores.FONDO_APP;
        }
        Color actual = panel.getBackground();
        if (esColorOscuro(actual)) {
            return Colores.FONDO_APP;
        }
        return actual != null ? actual : Colores.FONDO_APP;
    }

    private static boolean esColorOscuro(Color c) {
        if (c == null) {
            return false;
        }
        int brillo = c.getRed() + c.getGreen() + c.getBlue();
        return brillo < 380;
    }
}
