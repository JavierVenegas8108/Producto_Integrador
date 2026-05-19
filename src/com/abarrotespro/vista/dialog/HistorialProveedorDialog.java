package com.abarrotespro.vista.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import com.abarrotespro.modelo.Proveedor;
import com.abarrotespro.modelo.RegistroSurtido;
import com.abarrotespro.vista.util.Colores;
import com.abarrotespro.vista.util.ComponentesUi;
import com.abarrotespro.vista.util.DialogoVentanaUtil;
import com.abarrotespro.vista.util.FormatoIdUtil;

/**
 * Muestra productos y surtidos asociados a un proveedor.
 */
public final class HistorialProveedorDialog extends JDialog {

    private HistorialProveedorDialog(Window padre, Proveedor proveedor,
            List<Producto> productos, List<RegistroSurtido> surtidos) {
        super(padre, "Historial - " + proveedor.getRazonSocial(), ModalityType.APPLICATION_MODAL);

        JPanel contenido = new JPanel(new BorderLayout(0, 12));
        contenido.setOpaque(false);
        contenido.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel titulo = new JLabel("Productos y surtidos del proveedor");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titulo.setForeground(Colores.NEGRO_TEXTO);
        contenido.add(titulo, BorderLayout.NORTH);

        String[] columnas = {"Tipo", "Fecha / ID", "Detalle", "Cant.", "Costo"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Producto p : productos) {
            modelo.addRow(new Object[]{
                    "Producto",
                    FormatoIdUtil.formatearIdVisual(p.getId()),
                    p.getNombre(),
                    p.getStock(),
                    ComponentesUi.formatearMoneda(p.getPrecioCompra())
            });
        }
        for (RegistroSurtido r : surtidos) {
            modelo.addRow(new Object[]{
                    "Surtido",
                    r.getFechaFormateada(),
                    r.getNombreProducto(),
                    r.getCantidad(),
                    ComponentesUi.formatearMoneda(r.getPrecioCompra())
            });
        }

        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(26);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contenido.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton cerrar = ComponentesUi.crearBotonSecundario("Cerrar", 40);
        cerrar.addActionListener(e -> dispose());
        JPanel pie = new JPanel();
        pie.setOpaque(false);
        pie.add(cerrar);
        contenido.add(pie, BorderLayout.SOUTH);

        setContentPane(contenido);
        DialogoVentanaUtil.aplicarVentanaModal(this, padre, contenido);
    }

    public static void mostrar(Window padre, Proveedor proveedor,
            List<Producto> productos, List<RegistroSurtido> surtidos) {
        HistorialProveedorDialog d = new HistorialProveedorDialog(padre, proveedor, productos, surtidos);
        d.setVisible(true);
    }
}
