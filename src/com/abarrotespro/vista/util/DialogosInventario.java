package com.abarrotespro.vista.util;

import com.abarrotespro.modelo.Producto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Optional;

/**
 * Dialogos modales personalizados para el modulo de inventario.
 */
public final class DialogosInventario {

    public record DatosEdicion(String nombre, double precio) {
    }

    private DialogosInventario() {
    }

    public static Optional<DatosEdicion> mostrarEditarProducto(Window padre, Producto producto) {
        JDialog dialogo = crearDialogoBase(padre);
        JPanel contenido = crearContenidoDialogo();

        contenido.add(crearTituloDialogo("Editar Producto"));
        contenido.add(Box.createVerticalStrut(4));
        JLabel subtitulo = crearSubtitulo("Modifica la informacion del articulo");
        contenido.add(subtitulo);
        contenido.add(Box.createVerticalStrut(16));

        JLabel lblNombre = crearEtiquetaCampo("Nombre del Producto");
        JTextField campoNombre = ComponentesUi.crearCampoTexto("");
        campoNombre.setText(producto.getNombre());
        contenido.add(lblNombre);
        contenido.add(Box.createVerticalStrut(6));
        contenido.add(campoNombre);
        contenido.add(Box.createVerticalStrut(14));

        JLabel lblPrecio = crearEtiquetaCampo("Precio de Venta ($)");
        JTextField campoPrecio = ComponentesUi.crearCampoTexto("");
        campoPrecio.setText(String.valueOf(producto.getPrecio()));
        contenido.add(lblPrecio);
        contenido.add(Box.createVerticalStrut(6));
        contenido.add(campoPrecio);
        contenido.add(Box.createVerticalStrut(24));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        JButton cancelar = ComponentesUi.crearBotonSecundario("Cancelar", 40);
        JButton guardar = ComponentesUi.crearBotonPrimario("Guardar Cambios", 40);
        guardar.setPreferredSize(new Dimension(160, 40));
        botones.add(cancelar);
        botones.add(guardar);
        contenido.add(botones);

        finalizarDialogo(dialogo, contenido);
        final Optional<DatosEdicion>[] resultado = new Optional[]{Optional.empty()};

        cancelar.addActionListener(e -> dialogo.dispose());
        guardar.addActionListener(e -> {
            try {
                String nom = campoNombre.getText().trim();
                double pre = Double.parseDouble(campoPrecio.getText().trim());
                if (nom.isEmpty() || pre < 0) {
                    throw new IllegalArgumentException();
                }
                resultado[0] = Optional.of(new DatosEdicion(nom, pre));
                dialogo.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogo,
                        "Datos invalidos. Verifique nombre y precio.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialogo.getRootPane().setDefaultButton(guardar);
        dialogo.setVisible(true);
        return resultado[0];
    }

    public static Optional<Integer> mostrarSurtirInventario(Window padre, Producto producto) {
        JDialog dialogo = crearDialogoBase(padre);
        JPanel contenido = crearContenidoDialogo();

        contenido.add(crearTituloDialogo("Surtir Inventario"));
        contenido.add(Box.createVerticalStrut(4));
        JLabel subtitulo = crearSubtitulo("Ingresa unidades para: " + producto.getNombre());
        contenido.add(subtitulo);
        contenido.add(Box.createVerticalStrut(16));

        JLabel lblUnidades = crearEtiquetaCampo("Unidades a Ingresar");
        JTextField campoUnidades = ComponentesUi.crearCampoTexto("");
        contenido.add(lblUnidades);
        contenido.add(Box.createVerticalStrut(6));
        contenido.add(campoUnidades);
        contenido.add(Box.createVerticalStrut(14));

        JPanel panelStock = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Colores.AZUL_CLARO);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panelStock.setOpaque(false);
        panelStock.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel lblNuevoStock = new JLabel("Nuevo Stock");
        lblNuevoStock.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblNuevoStock.setForeground(Colores.GRIS_TEXTO);
        JLabel valorStock = new JLabel(String.valueOf(producto.getStock()));
        valorStock.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valorStock.setForeground(Colores.AZUL_PRIMARIO);

        panelStock.setOpaque(false);
        panelStock.add(lblNuevoStock, BorderLayout.WEST);
        panelStock.add(valorStock, BorderLayout.EAST);

        campoUnidades.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void actualizar() {
                try {
                    int extra = Integer.parseInt(campoUnidades.getText().trim());
                    if (extra < 0) {
                        extra = 0;
                    }
                    valorStock.setText(String.valueOf(producto.getStock() + extra));
                } catch (NumberFormatException ex) {
                    valorStock.setText(String.valueOf(producto.getStock()));
                }
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                actualizar();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                actualizar();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                actualizar();
            }
        });

        contenido.add(panelStock);
        contenido.add(Box.createVerticalStrut(24));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        JButton cancelar = ComponentesUi.crearBotonSecundario("Cancelar", 40);
        JButton guardar = ComponentesUi.crearBotonPrimario("Guardar Inventario", 40);
        guardar.setPreferredSize(new Dimension(170, 40));
        botones.add(cancelar);
        botones.add(guardar);
        contenido.add(botones);

        finalizarDialogo(dialogo, contenido);
        final Optional<Integer>[] resultado = new Optional[]{Optional.empty()};

        cancelar.addActionListener(e -> dialogo.dispose());
        guardar.addActionListener(e -> {
            try {
                int cant = Integer.parseInt(campoUnidades.getText().trim());
                if (cant <= 0) {
                    throw new IllegalArgumentException();
                }
                resultado[0] = Optional.of(cant);
                dialogo.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogo,
                        "Ingrese una cantidad valida mayor a cero.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialogo.getRootPane().setDefaultButton(guardar);
        dialogo.setVisible(true);
        return resultado[0];
    }

    public static boolean mostrarConfirmarEliminar(Window padre, Producto producto) {
        JDialog dialogo = crearDialogoBase(padre);
        JPanel contenido = crearContenidoDialogo();

        contenido.add(crearTituloDialogo("Eliminar Producto"));
        contenido.add(Box.createVerticalStrut(8));

        JLabel icono = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(254, 226, 226));
                g2.fillOval(0, 0, getWidth(), getHeight());
                IconosUi.crear(IconosUi.TipoIcono.BASURA, 22, Colores.ROJO)
                        .paintIcon(this, g2, 9, 9);
                g2.dispose();
            }
        };
        icono.setPreferredSize(new Dimension(40, 40));
        icono.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel mensaje = new JLabel("<html>Desea eliminar el articulo<br><b>"
                + producto.getNombre() + "</b>?<br>Esta accion no se puede deshacer.</html>");
        mensaje.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mensaje.setForeground(Colores.NEGRO_TEXTO);
        mensaje.setBorder(new EmptyBorder(12, 0, 20, 0));

        contenido.add(icono);
        contenido.add(mensaje);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        JButton cancelar = ComponentesUi.crearBotonSecundario("Cancelar", 40);
        JButton eliminar = ComponentesUi.crearBotonRojo("Eliminar");
        eliminar.setPreferredSize(new Dimension(120, 40));
        eliminar.setText("Eliminar");
        botones.add(cancelar);
        botones.add(eliminar);
        contenido.add(botones);

        finalizarDialogo(dialogo, contenido);
        final boolean[] confirmado = {false};

        cancelar.addActionListener(e -> dialogo.dispose());
        eliminar.addActionListener(e -> {
            confirmado[0] = true;
            dialogo.dispose();
        });

        dialogo.setVisible(true);
        return confirmado[0];
    }

    private static JDialog crearDialogoBase(Window padre) {
        Frame frame = padre instanceof Frame ? (Frame) padre : null;
        JDialog dialogo = new JDialog(frame, true);
        dialogo.setLocationRelativeTo(padre);
        dialogo.setResizable(false);
        dialogo.setUndecorated(true);
        return dialogo;
    }

    private static void finalizarDialogo(JDialog dialogo, JPanel contenido) {
        dialogo.setContentPane(envolverDialogo(contenido));
        dialogo.pack();
    }

    private static JLabel crearTituloDialogo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(Colores.NEGRO_TEXTO);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private static JPanel envolverDialogo(JPanel contenido) {
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
        return marco;
    }

    private static JPanel crearContenidoDialogo() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private static JLabel crearSubtitulo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(Colores.GRIS_TEXTO);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private static JLabel crearEtiquetaCampo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(Colores.NEGRO_TEXTO);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }
}
