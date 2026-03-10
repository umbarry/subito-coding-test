-- V1.0__Initial_Schema.sql
-- Initial database schema for Subito Coding Test

-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    net_price NUMERIC(19, 2) NOT NULL,
    vat_percentage NUMERIC(19, 2) NOT NULL,
    available_items INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    version BIGINT,
    CONSTRAINT check_net_price CHECK (net_price >= 0),
    CONSTRAINT check_vat_percentage CHECK (vat_percentage >= 0),
    CONSTRAINT check_available_items CHECK (available_items >= 0)
);

CREATE INDEX idx_products_name ON products(name);

-- Create baskets table
CREATE TABLE IF NOT EXISTS baskets (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    version BIGINT
);

CREATE INDEX idx_baskets_status ON baskets(status);

-- Create orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    basket_id BIGINT,
    status VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    postal_code VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL,
    tracking_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    version BIGINT,
    CONSTRAINT fk_orders_basket FOREIGN KEY (basket_id) REFERENCES baskets(id),
    CONSTRAINT uq_orders_basket UNIQUE (basket_id)
);

CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_email ON orders(email);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_tracking_url ON orders(tracking_url);

-- Create order_items table
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    unit_net_price NUMERIC(19, 2) NOT NULL,
    price NUMERIC(19, 2) NOT NULL,
    vat NUMERIC(19, 2) NOT NULL,
    version BIGINT,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT check_quantity CHECK (quantity > 0),
    CONSTRAINT check_unit_net_price CHECK (unit_net_price >= 0),
    CONSTRAINT check_price CHECK (price >= 0),
    CONSTRAINT check_vat CHECK (vat >= 0)
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- Create basket_items table
CREATE TABLE IF NOT EXISTS basket_items (
    id BIGSERIAL PRIMARY KEY,
    basket_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    price DECIMAL(12,2) NOT NULL CHECK (price >= 0),
    vat DECIMAL(12,2) NOT NULL CHECK (vat >= 0),
    created_at TIMESTAMP NOT NULL,
    version BIGINT,
    CONSTRAINT fk_basket_items_basket FOREIGN KEY (basket_id) REFERENCES baskets(id) ON DELETE CASCADE,
    CONSTRAINT fk_basket_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT check_basket_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_basket_items_basket_id ON basket_items(basket_id);
CREATE INDEX idx_basket_items_product_id ON basket_items(product_id);

-- Create payments table
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(255) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    version BIGINT,
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT uq_payments_payment_id UNIQUE (payment_id)
);

CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_payment_id ON payments(payment_id);
CREATE INDEX idx_payments_status ON payments(status);

-- Create notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    sent BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    version BIGINT,
    CONSTRAINT fk_notifications_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT uk_order_notification_type UNIQUE (order_id, type)
);

CREATE INDEX idx_notifications_order_id ON notifications(order_id);
CREATE INDEX idx_notifications_order_type ON notifications(order_id, type);
CREATE INDEX idx_notifications_sent ON notifications(sent);

-- Create flyway_schema_history table (managed by Flyway)
-- This is created automatically by Flyway

