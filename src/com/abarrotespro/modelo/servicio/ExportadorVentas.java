package com.abarrotespro.modelo.servicio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.abarrotespro.modelo.dto.FilaReporteVenta;

public final class ExportadorVentas {

    private static final DateTimeFormatter FORMATO_ARCHIVO =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.forLanguageTag("es-MX"));

    private ExportadorVentas() {
    }

    public static Path exportarCsv(List<FilaReporteVenta> filas) throws IOException {
        Path directorio = Paths.get(System.getProperty("user.dir"), "data", "reportes");
        Files.createDirectories(directorio);
        Path archivo = directorio.resolve("ventas_" + LocalDateTime.now().format(FORMATO_ARCHIVO) + ".csv");

        StringBuilder sb = new StringBuilder();
        sb.append("Fecha de venta,Hora de venta,Número de ticket,Producto,Cantidad,");
        sb.append("Precio Unitario,Importe de línea,Total del Ticket,Usuario que atendió\n");

        for (FilaReporteVenta fila : filas) {
            sb.append(escaparCsv(fila.getFechaVenta())).append(',');
            sb.append(escaparCsv(fila.getHoraVenta())).append(',');
            sb.append(fila.getNumeroTicket()).append(',');
            sb.append(escaparCsv(fila.getProducto())).append(',');
            sb.append(fila.getCantidad()).append(',');
            sb.append(String.format(Locale.US, "%.2f", fila.getPrecioUnitario())).append(',');
            sb.append(String.format(Locale.US, "%.2f", fila.getImporteLinea())).append(',');
            sb.append(String.format(Locale.US, "%.2f", fila.getTotalTicket())).append(',');
            sb.append(escaparCsv(fila.getUsuario())).append('\n');
        }

        Files.writeString(archivo, sb.toString(), StandardCharsets.UTF_8);
        return archivo;
    }

    private static String escaparCsv(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
