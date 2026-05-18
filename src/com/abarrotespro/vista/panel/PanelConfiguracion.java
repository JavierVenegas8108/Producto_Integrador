package com.abarrotespro.vista.panel;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.abarrotespro.vista.util.Colores;
import com.abarrotespro.vista.util.ComponentesUi;
import com.abarrotespro.vista.util.TemaUi;

/**
 * Modulo de configuracion: modo oscuro y cierre de sesion.
 */
public class PanelConfiguracion extends JPanel {

    private JToggleButton toggleModoOscuro;
    private final JButton botonCerrarSesion;

    public PanelConfiguracion() {
        setBackground(Colores.FONDO_APP);
        setLayout(new GridBagLayout());

        JPanel tarjeta = ComponentesUi.crearPanelRedondeado(Colores.FONDO_TARJETA, 16);
        tarjeta.putClientProperty("tema.decorado", Boolean.TRUE);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(new EmptyBorder(32, 40, 32, 40));
        tarjeta.setPreferredSize(new Dimension(480, 320));

        JLabel titulo = new JLabel("Configuracion del sistema");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setForeground(Colores.NEGRO_TEXTO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel("Personaliza la apariencia y la sesion de usuario");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitulo.setForeground(Colores.GRIS_TEXTO);
        subtitulo.putClientProperty(TemaUi.PROP_SUBTITULO, Boolean.TRUE);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel filaOscuro = crearFilaModoOscuro();
        filaOscuro.setAlignmentX(Component.LEFT_ALIGNMENT);

        botonCerrarSesion = ComponentesUi.crearBotonRojo("Cerrar Sesion");
        botonCerrarSesion.setAlignmentX(Component.LEFT_ALIGNMENT);
        botonCerrarSesion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        tarjeta.add(titulo);
        tarjeta.add(Box.createVerticalStrut(6));
        tarjeta.add(subtitulo);
        tarjeta.add(Box.createVerticalStrut(28));
        tarjeta.add(filaOscuro);
        tarjeta.add(Box.createVerticalStrut(32));
        tarjeta.add(botonCerrarSesion);

        add(tarjeta, new GridBagConstraints());
    }

    private JPanel crearFilaModoOscuro() {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel etiqueta = new JLabel("Modo Oscuro");
        etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 14));
        etiqueta.setForeground(Colores.NEGRO_TEXTO);

        toggleModoOscuro = new JToggleButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                boolean on = isSelected();
                g2.setColor(on ? Colores.AZUL_PRIMARIO : Colores.GRIS_BORDE);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
                int diam = h - 6;
                int x = on ? w - diam - 3 : 3;
                g2.setColor(Color.WHITE);
                g2.fillOval(x, 3, diam, diam);
                g2.dispose();
            }
        };
        toggleModoOscuro.setPreferredSize(new Dimension(52, 28));
        toggleModoOscuro.setFocusPainted(false);
        toggleModoOscuro.setBorderPainted(false);
        toggleModoOscuro.setContentAreaFilled(false);
        toggleModoOscuro.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        fila.add(etiqueta);
        fila.add(toggleModoOscuro);
        return fila;
    }

    public JToggleButton getToggleModoOscuro() {
        return toggleModoOscuro;
    }

    public JButton getBotonCerrarSesion() {
        return botonCerrarSesion;
    }
}
