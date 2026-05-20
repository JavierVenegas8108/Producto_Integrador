package com.abarrotespro.controlador;

import java.util.Set;

import com.abarrotespro.excepcion.DineroInsuficienteException;
import com.abarrotespro.modelo.Caja;
import com.abarrotespro.modelo.EstadoVenta;
import com.abarrotespro.modelo.Inventario;
import com.abarrotespro.modelo.MetodoPago;
import com.abarrotespro.modelo.Producto;
import com.abarrotespro.modelo.Venta;

/**
 * Logica de cobro, billetes rapidos y descuento de inventario al pagar.
 */
public class VentaController {

    private static final Set<Integer> DENOMINACIONES_VALIDAS =
            Set.of(20, 50, 100, 200, 500, 1000);

    private final Inventario inventario;
    private final Caja caja;
    private double montoAcumuladoEfectivo;

    public VentaController(Inventario inventario, Caja caja) {
        this.inventario = inventario;
        this.caja = caja;
        this.montoAcumuladoEfectivo = 0;
    }

    public Venta iniciarVenta(int id, String cajero) {
        return new Venta(id, cajero);
    }

    /**
     * Agrega producto al ticket validando existencia (sin descontar stock hasta el cobro).
     */
    public void agregarProducto(Venta venta, Producto producto, int cantidad) {
        if (venta.getEstado() != EstadoVenta.PENDIENTE) {
            throw new IllegalStateException("La venta ya no esta pendiente");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("Cantidad invalida");
        }
        Producto enInventario = inventario.buscarPorId(producto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no en inventario"));
        if (enInventario.getStockActual() < cantidad) {
            throw new IllegalStateException("Stock insuficiente para " + enInventario.getNombre());
        }
        venta.agregarProducto(enInventario, cantidad);
    }

    public void agregarBillete(int denominacion) {
        if (!DENOMINACIONES_VALIDAS.contains(denominacion)) {
            throw new IllegalArgumentException("Denominacion no valida: " + denominacion);
        }
        montoAcumuladoEfectivo += denominacion;
    }

    public void reiniciarAcumuladorEfectivo() {
        montoAcumuladoEfectivo = 0;
    }

    public double getMontoAcumuladoEfectivo() {
        return montoAcumuladoEfectivo;
    }

    /**
     * Cierra la venta, registra ingreso en caja y descuenta inventario.
     *
     * @return cambio entregado (solo efectivo)
     */
    public double procesarPago(Venta venta, MetodoPago metodoPago) throws DineroInsuficienteException {
        if (venta.getEstado() != EstadoVenta.PENDIENTE) {
            throw new IllegalStateException("La venta ya fue procesada");
        }
        if (venta.estaVacia()) {
            throw new IllegalStateException("No hay productos en la venta");
        }

        double total = venta.getMontoCobrable();
        double cambio = 0;

        if (metodoPago == MetodoPago.EFECTIVO) {
            if (montoAcumuladoEfectivo < total) {
                throw new DineroInsuficienteException(total, montoAcumuladoEfectivo);
            }
            cambio = montoAcumuladoEfectivo - total;
            venta.setMontoRecibido(montoAcumuladoEfectivo);
            venta.setCambio(cambio);
            reiniciarAcumuladorEfectivo();
        } else {
            venta.setMontoRecibido(total);
            venta.setCambio(0);
        }

        inventario.aplicarSalidaPorVenta(venta);
        caja.registrarIngreso(total, metodoPago, "Venta #" + venta.getId());

        venta.setMetodoPago(metodoPago);
        venta.setEstado(EstadoVenta.PAGADA);
        return cambio;
    }
}
