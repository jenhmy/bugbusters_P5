-- =========================================================
-- BUGBUSTERS · PRODUCTO 4
-- IMPLEMENTACIÓN MEDIANTE ORM
-- SCRIPT DE CREACIÓN DE BASE DE DATOS Y TABLAS
-- =========================================================
-- Este script crea la base de datos del Producto 4 y su
-- estructura principal en MySQL para garantizar el orden definido.
--
-- NOTA TÉCNICA: Aunque JPA (a través de persistence.xml con la propiedad 'hbm2ddl.auto') puede generar estas tablas
-- automáticamente, Hibernate tiende a crear las columnas en ORDEN ALFABÉTICO. Ejecutamos este script manualmente ANTES
-- de arrancar la aplicación para garantizar que los IDs sean la primera columna y la estructura sea visualmente lógica.
--
-- Tablas incluidas:
--   - Clientes (Mapeada por entidad Cliente.java)
--   - Articulos (Mapeada por entidad Articulo.java)
--   - Pedidos (Mapeada por entidad Pedido.java)
--
-- =========================================================

-- CREATE DATABASE IF NOT EXISTS producto4;
-- USE producto4;

-- Limpieza de seguridad por si se re-ejecuta el script
-- SET FOREIGN_KEY_CHECKS = 0;
-- DROP TABLE IF EXISTS Pedidos;
-- DROP TABLE IF EXISTS Articulos;
-- DROP TABLE IF EXISTS Clientes;
-- SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- TABLA CLIENTES
-- =========================================================
CREATE TABLE Clientes (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nombre VARCHAR(255),
    domicilio VARCHAR(255),
    nif VARCHAR(255) NOT NULL UNIQUE,
    tipo_cliente VARCHAR(31) NOT NULL
);

-- =========================================================
-- TABLA ARTICULOS
-- =========================================================
CREATE TABLE Articulos (
    id_articulo INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(255) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    precio_venta DECIMAL(10,2),
    gastos_envio DECIMAL(10,2),
    tiempo_preparacion INT,
    cantidad_disponible INT
);

-- =========================================================
-- TABLA PEDIDOS
-- =========================================================
CREATE TABLE Pedidos (
    id_pedido INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    id_articulo INT NOT NULL,
    cantidad INT NOT NULL,
    fecha_hora DATETIME(6),
    estado VARCHAR(255),
    CONSTRAINT fk_cliente FOREIGN KEY (id_cliente) REFERENCES Clientes(id_cliente),
    CONSTRAINT fk_articulo FOREIGN KEY (id_articulo) REFERENCES Articulos(id_articulo)
);

-- =========================================================
-- COMPROBACIONES OPCIONALES
-- =========================================================
-- SHOW TABLES;
-- DESCRIBE clientes;
-- DESCRIBE articulos;
-- DESCRIBE pedidos;