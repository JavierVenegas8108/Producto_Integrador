package com.abarrotespro.modelo.servicio;

import com.abarrotespro.modelo.Producto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exporta inventario a CSV/TXT usando ID numerico entero (sin ceros a la izquierda).
 */
public final class ExportadorInventario {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private ExportadorInventario() {
    }

    public static Path exportarCsv(List<Producto> productos) throws IOException {
        Path archivo = Paths.get(System.getProperty("user.dir"), "data",
                "inventario_" + LocalDateTime.now().format(FORMATO_FECHA) + ".csv");
        Files.createDirectories(archivo.getParent());
        StringBuilder sb = new StringBuilder();
        sb.append("id,nombre,precio,stock,stock_minimo,categoria,ruta_imagen\n");
        for (Producto p : productos) {
            sb.append(p.getId()).append(',');
            sb.append(escaparCsv(p.getNombre())).append(',');
            sb.append(p.getPrecio()).append(',');
            sb.append(p.getStock()).append(',');
            sb.append(p.getStockMinimo()).append(',');
            sb.append(escaparCsv(p.getCategoria())).append(',');
            sb.append(escaparCsv(p.getRutaImagen() != null ? p.getRutaImagen() : "")).append('\n');
        }
        Files.writeString(archivo, sb.toString(), StandardCharsets.UTF_8);
        return archivo;
    }

    public static Path exportarBajoStockCsv(List<Producto> productos) throws IOException {
        Path archivo = Paths.get(System.getProperty("user.dir"), "data",
                "bajo_stock_" + LocalDateTime.now().format(FORMATO_FECHA) + ".csv");
        Files.createDirectories(archivo.getParent());
        StringBuilder sb = new StringBuilder();
        sb.append("id,nombre,stock_actual,stock_minimo,diferencia,categoria\n");
        for (Producto p : productos) {
            int diferencia = p.getStockMinimo() - p.getStock();
            sb.append(p.getId()).append(',');
            sb.append(escaparCsv(p.getNombre())).append(',');
            sb.append(p.getStock()).append(',');
            sb.append(p.getStockMinimo()).append(',');
            sb.append(diferencia).append(',');
            sb.append(escaparCsv(p.getCategoria())).append('\n');
        }
        Files.writeString(archivo, sb.toString(), StandardCharsets.UTF_8);
        return archivo;
    }

    public static Path exportarTxt(List<Producto> productos) throws IOException {
        Path archivo = Paths.get(System.getProperty("user.dir"), "data",
                "inventario_" + LocalDateTime.now().format(FORMATO_FECHA) + ".txt");
        Files.createDirectories(archivo.getParent());
        StringBuilder sb = new StringBuilder();
        sb.append("REPORTE DE INVENTARIO\n");
        sb.append("=====================\n");
        for (Producto p : productos) {
            sb.append("ID: ").append(p.getId()).append('\n');
            sb.append("Nombre: ").append(p.getNombre()).append('\n');
            sb.append("Precio: ").append(String.format("%.2f", p.getPrecio())).append('\n');
            sb.append("Stock: ").append(p.getStock()).append('\n');
            sb.append("Stock minimo: ").append(p.getStockMinimo()).append("\n---\n");
        }
        Files.writeString(archivo, sb.toString(), StandardCharsets.UTF_8);
        return archivo;
    }

    private static String escaparCsv(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.contains(",") || valor.contains("\"")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
