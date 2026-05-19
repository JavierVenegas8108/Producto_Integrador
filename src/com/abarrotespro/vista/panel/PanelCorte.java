package com.abarrotespro.vista.panel;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.abarrotespro.modelo.Corte;
import com.abarrotespro.vista.util.Colores;
import com.abarrotespro.vista.util.ComponentesUi;

/**
 * Modulo de corte de caja con resumen y historial.
 */
public class PanelCorte extends JPanel {

    private JLabel etiquetaTotalCaja;
    private JLabel etiquetaVentasHoy;
    private JLabel etiquetaPromedio;
    private JLabel etiquetaEntradas;
    private JPanel listaHistorial;
    private JButton botonCerrarDia;
    private JComboBox<String> comboFiltroFecha;
    private JTextField campoFechaDesde;
    private JTextField campoFechaHasta;
    private JButton botonVerReporte;
    private JButton botonExportarCsv;

    public static final String FILTRO_HOY = "Hoy";
    public static final String FILTRO_ULTIMOS_7 = "Ultimos 7 dias";
    public static final String FILTRO_RANGO = "Rango Personalizado";

    public PanelCorte() {
        setBackground(Colores.FONDO_APP);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 8, 0, 0));

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        contenido.add(crearTarjetaPrincipal());
        contenido.add(Box.createVerticalStrut(16));
        contenido.add(crearTarjetasResumen());
        contenido.add(Box.createVerticalStrut(24));
        contenido.add(crearSeccionReporteVentas());
        contenido.add(Box.createVerticalStrut(24));
        contenido.add(crearSeccionHistorial());
        contenido.add(Box.createVerticalStrut(24));
        contenido.add(crearBotonCerrar());

        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel crearTarjetaPrincipal() {
        JPanel tarjeta = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Colores.AZUL_OSCURO);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        tarjeta.setOpaque(false);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(new EmptyBorder(28, 32, 28, 32));
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel lbl = new JLabel("TOTAL EN CAJA (EFECTIVO)");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(191, 219, 254));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        etiquetaTotalCaja = new JLabel("$0.00");
        etiquetaTotalCaja.setFont(new Font("Segoe UI", Font.BOLD, 42));
        etiquetaTotalCaja.setForeground(Color.WHITE);
        etiquetaTotalCaja.setAlignmentX(Component.LEFT_ALIGNMENT);

        tarjeta.add(lbl);
        tarjeta.add(Box.createVerticalStrut(8));
        tarjeta.add(etiquetaTotalCaja);
        return tarjeta;
    }

    private JPanel crearTarjetasResumen() {
        JPanel fila = new JPanel(new GridLayout(1, 3, 16, 0));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        etiquetaVentasHoy = crearTarjetaPequena(fila, "Ventas hoy", "$0.00");
        etiquetaPromedio = crearTarjetaPequena(fila, "Promedio Ticket", "$0.00");
        etiquetaEntradas = crearTarjetaPequena(fila, "Utilidad hoy", "$0.00");
        return fila;
    }

    private JLabel crearTarjetaPequena(JPanel contenedor, String titulo, String valorInicial) {
        JPanel tarjeta = ComponentesUi.crearPanelRedondeado(Color.WHITE, 12);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitulo.setForeground(Colores.GRIS_TEXTO);

        JLabel lblValor = new JLabel(valorInicial);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValor.setForeground(Colores.NEGRO_TEXTO);

        tarjeta.add(lblTitulo);
        tarjeta.add(Box.createVerticalStrut(8));
        tarjeta.add(lblValor);
        contenedor.add(tarjeta);
        return lblValor;
    }

    private JPanel crearSeccionReporteVentas() {
        JPanel seccion = ComponentesUi.crearPanelRedondeado(Color.WHITE, 12);
        seccion.setLayout(new BoxLayout(seccion, BoxLayout.Y_AXIS));
        seccion.setBorder(new EmptyBorder(20, 24, 20, 24));
        seccion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JLabel titulo = new JLabel("Reporte de Ventas por Fecha");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(Colores.NEGRO_TEXTO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel filaFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        filaFiltro.setOpaque(false);
        filaFiltro.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaFiltro.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JLabel lblFiltro = new JLabel("Periodo:");
        lblFiltro.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblFiltro.setForeground(Colores.GRIS_TEXTO);

        comboFiltroFecha = new JComboBox<>(new String[] { FILTRO_HOY, FILTRO_ULTIMOS_7, FILTRO_RANGO });
        comboFiltroFecha.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboFiltroFecha.setPreferredSize(new Dimension(200, 32));

        JLabel lblDesde = new JLabel("Desde (dd/MM/yyyy):");
        lblDesde.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesde.setForeground(Colores.GRIS_TEXTO);

        campoFechaDesde = ComponentesUi.crearCampoTexto("dd/MM/yyyy");
        campoFechaDesde.setPreferredSize(new Dimension(120, 36));
        campoFechaDesde.setEnabled(false);

        JLabel lblHasta = new JLabel("Hasta (dd/MM/yyyy):");
        lblHasta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHasta.setForeground(Colores.GRIS_TEXTO);

        campoFechaHasta = ComponentesUi.crearCampoTexto("dd/MM/yyyy");
        campoFechaHasta.setPreferredSize(new Dimension(120, 36));
        campoFechaHasta.setEnabled(false);

        comboFiltroFecha.addActionListener(e -> actualizarCamposRango());

        filaFiltro.add(lblFiltro);
        filaFiltro.add(comboFiltroFecha);
        filaFiltro.add(lblDesde);
        filaFiltro.add(campoFechaDesde);
        filaFiltro.add(lblHasta);
        filaFiltro.add(campoFechaHasta);

        JPanel filaBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filaBotones.setOpaque(false);
        filaBotones.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaBotones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        botonVerReporte = ComponentesUi.crearBotonPrimario("Ver Reporte de Ventas", 40);
        botonExportarCsv = ComponentesUi.crearBotonSecundario("Exportar a Excel (CSV)", 40);
        botonExportarCsv.setPreferredSize(new Dimension(220, 40));

        filaBotones.add(botonVerReporte);
        filaBotones.add(botonExportarCsv);

        seccion.add(titulo);
        seccion.add(Box.createVerticalStrut(12));
        seccion.add(filaFiltro);
        seccion.add(Box.createVerticalStrut(8));
        seccion.add(filaBotones);
        return seccion;
    }

    private void actualizarCamposRango() {
        boolean rango = FILTRO_RANGO.equals(comboFiltroFecha.getSelectedItem());
        campoFechaDesde.setEnabled(rango);
        campoFechaHasta.setEnabled(rango);
    }

    private JPanel crearSeccionHistorial() {
        JPanel seccion = new JPanel();
        seccion.setOpaque(false);
        seccion.setLayout(new BoxLayout(seccion, BoxLayout.Y_AXIS));
        seccion.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titulo = new JLabel("Historial de Cortes Anteriores");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(Colores.NEGRO_TEXTO);
        titulo.setBorder(new EmptyBorder(0, 0, 12, 0));

        listaHistorial = new JPanel();
        listaHistorial.setOpaque(false);
        listaHistorial.setLayout(new BoxLayout(listaHistorial, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(listaHistorial);
        scroll.setBorder(BorderFactory.createLineBorder(Colores.GRIS_BORDE, 1, true));
        scroll.setPreferredSize(new Dimension(0, 220));
        scroll.getViewport().setBackground(Color.WHITE);

        seccion.add(titulo);
        seccion.add(scroll);
        seccion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        return seccion;
    }

    private JPanel crearBotonCerrar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setOpaque(false);
        botonCerrarDia = ComponentesUi.crearBotonRojo("CERRAR DIA Y LIMPIAR CAJA");
        botonCerrarDia.setPreferredSize(new Dimension(400, 52));
        botonCerrarDia.setMaximumSize(new Dimension(500, 52));
        panel.add(botonCerrarDia);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        return panel;
    }

    public JButton getBotonCerrarDia() {
        return botonCerrarDia;
    }

    public JComboBox<String> getComboFiltroFecha() {
        return comboFiltroFecha;
    }

    public JTextField getCampoFechaDesde() {
        return campoFechaDesde;
    }

    public JTextField getCampoFechaHasta() {
        return campoFechaHasta;
    }

    public JButton getBotonVerReporte() {
        return botonVerReporte;
    }

    public JButton getBotonExportarCsv() {
        return botonExportarCsv;
    }

    public void actualizarResumen(double totalCaja, double ventasHoy,
                                double promedio, double entradas) {
        etiquetaTotalCaja.setText(ComponentesUi.formatearMoneda(totalCaja));
        etiquetaVentasHoy.setText(ComponentesUi.formatearMoneda(ventasHoy));
        etiquetaPromedio.setText(ComponentesUi.formatearMoneda(promedio));
        etiquetaEntradas.setText(ComponentesUi.formatearMoneda(entradas));
    }

    public void actualizarHistorial(List<Corte> cortes) {
        listaHistorial.removeAll();
        for (Corte corte : cortes) {
            listaHistorial.add(crearFilaHistorial(corte));
            listaHistorial.add(Box.createVerticalStrut(4));
        }
        if (cortes.isEmpty()) {
            JLabel vacio = new JLabel("Sin cortes registrados");
            vacio.setBorder(new EmptyBorder(16, 16, 16, 16));
            vacio.setForeground(Colores.GRIS_TEXTO);
            listaHistorial.add(vacio);
        }
        listaHistorial.revalidate();
        listaHistorial.repaint();
    }

    private JPanel crearFilaHistorial(Corte corte) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(true);
        fila.setBackground(Color.WHITE);
        fila.setBorder(new EmptyBorder(14, 20, 14, 20));
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        JLabel monto = new JLabel(ComponentesUi.formatearMoneda(corte.getMonto()));
        monto.setFont(new Font("Segoe UI", Font.BOLD, 15));
        monto.setForeground(Colores.AZUL_PRIMARIO);

        JLabel detalle = new JLabel(corte.getFechaFormateada() + "  |  " + corte.getUsuarioNombre());
        detalle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detalle.setForeground(Colores.GRIS_TEXTO);
        detalle.setHorizontalAlignment(SwingConstants.RIGHT);

        fila.add(monto, BorderLayout.WEST);
        fila.add(detalle, BorderLayout.EAST);
        return fila;
    }
}
