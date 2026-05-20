package com.abarrotespro.vista.panel;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.abarrotespro.modelo.LineaVenta;
import com.abarrotespro.modelo.Producto;
import com.abarrotespro.modelo.Venta;
import com.abarrotespro.vista.util.Colores;
import com.abarrotespro.vista.util.ComponentesUi;
import com.abarrotespro.vista.util.IconosUi;

/**
 * Modulo de venta: catalogo de productos y ticket activo.
 */
public class PanelVenta extends JPanel {

    private JPanel contenedorProductos;
    private JPanel contenedorTicket;
    private JLabel etiquetaTotal;
    private JButton botonCobrar;
    private JTextField campoBusqueda;

    public PanelVenta() {
        setBackground(Colores.FONDO_APP);
        setLayout(new BorderLayout(16, 0));
        setBorder(new EmptyBorder(0, 0, 0, 0));

        add(crearPanelCatalogo(), BorderLayout.CENTER);
        add(crearPanelTicket(), BorderLayout.EAST);
    }

    private JPanel crearPanelCatalogo() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 8, 0, 0));

        JPanel busqueda = new JPanel(new BorderLayout(8, 0));
        busqueda.setOpaque(false);
        busqueda.setBorder(new EmptyBorder(0, 0, 8, 0));

        Icon iconoLupa = IconosUi.crear(IconosUi.TipoIcono.BUSQUEDA, 16, Colores.GRIS_TEXTO);
        campoBusqueda = ComponentesUi.crearCampoTextoConIcono(iconoLupa, "Buscar por nombre o ID...");
        campoBusqueda.setPreferredSize(new Dimension(300, 42));

        busqueda.add(campoBusqueda, BorderLayout.CENTER);
        panel.add(busqueda, BorderLayout.NORTH);

        contenedorProductos = new JPanel();
        contenedorProductos.setOpaque(false);
        contenedorProductos.setLayout(new GridLayout(0, 5, 12, 12));

        JScrollPane scroll = new JScrollPane(contenedorProductos);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelTicket() {
        JPanel panel = ComponentesUi.crearPanelRedondeado(Colores.FONDO_TARJETA, 16);
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(320, 0));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("Ticket de Venta");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(Colores.NEGRO_TEXTO);
        panel.add(titulo, BorderLayout.NORTH);

        contenedorTicket = new JPanel();
        contenedorTicket.setOpaque(false);
        contenedorTicket.setLayout(new BoxLayout(contenedorTicket, BoxLayout.Y_AXIS));

        JScrollPane scrollTicket = new JScrollPane(contenedorTicket);
        scrollTicket.setBorder(null);
        scrollTicket.setOpaque(false);
        scrollTicket.getViewport().setOpaque(false);
        panel.add(scrollTicket, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(0, 10));
        pie.setOpaque(false);
        pie.setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        JLabel lblTotal = new JLabel("Total:");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        etiquetaTotal = new JLabel("$0.00");
        etiquetaTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        etiquetaTotal.setForeground(Colores.AZUL_PRIMARIO);
        etiquetaTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        totalRow.add(lblTotal, BorderLayout.WEST);
        totalRow.add(etiquetaTotal, BorderLayout.EAST);

        botonCobrar = ComponentesUi.crearBotonPrimario("Cobrar Venta", 44);

        pie.add(totalRow, BorderLayout.NORTH);
        pie.add(botonCobrar, BorderLayout.SOUTH);
        panel.add(pie, BorderLayout.SOUTH);
        return panel;
    }

    public JTextField getCampoBusqueda() {
        return campoBusqueda;
    }

    public JButton getBotonCobrar() {
        return botonCobrar;
    }

    public void actualizarProductos(List<Producto> productos, ActionListener alAgregar) {
        contenedorProductos.removeAll();
        for (Producto p : productos) {
            JPanel celda = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            celda.setOpaque(false);
            celda.add(new TarjetaProducto(p, alAgregar));
            contenedorProductos.add(celda);
        }
        contenedorProductos.revalidate();
        contenedorProductos.repaint();
    }

    public void actualizarTicket(Venta venta, Consumer<Integer> alEliminar) {
        contenedorTicket.removeAll();
        List<LineaVenta> lineas = venta.getLineas();
        for (int i = 0; i < lineas.size(); i++) {
            contenedorTicket.add(crearLineaTicket(lineas.get(i), i, alEliminar));
            contenedorTicket.add(Box.createVerticalStrut(8));
        }
        if (lineas.isEmpty()) {
            JLabel vacio = new JLabel("Sin articulos en el ticket");
            vacio.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            vacio.setForeground(Colores.GRIS_TEXTO);
            vacio.setBorder(new EmptyBorder(20, 0, 0, 0));
            contenedorTicket.add(vacio);
        }
        etiquetaTotal.setText(ComponentesUi.formatearMoneda(venta.getMontoCobrable()));
        contenedorTicket.revalidate();
        contenedorTicket.repaint();
    }

    private JPanel crearLineaTicket(LineaVenta linea, int indice, Consumer<Integer> alEliminar) {
        JPanel fila = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Colores.FONDO_LINEA_TICKET);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
            }
        };
        fila.setOpaque(false);
        fila.setBorder(new EmptyBorder(10, 10, 10, 10));
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        Producto p = linea.getProducto();
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel nombre = new JLabel(p.getNombre());
        nombre.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel detalle = new JLabel(linea.getCantidad() + " x "
                + ComponentesUi.formatearMoneda(linea.getPrecioVentaUnitario()));
        detalle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        detalle.setForeground(Colores.GRIS_TEXTO);

        JLabel subtotal = new JLabel(ComponentesUi.formatearMoneda(linea.getSubtotal()));
        subtotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        subtotal.setForeground(Colores.NEGRO_TEXTO);

        info.add(nombre);
        info.add(detalle);

        JButton eliminar = new JButton(IconosUi.crear(IconosUi.TipoIcono.BASURA, 16, Colores.ROJO));
        eliminar.setToolTipText("Quitar del ticket");
        eliminar.setPreferredSize(new Dimension(32, 28));
        eliminar.setBorderPainted(false);
        eliminar.setContentAreaFilled(false);
        eliminar.setFocusPainted(false);
        eliminar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eliminar.addActionListener(e -> alEliminar.accept(indice));

        fila.add(info, BorderLayout.CENTER);
        JPanel derecha = new JPanel(new BorderLayout());
        derecha.setOpaque(false);
        derecha.add(subtotal, BorderLayout.CENTER);
        derecha.add(eliminar, BorderLayout.EAST);
        fila.add(derecha, BorderLayout.EAST);
        return fila;
    }
}
