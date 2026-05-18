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
import com.abarrotespro.modelo.servicio.LectorTickets;
import com.abarrotespro.vista.panel.PanelConfiguracion;
import com.abarrotespro.vista.panel.PanelTickets;
import com.abarrotespro.vista.util.TemaUi;
import com.abarrotespro.vista.VistaLogin;
import com.abarrotespro.vista.VistaPrincipal;
import com.abarrotespro.vista.panel.PanelCorte;
import com.abarrotespro.vista.panel.PanelInventario;
import com.abarrotespro.vista.panel.PanelVenta;
import com.abarrotespro.vista.util.DialogosInventario;
import com.abarrotespro.vista.util.GestorImagenProducto;

/**
 * Controlador principal: enlaza vistas con el modelo y coordina la logica de negocio.
 */
public class ControladorPrincipal {

    private final SistemaPos modelo;
    private VistaLogin vistaLogin;
    private VistaPrincipal vistaPrincipal;
    private ProveedorController controladorProveedores;

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
        configurarProveedores();
        configurarCorte();
        configurarTickets();
        configurarConfiguracion();

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
                } else if (VistaPrincipal.CARD_PROVEEDORES.equals(card)) {
                    if (controladorProveedores != null) {
                        controladorProveedores.refrescarTabla();
                    }
                } else if (VistaPrincipal.CARD_CORTE.equals(card)) {
                    refrescarCorte();
                } else if (VistaPrincipal.CARD_TICKETS.equals(card)) {
                    refrescarTickets();
                }
            });
        });
    }

    private void configurarProveedores() {
        controladorProveedores = new ProveedorController(
                modelo,
                vistaPrincipal.getPanelProveedores(),
                vistaPrincipal);
        controladorProveedores.inicializar();
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
            refrescarTickets();
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
            try {
                producto.setNombre(datos.nombre());
                producto.setPrecio(datos.precio());
                producto.setStockMinimo(datos.stockMinimo());
                aplicarImagenProducto(producto, datos);
                modelo.actualizarProducto(producto);
                refrescarInventario();
                refrescarCatalogo();
                JOptionPane.showMessageDialog(vistaPrincipal,
                        "Producto actualizado correctamente", "Operacion exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(vistaPrincipal,
                        "No se pudo guardar la imagen del producto.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void mostrarDialogoNuevoProducto() {
        DialogosInventario.mostrarNuevoProducto(vistaPrincipal).ifPresent(datos -> {
            try {
                com.abarrotespro.modelo.Producto nuevo = modelo.crearProducto(
                        datos.nombre(), datos.precio(), datos.stockInicial(), datos.stockMinimo(), null);
                aplicarImagenProducto(nuevo, datos);
                modelo.actualizarProducto(nuevo);
                refrescarInventario();
                refrescarCatalogo();
                JOptionPane.showMessageDialog(vistaPrincipal,
                        "Producto registrado", "Operacion exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(vistaPrincipal,
                        "No se pudo guardar la imagen del producto.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void aplicarImagenProducto(com.abarrotespro.modelo.Producto producto,
            DialogosInventario.DatosFormularioProducto datos) throws Exception {
        if (datos.archivoImagenNuevo() != null) {
            String ruta = GestorImagenProducto.guardarImagen(datos.archivoImagenNuevo(), producto.getId());
            producto.setRutaImagen(ruta);
        } else if (datos.rutaImagen() != null && !datos.rutaImagen().isBlank()) {
            producto.setRutaImagen(datos.rutaImagen());
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

    private void configurarTickets() {
        PanelTickets panel = vistaPrincipal.getPanelTickets();
        panel.getListaArchivos().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            String seleccion = panel.getListaArchivos().getSelectedValue();
            if (seleccion == null) {
                panel.mostrarContenido("");
                return;
            }
            try {
                panel.mostrarContenido(LectorTickets.leerContenido(seleccion));
            } catch (Exception ex) {
                panel.mostrarContenido("No se pudo leer el archivo:\n" + ex.getMessage());
            }
        });
    }

    private void refrescarTickets() {
        vistaPrincipal.getPanelTickets().actualizarLista(LectorTickets.listarArchivos());
    }

    private void configurarConfiguracion() {
        PanelConfiguracion panel = vistaPrincipal.getPanelConfiguracion();

        panel.getToggleModoOscuro().addActionListener(e -> {
            boolean oscuro = panel.getToggleModoOscuro().isSelected();
            TemaUi.aplicarModoOscuro(oscuro);
            vistaPrincipal.refrescarTema();
        });

        panel.getBotonCerrarSesion().addActionListener(e -> {
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
