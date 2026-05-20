package com.abarrotespro.controlador;

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.abarrotespro.excepcion.DineroInsuficienteException;
import com.abarrotespro.modelo.SistemaPos;
import com.abarrotespro.modelo.Venta;
import com.abarrotespro.vista.dialog.DialogoCobro;
import com.abarrotespro.modelo.dto.FilaReporteVenta;
import com.abarrotespro.modelo.EntradaMercancia;
import com.abarrotespro.modelo.servicio.ExportadorInventario;
import com.abarrotespro.modelo.servicio.ExportadorVentas;
import com.abarrotespro.modelo.servicio.GeneradorTicket;
import com.abarrotespro.modelo.servicio.LectorTickets;
import com.abarrotespro.modelo.servicio.ReporteVentasServicio;
import com.abarrotespro.vista.dialog.ReporteBajoStockDialog;
import com.abarrotespro.vista.dialog.ReporteVentasDialog;
import com.abarrotespro.vista.dialog.VistaPreviaTicketDialog;
import com.abarrotespro.vista.panel.PanelConfiguracion;
import com.abarrotespro.vista.panel.PanelTickets;
import com.abarrotespro.vista.util.TemaUi;
import com.abarrotespro.vista.VistaLogin;
import com.abarrotespro.vista.VistaPrincipal;
import com.abarrotespro.vista.panel.PanelCorte;
import com.abarrotespro.vista.panel.PanelInventario;
import com.abarrotespro.vista.panel.PanelVenta;
import com.abarrotespro.vista.util.DialogosInventario;
import com.abarrotespro.vista.util.DialogosVenta;
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
        solicitarAperturaCajaSiNecesario();

        vistaPrincipal.setVisible(true);
    }

    private void solicitarAperturaCajaSiNecesario() {
        if (modelo.isCajaAbierta()) {
            return;
        }
        String fondo = JOptionPane.showInputDialog(vistaPrincipal,
                "La caja esta cerrada. Ingrese el fondo inicial en efectivo:",
                "Apertura de caja",
                JOptionPane.QUESTION_MESSAGE);
        if (fondo == null || fondo.isBlank()) {
            modelo.abrirCaja(0);
            return;
        }
        try {
            double monto = Double.parseDouble(fondo.trim());
            modelo.abrirCaja(monto);
            refrescarCorte();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(vistaPrincipal,
                    "Monto invalido. Se abrio la caja con $0.00",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            modelo.abrirCaja(0);
            refrescarCorte();
        }
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
        com.abarrotespro.modelo.Producto producto = modelo.buscarProductoPorId(idProducto);
        if (producto == null) {
            return;
        }
        DialogosVenta.preguntarCantidadAgregar(vistaPrincipal, producto).ifPresent(cantidad -> {
            String error = modelo.agregarAlTicket(idProducto, cantidad);
            if (error != null) {
                JOptionPane.showMessageDialog(vistaPrincipal, error, "Aviso",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                refrescarVenta();
            }
        });
    }

    private void cobrarVenta() {
        Venta ventaActual = modelo.getVentaActual();
        if (ventaActual == null || ventaActual.estaVacia()) {
            JOptionPane.showMessageDialog(vistaPrincipal, "El ticket esta vacio", "Ticket vacio",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!modelo.isCajaAbierta()) {
            solicitarAperturaCajaSiNecesario();
        }
        DialogoCobro.mostrar(vistaPrincipal, ventaActual, modelo.getVentaController())
                .ifPresent(result -> finalizarCobro(result.metodoPago()));
    }

    private void finalizarCobro(com.abarrotespro.modelo.MetodoPago metodoPago) {
        try {
            Venta ventaCerrada = modelo.cobrarVenta(metodoPago);
            if (ventaCerrada == null) {
                return;
            }
            String mensajeCambio = "";
            if (metodoPago == com.abarrotespro.modelo.MetodoPago.EFECTIVO
                    && ventaCerrada.getCambio() > 0) {
                mensajeCambio = "\nCambio: $" + String.format("%.2f", ventaCerrada.getCambio());
            }
            String contenidoTicket = GeneradorTicket.generarContenido(ventaCerrada);
            try {
                var archivoTicket = GeneradorTicket.generar(ventaCerrada);
                JOptionPane.showMessageDialog(vistaPrincipal,
                        "Venta cobrada exitosamente." + mensajeCambio
                                + "\nTicket guardado en:\n" + archivoTicket,
                        "Operacion exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
                VistaPreviaTicketDialog.mostrar(vistaPrincipal, contenidoTicket);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(vistaPrincipal,
                        "Venta cobrada, pero no se pudo generar el ticket:\n" + ex.getMessage(),
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE);
                VistaPreviaTicketDialog.mostrar(vistaPrincipal, contenidoTicket);
            }
            
            StringBuilder alertaBajoStock = new StringBuilder();
            for (com.abarrotespro.modelo.Producto p : modelo.obtenerProductosBajoStock()) {
                alertaBajoStock.append("• ").append(p.getNombre())
                        .append(" (Stock actual: ").append(p.getStock())
                        .append(" | Minimo: ").append(p.getStockMinimo()).append(")\n");
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
            refrescarInventario();
        } catch (DineroInsuficienteException ex) {
            JOptionPane.showMessageDialog(vistaPrincipal, ex.getMessage(),
                    "Dinero insuficiente", JOptionPane.WARNING_MESSAGE);
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
                    if (modelo.getVentaActual() == null
                            || indice < 0 || indice >= modelo.getVentaActual().getLineas().size()) {
                        return;
                    }
                    var linea = modelo.getVentaActual().getLineas().get(indice);
                    DialogosVenta.preguntarCantidadEliminar(vistaPrincipal,
                            linea.getProducto().getNombre(), linea.getCantidad())
                            .ifPresent(cant -> {
                                String error = modelo.reducirLineaTicket(indice, cant);
                                if (error != null) {
                                    JOptionPane.showMessageDialog(vistaPrincipal, error,
                                            "Aviso", JOptionPane.WARNING_MESSAGE);
                                }
                                refrescarVenta();
                            });
                });
    }

    private void configurarInventario() {
        PanelInventario panel = vistaPrincipal.getPanelInventario();

        panel.getBotonNuevo().addActionListener(e -> mostrarDialogoNuevoProducto());
        panel.getBotonRegistroMercancia().addActionListener(e -> mostrarRegistroMercancia());
        panel.getBotonReporteBajoStock().addActionListener(e -> mostrarReporteBajoStock());

        panel.actualizarTabla(
                modelo.getProductos(),
                (id, datos) -> {
                    modelo.surtirProducto(id, datos.cantidad(), datos.precioCompra());
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
        DialogosInventario.mostrarEditarProducto(vistaPrincipal, producto,
                modelo.getProveedoresActivos()).ifPresent(datos -> {
            try {
                producto.setNombre(datos.nombre());
                producto.setPrecioCompra(datos.precioCompra());
                producto.setPrecioVenta(datos.precioVenta());
                producto.setStockMinimo(datos.stockMinimo());
                producto.setIdProveedor(datos.idProveedor());
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
        DialogosInventario.mostrarNuevoProducto(vistaPrincipal,
                modelo.getProveedoresActivos()).ifPresent(datos -> {
            try {
                com.abarrotespro.modelo.Producto nuevo = modelo.crearProducto(
                        datos.nombre(), datos.precioCompra(), datos.precioVenta(),
                        datos.stockInicial(), datos.stockMinimo(), datos.idProveedor(), null);
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
            producto.setNombre(GestorImagenProducto.nombreVisibleDesdeArchivo(datos.archivoImagenNuevo().getName()));
        } else if (datos.rutaImagen() != null && !datos.rutaImagen().isBlank()) {
            producto.setRutaImagen(datos.rutaImagen());
            String nombreDerivado = GestorImagenProducto.nombreVisibleDesdeArchivo(datos.rutaImagen());
            if (!nombreDerivado.isBlank()) {
                producto.setNombre(nombreDerivado);
            }
        }
    }

    private void mostrarRegistroMercancia() {
        DialogosInventario.mostrarRegistroMercancia(vistaPrincipal, modelo.getProductos(),
                modelo.getHistorialSurtidos())
                .ifPresent(entradas -> {
                    int actualizados = modelo.registrarEntradaMercanciaMasiva(entradas);
                    if (actualizados > 0) {
                        refrescarInventario();
                        refrescarCatalogo();
                        int unidades = entradas.stream().mapToInt(EntradaMercancia::cantidad).sum();
                        JOptionPane.showMessageDialog(vistaPrincipal,
                                "Entrada registrada: " + actualizados + " producto(s), "
                                        + unidades + " unidad(es) agregadas.",
                                "Operacion exitosa",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                });
    }

    private void mostrarReporteBajoStock() {
        List<com.abarrotespro.modelo.Producto> bajoStock = modelo.obtenerProductosBajoStock();
        if (bajoStock.isEmpty()) {
            JOptionPane.showMessageDialog(vistaPrincipal,
                    "No hay productos en bajo stock. Todos cumplen el minimo configurado.",
                    "Sin alertas",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Object[] opciones = {"Ver reporte", "Exportar CSV", "Cancelar"};
        int opcion = JOptionPane.showOptionDialog(vistaPrincipal,
                "Se encontraron " + bajoStock.size() + " producto(s) en bajo stock.",
                "Reporte de bajo stock",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                opciones,
                opciones[0]);

        if (opcion == 0) {
            ReporteBajoStockDialog.mostrar(vistaPrincipal, bajoStock);
        } else if (opcion == 1) {
            try {
                var archivo = ExportadorInventario.exportarBajoStockCsv(bajoStock);
                JOptionPane.showMessageDialog(vistaPrincipal,
                        "Reporte exportado correctamente:\n" + archivo,
                        "Exportacion exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(vistaPrincipal,
                        "No se pudo exportar el reporte:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refrescarInventario() {
        vistaPrincipal.getPanelInventario().actualizarTabla(
                modelo.getProductos(),
                (id, datos) -> {
                    modelo.surtirProducto(id, datos.cantidad(), datos.precioCompra());
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
        panel.getBotonVerReporte().addActionListener(e -> mostrarReporteVentas());
        panel.getBotonExportarCsv().addActionListener(e -> exportarReporteVentasCsv());
    }

    private void mostrarReporteVentas() {
        try {
            LocalDate[] rango = resolverRangoFechasReporte();
            List<FilaReporteVenta> filas = modelo.consultarReporteVentas(rango[0], rango[1]);
            if (filas.isEmpty()) {
                JOptionPane.showMessageDialog(vistaPrincipal,
                        "No hay ventas registradas en el periodo seleccionado.",
                        "Sin resultados",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            ReporteVentasDialog.mostrar(vistaPrincipal, filas);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(vistaPrincipal,
                    "Formato de fecha invalido. Use dd/MM/yyyy",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(vistaPrincipal,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportarReporteVentasCsv() {
        try {
            LocalDate[] rango = resolverRangoFechasReporte();
            List<FilaReporteVenta> filas = modelo.consultarReporteVentas(rango[0], rango[1]);
            if (filas.isEmpty()) {
                JOptionPane.showMessageDialog(vistaPrincipal,
                        "No hay ventas para exportar en el periodo seleccionado.",
                        "Sin resultados",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Path archivo = ExportadorVentas.exportarCsv(filas);
            JOptionPane.showMessageDialog(vistaPrincipal,
                    "Reporte exportado correctamente:\n" + archivo,
                    "Exportacion exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(vistaPrincipal,
                    "Formato de fecha invalido. Use dd/MM/yyyy",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(vistaPrincipal,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(vistaPrincipal,
                    "No se pudo exportar el reporte:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate[] resolverRangoFechasReporte() {
        PanelCorte panel = vistaPrincipal.getPanelCorte();
        String filtro = (String) panel.getComboFiltroFecha().getSelectedItem();
        LocalDate hoy = LocalDate.now();
        if (PanelCorte.FILTRO_HOY.equals(filtro)) {
            return new LocalDate[] { hoy, hoy };
        }
        if (PanelCorte.FILTRO_ULTIMOS_7.equals(filtro)) {
            return new LocalDate[] { hoy.minusDays(6), hoy };
        }
        LocalDate desde = ReporteVentasServicio.parsearFechaCampo(panel.getCampoFechaDesde().getText());
        LocalDate hasta = ReporteVentasServicio.parsearFechaCampo(panel.getCampoFechaHasta().getText());
        if (!ReporteVentasServicio.esRangoValido(desde, hasta)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final.");
        }
        return new LocalDate[] { desde, hasta };
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
            solicitarAperturaCajaSiNecesario();
        }
    }

    private void refrescarCorte() {
        PanelCorte panel = vistaPrincipal.getPanelCorte();
        panel.actualizarResumen(
                modelo.getTotalEnCaja(),
                modelo.getVentasHoy(),
                modelo.getPromedioTicket(),
                modelo.getUtilidadHoy());
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
                panel.mostrarContenido(null, "");
                return;
            }
            try {
                panel.mostrarContenido(seleccion, LectorTickets.leerContenido(seleccion));
            } catch (Exception ex) {
                panel.mostrarContenido(seleccion, "No se pudo leer el archivo:\n" + ex.getMessage());
            }
        });

        panel.getBotonReimprimir().addActionListener(e -> reimprimirTicketSeleccionado());
        panel.getBotonDevolucion().addActionListener(e -> realizarDevolucionTicket());
    }

    private void reimprimirTicketSeleccionado() {
        String archivo = vistaPrincipal.getPanelTickets().getArchivoSeleccionado();
        if (archivo == null) {
            return;
        }
        try {
            String contenido = LectorTickets.leerContenido(archivo);
            int ventaId = LectorTickets.extraerIdVenta(archivo);
            Venta venta = modelo.buscarVentaPorId(ventaId);
            if (venta != null && !LectorTickets.esCancelado(archivo)) {
                Path nuevo = GeneradorTicket.generar(venta);
                JOptionPane.showMessageDialog(vistaPrincipal,
                        "Ticket reimpreso en:\n" + nuevo, "Reimpresion",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(vistaPrincipal,
                        "Contenido del ticket:\n\n" + contenido, "Ticket (solo lectura)",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            refrescarTickets();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vistaPrincipal,
                    "No se pudo reimprimir: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void realizarDevolucionTicket() {
        String archivo = vistaPrincipal.getPanelTickets().getArchivoSeleccionado();
        if (archivo == null || LectorTickets.esCancelado(archivo)) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(vistaPrincipal,
                "Se realizara la devolucion del ticket seleccionado.\n"
                        + "Se restaurara el inventario y se ajustara el corte de caja.",
                "Confirmar devolucion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        String error = modelo.realizarDevolucion(archivo);
        if (error != null) {
            JOptionPane.showMessageDialog(vistaPrincipal, error, "Devolucion",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(vistaPrincipal,
                    "Devolucion registrada correctamente.", "Operacion exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
            refrescarTickets();
            refrescarInventario();
            refrescarCatalogo();
            refrescarCorte();
        }
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
