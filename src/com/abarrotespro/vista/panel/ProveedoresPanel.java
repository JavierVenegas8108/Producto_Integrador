package com.abarrotespro.vista.panel;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.abarrotespro.modelo.Proveedor;
import com.abarrotespro.vista.util.Colores;
import com.abarrotespro.vista.util.ComponentesUi;
import com.abarrotespro.vista.util.FormatoIdUtil;
import com.abarrotespro.vista.util.IconosUi;

/**
 * Vista principal del modulo de proveedores.
 */
public class ProveedoresPanel extends JPanel {

    public static final int COL_ID = 0;
    public static final int COL_RAZON = 1;
    public static final int COL_CONTACTO = 2;
    public static final int COL_TELEFONO = 3;
    public static final int COL_ESTADO = 4;
    public static final int COL_ACCIONES = 5;

    private DefaultTableModel modeloTabla;
    private JTable tabla;
    private JTextField campoBusqueda;
    private JButton botonNuevo;
    private List<Proveedor> proveedoresActuales;
    private Consumer<Proveedor> callbackEditar;
    private Consumer<Proveedor> callbackDesactivar;

    public ProveedoresPanel() {
        setBackground(Colores.FONDO_APP);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 8, 0, 0));
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearTabla(), BorderLayout.CENTER);
    }

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel tituloFila = new JPanel(new BorderLayout());
        tituloFila.setOpaque(false);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("Gestion de Proveedores");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(Colores.NEGRO_TEXTO);
        JLabel desc = new JLabel("Administra contactos y visitas de tus proveedores");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        desc.setForeground(Colores.GRIS_TEXTO);
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(desc);

        botonNuevo = ComponentesUi.crearBotonPrimario("+ Nuevo Proveedor", 40);
        botonNuevo.setPreferredSize(new Dimension(180, 40));
        tituloFila.add(textos, BorderLayout.WEST);
        tituloFila.add(botonNuevo, BorderLayout.EAST);

        JPanel busqueda = new JPanel(new BorderLayout());
        busqueda.setOpaque(false);
        busqueda.setBorder(new EmptyBorder(14, 0, 0, 0));
        campoBusqueda = ComponentesUi.crearCampoTexto("Buscar por razon social, contacto o telefono...");
        campoBusqueda.setPreferredSize(new Dimension(400, 42));
        busqueda.add(campoBusqueda, BorderLayout.WEST);

        panel.add(tituloFila);
        panel.add(busqueda);
        return panel;
    }

    private JScrollPane crearTabla() {
        String[] columnas = {"ID", "RAZON SOCIAL", "CONTACTO", "TELEFONO", "ESTADO", "ACCIONES"};
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
                return Object.class;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(50);
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

        tabla.getColumnModel().getColumn(COL_ID).setPreferredWidth(72);
        tabla.getColumnModel().getColumn(COL_RAZON).setPreferredWidth(220);
        tabla.getColumnModel().getColumn(COL_CONTACTO).setPreferredWidth(160);
        tabla.getColumnModel().getColumn(COL_TELEFONO).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(COL_ESTADO).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(COL_ACCIONES).setPreferredWidth(150);

        tabla.getColumnModel().getColumn(COL_ID).setCellRenderer(new CeldaIdRenderer());
        tabla.getColumnModel().getColumn(COL_ESTADO).setCellRenderer(new CeldaEstadoRenderer());
        tabla.getColumnModel().getColumn(COL_ACCIONES).setCellRenderer(new CeldaAccionesRenderer());

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                int columna = tabla.columnAtPoint(e.getPoint());
                if (fila < 0 || proveedoresActuales == null || fila >= proveedoresActuales.size()) {
                    return;
                }
                Proveedor proveedor = proveedoresActuales.get(fila);
                if (columna == COL_ACCIONES) {
                    Rectangle celda = tabla.getCellRect(fila, columna, true);
                    int mitad = celda.x + celda.width / 2;
                    if (e.getX() < mitad) {
                        if (callbackEditar != null) {
                            callbackEditar.accept(proveedor);
                        }
                    } else if (proveedor.isActivo() && callbackDesactivar != null) {
                        callbackDesactivar.accept(proveedor);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(Colores.GRIS_BORDE, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    public JTextField getCampoBusqueda() {
        return campoBusqueda;
    }

    public JButton getBotonNuevo() {
        return botonNuevo;
    }

    public void alEditar(Consumer<Proveedor> callback) {
        this.callbackEditar = callback;
    }

    public void alDesactivar(Consumer<Proveedor> callback) {
        this.callbackDesactivar = callback;
    }

    public void actualizarTabla(List<Proveedor> proveedores) {
        this.proveedoresActuales = proveedores;
        modeloTabla.setRowCount(0);
        for (Proveedor p : proveedores) {
            modeloTabla.addRow(new Object[]{
                    p.getId(),
                    p.getRazonSocial(),
                    p.getNombreContacto(),
                    p.getTelefono(),
                    p.getEstadoTexto(),
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
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setForeground(Colores.GRIS_TEXTO);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            return lbl;
        }
    }

    private static class CeldaEstadoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            boolean activo = "Activo".equals(value);
            lbl.setForeground(activo ? Colores.VERDE : Colores.GRIS_TEXTO);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            return lbl;
        }
    }

    private static class CeldaAccionesRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? Colores.SIDEBAR_ACTIVO : Color.WHITE);

            JButton editar = new JButton(IconosUi.crear(IconosUi.TipoIcono.LAPIZ, 16, Colores.AZUL_PRIMARIO));
            editar.setPreferredSize(new Dimension(32, 28));
            editar.setBorderPainted(false);
            editar.setContentAreaFilled(false);
            editar.setEnabled(false);

            JButton desactivar = new JButton("Desactivar");
            desactivar.setFont(new Font("Segoe UI", Font.BOLD, 10));
            desactivar.setForeground(Colores.ROJO);
            desactivar.setBorder(BorderFactory.createLineBorder(Colores.ROJO, 1, true));
            desactivar.setContentAreaFilled(false);
            desactivar.setFocusPainted(false);
            desactivar.setPreferredSize(new Dimension(80, 26));
            desactivar.setEnabled(false);

            panel.add(editar);
            panel.add(desactivar);
            return panel;
        }
    }
}
