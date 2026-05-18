package com.abarrotespro.vista.panel;

import com.abarrotespro.modelo.Producto;
import com.abarrotespro.vista.util.Colores;
import com.abarrotespro.vista.util.ComponentesUi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Modulo de gestion de inventario con tabla de productos.
 */
public class PanelInventario extends JPanel {

    private DefaultTableModel modeloTabla;
    private JTable tabla;
    private JButton botonNuevo;
    private List<Producto> productosActuales;
    private BiConsumer<Integer, Integer> callbackSurtir;
    private Consumer<Integer> callbackEliminar;

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
        String[] columnas = {"ITEM", "NOMBRE", "PRECIO", "STOCK ACTUAL", "ACCIONES"};
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

        tabla.getColumnModel().getColumn(0).setPreferredWidth(70);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(180);

        tabla.getColumnModel().getColumn(2).setCellRenderer(new CeldaPrecioRenderer());
        tabla.getColumnModel().getColumn(3).setCellRenderer(new CeldaStockRenderer());
        tabla.getColumnModel().getColumn(4).setCellRenderer(new CeldaAccionesRenderer());

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                int columna = tabla.columnAtPoint(e.getPoint());
                if (fila < 0 || columna != 4 || productosActuales == null || fila >= productosActuales.size()) {
                    return;
                }
                Rectangle celda = tabla.getCellRect(fila, columna, true);
                int mitad = celda.x + celda.width / 2;
                Producto producto = productosActuales.get(fila);
                if (e.getX() < mitad) {
                    solicitarSurtido(producto);
                } else {
                    confirmarEliminacion(producto);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(Colores.GRIS_BORDE, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private void solicitarSurtido(Producto producto) {
        String input = JOptionPane.showInputDialog(this, "Cantidad a surtir para " + producto.getNombre() + ":",
                "Surtir producto", JOptionPane.QUESTION_MESSAGE);
        if (input != null && callbackSurtir != null) {
            try {
                int cant = Integer.parseInt(input.trim());
                if (cant > 0) {
                    callbackSurtir.accept(producto.getId(), cant);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Cantidad invalida", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void confirmarEliminacion(Producto producto) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Eliminar " + producto.getNombre() + "?", "Confirmar",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION && callbackEliminar != null) {
            callbackEliminar.accept(producto.getId());
        }
    }

    public JButton getBotonNuevo() {
        return botonNuevo;
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
                    "surtir|eliminar"
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

    private static class CeldaStockRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? Colores.SIDEBAR_ACTIVO : Color.WHITE);
            panel.add(ComponentesUi.crearEtiquetaStock((Integer) value));
            return panel;
        }
    }

    private static class CeldaAccionesRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? Colores.SIDEBAR_ACTIVO : Color.WHITE);

            JButton surtir = new JButton("+ Surtir");
            surtir.setFont(new Font("Segoe UI", Font.BOLD, 11));
            surtir.setForeground(Colores.AZUL_PRIMARIO);
            surtir.setBorder(BorderFactory.createLineBorder(Colores.AZUL_PRIMARIO, 1, true));
            surtir.setContentAreaFilled(false);

            JButton eliminar = new JButton("🗑");
            eliminar.setForeground(Colores.ROJO);
            eliminar.setBorderPainted(false);
            eliminar.setContentAreaFilled(false);

            panel.add(surtir);
            panel.add(eliminar);
            return panel;
        }
    }
}
