package com.abarrotespro.vista.panel;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.abarrotespro.vista.util.Colores;

/**
 * Panel placeholder para modulos aun no implementados.
 */
public class PanelEnDesarrollo extends JPanel {

    public PanelEnDesarrollo(String nombreModulo) {
        setBackground(Colores.FONDO_APP);
        setLayout(new GridBagLayout());

        JPanel centro = new JPanel();
        centro.setOpaque(false);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));

        JLabel icono = new JLabel("▦", SwingConstants.CENTER);
        icono.setFont(new Font("Segoe UI", Font.PLAIN, 72));
        icono.setForeground(Colores.GRIS_BORDE);
        icono.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel texto = new JLabel("Modulo " + nombreModulo + " en desarrollo");
        texto.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        texto.setForeground(Colores.GRIS_TEXTO);
        texto.setAlignmentX(Component.CENTER_ALIGNMENT);
        texto.setBorder(new EmptyBorder(16, 0, 0, 0));

        centro.add(icono);
        centro.add(texto);

        add(centro, new GridBagConstraints());
    }
}
