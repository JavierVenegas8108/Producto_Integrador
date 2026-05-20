package com.abarrotespro.vista.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.BoxLayout;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.abarrotespro.controlador.VentaController;
import com.abarrotespro.modelo.MetodoPago;
import com.abarrotespro.modelo.Venta;
import com.abarrotespro.vista.util.Colores;
import com.abarrotespro.vista.util.ComponentesUi;
import com.abarrotespro.vista.util.GestorImagenProducto;

/**
 * Dialogo de cobro con metodo de pago, billetes rapidos e ingreso manual de efectivo.
 */
public final class DialogoCobro extends JDialog {

    private static final int ANCHO_ICONO_BILLETE = 108;
    private static final int ALTO_ICONO_BILLETE = 52;

    private final VentaController ventaController;
    private final double totalCobrar;
    private MetodoPago metodoSeleccionado;
    private JLabel etiquetaAcumulado;
    private JLabel etiquetaCambio;
    private JTextField campoEfectivoManual;
    private JPanel panelBilletes;
    private JPanel panelIngresoManual;
    private boolean confirmado;
    private boolean sincronizandoCampo;

    private DialogoCobro(Window padre, Venta venta, VentaController ventaController) {
        super(padre, "Cobrar venta", ModalityType.APPLICATION_MODAL);
        this.ventaController = ventaController;
        this.totalCobrar = venta.getTotal();
        this.metodoSeleccionado = MetodoPago.EFECTIVO;
        this.confirmado = false;
        ventaController.reiniciarAcumuladorEfectivo();
        construirUi();
        setSize(460, 560);
        setLocationRelativeTo(padre);
    }

    public static Optional<ResultadoCobro> mostrar(Window padre, Venta venta,
            VentaController ventaController) {
        DialogoCobro dialogo = new DialogoCobro(padre, venta, ventaController);
        dialogo.setVisible(true);
        if (!dialogo.confirmado) {
            return Optional.empty();
        }
        return Optional.of(new ResultadoCobro(
                dialogo.metodoSeleccionado,
                ventaController.getMontoAcumuladoEfectivo()));
    }

    private void construirUi() {
        JPanel contenido = new JPanel(new BorderLayout(0, 12));
        contenido.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel lblTotal = new JLabel("Total a cobrar: " + ComponentesUi.formatearMoneda(totalCobrar));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotal.setForeground(Colores.AZUL_PRIMARIO);
        contenido.add(lblTotal, BorderLayout.NORTH);

        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.add(crearPanelMetodoPago());
        centro.add(crearPanelBilletes());
        centro.add(crearPanelIngresoManual());
        contenido.add(centro, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(0, 8));
        etiquetaAcumulado = new JLabel("Efectivo recibido: $0.00", SwingConstants.CENTER);
        etiquetaAcumulado.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        etiquetaCambio = new JLabel(" ", SwingConstants.CENTER);
        etiquetaCambio.setFont(new Font("Segoe UI", Font.BOLD, 13));
        etiquetaCambio.setForeground(Colores.ROJO);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnCancelar = ComponentesUi.crearBotonSecundario("Cancelar", 36);
        JButton btnCobrar = ComponentesUi.crearBotonPrimario("Confirmar cobro", 36);
        btnCancelar.addActionListener(e -> dispose());
        btnCobrar.addActionListener(e -> confirmarCobro());
        botones.add(btnCancelar);
        botones.add(btnCobrar);

        pie.add(etiquetaAcumulado, BorderLayout.NORTH);
        pie.add(etiquetaCambio, BorderLayout.CENTER);
        pie.add(botones, BorderLayout.SOUTH);
        contenido.add(pie, BorderLayout.SOUTH);

        add(contenido);
        actualizarEtiquetasEfectivo();
        actualizarVisibilidadEfectivo();
    }

