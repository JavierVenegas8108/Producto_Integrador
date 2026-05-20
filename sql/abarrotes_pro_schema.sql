-- Esquema MySQL para Abarrotes Pro
CREATE DATABASE IF NOT EXISTS abarrotes_pro
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE abarrotes_pro;

CREATE TABLE IF NOT EXISTS productos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo_barras VARCHAR(32) UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    precio_venta DECIMAL(10,2) NOT NULL DEFAULT 0,
    stock_actual INT NOT NULL DEFAULT 0,
    stock_minimo INT NOT NULL DEFAULT 0,
    imagen VARCHAR(255) NULL COMMENT 'Ruta classpath ej. /img/Coca_300.jpg'
);

CREATE TABLE IF NOT EXISTS cajas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fondo_inicial DECIMAL(12,2) NOT NULL DEFAULT 0,
    ingresos DECIMAL(12,2) NOT NULL DEFAULT 0,
    egresos DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_caja DECIMAL(12,2) NOT NULL DEFAULT 0,
    estado TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1=abierta, 0=cerrada'
);

CREATE TABLE IF NOT EXISTS movimientos_caja (
    id INT AUTO_INCREMENT PRIMARY KEY,
    caja_id INT NOT NULL,
    tipo ENUM('INGRESO','EGRESO') NOT NULL,
    monto DECIMAL(12,2) NOT NULL,
    concepto VARCHAR(200),
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (caja_id) REFERENCES cajas(id)
);

CREATE TABLE IF NOT EXISTS ventas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    caja_id INT NOT NULL,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(12,2) NOT NULL,
    metodo_pago ENUM('EFECTIVO','TARJETA','TRANSFERENCIA') NOT NULL,
    monto_recibido DECIMAL(12,2) NOT NULL DEFAULT 0,
    cambio DECIMAL(12,2) NOT NULL DEFAULT 0,
    FOREIGN KEY (caja_id) REFERENCES cajas(id)
);

CREATE TABLE IF NOT EXISTS detalle_ventas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    venta_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (venta_id) REFERENCES ventas(id),
    FOREIGN KEY (producto_id) REFERENCES productos(id)
);

-- Caja inicial de ejemplo
INSERT INTO cajas (fondo_inicial, ingresos, egresos, total_caja, estado)
SELECT 500, 0, 0, 500, 1
WHERE NOT EXISTS (SELECT 1 FROM cajas);
