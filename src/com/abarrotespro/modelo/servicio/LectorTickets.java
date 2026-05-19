package com.abarrotespro.modelo.servicio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Lee y gestiona archivos de tickets en la carpeta tickets/.
 */
public final class LectorTickets {

    private static final Pattern PATRON_ID =
            Pattern.compile("ticket_(\\d+)(?:_cancelled)?_\\d{8}_\\d{6}\\.txt", Pattern.CASE_INSENSITIVE);

    private LectorTickets() {
    }

    public static Path directorioTickets() {
        return Paths.get(System.getProperty("user.dir"), "tickets");
    }

    public static List<String> listarArchivos() {
        Path dir = directorioTickets();
        List<String> nombres = new ArrayList<>();
        if (!Files.isDirectory(dir)) {
            return nombres;
        }
        try (Stream<Path> stream = Files.list(dir)) {
            stream.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".txt"))
                    .map(p -> p.getFileName().toString())
                    .sorted(Comparator.reverseOrder())
                    .forEach(nombres::add);
        } catch (IOException e) {
            System.err.println("Error al listar tickets: " + e.getMessage());
        }
        return nombres;
    }

    public static boolean esCancelado(String nombreArchivo) {
        return nombreArchivo != null && nombreArchivo.toLowerCase().contains("_cancelled");
    }

    public static int extraerIdVenta(String nombreArchivo) {
        if (nombreArchivo == null) {
            return -1;
        }
        Matcher m = PATRON_ID.matcher(nombreArchivo);
        if (m.matches()) {
            return Integer.parseInt(m.group(1));
        }
        return -1;
    }

    public static String leerContenido(String nombreArchivo) throws IOException {
        Path archivo = directorioTickets().resolve(nombreArchivo);
        if (!Files.isRegularFile(archivo)) {
            return "";
        }
        return Files.readString(archivo, StandardCharsets.UTF_8);
    }

    /** Renombra el ticket a ticket_ID_cancelled_fecha.txt */
    public static String marcarComoCancelado(String nombreArchivo) throws IOException {
        if (esCancelado(nombreArchivo)) {
            return nombreArchivo;
        }
        Path origen = directorioTickets().resolve(nombreArchivo);
        if (!Files.isRegularFile(origen)) {
            throw new IOException("Archivo no encontrado: " + nombreArchivo);
        }
        String nuevoNombre = nombreArchivo.replace(".txt", "")
                .replace("_cancelled", "")
                + "_cancelled.txt";
        Path destino = directorioTickets().resolve(nuevoNombre);
        Files.move(origen, destino, StandardCopyOption.REPLACE_EXISTING);
        String contenido = Files.readString(destino, StandardCharsets.UTF_8);
        if (!contenido.contains("*** TICKET ANULADO ***")) {
            contenido = "*** TICKET ANULADO - DEVOLUCION ***\n\n" + contenido;
            Files.writeString(destino, contenido, StandardCharsets.UTF_8);
        }
        return nuevoNombre;
    }
}
