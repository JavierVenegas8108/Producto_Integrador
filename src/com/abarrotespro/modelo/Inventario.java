package com.abarrotespro.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Catalogo de productos y operaciones de existencias.
 */
public class Inventario {

    private final List<Producto> productos;

    public Inventario(List<Producto> productosReferencia) {
        this.productos = productosReferencia;
    }

    public Inventario() {
        productos = new ArrayList<>();
    }

    public static Inventario crearConDatosDemo() {
        Inventario inventario = new Inventario();
        inventario.agregar(new Producto(1, "Leche Entera 1L", 22.50, 45, "Lacteos", "🥛"));
        inventario.agregar(new Producto(2, "Pan Blanco Bolsa", 38.00, 20, "Panaderia", "🍞"));
        inventario.agregar(new Producto(3, "Arroz 1kg", 28.90, 60, "Abarrotes", "🍚"));
        inventario.agregar(new Producto(4, "Aceite Vegetal 1L", 42.00, 30, "Abarrotes", "🫒"));
        inventario.agregar(new Producto(5, "Coca-Cola 600ml", 18.50, 80, "Bebidas", "🥤"));
        inventario.agregar(new Producto(6, "Paquete Mayoreo", 350.00, 5, "Abarrotes", "📦"));
        Producto leche = inventario.buscarPorId(1).orElseThrow();
        leche.setPrecioCompra(18.00);
        leche.setInventarioMinimo(10);
        leche.setIdProveedor(1);
        Producto aceite = inventario.buscarPorId(4).orElseThrow();
        aceite.setPrecioCompra(35.00);
        aceite.setInventarioMinimo(8);
        Producto paquete = inventario.buscarPorId(6).orElseThrow();
        paquete.setPrecioCompra(310.00);
        return inventario;
    }

    public void agregar(Producto producto) {
        productos.add(producto);
    }

    public List<Producto> getProductos() {
        return Collections.unmodifiableList(productos);
    }

    public Optional<Producto> buscarPorId(int id) {
        return productos.stream().filter(p -> p.getId() == id).findFirst();
    }

    public Optional<Producto> buscarPorNombre(String nombre) {
        if (nombre == null) {
            return Optional.empty();
        }
        return productos.stream()
                .filter(p -> p.getNombre().equalsIgnoreCase(nombre.trim()))
                .findFirst();
    }

    /**
     * Descuenta existencias al confirmar una venta pagada.
     */
    public void aplicarSalidaPorVenta(Venta venta) {
        for (LineaVenta linea : venta.getLineas()) {
            Producto enInventario = buscarPorId(linea.getProducto().getId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Producto no encontrado en inventario: " + linea.getProducto().getId()));
            if (!enInventario.reducirStock(linea.getCantidad())) {
                throw new IllegalStateException(
                        "Stock insuficiente para " + enInventario.getNombre()
                                + " (solicitado: " + linea.getCantidad()
                                + ", disponible: " + enInventario.getStockActual() + ")");
            }
        }
    }
}
