package com.abarrotespro.modelo;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.abarrotespro.controlador.CajaController;
import com.abarrotespro.controlador.VentaController;
import com.abarrotespro.excepcion.DineroInsuficienteException;
import com.abarrotespro.modelo.dao.PosPersistencia;
import com.abarrotespro.modelo.EntradaMercancia;
import com.abarrotespro.modelo.dto.EstadoPersistido;
import com.abarrotespro.modelo.dto.FilaReporteVenta;
import com.abarrotespro.modelo.servicio.LectorTickets;
import com.abarrotespro.modelo.servicio.ReporteVentasServicio;

/**
 * Modelo central del punto de venta: inventario, ventas, caja y usuarios.
 */
public class SistemaPos {

    private final List<Usuario> usuarios;
    private final List<Producto> productos;
    private final List<Proveedor> proveedores;
    private final List<Venta> ventasDelDia;
    private final List<Corte> historialCortes;
    private final List<RegistroSurtido> historialSurtidos;
    private final PosPersistencia persistencia;
    private final Caja caja;
    private final Inventario inventario;
    private final VentaController ventaController;
    private final CajaController cajaController;

    private Usuario usuarioActivo;
    private Venta ventaActual;
    private double entradasManuales;
    private int contadorVentas;
    private int contadorProductos;
    private int contadorCortes;
    private int contadorProveedores;

    public SistemaPos() {
        usuarios = new ArrayList<>();
        productos = new ArrayList<>();
        proveedores = new ArrayList<>();
        ventasDelDia = new ArrayList<>();
        historialCortes = new ArrayList<>();
        historialSurtidos = new ArrayList<>();
        persistencia = new PosPersistencia();
        caja = new Caja();
        inventario = new Inventario(productos);
        ventaController = new VentaController(inventario, caja);
        cajaController = new CajaController(caja);
        contadorVentas = 0;
        contadorProductos = 0;
        contadorCortes = 0;
        contadorProveedores = 0;
        entradasManuales = 0;
        usuarios.add(new Usuario("admin", "admin123", "Administrador", "AD"));
        if (persistencia.existenDatos()) {
            try {
                persistencia.cargar(this);
            } catch (Exception e) {
                System.err.println("No se pudieron cargar datos guardados: " + e.getMessage());
                inicializarDatos();
                persistir();
            }
        } else {
            inicializarDatos();
            persistir();
        }
        if (!caja.isAbierta()) {
            caja.abrirCaja(500.00);
        }
    }

    public void cargarHistorialSurtidos(List<RegistroSurtido> registros) {
        historialSurtidos.clear();
        if (registros != null) {
            historialSurtidos.addAll(registros);
        }
    }

    public void cargarEstado(EstadoPersistido estado) {
        productos.clear();
        proveedores.clear();
        ventasDelDia.clear();
        historialCortes.clear();
        historialSurtidos.clear();
        productos.addAll(estado.getProductos());
        proveedores.addAll(estado.getProveedores());
        ventasDelDia.addAll(estado.getVentas());
        historialCortes.addAll(estado.getCortes());
        entradasManuales = estado.getEntradasManuales();
        contadorVentas = estado.getContadorVentas();
        contadorProductos = estado.getContadorProductos();
        contadorCortes = estado.getContadorCortes();
        contadorProveedores = estado.getContadorProveedores();
        for (Venta v : ventasDelDia) {
            if (!v.isCerrada()) {
                v.setEstado(EstadoVenta.PAGADA);
            }
        }
        if (estado.isCajaAbierta()) {
            caja.restaurarEstado(
                    estado.getFondoInicial(),
                    estado.getIngresosEfectivo(),
                    estado.getIngresosTarjeta(),
                    estado.getIngresosTransferencia(),
                    estado.getEgresosEfectivo());
        }
    }

