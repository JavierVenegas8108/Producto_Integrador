package com.abarrotespro.vista;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.abarrotespro.vista.panel.PanelCorte;
import com.abarrotespro.vista.panel.PanelEnDesarrollo;
import com.abarrotespro.vista.panel.PanelInventario;
import com.abarrotespro.vista.panel.PanelVenta;
import com.abarrotespro.vista.panel.ProveedoresPanel;
import com.abarrotespro.vista.util.Colores;
import com.abarrotespro.vista.util.IconosUi;

/**
 * Ventana principal con sidebar, barra superior y paneles modulares (CardLayout).
 */
public class VistaPrincipal extends JFrame {

    public static final String CARD_VENTA = "venta";
    public static final String CARD_INVENTARIO = "inventario";
    public static final String CARD_TICKETS = "tickets";
    public static final String CARD_PROVEEDORES = "proveedores";
    public static final String CARD_CORTE = "corte";
    public static final String CARD_CONFIG = "configuracion";

    private final CardLayout cardLayout;
    private final JPanel panelContenido;
    private final JLabel etiquetaModulo;
    private final JLabel etiquetaUsuario;
    private final JLabel etiquetaIniciales;

    private final Map<String, JButton> botonesNav = new LinkedHashMap<>();

    private PanelVenta panelVenta;
    private PanelInventario panelInventario;
    private ProveedoresPanel panelProveedores;
    private PanelCorte panelCorte;
    private JButton botonCerrarSesion;

