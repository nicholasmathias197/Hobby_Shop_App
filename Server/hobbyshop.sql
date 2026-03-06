-- Create database
CREATE DATABASE IF NOT EXISTS hobby_shop_db;
USE hobby_shop_db;

-- =============================================
-- USERS AND AUTHENTICATION TABLES
-- =============================================

-- Roles table
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Customers/Users table
CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    city VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(50) DEFAULT 'USA',
    enabled BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- User roles junction table
CREATE TABLE customer_roles (
    customer_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (customer_id, role_id),
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- =============================================
-- PRODUCT CATALOG TABLES
-- =============================================

-- Categories table
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    image_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Brands table
CREATE TABLE brands (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    logo_url VARCHAR(255),
    website VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    category_id BIGINT,
    brand_id BIGINT,
    scale VARCHAR(20), -- For Gundam models: "1/144", "1/100", etc.
    color_code VARCHAR(50), -- For paints: "TS-21", "X-1", etc.
    paint_type VARCHAR(50), -- "Acrylic", "Enamel", "Lacquer"
    tool_type VARCHAR(50), -- "Cutter", "Brush", "Airbrush", etc.
    image_url VARCHAR(255),
    is_featured BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (brand_id) REFERENCES brands(id) ON DELETE SET NULL
);

-- Product images gallery
CREATE TABLE product_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Product reviews
CREATE TABLE product_reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    is_verified_purchase BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- =============================================
-- SHOPPING CART TABLES
-- =============================================

-- Shopping cart
CREATE TABLE carts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT UNIQUE,
    session_id VARCHAR(100), -- For guest users
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- Cart items
CREATE TABLE cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY unique_cart_product (cart_id, product_id)
);

-- =============================================
-- ORDER MANAGEMENT TABLES
-- =============================================

-- Orders
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT,
    guest_email VARCHAR(100),
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    payment_status VARCHAR(50) DEFAULT 'PENDING',
    subtotal DECIMAL(10, 2) NOT NULL,
    tax DECIMAL(10, 2) DEFAULT 0.00,
    shipping_cost DECIMAL(10, 2) DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL,
    shipping_address TEXT NOT NULL,
    shipping_city VARCHAR(100) NOT NULL,
    shipping_postal_code VARCHAR(20) NOT NULL,
    shipping_country VARCHAR(50) NOT NULL,
    tracking_number VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL
);

-- Order items
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT,
    product_name VARCHAR(255) NOT NULL,
    product_sku VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    price_per_unit DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
);

-- Order status history
CREATE TABLE order_status_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    comment TEXT,
    changed_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- =============================================
-- INSERT INITIAL DATA
-- =============================================

-- Insert roles
INSERT INTO roles (name) VALUES
('ROLE_USER'),
('ROLE_ADMIN');

-- Insert base user and admin (passwords should be BCrypt encoded in real application)
-- Password for user: 'password123' (BCrypt encoded)
INSERT INTO customers (email, password, first_name, last_name, phone, address, city, postal_code, email_verified) VALUES
('user@example.com', '$2a$10$YourHashedPasswordHere', 'John', 'Doe', '555-0101', '123 Main St', 'Anytown', '12345', TRUE),
('admin@hobbyshop.com', '$2a$10$YourHashedPasswordHere', 'Admin', 'User', '555-0102', '456 Admin Ave', 'Admin City', '67890', TRUE);

-- Assign roles to users
INSERT INTO customer_roles (customer_id, role_id) VALUES
(1, 1), -- user@example.com gets ROLE_USER
(2, 1), -- admin gets ROLE_USER
(2, 2); -- admin gets ROLE_ADMIN

-- Insert categories
INSERT INTO categories (name, description) VALUES
('Gundam Models', 'Plastic model kits from the Gundam universe'),
('Paints', 'Acrylic, enamel, and lacquer paints for models'),
('Tools', 'Hobby tools for building and painting models'),
('Accessories', 'Decals, topcoats, and other modeling accessories'),
('Airbrush Supplies', 'Airbrushes and related equipment');

-- Insert brands
INSERT INTO brands (name, description, website) VALUES
('Bandai', 'Manufacturer of Gundam model kits', 'https://www.bandai.com'),
('Tamiya', 'Japanese manufacturer of plastic model kits and paints', 'https://www.tamiya.com'),
('Mr. Color', 'Professional grade paints by GSI Creos', 'https://www.mr-hobby.com'),
('GodHand', 'Premium quality hobby tools', 'https://godhand-tools.com'),
('Testors', 'Model paints and supplies', 'https://www.testors.com');