    private JPanel crearPanelMetodoPago() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Metodo de pago"));
        ButtonGroup grupo = new ButtonGroup();
        JRadioButton rbEfectivo = new JRadioButton("Efectivo", true);
        JRadioButton rbTarjeta = new JRadioButton("Tarjeta");
        JRadioButton rbTransferencia = new JRadioButton("Transferencia");
        for (JRadioButton rb : new JRadioButton[] { rbEfectivo, rbTarjeta, rbTransferencia }) {
            grupo.add(rb);
            panel.add(rb);
            rb.addActionListener(e -> {
                if (rbEfectivo.isSelected()) {
                    metodoSeleccionado = MetodoPago.EFECTIVO;
                } else if (rbTarjeta.isSelected()) {
                    metodoSeleccionado = MetodoPago.TARJETA;
                    ventaController.reiniciarAcumuladorEfectivo();
                } else {
                    metodoSeleccionado = MetodoPago.TRANSFERENCIA;
                    ventaController.reiniciarAcumuladorEfectivo();
                }
                limpiarCampoManual();
                actualizarVisibilidadEfectivo();
                actualizarEtiquetasEfectivo();
            });
        }
        return panel;
    }

    private JPanel crearPanelBilletes() {
        panelBilletes = new JPanel(new GridLayout(2, 3, 8, 8));
        panelBilletes.setBorder(BorderFactory.createTitledBorder("Billetes rapidos"));
        for (int denominacion : new int[] { 20, 50, 100, 200, 500, 1000 }) {
            JButton btn = crearBotonBillete(denominacion);
            panelBilletes.add(btn);
        }
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.add(panelBilletes, BorderLayout.CENTER);
        JButton btnLimpiar = ComponentesUi.crearBotonSecundario("Limpiar efectivo", 32);
        btnLimpiar.addActionListener(e -> {
            ventaController.reiniciarAcumuladorEfectivo();
            limpiarCampoManual();
            actualizarEtiquetasEfectivo();
        });
        contenedor.add(btnLimpiar, BorderLayout.SOUTH);
        return contenedor;
    }

    private JButton crearBotonBillete(int denominacion) {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(ANCHO_ICONO_BILLETE + 8, ALTO_ICONO_BILLETE + 8));
        btn.setToolTipText("Agregar $" + denominacion);
        ImageIcon icono = GestorImagenProducto.cargarMiniatura(
                "/img/billetes/billete_" + denominacion + ".png",
                ANCHO_ICONO_BILLETE, ALTO_ICONO_BILLETE);
        if (icono != null) {
            btn.setIcon(icono);
        } else {
            btn.setText("$" + denominacion);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        }
        int valor = denominacion;
        btn.addActionListener(e -> {
            if (metodoSeleccionado == MetodoPago.EFECTIVO) {
                ventaController.agregarBillete(valor);
                sincronizarCampoConAcumulador();
                actualizarEtiquetasEfectivo();
            }
        });
        return btn;
    }

    private JPanel crearPanelIngresoManual() {
        panelIngresoManual = new JPanel(new BorderLayout(8, 4));
        panelIngresoManual.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));
        JLabel etiqueta = new JLabel("Ingresar cantidad exacta:");
        etiqueta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        campoEfectivoManual = new JTextField();
        campoEfectivoManual.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campoEfectivoManual.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                procesarEntradaManual();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                procesarEntradaManual();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                procesarEntradaManual();
            }
        });
        panelIngresoManual.add(etiqueta, BorderLayout.NORTH);
        panelIngresoManual.add(campoEfectivoManual, BorderLayout.CENTER);
        return panelIngresoManual;
    }

    private void procesarEntradaManual() {
        if (sincronizandoCampo || metodoSeleccionado != MetodoPago.EFECTIVO) {
            return;
        }
        String texto = campoEfectivoManual.getText().trim().replace("$", "").replace(",", "");
        if (texto.isEmpty()) {
            ventaController.establecerMontoEfectivo(0);
        } else {
            try {
                double monto = Double.parseDouble(texto);
                ventaController.establecerMontoEfectivo(monto);
            } catch (NumberFormatException ignored) {
                return;
            }
        }
        actualizarEtiquetasEfectivo();
    }

    private void sincronizarCampoConAcumulador() {
        sincronizandoCampo = true;
        double recibido = ventaController.getMontoAcumuladoEfectivo();
        if (recibido <= 0) {
            campoEfectivoManual.setText("");
        } else {
            campoEfectivoManual.setText(String.format("%.2f", recibido));
        }
        sincronizandoCampo = false;
    }

    private void limpiarCampoManual() {
        sincronizandoCampo = true;
        campoEfectivoManual.setText("");
        sincronizandoCampo = false;
    }

    private void actualizarVisibilidadEfectivo() {
        boolean efectivo = metodoSeleccionado == MetodoPago.EFECTIVO;
        panelBilletes.setVisible(efectivo);
        panelIngresoManual.setVisible(efectivo);
    }

    private void confirmarCobro() {
        if (metodoSeleccionado == MetodoPago.EFECTIVO) {
            procesarEntradaManual();
            double recibido = ventaController.getMontoAcumuladoEfectivo();
            if (recibido < totalCobrar) {
                JOptionPane.showMessageDialog(this,
                        "El efectivo recibido (" + ComponentesUi.formatearMoneda(recibido)
                                + ") no cubre el total a cobrar ("
                                + ComponentesUi.formatearMoneda(totalCobrar) + ").",
                        "Dinero insuficiente",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        confirmado = true;
        dispose();
    }

    private void actualizarEtiquetasEfectivo() {
        if (metodoSeleccionado == MetodoPago.EFECTIVO) {
            double recibido = ventaController.getMontoAcumuladoEfectivo();
            etiquetaAcumulado.setText("Efectivo recibido: " + ComponentesUi.formatearMoneda(recibido));
            if (recibido >= totalCobrar) {
                double cambio = recibido - totalCobrar;
                etiquetaCambio.setText("Cambio: " + ComponentesUi.formatearMoneda(cambio));
                etiquetaCambio.setForeground(Colores.VERDE);
            } else {
                double faltante = totalCobrar - recibido;
                etiquetaCambio.setText("Faltan: " + ComponentesUi.formatearMoneda(faltante));
                etiquetaCambio.setForeground(Colores.ROJO);
            }
        } else {
            etiquetaAcumulado.setText("Pago con " + metodoSeleccionado.getEtiqueta().toLowerCase());
            etiquetaCambio.setText(" ");
        }
    }

    public record ResultadoCobro(MetodoPago metodoPago, double efectivoRecibido) {
    }
}
