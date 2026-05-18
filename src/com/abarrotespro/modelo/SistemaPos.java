package com.abarrotespro.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Modelo central del punto de venta: inventario, ventas, caja y usuarios.
 */
public class SistemaPos {

    private final List<Usuario> usuarios;
    private final List<Producto> productos;
    private final List<Venta> ventasDelDia;
    private final List<Corte> historialCortes;

    private Usuario usuarioActivo;
    private Venta ventaActual;
    private double totalEnCaja;
    private double entradasManuales;
    private int contadorVentas;
    private int contadorProductos;
    private int contadorCortes;

    public SistemaPos() {
        usuarios = new ArrayList<>();
        productos = new ArrayList<>();
        ventasDelDia = new ArrayList<>();
        historialCortes = new ArrayList<>();
        contadorVentas = 0;
        contadorProductos = 0;
        contadorCortes = 0;
        totalEnCaja = 1250.50;
        entradasManuales = 200.00;
        inicializarDatos();
    }

    private void inicializarDatos() {
        usuarios.add(new Usuario("admin", "admin123", "Administrador", "AD"));

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
        return null;
    }

    public void eliminarLineaTicket(int indice) {
        if (ventaActual == null || indice < 0 || indice >= ventaActual.getLineas().size()) {
            return;
        }
        LineaVenta linea = ventaActual.getLineas().get(indice);
        linea.getProducto().aumentarStock(linea.getCantidad());
        ventaActual.eliminarLinea(indice);
    }

    /** Cierra el ticket y suma al total en caja. */
    public String cobrarVenta() {
        if (ventaActual == null || ventaActual.estaVacia()) {
            return "El ticket esta vacio";
        }
        double total = ventaActual.getTotal();
        ventaActual.setCerrada(true);
        ventasDelDia.add(ventaActual);
        totalEnCaja += total;
        ventaActual = new Venta(++contadorVentas, usuarioActivo.getNombreCompleto());
        System.out.println("Venta cobrada: $" + String.format("%.2f", total));
        return null;
    }

    public Producto buscarProductoPorId(int id) {
        return productos.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    public void surtirProducto(int id, int cantidad) {
        Producto p = buscarProductoPorId(id);
        if (p != null) {
            p.aumentarStock(cantidad);
            System.out.println("Stock actualizado para producto #" + id);
        }
    }

    public void eliminarProducto(int id) {
        productos.removeIf(p -> p.getId() == id);
    }

    public Producto crearProducto(String nombre, double precio, int stock, int stockMinimo) {
        contadorProductos++;
        Producto nuevo = new Producto(contadorProductos, nombre, precio, stock, "General", "📦");
        productos.add(nuevo);
        return nuevo;
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
        System.out.println("Corte de caja realizado: $" + String.format("%.2f", monto));
        return corte;
    }

    public void agregarEntradaManual(double monto) {
        if (monto > 0) {
            entradasManuales += monto;
            totalEnCaja += monto;
        }
    }
}