-- Insert sample products (Gundam Models)
INSERT INTO products (sku, name, description, price, stock_quantity, category_id, brand_id, scale, is_featured) VALUES
('BAN-RG-001', 'RG 1/144 RX-78-2 Gundam', 'Real Grade version of the iconic RX-78-2 Gundam', 29.99, 15, 1, 1, '1/144', TRUE),
('BAN-MG-001', 'MG 1/100 Freedom Gundam 2.0', 'Master Grade Freedom Gundam Ver. 2.0', 54.99, 8, 1, 1, '1/100', TRUE),
('BAN-PG-001', 'PG 1/60 Unleashed RX-78-2', 'Perfect Grade Unleashed RX-78-2 Gundam', 299.99, 3, 1, 1, '1/60', TRUE),
('BAN-HG-001', 'HG 1/144 Wing Gundam', 'High Grade Wing Gundam TV version', 18.99, 25, 1, 1, '1/144', FALSE);

-- Insert sample products (Tamiya Paints)
INSERT INTO products (sku, name, description, price, stock_quantity, category_id, brand_id, color_code, paint_type, is_featured) VALUES
('TAM-XF-1', 'Tamiya XF-1 Flat Black', 'Acrylic paint - Flat Black 10ml', 3.99, 50, 2, 2, 'XF-1', 'Acrylic', TRUE),
('TAM-XF-2', 'Tamiya XF-2 Flat White', 'Acrylic paint - Flat White 10ml', 3.99, 45, 2, 2, 'XF-2', 'Acrylic', FALSE),
('TAM-X-7', 'Tamiya X-7 Red', 'Gloss acrylic paint - Red 10ml', 3.99, 30, 2, 2, 'X-7', 'Acrylic', FALSE),
('TAM-TS-21', 'Tamiya TS-21 Gold', 'Spray paint - Gold', 8.99, 20, 2, 2, 'TS-21', 'Lacquer', TRUE);

-- Insert sample products (Mr. Color Paints)
INSERT INTO products (sku, name, description, price, stock_quantity, category_id, brand_id, color_code, paint_type) VALUES
('MRC-C-001', 'Mr. Color C1 White', 'Lacquer paint - White', 4.50, 40, 2, 3, 'C1', 'Lacquer'),
('MRC-C-002', 'Mr. Color C2 Black', 'Lacquer paint - Black', 4.50, 40, 2, 3, 'C2', 'Lacquer'),
('MRC-C-058', 'Mr. Color C58 Orange Yellow', 'Lacquer paint - Orange Yellow', 4.50, 25, 2, 3, 'C58', 'Lacquer'),
('MRC-C-068', 'Mr. Color C68 Red Madder', 'Lacquer paint - Red Madder', 4.50, 25, 2, 3, 'C68', 'Lacquer');

-- Insert sample products (Hobby Tools)
INSERT INTO products (sku, name, description, price, stock_quantity, category_id, brand_id, tool_type, is_featured) VALUES
('GDH-PN-120', 'GodHand Ultimate Nipper', 'Premium side cutters for plastic models', 69.99, 10, 3, 4, 'Cutter', TRUE),
('TAM-TR-001', 'Tamiya Craft Glue Set', 'Basic modeling glue set', 12.99, 30, 3, 2, 'Adhesive', FALSE),
('TAM-FL-001', 'Tamiya File Set', 'Set of 3 precision files', 9.99, 25, 3, 2, 'File', FALSE),
('MRC-CL-001', 'Mr. Hobby Procon Airbrush', 'Double action airbrush', 89.99, 5, 5, 3, 'Airbrush', TRUE);

-- Insert sample product reviews
INSERT INTO product_reviews (product_id, customer_id, rating, comment, is_verified_purchase) VALUES
(1, 1, 5, 'Amazing kit! Great detail and articulation.', TRUE),
(5, 1, 4, 'Good paint, goes on smooth.', TRUE),
(13, 2, 5, 'Best nippers I''ve ever used!', TRUE);

-- Create indexes for better performance
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_brand ON products(brand_id);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_order_number ON orders(order_number);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX idx_product_reviews_product ON product_reviews(product_id);
CREATE INDEX idx_product_reviews_customer ON product_reviews(customer_id);