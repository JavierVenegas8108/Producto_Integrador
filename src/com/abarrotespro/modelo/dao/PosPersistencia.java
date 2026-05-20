package com.abarrotespro.modelo.dao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.abarrotespro.modelo.Corte;
import com.abarrotespro.modelo.LineaVenta;
import com.abarrotespro.modelo.Producto;
import com.abarrotespro.modelo.Proveedor;
import com.abarrotespro.modelo.RegistroSurtido;
import com.abarrotespro.modelo.SistemaPos;
import com.abarrotespro.modelo.Venta;
import com.abarrotespro.modelo.dto.EstadoPersistido;

/**
 * DAO de persistencia en archivos locales (carpeta data/).
 */
public class PosPersistencia {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String SEP = "\t";

    private final Path directorioDatos;

    public PosPersistencia() {
        this.directorioDatos = Paths.get(System.getProperty("user.dir"), "data");
    }

    public boolean existenDatos() {
        return Files.isRegularFile(directorioDatos.resolve("productos.tsv"));
    }

    public void limpiarDatosPersistidos() {
        try {
            Files.createDirectories(directorioDatos);
            String[] archivos = {
                    "productos.tsv", "proveedores.tsv", "ventas.tsv", "lineas_venta.tsv",
                    "cortes.tsv", "surtidos.tsv", "estado.properties"
            };
            for (String nombre : archivos) {
                Files.deleteIfExists(directorioDatos.resolve(nombre));
            }
        } catch (IOException e) {
            System.err.println("Error al limpiar datos persistidos: " + e.getMessage());
        }
    }

    public void guardar(SistemaPos sistema) {
        try {
            Files.createDirectories(directorioDatos);
            guardarProductos(sistema.getProductos());
            guardarProveedores(sistema.getProveedores());
            guardarVentas(sistema.getVentasDelDia());
            guardarCortes(sistema.getHistorialCortes());
            guardarHistorialSurtidos(sistema.getHistorialSurtidos());
            guardarEstado(sistema.exportarEstado());
        } catch (IOException e) {
            System.err.println("Error al guardar datos: " + e.getMessage());
        }
    }

    public void cargar(SistemaPos sistema) throws IOException {
        List<Producto> productos = cargarProductos();
        List<Proveedor> proveedores = cargarProveedores();
        List<Venta> ventas = cargarVentas(productos);
        List<Corte> cortes = cargarCortes();
        Properties props = new Properties();
        Path estadoFile = directorioDatos.resolve("estado.properties");
        if (Files.exists(estadoFile)) {
            try (var in = Files.newInputStream(estadoFile)) {
                props.load(in);
            }
        }
        double totalCaja = Double.parseDouble(props.getProperty("totalEnCaja", "0"));
        double entradas = Double.parseDouble(props.getProperty("entradasManuales", "0"));
        int contVentas = Integer.parseInt(props.getProperty("contadorVentas", "0"));
        int contProd = Integer.parseInt(props.getProperty("contadorProductos", "0"));
        int contCortes = Integer.parseInt(props.getProperty("contadorCortes", "0"));
        int contProv = Integer.parseInt(props.getProperty("contadorProveedores", "0"));
        double fondoInicial = Double.parseDouble(props.getProperty("fondoInicial", String.valueOf(totalCaja)));
        double ingresosEfectivo = Double.parseDouble(props.getProperty("ingresosEfectivo", "0"));
        double ingresosTarjeta = Double.parseDouble(props.getProperty("ingresosTarjeta", "0"));
        double ingresosTransferencia = Double.parseDouble(props.getProperty("ingresosTransferencia", "0"));
        double egresosEfectivo = Double.parseDouble(props.getProperty("egresosEfectivo", "0"));
        boolean cajaAbierta = Boolean.parseBoolean(props.getProperty("cajaAbierta", "true"));

        EstadoPersistido estado = new EstadoPersistido(
                productos, ventas, cortes, proveedores, totalCaja, entradas,
                contVentas, contProd, contCortes, contProv,
                fondoInicial, ingresosEfectivo, ingresosTarjeta, ingresosTransferencia,
                egresosEfectivo, cajaAbierta);
        sistema.cargarEstado(estado);
        sistema.cargarHistorialSurtidos(cargarHistorialSurtidos());
    }

    private void guardarEstado(EstadoPersistido estado) throws IOException {
        Properties props = new Properties();
        props.setProperty("totalEnCaja", String.valueOf(estado.getTotalEnCaja()));
        props.setProperty("entradasManuales", String.valueOf(estado.getEntradasManuales()));
        props.setProperty("contadorVentas", String.valueOf(estado.getContadorVentas()));
        props.setProperty("contadorProductos", String.valueOf(estado.getContadorProductos()));
        props.setProperty("contadorCortes", String.valueOf(estado.getContadorCortes()));
        props.setProperty("contadorProveedores", String.valueOf(estado.getContadorProveedores()));
        props.setProperty("fondoInicial", String.valueOf(estado.getFondoInicial()));
        props.setProperty("ingresosEfectivo", String.valueOf(estado.getIngresosEfectivo()));
        props.setProperty("ingresosTarjeta", String.valueOf(estado.getIngresosTarjeta()));
        props.setProperty("ingresosTransferencia", String.valueOf(estado.getIngresosTransferencia()));
        props.setProperty("egresosEfectivo", String.valueOf(estado.getEgresosEfectivo()));
        props.setProperty("cajaAbierta", String.valueOf(estado.isCajaAbierta()));
        Path archivo = directorioDatos.resolve("estado.properties");
        try (var out = Files.newOutputStream(archivo)) {
            props.store(out, "Estado del punto de venta Abarrotes Pro");
        }
    }

