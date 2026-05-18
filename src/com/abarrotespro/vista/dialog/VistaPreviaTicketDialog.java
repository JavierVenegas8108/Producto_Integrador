package com.abarrotespro.vista.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import com.abarrotespro.vista.util.ComponentesUi;

public final class VistaPreviaTicketDialog extends JDialog {

    private static final int ANCHO_TICKET = 42;

    private VistaPreviaTicketDialog(Window padre, String contenidoTicket) {
        super(padre, "Vista previa del ticket", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setBackground(Color.WHITE);
        raiz.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextArea area = new JTextArea(centrarContenido(contenidoTicket));
        area.setEditable(false);
        area.setOpaque(true);
        area.setBackground(Color.WHITE);
        area.setForeground(Color.BLACK);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setLineWrap(false);
        area.setBorder(new EmptyBorder(12, 16, 12, 16));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(420, 480));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoton.setOpaque(false);
        JButton cerrar = ComponentesUi.crearBotonSecundario("Cerrar", 40);
        cerrar.setPreferredSize(new Dimension(140, 40));
        cerrar.addActionListener(e -> dispose());
        panelBoton.add(cerrar);

        raiz.add(scroll, BorderLayout.CENTER);
        raiz.add(panelBoton, BorderLayout.SOUTH);
        setContentPane(raiz);
        pack();
        setLocationRelativeTo(padre);
    }

    public static void mostrar(Window padre, String contenidoTicket) {
        if (contenidoTicket == null || contenidoTicket.isBlank()) {
            return;
        }
        VistaPreviaTicketDialog dialogo = new VistaPreviaTicketDialog(padre, contenidoTicket);
        dialogo.setVisible(true);
    }

    private static String centrarContenido(String texto) {
        StringBuilder resultado = new StringBuilder();
        String[] lineas = texto.split("\n", -1);
        for (String linea : lineas) {
            String limpia = linea.stripTrailing();
            if (limpia.isEmpty()) {
                resultado.append('\n');
                continue;
            }
            if (limpia.length() >= ANCHO_TICKET) {
                resultado.append(limpia).append('\n');
            } else {
                int espacios = (ANCHO_TICKET - limpia.length()) / 2;
                resultado.append(" ".repeat(espacios)).append(limpia).append('\n');
            }
        }
        return resultado.toString();
    }
}
