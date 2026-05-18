package com.abarrotespro.modelo.servicio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Lee el historial de tickets guardados en la carpeta tickets/.
 */
public final class LectorTickets {

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
            stream.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".txt"))
                    .map(p -> p.getFileName().toString())
                    .sorted(Comparator.reverseOrder())
                    .forEach(nombres::add);
        } catch (IOException e) {
            System.err.println("Error al listar tickets: " + e.getMessage());
        }
        return nombres;
    }

    public static String leerContenido(String nombreArchivo) throws IOException {
        Path archivo = directorioTickets().resolve(nombreArchivo);
        if (!Files.isRegularFile(archivo)) {
            return "";
        }
        return Files.readString(archivo, StandardCharsets.UTF_8);
    }
}
