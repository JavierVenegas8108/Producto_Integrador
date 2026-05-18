package com.abarrotespro.modelo.servicio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.abarrotespro.modelo.LineaVenta;
import com.abarrotespro.modelo.Venta;

/**
 * Genera archivos de ticket de venta en formato texto.
 */
public final class GeneradorTicket {

    private static final String NOMBRE_NEGOCIO = "Abarrotes Pro";
    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("es-MX"));
    private static final DateTimeFormatter HORA =
            DateTimeFormatter.ofPattern("HH:mm:ss", Locale.forLanguageTag("es-MX"));

    private GeneradorTicket() {
    }

    public static Path generar(Venta venta) throws IOException {
        Path directorio = Paths.get(System.getProperty("user.dir"), "tickets");
        Files.createDirectories(directorio);

        String nombreArchivo = String.format("ticket_%03d_%s.txt",
                venta.getId(),
                venta.getFechaHora().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        Path archivo = directorio.resolve(nombreArchivo);

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("           ").append(NOMBRE_NEGOCIO.toUpperCase()).append("\n");
        sb.append("========================================\n");
        sb.append("Fecha: ").append(venta.getFechaHora().format(FECHA)).append("\n");
        sb.append("Hora:  ").append(venta.getFechaHora().format(HORA)).append("\n");
        sb.append("Ticket #: ").append(venta.getId()).append("\n");
        sb.append("Atendio: ").append(venta.getUsuarioNombre()).append("\n");
        sb.append("----------------------------------------\n");
        sb.append("DETALLE DE COMPRA\n");
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-22s %4s %8s %10s%n",
                "Producto", "Cant", "P.Unit", "Importe"));
        sb.append("----------------------------------------\n");

        for (LineaVenta linea : venta.getLineas()) {
            String nombre = truncar(linea.getProducto().getNombre(), 22);
            sb.append(String.format(Locale.US, "%-22s %4d %8.2f %10.2f%n",
                    nombre,
                    linea.getCantidad(),
                    linea.getProducto().getPrecio(),
                    linea.getSubtotal()));
        }

        sb.append("----------------------------------------\n");
        sb.append(String.format(Locale.US, "%42s%n", "TOTAL: $" + String.format("%.2f", venta.getTotal())));
        sb.append("========================================\n");
        sb.append("   Gracias por su preferencia!\n");
        sb.append("        Vuelva pronto.\n");
        sb.append("========================================\n");

        Files.writeString(archivo, sb.toString(), StandardCharsets.UTF_8);
        return archivo;
    }

    private static String truncar(String texto, int max) {
        if (texto.length() <= max) {
            return texto;
        }
        return texto.substring(0, max - 1) + ".";
    }
}
