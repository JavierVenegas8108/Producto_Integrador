package com.abarrotespro.vista.dialog;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.abarrotespro.modelo.Proveedor;
import com.abarrotespro.vista.util.Colores;
import com.abarrotespro.vista.util.ComponentesUi;
import com.abarrotespro.vista.util.DialogoVentanaUtil;

/**
 * Dialogo modal para captura y edicion de proveedores (GridBagLayout).
 */
public class ProveedorDialog extends JDialog {

    private final JTextField campoRazonSocial;
    private final JTextField campoContacto;
    private final JTextField campoTelefono;
    private final JTextField campoCorreo;
    private final JTextField campoDireccion;
    private final JTextField campoDiasVisita;
    private Proveedor resultado;

    public ProveedorDialog(Window padre, Proveedor proveedor) {
        super(padre, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setResizable(false);

        boolean edicion = proveedor != null;
        Proveedor datos = proveedor != null ? proveedor : new Proveedor();

        JPanel contenido = new JPanel(new GridBagLayout());
        contenido.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int fila = 0;
        agregarCampo(contenido, gbc, fila++, "Razon Social *",
                campoRazonSocial = crearCampo(datos.getRazonSocial()));
        agregarCampo(contenido, gbc, fila++, "Nombre de Contacto *",
                campoContacto = crearCampo(datos.getNombreContacto()));
        agregarCampo(contenido, gbc, fila++, "Telefono *",
                campoTelefono = crearCampo(datos.getTelefono()));
        agregarCampo(contenido, gbc, fila++, "Correo Electronico",
                campoCorreo = crearCampo(datos.getCorreo()));
        agregarCampo(contenido, gbc, fila++, "Direccion",
                campoDireccion = crearCampo(datos.getDireccion()));
        agregarCampo(contenido, gbc, fila++, "Dias de Visita",
                campoDiasVisita = crearCampo(datos.getDiasVisita()));

        gbc.gridy = fila * 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(18, 8, 0, 8);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        botones.setOpaque(false);
        JButton cancelar = ComponentesUi.crearBotonSecundario("Cancelar", 40);
        JButton guardar = ComponentesUi.crearBotonPrimario(
                edicion ? "Guardar Cambios" : "Registrar Proveedor", 40);
        guardar.setPreferredSize(new Dimension(170, 40));
        botones.add(cancelar);
        botones.add(guardar);
        contenido.add(botones, gbc);

        JPanel marco = crearMarco(contenido);
        JLabel titulo = new JLabel(edicion ? "Editar Proveedor" : "Nuevo Proveedor", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(Colores.NEGRO_TEXTO);
        titulo.setBorder(new EmptyBorder(0, 0, 12, 0));
        marco.add(titulo, BorderLayout.NORTH);

        setContentPane(marco);
        DialogoVentanaUtil.aplicarVentanaModal(this, padre, marco);

        cancelar.addActionListener(e -> dispose());
        guardar.addActionListener(e -> {
            String razon = campoRazonSocial.getText().trim();
            String contacto = campoContacto.getText().trim();
            String telefono = campoTelefono.getText().trim();
            if (razon.isEmpty() || contacto.isEmpty() || telefono.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Razon Social, Contacto y Telefono son obligatorios.",
                        "Campos requeridos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Proveedor p = new Proveedor();
            p.setId(datos.getId());
            p.setRazonSocial(razon);
            p.setNombreContacto(contacto);
            p.setTelefono(telefono);
            p.setCorreo(campoCorreo.getText().trim());
            p.setDireccion(campoDireccion.getText().trim());
            p.setDiasVisita(campoDiasVisita.getText().trim());
            p.setActivo(datos.isActivo());
            resultado = p;
            dispose();
        });

        getRootPane().setDefaultButton(guardar);
    }

    private static JTextField crearCampo(String valor) {
        JTextField campo = ComponentesUi.crearCampoTexto("");
        if (valor != null) {
            campo.setText(valor);
        }
        return campo;
    }

    private static void agregarCampo(JPanel panel, GridBagConstraints gbc, int fila,
            String etiqueta, JComponent campo) {
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.gridy = fila * 2;
        gbc.insets = new Insets(fila == 0 ? 0 : 4, 8, 2, 8);
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(Colores.NEGRO_TEXTO);
        panel.add(lbl, gbc);

        gbc.gridy = fila * 2 + 1;
        gbc.insets = new Insets(0, 8, 8, 8);
        panel.add(campo, gbc);
    }

    private static JPanel crearMarco(JPanel contenido) {
        JPanel marco = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(Colores.GRIS_BORDE);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        marco.setOpaque(false);
        marco.setBorder(new EmptyBorder(24, 28, 24, 28));

        marco.add(contenido, BorderLayout.CENTER);
        marco.setPreferredSize(new Dimension(460, marco.getPreferredSize().height + 40));
        return marco;
    }

    public Proveedor getProveedorResultado() {
        return resultado;
    }
}
