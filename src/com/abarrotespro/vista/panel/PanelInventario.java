package com.abarrotespro.vista.panel;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.abarrotespro.modelo.Producto;
import com.abarrotespro.vista.util.Colores;
import com.abarrotespro.vista.util.ComponentesUi;
import com.abarrotespro.vista.util.DialogosInventario;
import com.abarrotespro.vista.util.IconosUi;

/**
 * Modulo de gestion de inventario con tabla de productos.
 */
public class PanelInventario extends JPanel {

    private static final int COL_ACCIONES = 5;

    private DefaultTableModel modeloTabla;
    private JTable tabla;
    private JButton botonNuevo;
    private List<Producto> productosActuales;
    private BiConsumer<Integer, Integer> callbackSurtir;
    private Consumer<Integer> callbackEliminar;
    private Consumer<Producto> callbackEditar;

    public PanelInventario() {
        setBackground(Colores.FONDO_APP);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 8, 0, 0));

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearTabla(), BorderLayout.CENTER);
    }

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Gestion de Inventario");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(Colores.NEGRO_TEXTO);

        JLabel desc = new JLabel("Administra productos, precios y existencias de tu tienda");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        desc.setForeground(Colores.GRIS_TEXTO);

        textos.add(titulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(desc);

        botonNuevo = ComponentesUi.crearBotonPrimario("+ Nuevo Producto", 40);
        botonNuevo.setPreferredSize(new Dimension(180, 40));

        panel.add(textos, BorderLayout.WEST);
        panel.add(botonNuevo, BorderLayout.EAST);
        return panel;
    }

    private JScrollPane crearTabla() {
        String[] columnas = {"ITEM", "NOMBRE", "PRECIO", "STOCK ACTUAL", "ST. MINIMO", "ACCIONES"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(52);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setSelectionBackground(Colores.SIDEBAR_ACTIVO);

        JTableHeader header = tabla.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setForeground(Colores.GRIS_TEXTO);
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setReorderingAllowed(false);

        tabla.getColumnModel().getColumn(0).setPreferredWidth(60);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(140);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(80);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(COL_ACCIONES).setPreferredWidth(200);

        tabla.getColumnModel().getColumn(2).setCellRenderer(new CeldaPrecioRenderer());
        tabla.getColumnModel().getColumn(3).setCellRenderer(new CeldaStockRenderer());
        tabla.getColumnModel().getColumn(4).setCellRenderer(new CeldaStockMinimoRenderer());
        tabla.getColumnModel().getColumn(COL_ACCIONES).setCellRenderer(new CeldaAccionesRenderer());

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                int columna = tabla.columnAtPoint(e.getPoint());

                if (fila < 0 || productosActuales == null || fila >= productosActuales.size()) {
                    return;
                }

                Producto producto = productosActuales.get(fila);

                if (columna == COL_ACCIONES) {
                    int accion = detectarAccionClic(e, fila, columna);
                    switch (accion) {
                        case 0 -> solicitarEdicion(producto);
                        case 1 -> solicitarSurtido(producto);
                        case 2 -> confirmarEliminacion(producto);
                        default -> { }
                    }
                } else if (e.getClickCount() == 2) {
                    solicitarEdicion(producto);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(Colores.GRIS_BORDE, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    /** 0 = editar, 1 = surtir, 2 = eliminar */
    private int detectarAccionClic(MouseEvent e, int fila, int columna) {
        Rectangle celda = tabla.getCellRect(fila, columna, true);
        int relX = e.getX() - celda.x;
        int tercio = celda.width / 3;
        if (relX < tercio) {
            return 0;
        }
        if (relX < tercio * 2) {
            return 1;
        }
        return 2;
    }

    private void solicitarSurtido(Producto producto) {
        Window padre = SwingUtilities.getWindowAncestor(this);
        DialogosInventario.mostrarSurtirInventario(padre, producto).ifPresent(cant -> {
            if (callbackSurtir != null) {
                callbackSurtir.accept(producto.getId(), cant);
            }
        });
    }

    private void confirmarEliminacion(Producto producto) {
        Window padre = SwingUtilities.getWindowAncestor(this);
        if (DialogosInventario.mostrarConfirmarEliminar(padre, producto) && callbackEliminar != null) {
            callbackEliminar.accept(producto.getId());
        }
    }

    public JButton getBotonNuevo() {
        return botonNuevo;
    }

    public void alEditarProducto(Consumer<Producto> alEditar) {
        this.callbackEditar = alEditar;
    }

    private void solicitarEdicion(Producto producto) {
        if (callbackEditar != null) {
            callbackEditar.accept(producto);
        }
    }

    public void actualizarTabla(List<Producto> productos,
                                BiConsumer<Integer, Integer> alSurtir,
                                Consumer<Integer> alEliminar) {
        this.productosActuales = productos;
        this.callbackSurtir = alSurtir;
        this.callbackEliminar = alEliminar;

        modeloTabla.setRowCount(0);
        for (Producto p : productos) {
            modeloTabla.addRow(new Object[]{
                    "#" + p.getId(),
                    p.getNombre(),
                    ComponentesUi.formatearMoneda(p.getPrecio()),
                    p.getStock(),
                    p.getStockMinimo(),
                    ""
            });
        }
    }

    private static class CeldaPrecioRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            lbl.setForeground(Colores.AZUL_PRIMARIO);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            return lbl;
        }
    }

    private class CeldaStockRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? Colores.SIDEBAR_ACTIVO : Color.WHITE);

            if (productosActuales != null && row < productosActuales.size()) {
                Producto p = productosActuales.get(row);

                if (p.getStock() <= p.getStockMinimo()) {
                    JLabel lblAlerta = ComponentesUi.crearEtiquetaStock((Integer) value);
                    lblAlerta.setForeground(Color.RED);
                    lblAlerta.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    panel.setBackground(new Color(255, 230, 230));
                    panel.add(lblAlerta);
                    return panel;
                }
            }

            panel.add(ComponentesUi.crearEtiquetaStock((Integer) value));
            return panel;
        }
    }

    private static class CeldaAccionesRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 8));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? Colores.SIDEBAR_ACTIVO : Color.WHITE);

            JButton editar = crearBotonIconoTabla(IconosUi.TipoIcono.LAPIZ, Colores.AZUL_PRIMARIO, "Editar");
            JButton surtir = new JButton("+ Surtir");
            surtir.setFont(new Font("Segoe UI", Font.BOLD, 11));
            surtir.setForeground(Colores.AZUL_PRIMARIO);
            surtir.setBorder(BorderFactory.createLineBorder(Colores.AZUL_PRIMARIO, 1, true));
            surtir.setContentAreaFilled(false);
            surtir.setFocusPainted(false);
            surtir.setPreferredSize(new Dimension(72, 28));
            surtir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JButton eliminar = crearBotonIconoTabla(IconosUi.TipoIcono.BASURA, Colores.ROJO, "Eliminar");

            panel.add(editar);
            panel.add(surtir);
            panel.add(eliminar);
            return panel;
        }

        private static JButton crearBotonIconoTabla(IconosUi.TipoIcono tipo, Color color, String tooltip) {
            JButton boton = new JButton(IconosUi.crear(tipo, 16, color));
            boton.setToolTipText(tooltip);
            boton.setPreferredSize(new Dimension(32, 28));
            boton.setFocusPainted(false);
            boton.setBorderPainted(false);
            boton.setContentAreaFilled(false);
            boton.setEnabled(false);
            return boton;
        }
    }

    private static class CeldaStockMinimoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            lbl.setForeground(Colores.GRIS_TEXTO);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            return lbl;
        }
    }
}