    public VistaPrincipal() {
        setTitle("Abarrotes Pro - Punto de Venta");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1100, 700));

        cardLayout = new CardLayout();
        panelContenido = new JPanel(cardLayout);
        panelContenido.setBackground(Colores.FONDO_APP);

        etiquetaModulo = new JLabel("Venta");
        etiquetaUsuario = new JLabel("Administrador");
        etiquetaIniciales = new JLabel("AD");

        construirModulos();
        setLayout(new BorderLayout());
        add(crearSidebar(), BorderLayout.WEST);
        add(crearAreaPrincipal(), BorderLayout.CENTER);
    }

    private void construirModulos() {
        panelVenta = new PanelVenta();
        panelInventario = new PanelInventario();
        panelProveedores = new ProveedoresPanel();
        panelCorte = new PanelCorte();

        panelContenido.add(panelVenta, CARD_VENTA);
        panelContenido.add(panelInventario, CARD_INVENTARIO);
        panelContenido.add(new PanelEnDesarrollo("Tickets"), CARD_TICKETS);
        panelContenido.add(panelProveedores, CARD_PROVEEDORES);
        panelContenido.add(panelCorte, CARD_CORTE);
        panelContenido.add(new PanelEnDesarrollo("Configuracion"), CARD_CONFIG);
    }

    private JPanel crearSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(Color.WHITE);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Colores.GRIS_BORDE));

        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 14));
        logo.setOpaque(false);
        logo.setMaximumSize(new Dimension(220, 58));
        JLabel icono = new JLabel("🛒");
        icono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        JLabel nombre = new JLabel("Abarrotes Pro");
        nombre.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nombre.setForeground(Colores.AZUL_PRIMARIO);
        logo.add(icono);
        logo.add(nombre);
        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(4));

        agregarBotonNav(sidebar, "Venta", CARD_VENTA, IconosUi.TipoIcono.VENTA);
        agregarBotonNav(sidebar, "Inventario", CARD_INVENTARIO, IconosUi.TipoIcono.INVENTARIO);
        agregarBotonNav(sidebar, "Tickets", CARD_TICKETS, IconosUi.TipoIcono.TICKETS);
        agregarBotonNav(sidebar, "Proveedores", CARD_PROVEEDORES, IconosUi.TipoIcono.PROVEEDORES);
        agregarBotonNav(sidebar, "Corte", CARD_CORTE, IconosUi.TipoIcono.CORTE);
        agregarBotonNav(sidebar, "Configuracion", CARD_CONFIG, IconosUi.TipoIcono.CONFIGURACION);

        sidebar.add(Box.createVerticalGlue());

        botonCerrarSesion = new JButton("Cerrar Sesion");
        botonCerrarSesion.setFont(new Font("Segoe UI", Font.BOLD, 13));
        botonCerrarSesion.setForeground(Colores.ROJO);
        botonCerrarSesion.setBorderPainted(false);
        botonCerrarSesion.setContentAreaFilled(false);
        botonCerrarSesion.setAlignmentX(Component.CENTER_ALIGNMENT);
        botonCerrarSesion.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botonCerrarSesion.setBorder(new EmptyBorder(16, 0, 24, 0));
        sidebar.add(botonCerrarSesion);

        return sidebar;
    }

    private void agregarBotonNav(JPanel sidebar, String texto, String card, IconosUi.TipoIcono tipoIcono) {
        JButton btn = new JButton();
        btn.setName(card);
        btn.setLayout(new BorderLayout(10, 0));
        btn.setBorder(new EmptyBorder(8, 20, 8, 16));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setMaximumSize(new Dimension(220, 36));
        btn.setPreferredSize(new Dimension(220, 36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel icono = new JLabel(IconosUi.crear(tipoIcono, 18, Colores.GRIS_TEXTO));
        icono.setName("iconoNav");
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        etiqueta.setForeground(Colores.NEGRO_TEXTO);
        etiqueta.setName("textoNav");

        btn.add(icono, BorderLayout.WEST);
        btn.add(etiqueta, BorderLayout.CENTER);

        botonesNav.put(card, btn);
        sidebar.add(btn);
    }

    private JPanel crearAreaPrincipal() {
        JPanel area = new JPanel(new BorderLayout());
        area.setBackground(Colores.FONDO_APP);
        area.add(crearBarraSuperior(), BorderLayout.NORTH);
        area.add(panelContenido, BorderLayout.CENTER);
        return area;
    }

    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Color.WHITE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Colores.GRIS_BORDE),
                new EmptyBorder(16, 24, 16, 24)));

        etiquetaModulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        etiquetaModulo.setForeground(Colores.NEGRO_TEXTO);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        derecha.setOpaque(false);

        etiquetaUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        etiquetaUsuario.setForeground(Colores.NEGRO_TEXTO);

        JPanel estado = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        estado.setOpaque(false);
        JLabel punto = new JLabel("●");
        punto.setForeground(Colores.VERDE);
        punto.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        JLabel online = new JLabel("Sistema Online");
        online.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        online.setForeground(Colores.GRIS_TEXTO);
        estado.add(punto);
        estado.add(online);

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Colores.AZUL_PRIMARIO);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                String t = etiquetaIniciales.getText();
                int x = (getWidth() - fm.stringWidth(t)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(t, x, y);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setOpaque(false);
        etiquetaIniciales.setVisible(false);

        derecha.add(etiquetaUsuario);
        derecha.add(estado);
        derecha.add(avatar);

        barra.add(etiquetaModulo, BorderLayout.WEST);
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    public void mostrarModulo(String card, String tituloModulo) {
        cardLayout.show(panelContenido, card);
        etiquetaModulo.setText(tituloModulo);
        botonesNav.forEach((nombre, btn) -> {
            boolean activo = nombre.equals(card);
            btn.setBackground(activo ? Colores.SIDEBAR_ACTIVO : Color.WHITE);
            btn.setOpaque(activo);

            Component[] hijos = btn.getComponents();
            for (Component hijo : hijos) {
                if (hijo instanceof JLabel lbl) {
                    if ("iconoNav".equals(lbl.getName())) {
                        lbl.setIcon(IconosUi.crear(
                                iconoNavParaCard(nombre), 18,
                                activo ? Colores.AZUL_PRIMARIO : Colores.GRIS_TEXTO));
                    } else if ("textoNav".equals(lbl.getName())) {
                        lbl.setForeground(activo ? Colores.AZUL_PRIMARIO : Colores.NEGRO_TEXTO);
                        lbl.setFont(new Font("Segoe UI", activo ? Font.BOLD : Font.PLAIN, 14));
                    }
                }
            }
        });
    }

    private static IconosUi.TipoIcono iconoNavParaCard(String card) {
        return switch (card) {
            case CARD_VENTA -> IconosUi.TipoIcono.VENTA;
            case CARD_INVENTARIO -> IconosUi.TipoIcono.INVENTARIO;
            case CARD_TICKETS -> IconosUi.TipoIcono.TICKETS;
            case CARD_PROVEEDORES -> IconosUi.TipoIcono.PROVEEDORES;
            case CARD_CORTE -> IconosUi.TipoIcono.CORTE;
            case CARD_CONFIG -> IconosUi.TipoIcono.CONFIGURACION;
            default -> IconosUi.TipoIcono.VENTA;
        };
    }

    public void configurarUsuario(String nombre, String iniciales) {
        etiquetaUsuario.setText(nombre);
        etiquetaIniciales.setText(iniciales);
    }

    public Map<String, JButton> getBotonesNav() {
        return botonesNav;
    }

    public JButton getBotonCerrarSesion() {
        return botonCerrarSesion;
    }

    public PanelVenta getPanelVenta() {
        return panelVenta;
    }

    public PanelInventario getPanelInventario() {
        return panelInventario;
    }

    public PanelCorte getPanelCorte() {
        return panelCorte;
    }

    public ProveedoresPanel getPanelProveedores() {
        return panelProveedores;
    }
}
