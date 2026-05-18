package com.abarrotespro.vista.panel;

import com.abarrotespro.modelo.Producto;
import com.abarrotespro.vista.util.Colores;
import com.abarrotespro.vista.util.ComponentesUi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

/**
 * Tarjeta visual de un producto en el modulo de venta.
 */
public class TarjetaProducto extends JPanel {

    public TarjetaProducto(Producto producto, ActionListener alAgregar) {
        setOpaque(false);
        setPreferredSize(new Dimension(170, 200));
        setLayout(new BorderLayout());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel tarjeta = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.setColor(Colores.GRIS_BORDE);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        tarjeta.setOpaque(false);
        tarjeta.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel emoji = new JLabel(producto.getEmoji(), SwingConstants.CENTER);
        emoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel nombre = new JLabel("<html><center>" + producto.getNombre() + "</center></html>");
        nombre.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nombre.setForeground(Colores.NEGRO_TEXTO);
        nombre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel precio = new JLabel(ComponentesUi.formatearMoneda(producto.getPrecio()));
        precio.setFont(new Font("Segoe UI", Font.BOLD, 14));
        precio.setForeground(Colores.AZUL_PRIMARIO);
        precio.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel stock = ComponentesUi.crearEtiquetaStock(producto.getStock());
        stock.setAlignmentX(Component.CENTER_ALIGNMENT);

        info.add(nombre);
        info.add(Box.createVerticalStrut(8));
        info.add(precio);
        info.add(Box.createVerticalStrut(6));
        info.add(stock);

        tarjeta.add(emoji, BorderLayout.NORTH);
        tarjeta.add(info, BorderLayout.CENTER);
        add(tarjeta);

        if (producto.tieneStock()) {
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    alAgregar.actionPerformed(
                            new java.awt.event.ActionEvent(producto, producto.getId(), "agregar"));
                }
            });
        } else {
            setEnabled(false);
            tarjeta.setForeground(Colores.GRIS_TEXTO);
        }
    }
}
