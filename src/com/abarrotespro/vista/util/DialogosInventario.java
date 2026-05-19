package com.abarrotespro.vista.util;

import com.abarrotespro.modelo.EntradaMercancia;
import com.abarrotespro.modelo.Producto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Dialogos modales personalizados para el modulo de inventario (flat design centrado).
 */
public final class DialogosInventario {

    public record DatosFormularioProducto(
            String nombre, double precio, int stockMinimo,
            int stockInicial, String rutaImagen, File archivoImagenNuevo) {
    }

    private DialogosInventario() {
    }

    public static Optional<DatosFormularioProducto> mostrarNuevoProducto(Window padre) {
        return mostrarFormularioProducto(padre, null, true);
    }

    public static Optional<DatosFormularioProducto> mostrarEditarProducto(Window padre, Producto producto) {
        return mostrarFormularioProducto(padre, producto, false);
    }

    private static Optional<DatosFormularioProducto> mostrarFormularioProducto(
            Window padre, Producto producto, boolean esNuevo) {

        JDialog dialogo = crearDialogoBase(padre);
        JPanel contenido = crearContenidoDialogoCentrado();

        String titulo = esNuevo ? "Nuevo Producto" : "Editar Producto";
        contenido.add(crearTituloDialogo(titulo));
        contenido.add(Box.createVerticalStrut(6));
        contenido.add(crearSubtituloCentrado(esNuevo
                ? "Registra un nuevo articulo en el inventario"
                : "Modifica la informacion del articulo"));
        contenido.add(Box.createVerticalStrut(20));

        ZonaArrastreImagen zonaImagen = new ZonaArrastreImagen(
                producto != null ? producto.getRutaImagen() : null,
                producto != null ? producto.getEmoji() : "📦");
        zonaImagen.enlazarExplorador(dialogo);
        contenido.add(zonaImagen);
        contenido.add(Box.createVerticalStrut(20));

        JTextField campoNombre = centrarCampo(ComponentesUi.crearCampoTexto(""));
        if (producto != null) {
            campoNombre.setText(producto.getNombre());
        }
        contenido.add(crearEtiquetaCampoCentrada("Nombre del Producto"));
        contenido.add(Box.createVerticalStrut(6));
        contenido.add(campoNombre);
        contenido.add(Box.createVerticalStrut(14));

        JTextField campoPrecio = centrarCampo(ComponentesUi.crearCampoTexto(""));
        if (producto != null) {
            campoPrecio.setText(String.valueOf(producto.getPrecio()));
        }
        contenido.add(crearEtiquetaCampoCentrada("Precio de Venta ($)"));
        contenido.add(Box.createVerticalStrut(6));
        contenido.add(campoPrecio);
        contenido.add(Box.createVerticalStrut(14));

        JTextField campoStockMinimo = centrarCampo(ComponentesUi.crearCampoTexto(""));
        if (producto != null) {
            campoStockMinimo.setText(String.valueOf(producto.getStockMinimo()));
        } else {
            campoStockMinimo.setText("5");
        }
        contenido.add(crearEtiquetaCampoCentrada("Stock Minimo"));
        contenido.add(Box.createVerticalStrut(6));
        contenido.add(campoStockMinimo);

        JTextField campoStockInicial = null;
        if (esNuevo) {
            contenido.add(Box.createVerticalStrut(14));
            campoStockInicial = centrarCampo(ComponentesUi.crearCampoTexto(""));
            campoStockInicial.setText("0");
            contenido.add(crearEtiquetaCampoCentrada("Stock Inicial"));
            contenido.add(Box.createVerticalStrut(6));
            contenido.add(campoStockInicial);
        }

        contenido.add(Box.createVerticalStrut(24));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        botones.setOpaque(false);
        JButton cancelar = ComponentesUi.crearBotonSecundario("Cancelar", 40);
        JButton guardar = ComponentesUi.crearBotonPrimario(
                esNuevo ? "Guardar Producto" : "Guardar Cambios", 40);
        guardar.setPreferredSize(new Dimension(170, 40));
        botones.add(cancelar);
        botones.add(guardar);
        contenido.add(botones);

        finalizarDialogo(dialogo, contenido, 440, padre);
        final Optional<DatosFormularioProducto>[] resultado = new Optional[]{Optional.empty()};
        final JTextField stockInicialFinal = campoStockInicial;

        cancelar.addActionListener(e -> dialogo.dispose());
        guardar.addActionListener(e -> {
            try {
                String nom = campoNombre.getText().trim();
                double pre = Double.parseDouble(campoPrecio.getText().trim());
                int stkMin = Integer.parseInt(campoStockMinimo.getText().trim());
                int stkIni = esNuevo ? Integer.parseInt(stockInicialFinal.getText().trim()) : 0;
                if (nom.isEmpty() || pre < 0 || stkMin < 0 || stkIni < 0) {
                    throw new IllegalArgumentException();
                }
                resultado[0] = Optional.of(new DatosFormularioProducto(
                        nom, pre, stkMin, stkIni,
                        zonaImagen.getRutaActual(),
                        zonaImagen.getArchivoPendiente()));
                dialogo.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogo,
                        "Datos invalidos. Verifique los campos ingresados.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialogo.getRootPane().setDefaultButton(guardar);
        dialogo.setVisible(true);
        return resultado[0];
    }

    public static Optional<Integer> mostrarSurtirInventario(Window padre, Producto producto) {
        JDialog dialogo = crearDialogoBase(padre);
        JPanel contenido = crearContenidoDialogoCentrado();

        contenido.add(crearTituloDialogo("Surtir Inventario"));
        contenido.add(Box.createVerticalStrut(4));
        contenido.add(crearSubtituloCentrado("Ingresa unidades para: " + producto.getNombre()));
        contenido.add(Box.createVerticalStrut(16));

        contenido.add(crearEtiquetaCampoCentrada("Unidades a Ingresar"));
        contenido.add(Box.createVerticalStrut(6));
        JTextField campoUnidades = centrarCampo(ComponentesUi.crearCampoTexto(""));
        contenido.add(campoUnidades);
        contenido.add(Box.createVerticalStrut(14));

        JPanel panelStock = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0)) {
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
        panelStock.setBorder(new EmptyBorder(12, 20, 12, 20));
        panelStock.setMaximumSize(new Dimension(360, 48));

        JLabel lblNuevoStock = new JLabel("Nuevo Stock:");
        lblNuevoStock.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblNuevoStock.setForeground(Colores.GRIS_TEXTO);
        JLabel valorStock = new JLabel(String.valueOf(producto.getStock()));
        valorStock.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valorStock.setForeground(Colores.AZUL_PRIMARIO);
        panelStock.add(lblNuevoStock);
        panelStock.add(valorStock);
        contenido.add(panelStock);
        contenido.add(Box.createVerticalStrut(24));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        botones.setOpaque(false);
        JButton cancelar = ComponentesUi.crearBotonSecundario("Cancelar", 40);
        JButton guardar = ComponentesUi.crearBotonPrimario("Guardar Inventario", 40);
        guardar.setPreferredSize(new Dimension(170, 40));
        botones.add(cancelar);
        botones.add(guardar);
        contenido.add(botones);

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

        finalizarDialogo(dialogo, contenido, 400, padre);
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
        JPanel contenido = crearContenidoDialogoCentrado();

        contenido.add(crearTituloDialogo("Eliminar Producto"));
        contenido.add(Box.createVerticalStrut(12));

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
        icono.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel mensaje = new JLabel("<html><center>Desea eliminar el articulo<br><b>"
                + producto.getNombre() + "</b>?<br>Esta accion no se puede deshacer.</center></html>");
        mensaje.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mensaje.setForeground(Colores.NEGRO_TEXTO);
        mensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        mensaje.setBorder(new EmptyBorder(12, 0, 20, 0));

        contenido.add(icono);
        contenido.add(mensaje);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        botones.setOpaque(false);
        JButton cancelar = ComponentesUi.crearBotonSecundario("Cancelar", 40);
        JButton eliminar = ComponentesUi.crearBotonRojo("Eliminar");
        eliminar.setPreferredSize(new Dimension(120, 40));
        botones.add(cancelar);
        botones.add(eliminar);
        contenido.add(botones);

        finalizarDialogo(dialogo, contenido, 400, padre);
        final boolean[] confirmado = {false};

        cancelar.addActionListener(e -> dialogo.dispose());
        eliminar.addActionListener(e -> {
            confirmado[0] = true;
            dialogo.dispose();
        });

        dialogo.setVisible(true);
        return confirmado[0];
    }

    /**
     * Registro de mercancia: entrada individual o masiva al inventario.
     * @return lista de entradas confirmadas (puede estar vacia si se cancela)
     */
    public static Optional<List<EntradaMercancia>> mostrarRegistroMercancia(
            Window padre, List<Producto> productos) {

        if (productos == null || productos.isEmpty()) {
            JOptionPane.showMessageDialog(padre,
                    "No hay productos registrados en el inventario.",
                    "Sin productos", JOptionPane.INFORMATION_MESSAGE);
            return Optional.empty();
        }

        JDialog dialogo = crearDialogoBase(padre);
        JPanel contenido = crearContenidoDialogoCentrado();

        contenido.add(crearTituloDialogo("Registro de Mercancia"));
        contenido.add(Box.createVerticalStrut(4));
        contenido.add(crearSubtituloCentrado("Ingresa existencias de forma individual o masiva"));
        contenido.add(Box.createVerticalStrut(16));

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pestanas.setAlignmentX(Component.CENTER_ALIGNMENT);
        pestanas.setMaximumSize(new Dimension(380, 320));

        // --- Entrada individual ---
        JPanel panelIndividual = new JPanel();
        panelIndividual.setOpaque(false);
        panelIndividual.setLayout(new BoxLayout(panelIndividual, BoxLayout.Y_AXIS));

        JComboBox<String> comboProductos = new JComboBox<>();
        for (Producto p : productos) {
            comboProductos.addItem(FormatoIdUtil.formatearIdVisual(p.getId()) + " - " + p.getNombre());
        }
        comboProductos.setMaximumSize(new Dimension(340, 36));
        comboProductos.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField campoCantidad = centrarCampo(ComponentesUi.crearCampoTexto("1"));
        JLabel lblStockActual = new JLabel("", SwingConstants.CENTER);
        lblStockActual.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStockActual.setForeground(Colores.GRIS_TEXTO);
        lblStockActual.setAlignmentX(Component.CENTER_ALIGNMENT);

        Runnable actualizarStockLabel = () -> {
            int idx = comboProductos.getSelectedIndex();
            if (idx >= 0 && idx < productos.size()) {
                Producto p = productos.get(idx);
                lblStockActual.setText("Stock actual: " + p.getStock() + " unidades");
            }
        };
        comboProductos.addActionListener(e -> actualizarStockLabel.run());
        actualizarStockLabel.run();

        panelIndividual.add(crearEtiquetaCampoCentrada("Producto"));
        panelIndividual.add(Box.createVerticalStrut(6));
        panelIndividual.add(comboProductos);
        panelIndividual.add(Box.createVerticalStrut(12));
        panelIndividual.add(crearEtiquetaCampoCentrada("Cantidad a ingresar"));
        panelIndividual.add(Box.createVerticalStrut(6));
        panelIndividual.add(campoCantidad);
        panelIndividual.add(Box.createVerticalStrut(8));
        panelIndividual.add(lblStockActual);
        pestanas.addTab("Individual", panelIndividual);

        // --- Entrada masiva ---
        String[] columnas = {"ID", "Producto", "Stock actual", "Cant. a ingresar"};
        DefaultTableModel modeloMasivo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 2 || columnIndex == 3) {
                    return Integer.class;
                }
                return String.class;
            }
        };
        for (Producto p : productos) {
            modeloMasivo.addRow(new Object[]{
                    p.getId(), p.getNombre(), p.getStock(), 0
            });
        }
        JTable tablaMasiva = new JTable(modeloMasivo);
        tablaMasiva.setRowHeight(28);
        tablaMasiva.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaMasiva.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaMasiva.getColumnModel().getColumn(1).setPreferredWidth(160);
        tablaMasiva.getColumnModel().getColumn(2).setPreferredWidth(90);
        tablaMasiva.getColumnModel().getColumn(3).setPreferredWidth(100);

