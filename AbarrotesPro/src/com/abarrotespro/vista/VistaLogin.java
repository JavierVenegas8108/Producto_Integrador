package com.abarrotespro.vista;

import com.abarrotespro.vista.util.Colores;
import com.abarrotespro.vista.util.ComponentesUi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Pantalla de inicio de sesion del punto de venta.
 */
public class VistaLogin extends JFrame {

    private JTextField campoUsuario;
    private JPasswordField campoContrasena;
    private JButton botonEntrar;
    private JLabel etiquetaError;

    public VistaLogin() {
        configurarVentana();
        setContentPane(crearContenido());
    }

    private void configurarVentana() {
        setTitle("Abarrotes Pro - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Colores.FONDO_LOGIN);
    }

    private JPanel crearContenido() {
        JPanel raiz = new JPanel(new GridBagLayout());
        raiz.setBackground(Colores.FONDO_LOGIN);
        raiz.setBorder(new EmptyBorder(40, 40, 40, 40));

        JPanel tarjeta = ComponentesUi.crearPanelRedondeado(Colores.BLANCO, 20);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(new EmptyBorder(40, 45, 40, 45));
        tarjeta.setPreferredSize(new Dimension(380, 520));

        JLabel icono = new JLabel("🛒", SwingConstants.CENTER);
        icono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        icono.setForeground(Colores.AZUL_PRIMARIO);
        icono.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titulo = new JLabel("Abarrotes Pro");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titulo.setForeground(Colores.NEGRO_TEXTO);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel("Inicia sesion en el punto de venta");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitulo.setForeground(Colores.GRIS_TEXTO);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        tarjeta.add(icono);
        tarjeta.add(Box.createVerticalStrut(12));
        tarjeta.add(titulo);
        tarjeta.add(Box.createVerticalStrut(6));
        tarjeta.add(subtitulo);
        tarjeta.add(Box.createVerticalStrut(30));

        tarjeta.add(crearCampoConEtiqueta("Usuario", "👤", true));
        tarjeta.add(Box.createVerticalStrut(16));
        tarjeta.add(crearCampoConEtiqueta("Contrasena", "🔒", false));
        tarjeta.add(Box.createVerticalStrut(8));

        etiquetaError = new JLabel(" ");
        etiquetaError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        etiquetaError.setForeground(Colores.ROJO);
        etiquetaError.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(etiquetaError);
        tarjeta.add(Box.createVerticalStrut(12));

        botonEntrar = ComponentesUi.crearBotonPrimario("Entrar al Sistema", 48);
        botonEntrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        botonEntrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        tarjeta.add(botonEntrar);
        tarjeta.add(Box.createVerticalStrut(24));

        JLabel pie = new JLabel("V2.0 SAAS EDITION");
        pie.setFont(new Font("Segoe UI", Font.BOLD, 10));
        pie.setForeground(Colores.GRIS_TEXTO);
        pie.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(pie);

        raiz.add(tarjeta);
        return raiz;
    }

    private JPanel crearCampoConEtiqueta(String etiqueta, String icono, boolean esUsuario) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel lbl = new JLabel(icono + "  " + etiqueta);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(Colores.GRIS_TEXTO);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(6));

        if (esUsuario) {
            campoUsuario = ComponentesUi.crearCampoTexto("Ingresa tu usuario");
            campoUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            panel.add(campoUsuario);
        } else {
            campoContrasena = ComponentesUi.crearCampoContrasena();
            campoContrasena.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            panel.add(campoContrasena);
        }
        return panel;
    }

    public JTextField getCampoUsuario() {
        return campoUsuario;
    }

    public JPasswordField getCampoContrasena() {
        return campoContrasena;
    }

    public JButton getBotonEntrar() {
        return botonEntrar;
    }

    public void mostrarError(String mensaje) {
        etiquetaError.setText(mensaje);
    }

    public void limpiarError() {
        etiquetaError.setText(" ");
    }
}
