package com.abarrotespro.vista.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.abarrotespro.modelo.Producto;
import com.abarrotespro.vista.util.Colores;
import com.abarrotespro.vista.util.ComponentesUi;
import com.abarrotespro.vista.util.FormatoIdUtil;

/**
 * Vista filtrada de productos con existencia en o por debajo del stock minimo.
 */
public final class ReporteBajoStockDialog extends JDialog {

    private ReporteBajoStockDialog(Window padre, List<Producto> productos) {
        super(padre, "Reporte de bajo stock", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(720, 420));

        JPanel raiz = new JPanel(new BorderLayout(0, 12));
        raiz.setBackground(Colores.FONDO_APP);
        raiz.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titulo = new JLabel("Productos en bajo stock (" + productos.size() + ")");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titulo.setForeground(Colores.NEGRO_TEXTO);

        String[] columnas = {"ID", "Producto", "Stock actual", "Stock minimo", "Faltante", "Categoria"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Producto p : productos) {
            int faltante = Math.max(0, p.getStockMinimo() - p.getStock());
            modelo.addRow(new Object[]{
                    FormatoIdUtil.formatearIdVisual(p.getId()),
                    p.getNombre(),
                    p.getStock(),
                    p.getStockMinimo(),
                    faltante,
                    p.getCategoria()
            });
        }

        JTable tabla = new JTable(modelo);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setRowHeight(28);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.getColumnModel().getColumn(1).setPreferredWidth(200);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(null);

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoton.setOpaque(false);
        JButton cerrar = ComponentesUi.crearBotonSecundario("Cerrar", 36);
        cerrar.addActionListener(e -> dispose());
        panelBoton.add(cerrar);

        raiz.add(titulo, BorderLayout.NORTH);
        raiz.add(scroll, BorderLayout.CENTER);
        raiz.add(panelBoton, BorderLayout.SOUTH);
        setContentPane(raiz);
        pack();
        setLocationRelativeTo(padre);
    }

    public static void mostrar(Window padre, List<Producto> productos) {
        ReporteBajoStockDialog dialogo = new ReporteBajoStockDialog(padre, productos);
        dialogo.setVisible(true);
    }
}
