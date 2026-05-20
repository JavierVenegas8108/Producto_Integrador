package com.abarrotespro.vista.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.BoxLayout;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import com.abarrotespro.controlador.VentaController;
import com.abarrotespro.modelo.MetodoPago;
import com.abarrotespro.modelo.Venta;
import com.abarrotespro.vista.util.Colores;
import com.abarrotespro.vista.util.ComponentesUi;

/**
 * Dialogo de cobro con metodo de pago y billetes rapidos.
 */
public final class DialogoCobro extends JDialog {

    private final VentaController ventaController;
    private final double totalCobrar;
    private MetodoPago metodoSeleccionado;
    private JLabel etiquetaAcumulado;
    private JLabel etiquetaCambio;
    private boolean confirmado;

    private DialogoCobro(Window padre, Venta venta, VentaController ventaController) {
        super(padre, "Cobrar venta", ModalityType.APPLICATION_MODAL);
        this.ventaController = ventaController;
        this.totalCobrar = venta.getMontoCobrable();
        this.metodoSeleccionado = MetodoPago.EFECTIVO;
        this.confirmado = false;
        ventaController.reiniciarAcumuladorEfectivo();
        construirUi();
        setSize(420, 480);
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
        contenido.add(centro, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(0, 8));
        etiquetaAcumulado = new JLabel("Efectivo recibido: $0.00", SwingConstants.CENTER);
        etiquetaAcumulado.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        etiquetaCambio = new JLabel(" ", SwingConstants.CENTER);
        etiquetaCambio.setFont(new Font("Segoe UI", Font.BOLD, 13));
        etiquetaCambio.setForeground(Colores.VERDE);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnCancelar = ComponentesUi.crearBotonSecundario("Cancelar", 36);
        JButton btnCobrar = ComponentesUi.crearBotonPrimario("Confirmar cobro", 36);
        btnCancelar.addActionListener(e -> dispose());
        btnCobrar.addActionListener(e -> {
            confirmado = true;
            dispose();
        });
        botones.add(btnCancelar);
        botones.add(btnCobrar);

        pie.add(etiquetaAcumulado, BorderLayout.NORTH);
        pie.add(etiquetaCambio, BorderLayout.CENTER);
        pie.add(botones, BorderLayout.SOUTH);
        contenido.add(pie, BorderLayout.SOUTH);

        add(contenido);
        actualizarEtiquetasEfectivo();
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
                actualizarEtiquetasEfectivo();
            });
        }
        return panel;
    }

    private JPanel crearPanelBilletes() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Billetes rapidos"));
        for (int denominacion : new int[] { 20, 50, 100, 200, 500, 1000 }) {
            JButton btn = new JButton("$" + denominacion);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            int valor = denominacion;
            btn.addActionListener(e -> {
                if (metodoSeleccionado == MetodoPago.EFECTIVO) {
                    ventaController.agregarBillete(valor);
                    actualizarEtiquetasEfectivo();
                }
            });
            panel.add(btn);
        }
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.add(panel, BorderLayout.CENTER);
        JButton btnLimpiar = ComponentesUi.crearBotonSecundario("Limpiar efectivo", 32);
        btnLimpiar.addActionListener(e -> {
            ventaController.reiniciarAcumuladorEfectivo();
            actualizarEtiquetasEfectivo();
        });
        contenedor.add(btnLimpiar, BorderLayout.SOUTH);
        return contenedor;
    }

    private void actualizarEtiquetasEfectivo() {
        if (metodoSeleccionado == MetodoPago.EFECTIVO) {
            double recibido = ventaController.getMontoAcumuladoEfectivo();
            etiquetaAcumulado.setText("Efectivo recibido: " + ComponentesUi.formatearMoneda(recibido));
            if (recibido >= totalCobrar) {
                double cambio = recibido - totalCobrar;
                etiquetaCambio.setText("Cambio: " + ComponentesUi.formatearMoneda(cambio));
            } else {
                double faltante = totalCobrar - recibido;
                etiquetaCambio.setText("Faltan: " + ComponentesUi.formatearMoneda(faltante));
                etiquetaCambio.setForeground(Colores.ROJO);
            }
        } else {
            etiquetaAcumulado.setText("Pago con " + metodoSeleccionado.name().toLowerCase());
            etiquetaCambio.setText(" ");
        }
    }

    public record ResultadoCobro(MetodoPago metodoPago, double efectivoRecibido) {
    }
}
