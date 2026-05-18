package com.abarrotespro.modelo.servicio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.abarrotespro.modelo.LineaVenta;
import com.abarrotespro.modelo.Venta;
import com.abarrotespro.modelo.dto.FilaReporteVenta;

public final class ReporteVentasServicio {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("es-MX"));
    private static final DateTimeFormatter HORA =
            DateTimeFormatter.ofPattern("HH:mm:ss", Locale.forLanguageTag("es-MX"));
    private static final DateTimeFormatter FECHA_ARCHIVO =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Pattern PATRON_TOTAL =
            Pattern.compile("TOTAL:\\s*\\$?([\\d,]+\\.\\d{2})");
    private static final Pattern PATRON_NOMBRE_ARCHIVO =
            Pattern.compile("ticket_(\\d+)_(\\d{8}_\\d{6})\\.txt");

    private ReporteVentasServicio() {
    }

    public static List<FilaReporteVenta> consultar(LocalDate desde, LocalDate hasta,
            List<Venta> ventasEnMemoria) {
        Set<Integer> ticketsProcesados = new HashSet<>();
        List<FilaReporteVenta> filas = new ArrayList<>();

        if (ventasEnMemoria != null) {
            for (Venta venta : ventasEnMemoria) {
                if (!venta.isCerrada()) {
                    continue;
                }
                LocalDate fecha = venta.getFechaHora().toLocalDate();
                if (fecha.isBefore(desde) || fecha.isAfter(hasta)) {
                    continue;
                }
                ticketsProcesados.add(venta.getId());
                filas.addAll(convertirVenta(venta));
            }
        }

        Path directorio = LectorTickets.directorioTickets();
        if (Files.isDirectory(directorio)) {
            try (Stream<Path> stream = Files.list(directorio)) {
                stream.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".txt"))
                        .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                        .forEach(archivo -> {
                            try {
                                int ticketId = extraerIdDesdeNombre(archivo.getFileName().toString());
                                if (ticketId > 0 && ticketsProcesados.contains(ticketId)) {
                                    return;
                                }
                                List<FilaReporteVenta> delArchivo = parsearArchivoTicket(archivo);
                                if (delArchivo.isEmpty()) {
                                    return;
                                }
                                int idTicket = delArchivo.get(0).getNumeroTicket();
                                if (ticketsProcesados.contains(idTicket)) {
                                    return;
                                }
                                boolean agregado = false;
                                for (FilaReporteVenta fila : delArchivo) {
                                    LocalDate fecha = LocalDate.parse(fila.getFechaVenta(), FECHA);
                                    if (!fecha.isBefore(desde) && !fecha.isAfter(hasta)) {
                                        filas.add(fila);
                                        agregado = true;
                                    }
                                }
                                if (agregado) {
                                    ticketsProcesados.add(idTicket);
                                }
                            } catch (IOException ignored) {
                            }
                        });
            } catch (IOException ignored) {
            }
        }

        filas.sort(Comparator
                .comparing(FilaReporteVenta::getFechaVenta)
                .thenComparing(FilaReporteVenta::getHoraVenta)
                .thenComparingInt(FilaReporteVenta::getNumeroTicket));
        return filas;
    }

    private static List<FilaReporteVenta> convertirVenta(Venta venta) {
        List<FilaReporteVenta> filas = new ArrayList<>();
        String fecha = venta.getFechaHora().format(FECHA);
        String hora = venta.getFechaHora().format(HORA);
        double total = venta.getTotal();
        for (LineaVenta linea : venta.getLineas()) {
            filas.add(new FilaReporteVenta(
                    fecha,
                    hora,
                    venta.getId(),
                    linea.getProducto().getNombre(),
                    linea.getCantidad(),
                    linea.getProducto().getPrecio(),
                    linea.getSubtotal(),
                    total,
                    venta.getUsuarioNombre()));
        }
        return filas;
    }

    private static List<FilaReporteVenta> parsearArchivoTicket(Path archivo) throws IOException {
        String contenido = Files.readString(archivo, StandardCharsets.UTF_8);
        String nombre = archivo.getFileName().toString();

        int ticketId = extraerIdDesdeNombre(nombre);
        String fecha = null;
        String hora = null;
        String usuario = "";
        double totalTicket = 0;
        List<String[]> lineasProducto = new ArrayList<>();
        boolean enDetalle = false;

        for (String linea : contenido.split("\n")) {
            String texto = linea.trim();
            if (texto.startsWith("Fecha:")) {
                fecha = texto.substring(6).trim();
            } else if (texto.startsWith("Hora:")) {
                hora = texto.substring(5).trim();
            } else if (texto.startsWith("Ticket #:")) {
                try {
                    ticketId = Integer.parseInt(texto.substring(9).trim());
                } catch (NumberFormatException ignored) {
                }
            } else if (texto.startsWith("Atendio:")) {
                usuario = texto.substring(8).trim();
            } else if (texto.startsWith("Producto") && texto.contains("Cant")) {
                enDetalle = true;
            } else if (texto.contains("TOTAL:")) {
                Matcher matcher = PATRON_TOTAL.matcher(texto);
                if (matcher.find()) {
                    totalTicket = Double.parseDouble(matcher.group(1).replace(",", ""));
                }
                enDetalle = false;
            } else if (enDetalle) {
                if (texto.isEmpty() || texto.chars().allMatch(c -> c == '-')) {
                    continue;
                }
                String[] datos = parsearLineaProducto(linea);
                if (datos != null) {
                    lineasProducto.add(datos);
                }
            }
        }

        if (fecha == null || hora == null) {
            LocalDateTime desdeArchivo = extraerFechaHoraDesdeNombre(nombre);
            if (desdeArchivo != null) {
                if (fecha == null) {
                    fecha = desdeArchivo.format(FECHA);
                }
                if (hora == null) {
                    hora = desdeArchivo.format(HORA);
                }
            }
        }

        if (fecha == null || hora == null) {
            return List.of();
        }

        List<FilaReporteVenta> filas = new ArrayList<>();
        for (String[] datos : lineasProducto) {
            filas.add(new FilaReporteVenta(
                    fecha,
                    hora,
                    ticketId,
                    datos[0],
                    Integer.parseInt(datos[1]),
                    Double.parseDouble(datos[2]),
                    Double.parseDouble(datos[3]),
                    totalTicket,
                    usuario));
        }
        return filas;
    }

    private static String[] parsearLineaProducto(String linea) {
        String texto = linea.stripTrailing();
        if (texto.isBlank()) {
            return null;
        }
        String[] partes = texto.trim().split("\\s+");
        if (partes.length < 4) {
            return null;
        }
        try {
            double importe = Double.parseDouble(partes[partes.length - 1]);
            double precio = Double.parseDouble(partes[partes.length - 2]);
            int cantidad = Integer.parseInt(partes[partes.length - 3]);
            int indiceNombre = texto.lastIndexOf(partes[partes.length - 3]);
            String nombre = texto.substring(0, indiceNombre).trim();
            if (nombre.isEmpty()) {
                return null;
            }
            return new String[] {
                    nombre,
                    String.valueOf(cantidad),
                    String.valueOf(precio),
                    String.valueOf(importe)
            };
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static int extraerIdDesdeNombre(String nombre) {
        Matcher matcher = PATRON_NOMBRE_ARCHIVO.matcher(nombre);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }

    private static LocalDateTime extraerFechaHoraDesdeNombre(String nombre) {
        Matcher matcher = PATRON_NOMBRE_ARCHIVO.matcher(nombre);
        if (!matcher.matches()) {
            return null;
        }
        try {
            return LocalDateTime.parse(matcher.group(2), FECHA_ARCHIVO);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    public static LocalDate parsearFechaCampo(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new DateTimeParseException("Fecha vacia", texto, 0);
        }
        return LocalDate.parse(texto.trim(), FECHA);
    }

    public static boolean esRangoValido(LocalDate desde, LocalDate hasta) {
        return desde != null && hasta != null && !desde.isAfter(hasta);
    }
}
