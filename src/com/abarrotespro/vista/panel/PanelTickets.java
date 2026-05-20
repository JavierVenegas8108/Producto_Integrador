package com.abarrotespro.vista.panel;

import java.awt.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.abarrotespro.modelo.servicio.LectorTickets;
import com.abarrotespro.vista.util.Colores;
import com.abarrotespro.vista.util.ComponentesUi;

/**
 * Modulo de historial: lista archivos .txt de tickets y muestra su contenido.
 */
public class PanelTickets extends JPanel {

    private final DefaultListModel<String> modeloLista;
    private final JList<String> listaArchivos;
    private final JTextArea areaContenido;
    private final CardLayout cardContenido;
    private final JPanel panelCentro;
    private final JButton botonReimprimir;
    private final JButton botonDevolucion;
    private String archivoSeleccionado;

    public PanelTickets() {
        setBackground(Colores.FONDO_APP);
        setLayout(new BorderLayout(16, 0));
        setBorder(new EmptyBorder(0, 8, 0, 0));

        modeloLista = new DefaultListModel<>();
        listaArchivos = new JList<>(modeloLista);
        listaArchivos.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                String texto = String.valueOf(value);
                lbl.setOpaque(true);
                lbl.setBackground(isSelected ? Colores.SIDEBAR_ACTIVO : Colores.BLANCO);
                if (LectorTickets.esCancelado(texto)) {
                    lbl.setForeground(Colores.ROJO);
                    lbl.setText("<html><strike>" + texto + "</strike></html>");
                } else {
                    lbl.setForeground(Colores.NEGRO_TEXTO);
                    lbl.setText(texto);
                }
                lbl.setBorder(new EmptyBorder(4, 8, 4, 8));
                return lbl;
            }
        });
        listaArchivos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listaArchivos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaArchivos.setFixedCellHeight(32);
        listaArchivos.setBackground(Colores.BLANCO);
        listaArchivos.setForeground(Colores.NEGRO_TEXTO);
        listaArchivos.setSelectionBackground(Colores.SIDEBAR_ACTIVO);
        listaArchivos.setSelectionForeground(Colores.NEGRO_TEXTO);

        JScrollPane scrollLista = new JScrollPane(listaArchivos);
        scrollLista.setBorder(BorderFactory.createLineBorder(Colores.GRIS_BORDE, 1, true));
        scrollLista.setPreferredSize(new Dimension(280, 0));

        JPanel panelLista = new JPanel(new BorderLayout(0, 8));
        panelLista.setOpaque(false);
        JLabel tituloLista = new JLabel("Archivos de ticket");
        tituloLista.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tituloLista.setForeground(Colores.NEGRO_TEXTO);
        panelLista.add(tituloLista, BorderLayout.NORTH);
        panelLista.add(scrollLista, BorderLayout.CENTER);

        areaContenido = new JTextArea();
        areaContenido.setEditable(false);
        areaContenido.setFont(new Font("Consolas", Font.PLAIN, 13));
        areaContenido.setBackground(Colores.BLANCO);
        areaContenido.setForeground(Colores.NEGRO_TEXTO);
        areaContenido.setLineWrap(false);
        areaContenido.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel etiquetaVacio = new JLabel("Seleccione un ticket de la lista para ver su contenido");
        etiquetaVacio.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        etiquetaVacio.setForeground(Colores.GRIS_TEXTO);
        etiquetaVacio.setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane scrollContenido = new JScrollPane(areaContenido);
        scrollContenido.setBorder(null);

        cardContenido = new CardLayout();
        panelCentro = new JPanel(cardContenido);
        panelCentro.setOpaque(false);
        panelCentro.add(etiquetaVacio, "vacio");
        panelCentro.add(scrollContenido, "contenido");

        botonReimprimir = ComponentesUi.crearBotonSecundario("Reimprimir Ticket", 40);
        botonDevolucion = ComponentesUi.crearBotonRojo("Realizar Devolucion");
        botonDevolucion.setPreferredSize(new Dimension(180, 40));
        botonReimprimir.setPreferredSize(new Dimension(160, 40));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.setOpaque(false);
        panelBotones.setBorder(new EmptyBorder(12, 20, 16, 20));
        panelBotones.add(botonReimprimir);
        panelBotones.add(botonDevolucion);

        JPanel panelDetalle = ComponentesUi.crearPanelRedondeado(Colores.FONDO_TARJETA, 16);
        panelDetalle.putClientProperty("tema.decorado", Boolean.TRUE);
        panelDetalle.setLayout(new BorderLayout());
        panelDetalle.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel tituloDetalle = new JLabel("Detalle del ticket");
        tituloDetalle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tituloDetalle.setForeground(Colores.NEGRO_TEXTO);
        tituloDetalle.setBorder(new EmptyBorder(16, 20, 8, 20));

        panelDetalle.add(tituloDetalle, BorderLayout.NORTH);
        panelDetalle.add(panelCentro, BorderLayout.CENTER);
        panelDetalle.add(panelBotones, BorderLayout.SOUTH);

        add(panelLista, BorderLayout.WEST);
        add(panelDetalle, BorderLayout.CENTER);
    }

    public JList<String> getListaArchivos() {
        return listaArchivos;
    }

    public JButton getBotonReimprimir() {
        return botonReimprimir;
    }

    public JButton getBotonDevolucion() {
        return botonDevolucion;
    }

    public String getArchivoSeleccionado() {
        return archivoSeleccionado;
    }

    public void actualizarLista(List<String> archivos) {
        modeloLista.clear();
        for (String nombre : archivos) {
            modeloLista.addElement(nombre);
        }
        archivoSeleccionado = null;
        areaContenido.setText("");
        cardContenido.show(panelCentro, "vacio");
        actualizarBotonesAccion();
    }

    public void mostrarContenido(String nombreArchivo, String contenido) {
        archivoSeleccionado = nombreArchivo;
        if (contenido == null || contenido.isBlank()) {
            areaContenido.setText("");
            cardContenido.show(panelCentro, "vacio");
        } else {
            areaContenido.setText(contenido);
            areaContenido.setCaretPosition(0);
            cardContenido.show(panelCentro, "contenido");
        }
        actualizarBotonesAccion();
    }

    private void actualizarBotonesAccion() {
        boolean haySeleccion = archivoSeleccionado != null && !archivoSeleccionado.isBlank();
        boolean cancelado = haySeleccion && LectorTickets.esCancelado(archivoSeleccionado);
        botonReimprimir.setEnabled(haySeleccion);
        botonDevolucion.setEnabled(haySeleccion && !cancelado);
    }
}
