-- Tabla de productos
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de precios
CREATE TABLE prices (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    value NUMERIC(10,2) NOT NULL,
    init_date DATE NOT NULL,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT chk_date_valid CHECK (end_date IS NULL OR init_date < end_date)
);

-- Índices para mejorar las búsquedas por producto y fecha
CREATE INDEX idx_prices_product ON prices(product_id);
CREATE INDEX idx_prices_date ON prices(product_id, init_date, end_date);