    public EstadoPersistido exportarEstado() {
        return new EstadoPersistido(
                productos, ventasDelDia, historialCortes, proveedores,
                caja.calcularEfectivoEsperado(), entradasManuales,
                contadorVentas, contadorProductos, contadorCortes, contadorProveedores,
                caja.getFondoInicial(), caja.getIngresosEfectivo(), caja.getIngresosTarjeta(),
                caja.getIngresosTransferencia(), caja.getEgresosEfectivo(), caja.isAbierta());
    }

    private void persistir() {
        persistencia.guardar(this);
    }

    private void inicializarDatos() {
        agregarProducto("Leche Entera 1L", 22.50, 45, "Lacteos", "🥛");
        agregarProducto("Pan Blanco Bolsa", 38.00, 20, "Panaderia", "🍞");
        agregarProducto("Arroz 1kg", 28.90, 60, "Abarrotes", "🍚");
        agregarProducto("Aceite Vegetal 1L", 42.00, 30, "Abarrotes", "🫒");
        agregarProducto("Coca-Cola 600ml", 18.50, 80, "Bebidas", "🥤");
        agregarProducto("Sabritas Original", 19.00, 55, "Snacks", "🥔");
        agregarProducto("Huevo Carton 12pz", 52.00, 25, "Lacteos", "🥚");
        agregarProducto("Jabon de Barra", 15.00, 40, "Limpieza", "🧼");
        agregarProducto("Papel Higienico 4 rollos", 35.00, 18, "Limpieza", "🧻");
        agregarProducto("Atun en Lata", 24.50, 35, "Abarrotes", "🐟");
        agregarProducto("Manzana Roja kg", 45.00, 50, "Frutas", "🍎");
        agregarProducto("Agua Natural 1.5L", 12.00, 100, "Bebidas", "💧");

        historialCortes.add(new Corte(980.00, "Administrador"));
        historialCortes.add(new Corte(1120.75, "Administrador"));

        agregarProveedor("Distribuidora La Central", "Juan Perez", "555-1001",
                "contacto@lacentral.com", "Av. Reforma 120", "Lunes, Miercoles", true);
        agregarProveedor("Abarrotes del Norte", "Maria Lopez", "555-2045",
                "ventas@norte.com", "Calle 5 de Mayo 45", "Martes, Jueves", true);
    }

    private void agregarProveedor(String razon, String contacto, String telefono,
            String correo, String direccion, String dias, boolean activo) {
        contadorProveedores++;
        proveedores.add(new Proveedor(contadorProveedores, razon, contacto, telefono,
                correo, direccion, dias, activo));
    }

