package com.abarrotespro.vista.util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Iconos vectoriales dibujados para la interfaz (sidebar y tabla).
 */
public final class IconosUi {

    public enum TipoIcono {
        LAPIZ, BASURA, VENTA, INVENTARIO, TICKETS, PROVEEDORES, CORTE, CONFIGURACION,
        USUARIO, CANDADO, BUSQUEDA, OJO
    }

    private IconosUi() {
    }

    public static Icon crear(TipoIcono tipo, int tamano, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.translate(x, y);
                switch (tipo) {
                    case LAPIZ -> dibujarLapiz(g2, tamano);
                    case BASURA -> dibujarBasura(g2, tamano);
                    case VENTA -> dibujarCarrito(g2, tamano);
                    case INVENTARIO -> dibujarCaja(g2, tamano);
                    case TICKETS -> dibujarRecibo(g2, tamano);
                    case PROVEEDORES -> dibujarCamion(g2, tamano);
                    case CORTE -> dibujarPesos(g2, tamano);
                    case CONFIGURACION -> dibujarEngrane(g2, tamano);
                    case USUARIO -> dibujarUsuario(g2, tamano);
                    case CANDADO -> dibujarCandado(g2, tamano);
                    case BUSQUEDA -> dibujarLupa(g2, tamano);
                    case OJO -> dibujarOjo(g2, tamano);
                    default -> { }
                }
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return tamano;
            }

