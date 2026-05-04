-- =========================================================
-- BUGBUSTERS · PRODUCTO 4
-- IMPLEMENTACIÓN MEDIANTE ORM
-- DATOS DE PRUEBA
-- =========================================================
-- Este script inserta datos de prueba iniciales para el
-- Producto 4, manteniendo la misma base funcional del
-- Producto 3 como punto de partida.
--
-- =========================================================

USE producto4;

-- =========================================================
-- CLIENTES
-- =========================================================
INSERT INTO Clientes (email, nombre, domicilio, nif, tipo_cliente) VALUES
('maria.garcia@bugbusters.com', 'María García López', 'Calle Alcalá 45, Madrid', '12345678A', 'Premium'),
('juan.perez@bugbusters.com', 'Juan Pérez Martínez', 'Avenida Diagonal 320, Barcelona', '23456789B', 'Estandar'),
('laura.sanchez@bugbusters.com', 'Laura Sánchez Ruiz', 'Calle Gran Vía 12, Madrid', '34567890C', 'Premium'),
('carlos.lopez@bugbusters.com', 'Carlos López Fernández', 'Calle Colón 8, Valencia', '45678901D', 'Estandar'),
('ana.martin@bugbusters.com', 'Ana Martín Gómez', 'Paseo de la Castellana 150, Madrid', '56789012E', 'Premium'),
('david.rodriguez@bugbusters.com', 'David Rodríguez Torres', 'Calle Larios 22, Málaga', '67890123F', 'Estandar');

-- =========================================================
-- ARTICULOS
-- Stock inicial antes de descontar los pedidos históricos
-- =========================================================
INSERT INTO Articulos (
    codigo,
    descripcion,
    precio_venta,
    gastos_envio,
    tiempo_preparacion,
    cantidad_disponible
) VALUES
('A001', 'Monitor Samsung 27" QHD', 249.99, 9.50, 10, 12),
('A002', 'Teclado Logitech K120 USB', 19.99, 4.99, 20, 30),
('A003', 'Ratón Logitech G502 Gaming', 59.99, 5.99, 10, 20),
('A004', 'Portátil Dell 15 5520', 799.99, 12.50, 30, 8),
('A005', 'Auriculares Sony WH-1000XM5', 349.99, 6.99, 10, 15),
('A006', 'Disco SSD Samsung 1TB', 109.99, 5.50, 20, 25);

-- =========================================================
-- PEDIDOS
-- Se insertan mediante procedimiento para que el stock
-- se descuente automáticamente y quede coherente
-- =========================================================
INSERT INTO Pedidos (id_cliente, id_articulo, cantidad, fecha_hora, estado) VALUES
(1, 1, 2, '2026-03-20 12:34:25', 'ENVIADO'),
(2, 3, 1, '2026-03-21 09:15:10', 'ENVIADO'),
(3, 2, 1, '2026-03-21 18:42:03', 'ENVIADO'),
(1, 4, 1, '2026-03-22 11:05:47', 'ENVIADO'),
(4, 5, 2, '2026-03-22 16:20:30', 'ENVIADO'),
(2, 6, 1, '2026-03-23 10:10:10', 'ENVIADO');

-- =========================================================
-- CONSULTAS DE COMPROBACION OPCIONALES
-- =========================================================
-- SELECT * FROM Clientes;
-- SELECT * FROM Articulos;
-- SELECT * FROM Pedidos;

-- =========================================================
-- LIMPIEZA TOTAL OPCIONAL
-- Quitar '--' solo si quieres vaciar completamente los datos
-- =========================================================
-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE Pedidos;
-- TRUNCATE TABLE Articulos;
-- TRUNCATE TABLE Clientes;
-- SET FOREIGN_KEY_CHECKS = 1;