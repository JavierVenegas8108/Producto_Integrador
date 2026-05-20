package com.abarrotespro.controlador;

import java.util.List;

import com.abarrotespro.modelo.Caja;
import com.abarrotespro.modelo.MetodoPago;
import com.abarrotespro.modelo.Venta;

/**
 * Apertura de caja, egresos a proveedores y reporte de corte.
 */
public class CajaController {

    private final Caja caja;

    public CajaController(Caja caja) {
        this.caja = caja;
    }

    public void abrirCaja(double fondoInicial) {
        caja.abrirCaja(fondoInicial);
    }

    public void registrarPagoProveedor(String nombreProveedor, double monto, MetodoPago metodoPago) {
        String concepto = "Pago Proveedor: " + nombreProveedor;
        caja.registrarEgreso(monto, metodoPago, concepto);
    }

    public void imprimirReporteCorte(List<Venta> ventasDelDia) {
        double totalCaja = caja.calcularEfectivoEsperado();
        double ventasHoy = caja.calcularVentasHoy(ventasDelDia);
        double promedio = caja.calcularPromedioTicket(ventasDelDia);
        double utilidad = caja.calcularUtilidadHoy(ventasDelDia);

        System.out.println();
        System.out.println("+--------------------------------------------------+");
        System.out.println("|           CORTE DE CAJA - ABARROTES PRO          |");
        System.out.println("+--------------------------------------------------+");
        System.out.printf("|  TOTAL EN CAJA (EFECTIVO)     %16s |%n", formatear(totalCaja));
        System.out.println("+--------------------------------------------------+");
        System.out.printf("|  Ventas hoy                   %16s |%n", formatear(ventasHoy));
        System.out.printf("|  Promedio Ticket              %16s |%n", formatear(promedio));
        System.out.printf("|  Utilidad hoy                 %16s |%n", formatear(utilidad));
        System.out.println("+--------------------------------------------------+");
        System.out.println();
        System.out.println("  Detalle caja:");
        System.out.printf("    Fondo inicial:        %s%n", formatear(caja.getFondoInicial()));
        System.out.printf("    Ingresos efectivo:    %s%n", formatear(caja.getIngresosEfectivo()));
        System.out.printf("    Ingresos tarjeta:     %s%n", formatear(caja.getIngresosTarjeta()));
        System.out.printf("    Ingresos transfer.:   %s%n", formatear(caja.getIngresosTransferencia()));
        System.out.printf("    Egresos efectivo:     %s%n", formatear(caja.getEgresosEfectivo()));
    }

    public Caja getCaja() {
        return caja;
    }

    private static String formatear(double monto) {
        return String.format("$%,.2f", monto);
    }
}