            @Override
            public int getIconHeight() {
                return tamano;
            }
        };
    }

    public static JButton crearBotonIcono(TipoIcono tipo, Color color, String tooltip) {
        JButton boton = new JButton(crear(tipo, 16, color));
        boton.setToolTipText(tooltip);
        boton.setPreferredSize(new Dimension(32, 32));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    private static void dibujarLapiz(Graphics2D g2, int s) {
        float w = s;
        float h = s;
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine((int) (w * 0.65), (int) (h * 0.15), (int) (w * 0.2), (int) (h * 0.6));
        g2.drawLine((int) (w * 0.2), (int) (h * 0.6), (int) (w * 0.12), (int) (h * 0.88));
        g2.drawLine((int) (w * 0.35), (int) (h * 0.75), (int) (w * 0.12), (int) (h * 0.88));
        g2.fillPolygon(
                new int[]{(int) (w * 0.55), (int) (w * 0.72), (int) (w * 0.45)},
                new int[]{(int) (h * 0.25), (int) (h * 0.42), (int) (h * 0.52)}, 3);
    }

    private static void dibujarBasura(Graphics2D g2, int s) {
        float w = s;
        float h = s;
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine((int) (w * 0.25), (int) (h * 0.22), (int) (w * 0.75), (int) (h * 0.22));
        g2.drawLine((int) (w * 0.35), (int) (h * 0.22), (int) (w * 0.38), (int) (h * 0.12));
        g2.drawLine((int) (w * 0.65), (int) (h * 0.22), (int) (w * 0.62), (int) (h * 0.12));
        g2.draw(new RoundRectangle2D.Float(w * 0.28f, h * 0.28f, w * 0.44f, h * 0.58f, 3, 3));
        g2.drawLine((int) (w * 0.42), (int) (h * 0.4), (int) (w * 0.4), (int) (h * 0.78));
        g2.drawLine((int) (w * 0.5), (int) (h * 0.4), (int) (w * 0.5), (int) (h * 0.78));
        g2.drawLine((int) (w * 0.58), (int) (h * 0.4), (int) (w * 0.6), (int) (h * 0.78));
    }

    private static void dibujarCarrito(Graphics2D g2, int s) {
        float w = s;
        float h = s;
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D carrito = new Path2D.Float();
        carrito.moveTo(w * 0.15, h * 0.35);
        carrito.lineTo(w * 0.25, h * 0.35);
        carrito.lineTo(w * 0.35, h * 0.7);
        carrito.lineTo(w * 0.78, h * 0.7);
        carrito.lineTo(w * 0.85, h * 0.4);
        carrito.lineTo(w * 0.32, h * 0.4);
        carrito.closePath();
        g2.draw(carrito);
        g2.fillOval((int) (w * 0.38), (int) (h * 0.72), (int) (w * 0.12), (int) (h * 0.12));
        g2.fillOval((int) (w * 0.62), (int) (h * 0.72), (int) (w * 0.12), (int) (h * 0.12));
    }

    private static void dibujarCaja(Graphics2D g2, int s) {
        float w = s;
        float h = s;
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new RoundRectangle2D.Float(w * 0.2f, h * 0.35f, w * 0.6f, h * 0.5f, 4, 4));
        g2.drawLine((int) (w * 0.2), (int) (h * 0.35), (int) (w * 0.5), (int) (h * 0.2));
        g2.drawLine((int) (w * 0.8), (int) (h * 0.35), (int) (w * 0.5), (int) (h * 0.2));
        g2.drawLine((int) (w * 0.5), (int) (h * 0.2), (int) (w * 0.5), (int) (h * 0.85));
    }

    private static void dibujarRecibo(Graphics2D g2, int s) {
        float w = s;
        float h = s;
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new RoundRectangle2D.Float(w * 0.28f, h * 0.12f, w * 0.44f, h * 0.76f, 3, 3));
        for (int i = 0; i < 4; i++) {
            float y = h * (0.28f + i * 0.14f);
            g2.drawLine((int) (w * 0.36), (int) y, (int) (w * 0.64), (int) y);
        }
    }

    private static void dibujarCamion(Graphics2D g2, int s) {
        float w = s;
        float h = s;
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new RoundRectangle2D.Float(w * 0.12f, h * 0.38f, w * 0.55f, h * 0.32f, 3, 3));
        g2.draw(new RoundRectangle2D.Float(w * 0.55f, h * 0.45f, w * 0.28f, h * 0.25f, 3, 3));
        g2.fillOval((int) (w * 0.22), (int) (h * 0.68), (int) (w * 0.14), (int) (h * 0.14));
        g2.fillOval((int) (w * 0.62), (int) (h * 0.68), (int) (w * 0.14), (int) (h * 0.14));
    }

    private static void dibujarPesos(Graphics2D g2, int s) {
        g2.setFont(new Font("Segoe UI", Font.BOLD, s - 4));
        FontMetrics fm = g2.getFontMetrics();
        String texto = "$";
        int x = (s - fm.stringWidth(texto)) / 2;
        int y = (s - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(texto, x, y);
    }

    private static void dibujarUsuario(Graphics2D g2, int s) {
        float w = s;
        float h = s;
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawOval((int) (w * 0.32), (int) (h * 0.12), (int) (w * 0.36), (int) (h * 0.36));
        Path2D cuerpo = new Path2D.Float();
        cuerpo.moveTo(w * 0.22, h * 0.88);
        cuerpo.curveTo(w * 0.22, h * 0.58, w * 0.78, h * 0.58, w * 0.78, h * 0.88);
        g2.draw(cuerpo);
    }

    private static void dibujarCandado(Graphics2D g2, int s) {
        float w = s;
        float h = s;
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new RoundRectangle2D.Float(w * 0.28f, h * 0.48f, w * 0.44f, h * 0.38f, 4, 4));
        g2.draw(new RoundRectangle2D.Float(w * 0.36f, h * 0.18f, w * 0.28f, h * 0.34f, 8, 8));
        g2.drawLine((int) (w * 0.5), (int) (h * 0.62), (int) (w * 0.5), (int) (h * 0.72));
    }

    private static void dibujarOjo(Graphics2D g2, int s) {
        float w = s;
        float h = s;
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawOval((int) (w * 0.2), (int) (h * 0.28), (int) (w * 0.6), (int) (h * 0.38));
        g2.fillOval((int) (w * 0.42), (int) (h * 0.4), (int) (w * 0.16), (int) (h * 0.16));
    }

    private static void dibujarLupa(Graphics2D g2, int s) {
        float w = s;
        float h = s;
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawOval((int) (w * 0.18), (int) (h * 0.18), (int) (w * 0.48), (int) (h * 0.48));
        g2.drawLine((int) (w * 0.58), (int) (h * 0.58), (int) (w * 0.82), (int) (h * 0.82));
    }

    private static void dibujarEngrane(Graphics2D g2, int s) {
        float cx = s / 2f;
        float cy = s / 2f;
        float r = s * 0.32f;
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawOval((int) (cx - r), (int) (cy - r), (int) (2 * r), (int) (2 * r));
        g2.drawOval((int) (cx - r * 0.4), (int) (cy - r * 0.4), (int) (2 * r * 0.4), (int) (2 * r * 0.4));
        for (int i = 0; i < 6; i++) {
            double ang = Math.toRadians(i * 60);
            int x1 = (int) (cx + Math.cos(ang) * r * 0.55);
            int y1 = (int) (cy + Math.sin(ang) * r * 0.55);
            int x2 = (int) (cx + Math.cos(ang) * r * 0.95);
            int y2 = (int) (cy + Math.sin(ang) * r * 0.95);
            g2.drawLine(x1, y1, x2, y2);
        }
    }
}
