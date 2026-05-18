package com.abarrotespro.vista.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Fabrica de componentes visuales reutilizables con estilo moderno.
 */
public final class ComponentesUi {

    private ComponentesUi() {
    }

    /** Panel con esquinas redondeadas y sombra suave. */
    public static JPanel crearPanelRedondeado(Color fondo, int radio) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fondo);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radio, radio));
                g2.dispose();
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
    }

    public static JButton crearBotonPrimario(String texto, int alto) {
        JButton boton = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(Colores.AZUL_OSCURO.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(Colores.AZUL_PRIMARIO.brighter());
                } else {
                    g2.setColor(Colores.AZUL_PRIMARIO);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        boton.setPreferredSize(new Dimension(200, alto));
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    public static JButton crearBotonRojo(String texto) {
        JButton boton = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color color = getModel().isRollover() ? Colores.ROJO.brighter() : Colores.ROJO;
                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        boton.setPreferredSize(new Dimension(300, 48));
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    public static JTextField crearCampoTexto(String placeholder) {
        JTextField campo = new JTextField();
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colores.GRIS_BORDE, 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        campo.putClientProperty("JTextField.placeholderText", placeholder);
        return campo;
    }

    public static JPasswordField crearCampoContrasena() {
        JPasswordField campo = new JPasswordField();
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colores.GRIS_BORDE, 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        return campo;
    }

    public static JLabel crearEtiquetaStock(int stock) {
        JLabel etiqueta = new JLabel("Stock: " + stock) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Colores.VERDE_CLARO);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setColor(Colores.VERDE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 11));
        etiqueta.setOpaque(false);
        etiqueta.setPreferredSize(new Dimension(90, 24));
        return etiqueta;
    }

    public static String formatearMoneda(double monto) {
        return String.format("$%,.2f", monto);
    }
}
