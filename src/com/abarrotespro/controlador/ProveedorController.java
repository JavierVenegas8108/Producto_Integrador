package com.abarrotespro.controlador;

import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;

import com.abarrotespro.modelo.Proveedor;
import com.abarrotespro.modelo.SistemaPos;
import com.abarrotespro.vista.dialog.ProveedorDialog;
import com.abarrotespro.vista.panel.ProveedoresPanel;

/**
 * Controlador del modulo de proveedores (MVC).
 */
public class ProveedorController {

    private final SistemaPos modelo;
    private final ProveedoresPanel vista;
    private final Window ventanaPadre;

    public ProveedorController(SistemaPos modelo, ProveedoresPanel vista, Window ventanaPadre) {
        this.modelo = modelo;
        this.vista = vista;
        this.ventanaPadre = ventanaPadre;
    }

    public void inicializar() {
        vista.getBotonNuevo().addActionListener(e -> mostrarDialogoNuevo());
        vista.alEditar(this::mostrarDialogoEditar);
        vista.alDesactivar(this::desactivarProveedor);
        vista.getCampoBusqueda().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                refrescarTabla();
            }
        });
        refrescarTabla();
    }

    public void refrescarTabla() {
        String busqueda = vista.getCampoBusqueda().getText();
        vista.actualizarTabla(modelo.buscarProveedores(busqueda));
    }

    private void mostrarDialogoNuevo() {
        ProveedorDialog dialogo = new ProveedorDialog(ventanaPadre, null);
        dialogo.setVisible(true);
        Proveedor capturado = dialogo.getProveedorResultado();
        if (capturado != null) {
            modelo.guardarProveedor(capturado);
            refrescarTabla();
            JOptionPane.showMessageDialog(ventanaPadre,
                    "Proveedor registrado correctamente", "Operacion exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void mostrarDialogoEditar(Proveedor proveedor) {
        Proveedor existente = modelo.buscarProveedorPorId(proveedor.getId());
        if (existente == null) {
            return;
        }
        ProveedorDialog dialogo = new ProveedorDialog(ventanaPadre, existente);
        dialogo.setVisible(true);
        Proveedor capturado = dialogo.getProveedorResultado();
        if (capturado != null) {
            modelo.guardarProveedor(capturado);
            refrescarTabla();
            JOptionPane.showMessageDialog(ventanaPadre,
                    "Proveedor actualizado correctamente", "Operacion exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void desactivarProveedor(Proveedor proveedor) {
        int confirm = JOptionPane.showConfirmDialog(ventanaPadre,
                "Desactivar al proveedor " + proveedor.getRazonSocial() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            modelo.desactivarProveedor(proveedor.getId());
            refrescarTabla();
        }
    }
}
