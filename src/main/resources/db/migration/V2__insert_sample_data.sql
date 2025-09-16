-- Productos de ejemplo
INSERT INTO products (name, description) VALUES
('Producto A', 'Descripción del producto A'),
('Producto B', 'Descripción del producto B'),
('Producto C', 'Descripción del producto C'),
('Producto D', 'Descripción del producto D'),
('Producto E', 'Descripción del producto E');

-- ============================================
-- Precios de Producto A (historial completo)
-- ============================================
INSERT INTO prices (product_id, value, init_date, end_date) VALUES
(1, 100.00, '2025-01-01', '2025-03-31'),
(1, 110.00, '2025-04-01', '2025-06-30'),
(1, 120.00, '2025-07-01', NULL); -- precio vigente

-- ============================================
-- Precios de Producto B (precios consecutivos y cortos)
-- ============================================
INSERT INTO prices (product_id, value, init_date, end_date) VALUES
(2, 50.00, '2025-01-01', '2025-01-31'),
(2, 55.00, '2025-02-01', '2025-02-28'),
(2, 60.00, '2025-03-01', NULL); -- precio vigente

-- ============================================
-- Precios de Producto C (único precio vigente)
-- ============================================
INSERT INTO prices (product_id, value, init_date, end_date) VALUES
(3, 200.00, '2025-01-01', NULL);

-- ============================================
-- Precios de Producto D (precios históricos intercalados)
-- ============================================
INSERT INTO prices (product_id, value, init_date, end_date) VALUES
(4, 75.00, '2024-11-01', '2024-12-31'),
(4, 80.00, '2025-01-01', '2025-03-31'),
(4, 85.00, '2025-04-01', '2025-06-30'),
(4, 90.00, '2025-07-01', NULL); -- precio vigente

-- ============================================
-- Precios de Producto E (precios de prueba con gaps)
-- ============================================
INSERT INTO prices (product_id, value, init_date, end_date) VALUES
(5, 300.00, '2025-01-01', '2025-01-31'),
(5, 320.00, '2025-03-01', '2025-03-31'), -- hay un gap en febrero
(5, 350.00, '2025-04-01', NULL); -- precio vigente

-- ============================================
-- Casos de prueba variados
-- - Precios vigentes (end_date IS NULL)
-- - Precios históricos
-- - Precios consecutivos
-- - Precios con gaps
-- - Precios con rangos largos y cortos
-- ============================================