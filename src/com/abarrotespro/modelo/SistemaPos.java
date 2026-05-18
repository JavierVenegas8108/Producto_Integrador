package com.abarrotespro.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.abarrotespro.modelo.dao.PosPersistencia;
import com.abarrotespro.modelo.dto.EstadoPersistido;

/**
 * Modelo central del punto de venta: inventario, ventas, caja y usuarios.
 */
public class SistemaPos {

    private final List<Usuario> usuarios;
    private final List<Producto> productos;
    private final List<Proveedor> proveedores;
    private final List<Venta> ventasDelDia;
    private final List<Corte> historialCortes;
    private final PosPersistencia persistencia;

    private Usuario usuarioActivo;
    private Venta ventaActual;
    private double totalEnCaja;
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
        persistencia = new PosPersistencia();
        contadorVentas = 0;
        contadorProductos = 0;
        contadorCortes = 0;
        contadorProveedores = 0;
        totalEnCaja = 1250.50;
        entradasManuales = 200.00;
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
    }

    public void cargarEstado(EstadoPersistido estado) {
        productos.clear();
        proveedores.clear();
        ventasDelDia.clear();
        historialCortes.clear();
        productos.addAll(estado.getProductos());
        proveedores.addAll(estado.getProveedores());
        ventasDelDia.addAll(estado.getVentas());
        historialCortes.addAll(estado.getCortes());
        totalEnCaja = estado.getTotalEnCaja();
        entradasManuales = estado.getEntradasManuales();
        contadorVentas = estado.getContadorVentas();
        contadorProductos = estado.getContadorProductos();
        contadorCortes = estado.getContadorCortes();
        contadorProveedores = estado.getContadorProveedores();
    }

    public EstadoPersistido exportarEstado() {
        return new EstadoPersistido(
                productos, ventasDelDia, historialCortes, proveedores,
                totalEnCaja, entradasManuales,
                contadorVentas, contadorProductos, contadorCortes, contadorProveedores);
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

    public Usuario getUsuarioActivo() {
        return usuarioActivo;
    }

    public Venta getVentaActual() {
        return ventaActual;
    }

    public double getTotalEnCaja() {
        return totalEnCaja;
    }

    public double getEntradasManuales() {
        return entradasManuales;
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

    /** Filtra productos por texto de busqueda. */
    public List<Producto> buscarProductos(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return new ArrayList<>(productos);
        }
        String filtro = texto.trim().toLowerCase();
        return productos.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(filtro)
                        || p.getCategoria().toLowerCase().contains(filtro))
                .collect(Collectors.toList());
    }

    /** Agrega producto al ticket actual validando stock. */
    public String agregarAlTicket(int idProducto) {
        if (ventaActual == null) {
            return "No hay venta activa";
        }
        Producto producto = buscarProductoPorId(idProducto);
        if (producto == null) {
            return "Producto no encontrado";
        }
        if (!producto.tieneStock()) {
            return "Sin stock disponible";
        }
        ventaActual.agregarProducto(producto, 1);
        producto.reducirStock(1);
        persistir();
        return null;
    }

    public void eliminarLineaTicket(int indice) {
        if (ventaActual == null || indice < 0 || indice >= ventaActual.getLineas().size()) {
            return;
        }
        LineaVenta linea = ventaActual.getLineas().get(indice);
        linea.getProducto().aumentarStock(linea.getCantidad());
        ventaActual.eliminarLinea(indice);
        persistir();
    }

    /**
     * Cierra el ticket, actualiza caja e inventario persistido.
     * @return la venta cerrada o null si el ticket esta vacio
     */
    public Venta cobrarVenta() {
        if (ventaActual == null || ventaActual.estaVacia()) {
            return null;
        }
        double total = ventaActual.getTotal();
        ventaActual.setFechaHora(java.time.LocalDateTime.now());
        ventaActual.setCerrada(true);
        Venta ventaCerrada = ventaActual;
        ventasDelDia.add(ventaCerrada);
        totalEnCaja += total;
        ventaActual = new Venta(++contadorVentas, usuarioActivo.getNombreCompleto());
        persistir();
        System.out.println("Venta cobrada: $" + String.format("%.2f", total));
        return ventaCerrada;
    }

    public Producto buscarProductoPorId(int id) {
        return productos.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    public void surtirProducto(int id, int cantidad) {
        Producto p = buscarProductoPorId(id);
        if (p != null) {
            p.aumentarStock(cantidad);
            persistir();
            System.out.println("Stock actualizado para producto #" + id);
        }
    }

    public void eliminarProducto(int id) {
        productos.removeIf(p -> p.getId() == id);
        persistir();
    }

    public Producto crearProducto(String nombre, double precio, int stock, int stockMinimo) {
        return crearProducto(nombre, precio, stock, stockMinimo, null);
    }

    public Producto crearProducto(String nombre, double precio, int stock, int stockMinimo, String rutaImagen) {
        contadorProductos++;
        Producto nuevo = new Producto(contadorProductos, nombre, precio, stock, "General", "📦");
        nuevo.setStockMinimo(stockMinimo);
        nuevo.setRutaImagen(rutaImagen);
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
                        || p.getNombreContacto().toLowerCase().contains(filtro)
                        || p.getTelefono().contains(filtro))
                .collect(Collectors.toList());
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
        return ventasDelDia.stream().mapToDouble(Venta::getTotal).sum();
    }

    public double getPromedioTicket() {
        if (ventasDelDia.isEmpty()) {
            return 0;
        }
        return getVentasHoy() / ventasDelDia.size();
    }

    /** Realiza corte de caja y reinicia el monto en efectivo. */
    public Corte cerrarDiaYLimpiarCaja() {
        double monto = totalEnCaja;
        Corte corte = new Corte(monto, usuarioActivo.getNombreCompleto());
        historialCortes.add(0, corte);
        totalEnCaja = 0;
        entradasManuales = 0;
        ventasDelDia.clear();
        persistir();
        System.out.println("Corte de caja realizado: $" + String.format("%.2f", monto));
        return corte;
    }

    public void agregarEntradaManual(double monto) {
        if (monto > 0) {
            entradasManuales += monto;
            totalEnCaja += monto;
            persistir();
        }
    }
}