    private void guardarProductos(List<Producto> productos) throws IOException {
        List<String> lineas = new ArrayList<>();
        for (Producto p : productos) {
            lineas.add(String.join(SEP,
                    String.valueOf(p.getId()),
                    escapar(p.getNombre()),
                    String.valueOf(p.getPrecioCompra()),
                    String.valueOf(p.getPrecioVenta()),
                    String.valueOf(p.getStock()),
                    escapar(p.getCategoria()),
                    escapar(p.getEmoji()),
                    String.valueOf(p.getStockMinimo()),
                    escapar(p.getRutaImagen() != null ? p.getRutaImagen() : ""),
                    String.valueOf(p.getIdProveedor())));
        }
        Files.write(directorioDatos.resolve("productos.tsv"), lineas, StandardCharsets.UTF_8);
    }

    private List<Producto> cargarProductos() throws IOException {
        Path archivo = directorioDatos.resolve("productos.tsv");
        if (!Files.exists(archivo)) {
            return new ArrayList<>();
        }
        List<Producto> lista = new ArrayList<>();
        for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
            if (linea.isBlank()) {
                continue;
            }
            String[] p = linea.split(SEP, -1);
            if (p.length < 7) {
                continue;
            }
            Producto producto;
            if (p.length >= 10) {
                producto = new Producto(
                        Integer.parseInt(p[0]),
                        desescapar(p[1]),
                        Double.parseDouble(p[2]),
                        Double.parseDouble(p[3]),
                        Integer.parseInt(p[4]),
                        desescapar(p[5]),
                        desescapar(p[6]),
                        Integer.parseInt(p[7]),
                        Integer.parseInt(p[9]),
                        desescapar(p[8]));
            } else {
                double precio = Double.parseDouble(p[2]);
                producto = new Producto(
                        Integer.parseInt(p[0]),
                        desescapar(p[1]),
                        precio,
                        precio,
                        Integer.parseInt(p[3]),
                        desescapar(p[4]),
                        desescapar(p[5]),
                        Integer.parseInt(p[6]),
                        0,
                        p.length >= 8 ? desescapar(p[7]) : null);
            }
            lista.add(producto);
        }
        return lista;
    }

    private void guardarHistorialSurtidos(List<RegistroSurtido> registros) throws IOException {
        List<String> lineas = new ArrayList<>();
        for (RegistroSurtido r : registros) {
            lineas.add(String.join(SEP,
                    r.getFechaHora().format(ISO),
                    String.valueOf(r.getProductoId()),
                    escapar(r.getNombreProducto()),
                    String.valueOf(r.getCantidad()),
                    String.valueOf(r.getPrecioCompra()),
                    String.valueOf(r.getProveedorId()),
                    escapar(r.getNombreProveedor())));
        }
        Files.write(directorioDatos.resolve("surtidos.tsv"), lineas, StandardCharsets.UTF_8);
    }

    private List<RegistroSurtido> cargarHistorialSurtidos() throws IOException {
        Path archivo = directorioDatos.resolve("surtidos.tsv");
        if (!Files.exists(archivo)) {
            return new ArrayList<>();
        }
        List<RegistroSurtido> lista = new ArrayList<>();
        for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
            if (linea.isBlank()) {
                continue;
            }
            String[] p = linea.split(SEP, -1);
            if (p.length < 6) {
                continue;
            }
            lista.add(new RegistroSurtido(
                    LocalDateTime.parse(p[0], ISO),
                    Integer.parseInt(p[1]),
                    desescapar(p[2]),
                    Integer.parseInt(p[3]),
                    Double.parseDouble(p[4]),
                    Integer.parseInt(p[5]),
                    p.length >= 7 ? desescapar(p[6]) : "—"));
        }
        return lista;
    }

    private void guardarProveedores(List<Proveedor> proveedores) throws IOException {
        List<String> lineas = new ArrayList<>();
        for (Proveedor pr : proveedores) {
            lineas.add(String.join(SEP,
                    String.valueOf(pr.getId()),
                    escapar(pr.getRazonSocial()),
                    escapar(pr.getNombreContacto()),
                    escapar(pr.getTelefono()),
                    escapar(pr.getCorreo()),
                    escapar(pr.getDireccion()),
                    escapar(pr.getDiasVisita()),
                    String.valueOf(pr.isActivo())));
        }
        Files.write(directorioDatos.resolve("proveedores.tsv"), lineas, StandardCharsets.UTF_8);
    }

    private List<Proveedor> cargarProveedores() throws IOException {
        Path archivo = directorioDatos.resolve("proveedores.tsv");
        if (!Files.exists(archivo)) {
            return new ArrayList<>();
        }
        List<Proveedor> lista = new ArrayList<>();
        for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
            if (linea.isBlank()) {
                continue;
            }
            String[] p = linea.split(SEP, -1);
            if (p.length < 8) {
                continue;
            }
            lista.add(new Proveedor(
                    Integer.parseInt(p[0]),
                    desescapar(p[1]),
                    desescapar(p[2]),
                    desescapar(p[3]),
                    desescapar(p[4]),
                    desescapar(p[5]),
                    desescapar(p[6]),
                    Boolean.parseBoolean(p[7])));
        }
        return lista;
    }

    private void guardarVentas(List<Venta> ventas) throws IOException {
        List<String> lineasVentas = new ArrayList<>();
        List<String> lineasDetalle = new ArrayList<>();
        for (Venta v : ventas) {
            if (!v.isCerrada()) {
                continue;
            }
            lineasVentas.add(String.join(SEP,
                    String.valueOf(v.getId()),
                    v.getFechaHora().format(ISO),
                    escapar(v.getUsuarioNombre()),
                    String.valueOf(v.getTotal())));
            for (LineaVenta linea : v.getLineas()) {
                Producto prod = linea.getProducto();
                lineasDetalle.add(String.join(SEP,
                        String.valueOf(v.getId()),
                        String.valueOf(prod.getId()),
                        escapar(prod.getNombre()),
                        String.valueOf(linea.getCantidad()),
                        String.valueOf(linea.getPrecioVentaUnitario()),
                        String.valueOf(linea.getPrecioCompraUnitario())));
            }
        }
        Files.write(directorioDatos.resolve("ventas.tsv"), lineasVentas, StandardCharsets.UTF_8);
        Files.write(directorioDatos.resolve("lineas_venta.tsv"), lineasDetalle, StandardCharsets.UTF_8);
    }

    private List<Venta> cargarVentas(List<Producto> productos) throws IOException {
        Path archivoVentas = directorioDatos.resolve("ventas.tsv");
        Path archivoLineas = directorioDatos.resolve("lineas_venta.tsv");
        if (!Files.exists(archivoVentas)) {
            return new ArrayList<>();
        }

        List<String[]> detalles = new ArrayList<>();
        if (Files.exists(archivoLineas)) {
            for (String linea : Files.readAllLines(archivoLineas, StandardCharsets.UTF_8)) {
                if (!linea.isBlank()) {
                    detalles.add(linea.split(SEP, -1));
                }
            }
        }

        List<Venta> ventas = new ArrayList<>();
        for (String linea : Files.readAllLines(archivoVentas, StandardCharsets.UTF_8)) {
            if (linea.isBlank()) {
                continue;
            }
            String[] p = linea.split(SEP, -1);
            int ventaId = Integer.parseInt(p[0]);
            Venta venta = new Venta(ventaId, desescapar(p[2]));
            venta.setFechaHora(LocalDateTime.parse(p[1], ISO));
            venta.setCerrada(true);

            for (String[] d : detalles) {
                if (d.length < 5 || Integer.parseInt(d[0]) != ventaId) {
                    continue;
                }
                int prodId = Integer.parseInt(d[1]);
                double precioVenta = Double.parseDouble(d[4]);
                double precioCompra = d.length >= 6 ? Double.parseDouble(d[5]) : precioVenta;
                Producto producto = productos.stream()
                        .filter(pr -> pr.getId() == prodId)
                        .findFirst()
                        .orElseGet(() -> new Producto(prodId, desescapar(d[2]),
                                precioCompra, precioVenta, 0, "General", "📦", 0, 0, null));
                int cantidad = Integer.parseInt(d[3]);
                venta.agregarLinea(new LineaVenta(producto, cantidad, precioVenta, precioCompra));
            }
            ventas.add(venta);
        }
        return ventas;
    }

    private void guardarCortes(List<Corte> cortes) throws IOException {
        List<String> lineas = new ArrayList<>();
        for (Corte c : cortes) {
            lineas.add(String.join(SEP,
                    String.valueOf(c.getMonto()),
                    c.getFechaHora().format(ISO),
                    escapar(c.getUsuarioNombre())));
        }
        Files.write(directorioDatos.resolve("cortes.tsv"), lineas, StandardCharsets.UTF_8);
    }

    private List<Corte> cargarCortes() throws IOException {
        Path archivo = directorioDatos.resolve("cortes.tsv");
        if (!Files.exists(archivo)) {
            return new ArrayList<>();
        }
        List<Corte> lista = new ArrayList<>();
        for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
            if (linea.isBlank()) {
                continue;
            }
            String[] p = linea.split(SEP, -1);
            lista.add(new Corte(
                    Double.parseDouble(p[0]),
                    desescapar(p[2]),
                    LocalDateTime.parse(p[1], ISO)));
        }
        return lista;
    }

    private static String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n");
    }

    private static String desescapar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("\\t", "\t").replace("\\n", "\n").replace("\\\\", "\\");
    }
}
