package com.abarrotespro.vista.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Almacena y carga imagenes de productos en data/imagenes/.
 */
public final class GestorImagenProducto {

    private static final Path DIRECTORIO = Paths.get(System.getProperty("user.dir"), "data", "imagenes");
    private static final String[] EXTENSIONES = {".png", ".jpg", ".jpeg", ".gif", ".bmp"};

    private GestorImagenProducto() {
    }

    public static boolean esArchivoImagen(File archivo) {
        if (archivo == null || !archivo.isFile()) {
            return false;
        }
        String nombre = archivo.getName().toLowerCase();
        for (String ext : EXTENSIONES) {
            if (nombre.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /** Copia la imagen al directorio de datos y devuelve la ruta relativa guardada. */
    public static String guardarImagen(File origen, int idProducto) throws IOException {
        Files.createDirectories(DIRECTORIO);
        String extension = obtenerExtension(origen.getName());
        Path destino = DIRECTORIO.resolve("producto_" + idProducto + extension);
        Files.copy(origen.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
        return destino.toString();
    }

    public static ImageIcon cargarMiniatura(String ruta, int ancho, int alto) {
        if (ruta == null || ruta.isBlank()) {
            return null;
        }
        Path path = Paths.get(ruta);
        if (!Files.exists(path)) {
            return null;
        }
        ImageIcon icono = new ImageIcon(path.toString());
        Image escala = icono.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        return new ImageIcon(escala);
    }

    public static ImageIcon cargarMiniaturaOEmoji(String ruta, String emoji, int ancho, int alto) {
        ImageIcon miniatura = cargarMiniatura(ruta, ancho, alto);
        if (miniatura != null) {
            return miniatura;
        }
        JLabel temporal = new JLabel(emoji != null ? emoji : "📦");
        temporal.setFont(new Font("Segoe UI Emoji", Font.PLAIN, ancho - 8));
        temporal.setSize(ancho, alto);
        BufferedImage img = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        temporal.paint(g2);
        g2.dispose();
        return new ImageIcon(img);
    }

    private static String obtenerExtension(String nombre) {
        int punto = nombre.lastIndexOf('.');
        if (punto >= 0) {
            return nombre.substring(punto).toLowerCase();
        }
        return ".png";
    }
}
