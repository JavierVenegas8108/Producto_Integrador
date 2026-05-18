package com.abarrotespro.controlador;

import com.abarrotespro.modelo.SistemaPos;
import com.abarrotespro.vista.VistaLogin;
import com.abarrotespro.vista.VistaPrincipal;
import com.abarrotespro.vista.panel.PanelCorte;
import com.abarrotespro.vista.panel.PanelInventario;
import com.abarrotespro.vista.panel.PanelVenta;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * Controlador principal: enlaza vistas con el modelo y coordina la logica de negocio.
 */
public class ControladorPrincipal {

    private final SistemaPos modelo;
    private VistaLogin vistaLogin;
    private VistaPrincipal vistaPrincipal;

    public ControladorPrincipal() {
        this.modelo = new SistemaPos();
    }

    /** Inicia la aplicacion mostrando el login. */
    public void iniciar() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            mostrarLogin();
        });
    }

    private void mostrarLogin() {
        vistaLogin = new VistaLogin();
        vistaLogin.getBotonEntrar().addActionListener(e -> procesarLogin());
        vistaLogin.getCampoContrasena().addActionListener(e -> procesarLogin());
        vistaLogin.setVisible(true);
    }

    private void procesarLogin() {
        String usuario = vistaLogin.getCampoUsuario().getText().trim();
        String contrasena = new String(vistaLogin.getCampoContrasena().getPassword());

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            vistaLogin.mostrarError("Complete todos los campos");
            return;
        }

        if (modelo.iniciarSesion(usuario, contrasena)) {
            vistaLogin.limpiarError();
            vistaLogin.setVisible(false);
            vistaLogin.dispose();
            mostrarPrincipal();
        } else {
            vistaLogin.mostrarError("Usuario o contrasena incorrectos");
        }
    }

    private void mostrarPrincipal() {
        vistaPrincipal = new VistaPrincipal();
        vistaPrincipal.configurarUsuario(
                modelo.getUsuarioActivo().getNombreCompleto(),
                modelo.getUsuarioActivo().getIniciales());

        configurarNavegacion();
        configurarVenta();
        configurarInventario();
        configurarCorte();
        configurarCerrarSesion();

        vistaPrincipal.mostrarModulo(VistaPrincipal.CARD_VENTA, "Venta");
        refrescarVenta();
        refrescarInventario();
        refrescarCorte();

        vistaPrincipal.setVisible(true);
    }

    private void configurarNavegacion() {
        Map<String, String> titulos = Map.of(
                VistaPrincipal.CARD_VENTA, "Venta",
                VistaPrincipal.CARD_INVENTARIO, "Gestion de Inventario",
                VistaPrincipal.CARD_TICKETS, "Tickets",
                VistaPrincipal.CARD_PROVEEDORES, "Proveedores",
                VistaPrincipal.CARD_CORTE, "Corte de Caja",
                VistaPrincipal.CARD_CONFIG, "Configuracion"
        );

        vistaPrincipal.getBotonesNav().forEach((card, boton) -> {
            boton.addActionListener(e -> {
                vistaPrincipal.mostrarModulo(card, titulos.get(card));
                if (VistaPrincipal.CARD_VENTA.equals(card)) {
                    refrescarVenta();
                } else if (VistaPrincipal.CARD_INVENTARIO.equals(card)) {
                    refrescarInventario();
                } else if (VistaPrincipal.CARD_CORTE.equals(card)) {
                    refrescarCorte();
                }
            });
        });
    }

    private void configurarVenta() {
        PanelVenta panel = vistaPrincipal.getPanelVenta();

        panel.getCampoBusqueda().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                refrescarCatalogo();
            }
        });

        panel.actualizarProductos(
                modelo.buscarProductos(""),
                this::agregarProductoAlTicket);

        JButton btnCobrar = panel.getBotonCobrar();
        if (btnCobrar != null) {
            btnCobrar.addActionListener(e -> cobrarVenta());
        }
    }

    private void agregarProductoAlTicket(ActionEvent e) {
        int idProducto = e.getID();
        String error = modelo.agregarAlTicket(idProducto);
        if (error != null) {
            JOptionPane.showMessageDialog(vistaPrincipal, error, "Aviso",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            refrescarVenta();
        }
    }

    private void cobrarVenta() {
        String error = modelo.cobrarVenta();
        if (error != null) {
            JOptionPane.showMessageDialog(vistaPrincipal, error, "Ticket vacio",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(vistaPrincipal,
                    "Venta cobrada exitosamente", "Operacion exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
            refrescarVenta();
            refrescarCorte();
        }
    }

    private void refrescarVenta() {
        refrescarCatalogo();
        refrescarTicket();
    }

    private void refrescarCatalogo() {
        String busqueda = vistaPrincipal.getPanelVenta().getCampoBusqueda().getText();
        vistaPrincipal.getPanelVenta().actualizarProductos(
                modelo.buscarProductos(busqueda),
                this::agregarProductoAlTicket);
    }

    private void refrescarTicket() {
        vistaPrincipal.getPanelVenta().actualizarTicket(
                modelo.getVentaActual(),
                indice -> {
                    modelo.eliminarLineaTicket(indice);
                    refrescarVenta();
                });
    }

    private void configurarInventario() {
        PanelInventario panel = vistaPrincipal.getPanelInventario();

        panel.getBotonNuevo().addActionListener(e -> mostrarDialogoNuevoProducto());

        panel.actualizarTabla(
                modelo.getProductos(),
                (id, cantidad) -> {
                    modelo.surtirProducto(id, cantidad);
                    refrescarInventario();
                    refrescarCatalogo();
                },
                id -> {
                    modelo.eliminarProducto(id);
                    refrescarInventario();
                    refrescarCatalogo();
                });
    }

    private void mostrarDialogoNuevoProducto() {
        JTextField nombre = new JTextField();
        JTextField precio = new JTextField();
        JTextField stock = new JTextField();

        Object[] campos = {
                "Nombre:", nombre,
                "Precio:", precio,
                "Stock inicial:", stock
        };

        int opcion = JOptionPane.showConfirmDialog(vistaPrincipal, campos,
                "Nuevo Producto", JOptionPane.OK_CANCEL_OPTION);
        if (opcion == JOptionPane.OK_OPTION) {
            try {
                String nom = nombre.getText().trim();
                double pre = Double.parseDouble(precio.getText().trim());
                int stk = Integer.parseInt(stock.getText().trim());
                if (nom.isEmpty() || pre < 0 || stk < 0) {
                    throw new IllegalArgumentException();
                }
                modelo.crearProducto(nom, pre, stk);
                refrescarInventario();
                refrescarCatalogo();
                JOptionPane.showMessageDialog(vistaPrincipal,
                        "Producto registrado", "Operacion exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(vistaPrincipal,
                        "Datos invalidos", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refrescarInventario() {
        vistaPrincipal.getPanelInventario().actualizarTabla(
                modelo.getProductos(),
                (id, cantidad) -> {
                    modelo.surtirProducto(id, cantidad);
                    refrescarInventario();
                    refrescarCatalogo();
                },
                id -> {
                    modelo.eliminarProducto(id);
                    refrescarInventario();
                    refrescarCatalogo();
                });
    }

    private void configurarCorte() {
        PanelCorte panel = vistaPrincipal.getPanelCorte();
        panel.getBotonCerrarDia().addActionListener(e -> procesarCierreDia());
    }

    private void procesarCierreDia() {
        int confirm = JOptionPane.showConfirmDialog(vistaPrincipal,
                "Se cerrara el dia y se limpiara la caja. Desea continuar?",
                "Confirmar corte",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            modelo.cerrarDiaYLimpiarCaja();
            refrescarCorte();
            JOptionPane.showMessageDialog(vistaPrincipal,
                    "Corte de caja realizado", "Operacion exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void refrescarCorte() {
        PanelCorte panel = vistaPrincipal.getPanelCorte();
        panel.actualizarResumen(
                modelo.getTotalEnCaja(),
                modelo.getVentasHoy(),
                modelo.getPromedioTicket(),
                modelo.getEntradasManuales());
        panel.actualizarHistorial(modelo.getHistorialCortes());
    }

    private void configurarCerrarSesion() {
        JButton btn = vistaPrincipal.getBotonCerrarSesion();
        if (btn != null) {
            btn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(vistaPrincipal,
                        "Desea cerrar sesion?", "Confirmar",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    modelo.cerrarSesion();
                    vistaPrincipal.dispose();
                    mostrarLogin();
                }
            });
        }
    }
}
