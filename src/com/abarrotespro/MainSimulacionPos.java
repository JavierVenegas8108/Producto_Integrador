package com.abarrotespro;

import com.abarrotespro.controlador.CajaController;
import com.abarrotespro.controlador.VentaController;
import com.abarrotespro.excepcion.DineroInsuficienteException;
import java.util.ArrayList;
import java.util.List;

import com.abarrotespro.modelo.Caja;
import com.abarrotespro.modelo.Inventario;
import com.abarrotespro.modelo.MetodoPago;
import com.abarrotespro.modelo.Producto;
import com.abarrotespro.modelo.Venta;

/**
 * Flujo de prueba de caja, ventas e inventario con datos del sistema demo.
 */
public class MainSimulacionPos {

    public static void main(String[] args) {
        Inventario inventario = Inventario.crearConDatosDemo();
        Caja caja = new Caja();
        CajaController cajaController = new CajaController(caja);
        VentaController ventaController = new VentaController(inventario, caja);
        List<Venta> ventasDelDia = new ArrayList<>();

        System.out.println("=== ABARROTES PRO - SIMULACION POS ===\n");

        // --- Apertura ---
        System.out.println("1) Apertura de caja con fondo inicial $500.00");
        cajaController.abrirCaja(500.00);

        Producto leche = inventario.buscarPorNombre("Leche Entera 1L").orElseThrow();
        Producto aceite = inventario.buscarPorNombre("Aceite Vegetal 1L").orElseThrow();
        System.out.printf("   Stock inicial Leche (%s): %d | Aceite (%s): %d%n",
                leche.getIdFormateado(), leche.getStockActual(),
                aceite.getIdFormateado(), aceite.getStockActual());

        // --- Venta 1 (tarjeta) ---
        System.out.println("\n2) Venta #1: 1x Leche Entera 1L ($22.50) + 1x Aceite Vegetal 1L ($42.00) - TARJETA");
        Venta venta1 = ventaController.iniciarVenta(1, "Administrador");
        ventaController.agregarProducto(venta1, leche, 1);
        ventaController.agregarProducto(venta1, aceite, 1);
        try {
            ventaController.procesarPago(venta1, MetodoPago.TARJETA);
            ventasDelDia.add(venta1);
            System.out.printf("   Cobrado: %s | Metodo: %s%n",
                    formatear(venta1.getMontoCobrable()), venta1.getMetodoPago());
        } catch (DineroInsuficienteException e) {
            System.err.println("   Error inesperado: " + e.getMessage());
        }

        // --- Egreso proveedor ---
        System.out.println("\n3) Egreso: Pago $200.00 en EFECTIVO a Distribuidora La Central");
        cajaController.registrarPagoProveedor("Distribuidora La Central", 200.00, MetodoPago.EFECTIVO);

        // --- Venta 2 (efectivo con error y cambio) ---
        System.out.println("\n4) Venta #2: Paquete Mayoreo ($350.00) - EFECTIVO");
        Producto paquete = inventario.buscarPorNombre("Paquete Mayoreo").orElseThrow();
        Venta venta2 = ventaController.iniciarVenta(2, "Administrador");
        ventaController.agregarProducto(venta2, paquete, 1);

        System.out.println("   Cliente entrega billete de $200...");
        ventaController.agregarBillete(200);
        try {
            ventaController.procesarPago(venta2, MetodoPago.EFECTIVO);
        } catch (DineroInsuficienteException e) {
            System.out.println("   >> " + e.getMessage());
        }

        System.out.println("   Cliente entrega otro billete de $200...");
        ventaController.agregarBillete(200);
        try {
            double cambio = ventaController.procesarPago(venta2, MetodoPago.EFECTIVO);
            ventasDelDia.add(venta2);
            System.out.printf("   Pago exitoso. Recibido: %s | Cambio: %s%n",
                    formatear(venta2.getMontoRecibido()), formatear(cambio));
        } catch (DineroInsuficienteException e) {
            System.err.println("   Error: " + e.getMessage());
        }

        // --- Inventario post-ventas ---
        System.out.println("\n5) Inventario actualizado:");
        leche = inventario.buscarPorId(1).orElseThrow();
        aceite = inventario.buscarPorId(4).orElseThrow();
        paquete = inventario.buscarPorId(6).orElseThrow();
        System.out.printf("   Leche Entera 1L     [%s] stock: %d%n",
                leche.getIdFormateado(), leche.getStockActual());
        System.out.printf("   Aceite Vegetal 1L   [%s] stock: %d%n",
                aceite.getIdFormateado(), aceite.getStockActual());
        System.out.printf("   Paquete Mayoreo     [%s] stock: %d%n",
                paquete.getIdFormateado(), paquete.getStockActual());

        // --- Corte de caja ---
        System.out.println("\n6) Corte de caja (resumen como pantalla UI):");
        cajaController.imprimirReporteCorte(ventasDelDia);

        System.out.println("=== FIN DE SIMULACION ===");
    }

    private static String formatear(double monto) {
        return String.format("$%,.2f", monto);
    }
}
