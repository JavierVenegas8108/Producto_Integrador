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
import javax.swing.table.TableColumnModel;

import com.abarrotespro.modelo.Producto;
import com.abarrotespro.vista.util.Colores;
import com.abarrotespro.vista.util.ComponentesUi;
import com.abarrotespro.vista.util.DialogosInventario;
import com.abarrotespro.vista.util.FormatoIdUtil;
import com.abarrotespro.vista.util.GestorImagenProducto;
import com.abarrotespro.vista.util.IconosUi;

/**
 * Modulo de gestion de inventario con tabla de productos.
 */
public class PanelInventario extends JPanel {

    public static final int COL_ID = 0;
    public static final int COL_IMAGEN = 1;
    public static final int COL_NOMBRE = 2;
    public static final int COL_PRECIO = 3;
    public static final int COL_STOCK = 4;
    public static final int COL_MINIMO = 5;
    public static final int COL_ACCIONES = 6;

    private DefaultTableModel modeloTabla;
    private JTable tabla;
    private JButton botonNuevo;
    private JButton botonRegistroMercancia;
    private JButton botonReporteBajoStock;
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
        botonNuevo.setPreferredSize(new Dimension(170, 40));

        botonRegistroMercancia = ComponentesUi.crearBotonSecundario("Registro Mercancia", 40);
        botonRegistroMercancia.setPreferredSize(new Dimension(170, 40));

        botonReporteBajoStock = ComponentesUi.crearBotonSecundario("Bajo Stock", 40);
        botonReporteBajoStock.setPreferredSize(new Dimension(120, 40));

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(botonReporteBajoStock);
        acciones.add(botonRegistroMercancia);
        acciones.add(botonNuevo);

        panel.add(textos, BorderLayout.WEST);
        panel.add(acciones, BorderLayout.EAST);
        return panel;
    }

    private JScrollPane crearTabla() {
        String[] columnas = {"id", "IMAGEN", "NOMBRE", "PRECIO", "STOCK ACTUAL", "ST. MINIMO", "ACCIONES"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == COL_ID) {
                    return Integer.class;
                }
                if (columnIndex == COL_IMAGEN) {
                    return ImageIcon.class;
                }
                if (columnIndex == COL_STOCK || columnIndex == COL_MINIMO) {
                    return Integer.class;
                }
                return Object.class;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(58);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setSelectionBackground(Colores.SIDEBAR_ACTIVO);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader header = tabla.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setForeground(Colores.GRIS_TEXTO);
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setReorderingAllowed(false);

        TableColumnModel columnasTabla = tabla.getColumnModel();
        columnasTabla.getColumn(COL_ID).setPreferredWidth(72);
        columnasTabla.getColumn(COL_IMAGEN).setPreferredWidth(72);
        columnasTabla.getColumn(COL_NOMBRE).setPreferredWidth(200);
        columnasTabla.getColumn(COL_PRECIO).setPreferredWidth(95);
        columnasTabla.getColumn(COL_STOCK).setPreferredWidth(110);
        columnasTabla.getColumn(COL_MINIMO).setPreferredWidth(95);
        columnasTabla.getColumn(COL_ACCIONES).setPreferredWidth(195);

        columnasTabla.getColumn(COL_ID).setCellRenderer(new CeldaIdRenderer());
        columnasTabla.getColumn(COL_IMAGEN).setCellRenderer(new CeldaImagenRenderer());
        columnasTabla.getColumn(COL_PRECIO).setCellRenderer(new CeldaPrecioRenderer());
        columnasTabla.getColumn(COL_STOCK).setCellRenderer(new CeldaStockRenderer());
        columnasTabla.getColumn(COL_MINIMO).setCellRenderer(new CeldaStockMinimoRenderer());
        columnasTabla.getColumn(COL_ACCIONES).setCellRenderer(new CeldaAccionesRenderer());

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
                } else if (e.getClickCount() == 2 && columna != COL_IMAGEN) {
                    solicitarEdicion(producto);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(Colores.GRIS_BORDE, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

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

    public JButton getBotonRegistroMercancia() {
        return botonRegistroMercancia;
    }

    public JButton getBotonReporteBajoStock() {
        return botonReporteBajoStock;
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
                    p.getId(),
                    GestorImagenProducto.cargarMiniaturaOEmoji(
                            p.getRutaImagen(), p.getEmoji(), 44, 44),
                    p.getNombre(),
                    ComponentesUi.formatearMoneda(p.getPrecio()),
                    p.getStock(),
                    p.getStockMinimo(),
                    ""
            });
        }
    }

    private static class CeldaIdRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            if (value instanceof Integer id) {
                lbl.setText(FormatoIdUtil.formatearIdVisual(id));
            }
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(Colores.GRIS_TEXTO);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            return lbl;
        }
    }

    private static class CeldaImagenRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            lbl.setIcon(value instanceof ImageIcon icono ? icono : null);
            lbl.setText("");
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setVerticalAlignment(SwingConstants.CENTER);
            return lbl;
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

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? Colores.SIDEBAR_ACTIVO : Color.WHITE);

            if (productosActuales != null && row < productosActuales.size()) {
                Producto p = productosActuales.get(row);
                if (p.getStock() <= p.getStockMinimo()) {
                    JLabel lblAlerta = ComponentesUi.crearEtiquetaStock((Integer) value);
                    lblAlerta.setForeground(Color.RED);
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
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 10));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? Colores.SIDEBAR_ACTIVO : Color.WHITE);

            JButton editar = crearBotonIconoTabla(IconosUi.TipoIcono.LAPIZ, Colores.AZUL_PRIMARIO);
            JButton surtir = new JButton("+ Surtir");
            surtir.setFont(new Font("Segoe UI", Font.BOLD, 11));
            surtir.setForeground(Colores.AZUL_PRIMARIO);
            surtir.setBorder(BorderFactory.createLineBorder(Colores.AZUL_PRIMARIO, 1, true));
            surtir.setContentAreaFilled(false);
            surtir.setFocusPainted(false);
            surtir.setPreferredSize(new Dimension(72, 28));
            JButton eliminar = crearBotonIconoTabla(IconosUi.TipoIcono.BASURA, Colores.ROJO);

            panel.add(editar);
            panel.add(surtir);
            panel.add(eliminar);
            return panel;
        }

        private static JButton crearBotonIconoTabla(IconosUi.TipoIcono tipo, Color color) {
            JButton boton = new JButton(IconosUi.crear(tipo, 16, color));
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
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            return lbl;
        }
    }
}