        JScrollPane scrollMasivo = new JScrollPane(tablaMasiva);
        scrollMasivo.setPreferredSize(new Dimension(360, 220));
        pestanas.addTab("Masiva", scrollMasivo);

        contenido.add(pestanas);
        contenido.add(Box.createVerticalStrut(20));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        botones.setOpaque(false);
        JButton cancelar = ComponentesUi.crearBotonSecundario("Cancelar", 40);
        JButton registrar = ComponentesUi.crearBotonPrimario("Registrar Entrada", 40);
        registrar.setPreferredSize(new Dimension(170, 40));
        botones.add(cancelar);
        botones.add(registrar);
        contenido.add(botones);

        finalizarDialogo(dialogo, contenido, 440, padre);
        final Optional<List<EntradaMercancia>>[] resultado = new Optional[]{Optional.empty()};

        cancelar.addActionListener(e -> dialogo.dispose());
        registrar.addActionListener(e -> {
            List<EntradaMercancia> entradas = new ArrayList<>();
            int tab = pestanas.getSelectedIndex();
            try {
                if (tab == 0) {
                    int idx = comboProductos.getSelectedIndex();
                    int cant = Integer.parseInt(campoCantidad.getText().trim());
                    if (idx < 0 || cant <= 0) {
                        throw new IllegalArgumentException();
                    }
                    entradas.add(new EntradaMercancia(productos.get(idx).getId(), cant));
                } else {
                    for (int i = 0; i < modeloMasivo.getRowCount(); i++) {
                        Object valor = modeloMasivo.getValueAt(i, 3);
                        int cant = valor instanceof Number n ? n.intValue()
                                : Integer.parseInt(String.valueOf(valor).trim());
                        if (cant > 0) {
                            int id = (Integer) modeloMasivo.getValueAt(i, 0);
                            entradas.add(new EntradaMercancia(id, cant));
                        }
                    }
                    if (entradas.isEmpty()) {
                        throw new IllegalArgumentException();
                    }
                }
                resultado[0] = Optional.of(entradas);
                dialogo.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogo,
                        "Ingrese cantidades validas mayores a cero.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialogo.setVisible(true);
        return resultado[0];
    }

    private static JDialog crearDialogoBase(Window padre) {
        Frame frame = padre instanceof Frame ? (Frame) padre : null;
        JDialog dialogo = new JDialog(frame, true);
        dialogo.setResizable(false);
        dialogo.setUndecorated(true);
        return dialogo;
    }

    private static void finalizarDialogo(JDialog dialogo, JPanel contenido, int anchoMinimo, Window padre) {
        JPanel marco = envolverDialogo(contenido);
        marco.setPreferredSize(new Dimension(anchoMinimo, marco.getPreferredSize().height));
        dialogo.setContentPane(marco);
        DialogoVentanaUtil.aplicarVentanaModal(dialogo, padre, contenido);
    }

    private static JPanel crearContenidoDialogoCentrado() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(0, 8, 0, 8));
        return panel;
    }

    private static JTextField centrarCampo(JTextField campo) {
        campo.setAlignmentX(Component.CENTER_ALIGNMENT);
        campo.setMaximumSize(new Dimension(320, 42));
        campo.setPreferredSize(new Dimension(320, 42));
        return campo;
    }

    private static JLabel crearTituloDialogo(String texto) {
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(Colores.NEGRO_TEXTO);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    private static JLabel crearSubtituloCentrado(String texto) {
        JLabel lbl = new JLabel("<html><center>" + texto + "</center></html>", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(Colores.GRIS_TEXTO);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    private static JLabel crearEtiquetaCampoCentrada(String texto) {
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(Colores.NEGRO_TEXTO);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
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

        JPanel barraArrastre = new JPanel();
        barraArrastre.setOpaque(false);
        barraArrastre.setPreferredSize(new Dimension(10, 12));
        barraArrastre.setToolTipText("Arrastrar ventana");

        marco.add(barraArrastre, BorderLayout.NORTH);
        marco.add(contenido, BorderLayout.CENTER);
        return marco;
    }

}
