package com.abarrotespro.vista.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * Centrado y arrastre de ventanas modales sin decoracion (zonas no interactivas).
 */
public final class DialogoVentanaUtil {

    private DialogoVentanaUtil() {
    }

    /**
     * Centra el dialogo y habilita arrastre en etiquetas, paneles decorativos y marco.
     */
    public static void aplicarVentanaModal(JDialog dialogo, Window padre, Component raizContenido) {
        dialogo.pack();
        centrarEnPantalla(dialogo, padre);
        if (dialogo.getContentPane() instanceof JComponent marco) {
            registrarZonaArrastre(dialogo, marco);
        }
        if (raizContenido != null) {
            registrarArrastreEnNoInteractivos(dialogo, raizContenido);
        }
    }

    /** Centra respecto a la ventana padre o a la pantalla si no hay padre. */
    public static void centrarEnPantalla(JDialog dialogo, Window padre) {
        dialogo.setLocationRelativeTo(padre != null ? padre : null);
    }

    public static void registrarZonaArrastre(JDialog dialogo, JComponent zona) {
        if (zona == null) {
            return;
        }
        final Point offset = new Point();
        MouseAdapter presionar = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                offset.x = e.getXOnScreen() - dialogo.getX();
                offset.y = e.getYOnScreen() - dialogo.getY();
                zona.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                zona.setCursor(Cursor.getDefaultCursor());
            }
        };
        MouseMotionAdapter arrastrar = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
                    dialogo.setLocation(
                            e.getXOnScreen() - offset.x,
                            e.getYOnScreen() - offset.y);
                }
            }
        };
        zona.addMouseListener(presionar);
        zona.addMouseMotionListener(arrastrar);
    }

    private static void registrarArrastreEnNoInteractivos(JDialog dialogo, Component componente) {
        if (componente == null || esComponenteInteractivo(componente)) {
            return;
        }
        if (componente instanceof JLabel etiqueta) {
            registrarZonaArrastre(dialogo, etiqueta);
        } else if (componente instanceof JPanel panel && !esPanelVistaPreviaImagen(panel)) {
            registrarZonaArrastre(dialogo, panel);
        }
        if (componente instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) {
                registrarArrastreEnNoInteractivos(dialogo, hijo);
            }
        }
    }

    private static boolean esComponenteInteractivo(Component componente) {
        return componente instanceof JTextField
                || componente instanceof JPasswordField
                || componente instanceof JButton
                || componente instanceof JComboBox
                || componente instanceof JSpinner
                || componente instanceof JTable
                || componente instanceof JList
                || componente instanceof JScrollPane
                || componente instanceof JTextArea;
    }

    private static boolean esPanelVistaPreviaImagen(JPanel panel) {
        return panel.getClass().getName().contains("PanelVistaPreviaImagen");
    }
}
