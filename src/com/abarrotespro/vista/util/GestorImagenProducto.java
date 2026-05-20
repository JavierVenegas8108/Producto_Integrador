package com.abarrotespro.vista.util;

import com.abarrotespro.modelo.Producto;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Almacena y carga imagenes de productos en data/imagenes/.
 */
public final class GestorImagenProducto {

    private static final String RUTA_RELATIVA_IMG = Paths.get("src", "img").toString();
    private static final Path DIRECTORIO = Paths.get(System.getProperty("user.dir"), "data", "imagenes");
    private static final Path DIRECTORIO_SRC_IMG = Paths.get(System.getProperty("user.dir"), "src", "img");
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
        BufferedImage original = cargarImagenDesdeClasspath(ruta);
        if (original == null) {
            return null;
        }
        return escalarProporcional(original, ancho, alto);
    }

    public static ImageIcon cargarMiniaturaProducto(Producto producto, int ancho, int alto) {
        ImageIcon real = cargarMiniatura(producto.getRutaImagen(), ancho, alto);
        if (real != null) {
            return real;
        }
        ImageIcon placeholder = cargarMiniatura("/img/placeholder_generico.png", ancho, alto);
        if (placeholder != null) {
            return placeholder;
        }
        return crearIconoSinImagen(ancho, alto);
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

    public static String nombreVisibleDesdeArchivo(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            return "";
        }
        String nombre = Paths.get(nombreArchivo).getFileName().toString();
        int punto = nombre.lastIndexOf('.');
        String base = punto > 0 ? nombre.substring(0, punto) : nombre;
        return base.replace('_', ' ').trim();
    }

    public static String rutaImagenProyecto(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            return null;
        }
        return "/img/" + Paths.get(nombreArchivo).getFileName();
    }

    public static List<String> listarArchivosImagenProyecto() {
        List<String> archivos = new ArrayList<>();
        if (!Files.isDirectory(DIRECTORIO_SRC_IMG)) {
            return archivos;
        }
        try (var stream = Files.list(DIRECTORIO_SRC_IMG)) {
            stream.filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(GestorImagenProducto::esNombreArchivoImagen)
                    .sorted(Comparator.naturalOrder())
                    .forEach(archivos::add);
        } catch (IOException ignored) {
            return new ArrayList<>();
        }
        return archivos;
    }

    private static ImageIcon escalarProporcional(BufferedImage original, int ancho, int alto) {
        int origenAncho = original.getWidth();
        int origenAlto = original.getHeight();
        if (origenAncho <= 0 || origenAlto <= 0 || ancho <= 0 || alto <= 0) {
            return null;
        }
        double escala = Math.min((double) ancho / origenAncho, (double) alto / origenAlto);
        int anchoEscalado = Math.max(1, (int) Math.round(origenAncho * escala));
        int altoEscalado = Math.max(1, (int) Math.round(origenAlto * escala));
        Image suavizada = original.getScaledInstance(anchoEscalado, altoEscalado, Image.SCALE_SMOOTH);
        BufferedImage lienzo = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = lienzo.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        int x = (ancho - anchoEscalado) / 2;
        int y = (alto - altoEscalado) / 2;
        g2.drawImage(suavizada, x, y, null);
        g2.dispose();
        return new ImageIcon(lienzo);
    }

    private static BufferedImage cargarImagenDesdeClasspath(String ruta) {
        String[] candidatos = construirCandidatosRecurso(ruta);
        for (String candidato : candidatos) {
            try (InputStream in = GestorImagenProducto.class.getResourceAsStream(candidato)) {
                if (in == null) {
                    continue;
                }
                BufferedImage imagen = ImageIO.read(in);
                if (imagen != null) {
                    return imagen;
                }
            } catch (IOException ignored) {
                return null;
            }
        }
        return null;
    }

    private static String[] construirCandidatosRecurso(String ruta) {
        String normalizada = ruta.replace('\\', '/').trim();
        if (normalizada.startsWith("/")) {
            String nombre = Paths.get(normalizada).getFileName().toString();
            return new String[] {normalizada, "/img/" + nombre};
        }
        if (normalizada.startsWith("img/")) {
            String absoluto = "/" + normalizada;
            String nombre = Paths.get(normalizada).getFileName().toString();
            return new String[] {absoluto, "/img/" + nombre};
        }
        Path nombreArchivo = Paths.get(normalizada).getFileName();
        if (nombreArchivo != null) {
            return new String[] {"/img/" + nombreArchivo};
        }
        return new String[] {"/img/" + normalizada};
    }

    private static String obtenerExtension(String nombre) {
        int punto = nombre.lastIndexOf('.');
        if (punto >= 0) {
            return nombre.substring(punto).toLowerCase();
        }
        return ".png";
    }

    private static boolean esNombreArchivoImagen(String nombreArchivo) {
        String nombre = nombreArchivo.toLowerCase();
        for (String ext : EXTENSIONES) {
            if (nombre.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private static ImageIcon crearIconoSinImagen(int ancho, int alto) {
        BufferedImage img = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(226, 232, 240));
        g2.fillRoundRect(0, 0, ancho, alto, 10, 10);
        g2.setColor(new Color(148, 163, 184));
        g2.drawRoundRect(0, 0, ancho - 1, alto - 1, 10, 10);
        int margen = Math.max(6, ancho / 8);
        g2.drawRect(margen, margen, ancho - (margen * 2), alto - (margen * 2));
        g2.drawLine(margen, alto - margen, ancho - margen, margen);
        g2.fillOval(ancho / 3, alto / 3, Math.max(4, ancho / 8), Math.max(4, alto / 8));
        g2.dispose();
        return new ImageIcon(img);
    }
}
