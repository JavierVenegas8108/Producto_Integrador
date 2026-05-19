package com.abarrotespro.vista.util;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.*;

/**
 * Zona visual de arrastre y soltado de imagenes, estilo repositorio GitHub.
 */
public class ZonaArrastreImagen extends JPanel {

    private static final int ALTO_ZONA = 140;

    private final String emojiRespaldo;
    private String rutaActual;
    private File archivoPendiente;
    private boolean arrastrando;
    private Consumer<File> alSeleccionarArchivo;

    public ZonaArrastreImagen(String rutaInicial, String emoji) {
        this.rutaActual = rutaInicial;
        this.emojiRespaldo = emoji != null ? emoji : "📦";
        setOpaque(false);
        setAlignmentX(Component.CENTER_ALIGNMENT);
        setPreferredSize(new Dimension(360, ALTO_ZONA));
        setMaximumSize(new Dimension(360, ALTO_ZONA));
        configurarArrastre();
    }

    public void alSeleccionarArchivo(Consumer<File> callback) {
        this.alSeleccionarArchivo = callback;
    }

    public void establecerArchivo(File archivo) {
        if (archivo == null || !GestorImagenProducto.esArchivoImagen(archivo)) {
            return;
        }
        this.archivoPendiente = archivo;
        this.rutaActual = archivo.getAbsolutePath();
        repaint();
        if (alSeleccionarArchivo != null) {
            alSeleccionarArchivo.accept(archivo);
        }
    }

    public String getRutaActual() {
        return rutaActual;
    }

    public File getArchivoPendiente() {
        return archivoPendiente;
    }

    public boolean tieneImagen() {
        return archivoPendiente != null
                || (rutaActual != null && !rutaActual.isBlank());
    }

    public void abrirExplorador(Window padre) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar imagen del producto");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imagenes (*.png, *.jpg, *.jpeg, *.gif, *.bmp)",
                "png", "jpg", "jpeg", "gif", "bmp"));
        if (chooser.showOpenDialog(padre) == JFileChooser.APPROVE_OPTION) {
            establecerArchivo(chooser.getSelectedFile());
        }
    }

    private void configurarArrastre() {
        new DropTarget(this, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                if (aceptaArrastre(dtde)) {
                    arrastrando = true;
                    repaint();
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                } else {
                    dtde.rejectDrag();
                }
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                arrastrando = false;
                repaint();
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                arrastrando = false;
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable transferable = dtde.getTransferable();
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        @SuppressWarnings("unchecked")
                        List<File> archivos = (List<File>) transferable.getTransferData(
                                DataFlavor.javaFileListFlavor);
                        if (!archivos.isEmpty()) {
                            establecerArchivo(archivos.get(0));
                        }
                    }
                    dtde.dropComplete(true);
                } catch (Exception ex) {
                    dtde.rejectDrop();
                }
                repaint();
            }
        });
    }

    private static boolean aceptaArrastre(DropTargetDragEvent dtde) {
        return dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fondo = arrastrando ? Colores.AZUL_CLARO : new Color(248, 250, 252);
        Color borde = arrastrando ? Colores.AZUL_PRIMARIO : Colores.GRIS_BORDE;

        g2.setColor(fondo);
        g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 12, 12));
        g2.setColor(borde);
        g2.setStroke(new BasicStroke(arrastrando ? 2f : 1f,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{8, 6}, 0));
        g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 12, 12));

        if (tieneImagen()) {
            pintarVistaPrevia(g2);
        } else {
            pintarInstrucciones(g2);
        }
        g2.dispose();
    }

    private void pintarVistaPrevia(Graphics2D g2) {
        int tam = 72;
        int x = (getWidth() - tam) / 2;
        int y = 16;
        ImageIcon icono = GestorImagenProducto.cargarMiniaturaOEmoji(
                rutaActual, emojiRespaldo, tam, tam);
        if (icono != null) {
            icono.paintIcon(this, g2, x, y);
        }
        g2.setColor(Colores.GRIS_TEXTO);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        String nombre = archivoPendiente != null
                ? archivoPendiente.getName()
                : "Imagen del producto";
        if (nombre.length() > 36) {
            nombre = nombre.substring(0, 33) + "...";
        }
        FontMetrics fm = g2.getFontMetrics();
        int tx = (getWidth() - fm.stringWidth(nombre)) / 2;
        g2.drawString(nombre, tx, y + tam + 18);
        g2.setColor(Colores.AZUL_PRIMARIO);
        g2.drawString("Arrastra otra imagen para reemplazar", tx - 24, y + tam + 34);
    }

    private void pintarInstrucciones(Graphics2D g2) {
        int cx = getWidth() / 2;

        g2.setColor(Colores.GRIS_TEXTO);
        g2.setStroke(new BasicStroke(1.5f));
        int ix = cx - 14;
        int iy = 28;
        g2.drawRoundRect(ix, iy, 28, 34, 4, 4);
        g2.drawLine(ix + 6, iy + 10, ix + 22, iy + 10);
        g2.drawLine(ix + 6, iy + 18, ix + 18, iy + 18);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.setColor(Colores.NEGRO_TEXTO);
        String linea1 = "Arrastra la imagen aqui para agregarla";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(linea1, cx - fm.stringWidth(linea1) / 2, 82);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2.setColor(Colores.AZUL_PRIMARIO);
        String linea2 = "O elige un archivo";
        g2.drawString(linea2, cx - fm.stringWidth(linea2) / 2, 104);
    }

    /** Enlaza el clic en "O elige un archivo" con el explorador. */
    public void enlazarExplorador(Window padre) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!tieneImagen() || e.getY() > getHeight() * 0.6) {
                    abrirExplorador(padre);
                }
            }
        });
    }
}
