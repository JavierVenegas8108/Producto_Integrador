package com.abarrotespro.controlador;

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.abarrotespro.modelo.SistemaPos;
import com.abarrotespro.modelo.Venta;
import com.abarrotespro.modelo.servicio.GeneradorTicket;
import com.abarrotespro.vista.VistaLogin;
import com.abarrotespro.vista.VistaPrincipal;
import com.abarrotespro.vista.panel.PanelCorte;
import com.abarrotespro.vista.panel.PanelInventario;
import com.abarrotespro.vista.panel.PanelVenta;
import com.abarrotespro.vista.util.DialogosInventario;

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
        Venta ventaCerrada = modelo.cobrarVenta();
        if (ventaCerrada == null) {
            JOptionPane.showMessageDialog(vistaPrincipal, "El ticket esta vacio", "Ticket vacio",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            try {
                var archivoTicket = GeneradorTicket.generar(ventaCerrada);
                JOptionPane.showMessageDialog(vistaPrincipal,
                        "Venta cobrada exitosamente.\nTicket guardado en:\n" + archivoTicket,
                        "Operacion exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(vistaPrincipal,
                        "Venta cobrada, pero no se pudo generar el ticket:\n" + ex.getMessage(),
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE);
            }
            
            StringBuilder alertaBajoStock = new StringBuilder();
            
            for (com.abarrotespro.modelo.Producto p : modelo.getProductos()) {
                if (p.getStock() <= p.getStockMinimo()) {
                    alertaBajoStock.append("• ").append(p.getNombre())
                                   .append(" (Stock actual: ").append(p.getStock())
                                   .append(" | Mínimo: ").append(p.getStockMinimo()).append(")\n");
                }
            }
            
            
            if (alertaBajoStock.length() > 0) {
                JOptionPane.showMessageDialog(vistaPrincipal,
                        "⚠️ ¡Atención! Los siguientes productos están en stock mínimo o agotados:\n\n" + alertaBajoStock.toString(),
                        "Alerta de Inventario Escaso",
                        JOptionPane.WARNING_MESSAGE);
            }
            
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
        panel.alEditarProducto(this::mostrarDialogoEditarProducto);
    }

    private void mostrarDialogoEditarProducto(com.abarrotespro.modelo.Producto producto) {
        DialogosInventario.mostrarEditarProducto(vistaPrincipal, producto).ifPresent(datos -> {
            producto.setNombre(datos.nombre());
            producto.setPrecio(datos.precio());
            producto.setStockMinimo(datos.stockMinimo());
            modelo.registrarCambioInventario();
            refrescarInventario();
            refrescarCatalogo();
            JOptionPane.showMessageDialog(vistaPrincipal,
                    "Producto actualizado correctamente", "Operacion exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }
    private void mostrarDialogoNuevoProducto() {
        JTextField nombre = new JTextField();
        JTextField precio = new JTextField();
        JTextField stock = new JTextField();
        JTextField stockMinimo = new JTextField();

        Object[] campos = {
                "Nombre:", nombre,
                "Precio:", precio,
                "Stock inicial:", stock,
                "Stock mínimo de alerta:", stockMinimo 
        };

        int opcion = JOptionPane.showConfirmDialog(vistaPrincipal, campos,
                "Nuevo Producto", JOptionPane.OK_CANCEL_OPTION);
        if (opcion == JOptionPane.OK_OPTION) {
            try {
                String nom = nombre.getText().trim();
                double pre = Double.parseDouble(precio.getText().trim());
                int stk = Integer.parseInt(stock.getText().trim());
                int stkMin = Integer.parseInt(stockMinimo.getText().trim()); 

                
                if (nom.isEmpty() || pre < 0 || stk < 0 || stkMin < 0) {
                    throw new IllegalArgumentException();
                }
                
               
                modelo.crearProducto(nom, pre, stk, stkMin); 
                
                refrescarInventario();
                refrescarCatalogo();
                JOptionPane.showMessageDialog(vistaPrincipal,
                        "Producto registrado", "Operacion exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(vistaPrincipal,
                        "Datos invalidos. Asegúrese de ingresar números válidos.", "Error",
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
