package com.abarrotespro.vista.util;

import java.util.Optional;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.abarrotespro.modelo.Producto;

/**
 * Dialogos de cantidad para el modulo de ventas.
 */
public final class DialogosVenta {

    private DialogosVenta() {
    }

    public static Optional<Integer> preguntarCantidadAgregar(java.awt.Window padre, Producto producto) {
        String mensaje = "Producto: " + producto.getNombre()
                + "\nStock disponible: " + producto.getStock()
                + "\n\n¿Que cantidad desea agregar?";
        return preguntarCantidad(padre, mensaje, 1, producto.getStock());
    }

    public static Optional<Integer> preguntarCantidadEliminar(java.awt.Window padre,
            String nombreProducto, int cantidadEnTicket) {
        String mensaje = "Producto: " + nombreProducto
                + "\nEn ticket: " + cantidadEnTicket
                + "\n\n¿Que cantidad desea eliminar?";
        return preguntarCantidad(padre, mensaje, 1, cantidadEnTicket);
    }

    private static Optional<Integer> preguntarCantidad(java.awt.Window padre, String mensaje,
            int minimo, int maximo) {
        JTextField campo = new JTextField(String.valueOf(minimo), 8);
        int opcion = JOptionPane.showConfirmDialog(padre, new Object[]{mensaje, campo},
                "Cantidad", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) {
            return Optional.empty();
        }
        try {
            int cant = Integer.parseInt(campo.getText().trim());
            if (cant < minimo || cant > maximo) {
                JOptionPane.showMessageDialog(padre,
                        "Ingrese un valor entre " + minimo + " y " + maximo + ".",
                        "Cantidad invalida", JOptionPane.WARNING_MESSAGE);
                return Optional.empty();
            }
            return Optional.of(cant);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(padre,
                    "Ingrese un numero entero valido.",
                    "Cantidad invalida", JOptionPane.WARNING_MESSAGE);
            return Optional.empty();
        }
    }
}