    private void agregarProducto(String nombre, double precio, int stock, String categoria, String emoji) {
        contadorProductos++;
        productos.add(new Producto(contadorProductos, nombre, precio, stock, categoria, emoji));
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public List<Proveedor> getProveedores() {
        return proveedores;
    }

    public List<Venta> getVentasDelDia() {
        return ventasDelDia;
    }

    public List<Corte> getHistorialCortes() {
        return historialCortes;
    }

    public List<RegistroSurtido> getHistorialSurtidos() {
        return historialSurtidos;
    }

    public Usuario getUsuarioActivo() {
        return usuarioActivo;
    }

    public Venta getVentaActual() {
        return ventaActual;
    }

    public double getTotalEnCaja() {
        return caja.calcularEfectivoEsperado();
    }

    public double getEntradasManuales() {
        return entradasManuales;
    }

    public Caja getCaja() {
        return caja;
    }

    public VentaController getVentaController() {
        return ventaController;
    }

    public CajaController getCajaController() {
        return cajaController;
    }

    public boolean isCajaAbierta() {
        return caja.isAbierta();
    }

    public void abrirCaja(double fondoInicial) {
        caja.abrirCaja(fondoInicial);
        persistir();
    }

    /** Autentica al usuario y prepara una venta nueva. */
    public boolean iniciarSesion(String usuario, String contrasena) {
        Optional<Usuario> encontrado = usuarios.stream()
                .filter(u -> u.validarAcceso(usuario, contrasena))
                .findFirst();
        if (encontrado.isPresent()) {
            usuarioActivo = encontrado.get();
            ventaActual = new Venta(++contadorVentas, usuarioActivo.getNombreCompleto());
            System.out.println("Sesion iniciada correctamente");
            return true;
        }
        System.out.println("Credenciales invalidas");
        return false;
    }

    public void cerrarSesion() {
        usuarioActivo = null;
        ventaActual = null;
        System.out.println("Sesion cerrada");
    }

    /**
     * Filtra productos: ID exacto si el texto es numerico; nombre parcial (contains) en otro caso.
     */
    public List<Producto> buscarProductos(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return new ArrayList<>(productos);
        }
        String filtro = texto.trim();
        Integer idBuscado = parsearIdExacto(filtro);
        if (idBuscado != null) {
            return productos.stream()
                    .filter(p -> p.getId() == idBuscado)
                    .collect(Collectors.toList());
        }
        String filtroNombre = filtro.toLowerCase();
        return productos.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(filtroNombre))
                .collect(Collectors.toList());
    }

    private static Integer parsearIdExacto(String texto) {
        if (texto == null || !texto.matches("\\d+")) {
            return null;
        }
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Agrega producto al ticket actual validando stock. */
    public String agregarAlTicket(int idProducto, int cantidad) {
        if (ventaActual == null) {
            return "No hay venta activa";
        }
        if (cantidad <= 0) {
            return "Cantidad invalida";
        }
        Producto producto = buscarProductoPorId(idProducto);
        if (producto == null) {
            return "Producto no encontrado";
        }
        int enTicket = cantidadEnTicket(producto.getId());
        if (producto.getStock() < cantidad + enTicket) {
            return "Stock insuficiente (disponible: " + producto.getStock() + ")";
        }
        ventaActual.agregarProducto(producto, cantidad);
        persistir();
        return null;
    }

    private int cantidadEnTicket(int idProducto) {
        if (ventaActual == null) {
            return 0;
        }
        return ventaActual.getLineas().stream()
                .filter(l -> l.getProducto().getId() == idProducto)
                .mapToInt(LineaVenta::getCantidad)
                .sum();
    }

    public String reducirLineaTicket(int indice, int cantidad) {
        if (ventaActual == null || indice < 0 || indice >= ventaActual.getLineas().size()) {
            return "Linea no valida";
        }
        if (cantidad <= 0) {
            return "Cantidad invalida";
        }
        LineaVenta linea = ventaActual.getLineas().get(indice);
        if (cantidad > linea.getCantidad()) {
            return "No puede eliminar mas de lo agregado (" + linea.getCantidad() + ")";
        }
        if (cantidad == linea.getCantidad()) {
            ventaActual.eliminarLinea(indice);
        } else {
            linea.setCantidad(linea.getCantidad() - cantidad);
        }
        persistir();
        return null;
    }

    public void eliminarLineaTicket(int indice) {
        reducirLineaTicket(indice, ventaActual != null && indice >= 0 && indice < ventaActual.getLineas().size()
                ? ventaActual.getLineas().get(indice).getCantidad() : 0);
    }

    /**
     * Cierra el ticket con metodo de pago, actualiza caja e inventario.
     *
     * @return la venta cerrada o null si el ticket esta vacio
     */
    public Venta cobrarVenta(MetodoPago metodoPago) throws DineroInsuficienteException {
        if (ventaActual == null || ventaActual.estaVacia()) {
            return null;
        }
        ventaController.procesarPago(ventaActual, metodoPago);
        ventaActual.setFechaHora(java.time.LocalDateTime.now());
        Venta ventaCerrada = ventaActual;
        ventasDelDia.add(ventaCerrada);
        ventaActual = new Venta(++contadorVentas, usuarioActivo.getNombreCompleto());
        persistir();
        System.out.println("Venta cobrada: $" + String.format("%.2f", ventaCerrada.getMontoCobrable()));
        return ventaCerrada;
    }

    public void registrarPagoProveedor(String nombreProveedor, double monto, MetodoPago metodoPago) {
        cajaController.registrarPagoProveedor(nombreProveedor, monto, metodoPago);
        persistir();
    }

    public Producto buscarProductoPorId(int id) {
        return productos.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    public void surtirProducto(int id, int cantidad, double precioCompra) {
        Producto p = buscarProductoPorId(id);
        if (p == null || cantidad <= 0) {
            return;
        }
        p.aumentarStock(cantidad);
        if (precioCompra >= 0) {
            p.setPrecioCompra(precioCompra);
        }
        String nombreProv = "—";
        int idProv = p.getIdProveedor();
        if (idProv > 0) {
            Proveedor prov = buscarProveedorPorId(idProv);
            if (prov != null) {
                nombreProv = prov.getRazonSocial();
            }
        }
        historialSurtidos.add(0, new RegistroSurtido(
                java.time.LocalDateTime.now(), p.getId(), p.getNombre(),
                cantidad, p.getPrecioCompra(), idProv, nombreProv));
        persistir();
    }

    /**
     * Registra multiples entradas de mercancia.
     * @return numero de productos actualizados
     */
    public int registrarEntradaMercanciaMasiva(List<EntradaMercancia> entradas) {
        if (entradas == null || entradas.isEmpty()) {
            return 0;
        }
        int actualizados = 0;
        for (EntradaMercancia entrada : entradas) {
            if (entrada.cantidad() > 0) {
                surtirProducto(entrada.productoId(), entrada.cantidad(), -1);
                actualizados++;
            }
        }
        if (actualizados > 0) {
            persistir();
        }
        return actualizados;
    }

    /** Productos cuya existencia es menor o igual al stock minimo configurado. */
    public List<Producto> obtenerProductosBajoStock() {
        return productos.stream()
                .filter(p -> p.getStock() <= p.getStockMinimo())
                .collect(Collectors.toList());
    }

    public void eliminarProducto(int id) {
        productos.removeIf(p -> p.getId() == id);
        persistir();
    }

    public Producto crearProducto(String nombre, double precioVenta, int stock, int stockMinimo) {
        return crearProducto(nombre, precioVenta, precioVenta, stock, stockMinimo, 0, null);
    }

    public Producto crearProducto(String nombre, double precioCompra, double precioVenta,
            int stock, int stockMinimo, int idProveedor, String rutaImagen) {
        contadorProductos++;
        Producto nuevo = new Producto(contadorProductos, nombre, precioCompra, precioVenta,
                stock, "General", "📦", stockMinimo, idProveedor, rutaImagen);
        productos.add(nuevo);
        persistir();
        return nuevo;
    }

    public void actualizarProducto(Producto producto) {
        persistir();
    }

    public void registrarCambioInventario() {
        persistir();
    }

    public List<Proveedor> buscarProveedores(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return new ArrayList<>(proveedores);
        }
        String filtro = texto.trim().toLowerCase();
        return proveedores.stream()
                .filter(p -> p.getRazonSocial().toLowerCase().contains(filtro)
                        || p.getNombreContacto().toLowerCase().contains(filtro))
                .collect(Collectors.toList());
    }

    public List<Proveedor> getProveedoresActivos() {
        return proveedores.stream().filter(Proveedor::isActivo).collect(Collectors.toList());
    }

    public List<RegistroSurtido> getSurtidosPorProveedor(int idProveedor) {
        return historialSurtidos.stream()
                .filter(r -> r.getProveedorId() == idProveedor)
                .collect(Collectors.toList());
    }

    public List<Producto> getProductosPorProveedor(int idProveedor) {
        return productos.stream()
                .filter(p -> p.getIdProveedor() == idProveedor)
                .collect(Collectors.toList());
    }

    public Venta buscarVentaPorId(int id) {
        return ventasDelDia.stream().filter(v -> v.getId() == id).findFirst().orElse(null);
    }

    /** Devolucion: restaura stock, ajusta caja y anula archivo de ticket. */
    public String realizarDevolucion(String nombreArchivo) {
        if (LectorTickets.esCancelado(nombreArchivo)) {
            return "Este ticket ya fue anulado";
        }
        int ventaId = LectorTickets.extraerIdVenta(nombreArchivo);
        Venta venta = ventaId > 0 ? buscarVentaPorId(ventaId) : null;
        if (venta == null) {
            return "No se encontro la venta asociada al ticket";
        }
        for (LineaVenta linea : venta.getLineas()) {
            linea.getProducto().aumentarStock(linea.getCantidad());
        }
        MetodoPago metodo = venta.getMetodoPago() != null ? venta.getMetodoPago() : MetodoPago.EFECTIVO;
        caja.revertirIngreso(venta.getMontoCobrable(), metodo);
        venta.setEstado(EstadoVenta.CANCELADA);
        ventasDelDia.remove(venta);
        try {
            LectorTickets.marcarComoCancelado(nombreArchivo);
        } catch (IOException e) {
            return "Devolucion aplicada en sistema, pero no se pudo renombrar el archivo: " + e.getMessage();
        }
        persistir();
        return null;
    }

    public Proveedor buscarProveedorPorId(int id) {
        return proveedores.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    public Proveedor guardarProveedor(Proveedor proveedor) {
        if (proveedor.getId() <= 0) {
            contadorProveedores++;
            proveedor.setId(contadorProveedores);
            proveedores.add(proveedor);
        } else {
            Proveedor existente = buscarProveedorPorId(proveedor.getId());
            if (existente != null) {
                existente.setRazonSocial(proveedor.getRazonSocial());
                existente.setNombreContacto(proveedor.getNombreContacto());
                existente.setTelefono(proveedor.getTelefono());
                existente.setCorreo(proveedor.getCorreo());
                existente.setDireccion(proveedor.getDireccion());
                existente.setDiasVisita(proveedor.getDiasVisita());
                existente.setActivo(proveedor.isActivo());
            }
        }
        persistir();
        return proveedor;
    }

    public void desactivarProveedor(int id) {
        Proveedor p = buscarProveedorPorId(id);
        if (p != null) {
            p.setActivo(false);
            persistir();
        }
    }

    public double getVentasHoy() {
        return caja.calcularVentasHoy(ventasDelDia);
    }

    public double getUtilidadHoy() {
        return caja.calcularUtilidadHoy(ventasDelDia);
    }

    public double getPromedioTicket() {
        return caja.calcularPromedioTicket(ventasDelDia);
    }

    /** Realiza corte de caja y reinicia el monto en efectivo. */
    public Corte cerrarDiaYLimpiarCaja() {
        double monto = caja.calcularEfectivoEsperado();
        Corte corte = new Corte(monto, usuarioActivo.getNombreCompleto());
        historialCortes.add(0, corte);
        caja.cerrarDiaOperativo();
        entradasManuales = 0;
        ventasDelDia.clear();
        ventaController.reiniciarAcumuladorEfectivo();
        persistir();
        System.out.println("Corte de caja realizado: $" + String.format("%.2f", monto));
        return corte;
    }

    public void agregarEntradaManual(double monto) {
        if (monto > 0) {
            entradasManuales += monto;
            caja.registrarIngreso(monto, MetodoPago.EFECTIVO, "Entrada manual de efectivo");
            persistir();
        }
    }

    public List<FilaReporteVenta> consultarReporteVentas(LocalDate desde, LocalDate hasta) {
        return ReporteVentasServicio.consultar(desde, hasta, ventasDelDia);
    }
}
