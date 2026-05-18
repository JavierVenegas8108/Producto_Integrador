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

import com.abarrotespro.modelo.dto.FilaReporteVenta;
import com.abarrotespro.vista.util.Colores;
import com.abarrotespro.vista.util.ComponentesUi;

public final class ReporteVentasDialog extends JDialog {

    private ReporteVentasDialog(Window padre, List<FilaReporteVenta> filas) {
        super(padre, "Reporte de ventas", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(900, 480));

        JPanel raiz = new JPanel(new BorderLayout(0, 12));
        raiz.setBackground(Colores.FONDO_APP);
        raiz.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titulo = new JLabel("Detalle de ventas (" + filas.size() + " lineas)");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titulo.setForeground(Colores.NEGRO_TEXTO);

        String[] columnas = {
                "Fecha", "Hora", "Ticket", "Producto", "Cant.",
                "P. Unit.", "Importe", "Total ticket", "Usuario"
        };
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (FilaReporteVenta fila : filas) {
            modelo.addRow(new Object[] {
                    fila.getFechaVenta(),
                    fila.getHoraVenta(),
                    fila.getNumeroTicket(),
                    fila.getProducto(),
                    fila.getCantidad(),
                    String.format("%.2f", fila.getPrecioUnitario()),
                    String.format("%.2f", fila.getImporteLinea()),
                    String.format("%.2f", fila.getTotalTicket()),
                    fila.getUsuario()
            });
        }

        JTable tabla = new JTable(modelo);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setRowHeight(28);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(200);

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

    public static void mostrar(Window padre, List<FilaReporteVenta> filas) {
        ReporteVentasDialog dialogo = new ReporteVentasDialog(padre, filas);
        dialogo.setVisible(true);
    }
}
