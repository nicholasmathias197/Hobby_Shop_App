-- Create database
CREATE DATABASE IF NOT EXISTS hobby_shop_db;
USE hobby_shop_db;

-- =============================================
-- USERS AND AUTHENTICATION TABLES
-- =============================================

-- Roles table
CREATE TABLE roles
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Customers/Users table
CREATE TABLE customers
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    email          VARCHAR(100) NOT NULL UNIQUE,
    password       VARCHAR(255) NOT NULL,
    first_name     VARCHAR(50)  NOT NULL,
    last_name      VARCHAR(50)  NOT NULL,
    phone          VARCHAR(20),
    address        TEXT,
    city           VARCHAR(100),
    postal_code    VARCHAR(20),
    country        VARCHAR(50) DEFAULT 'USA',
    enabled        BOOLEAN     DEFAULT TRUE,
    email_verified BOOLEAN     DEFAULT FALSE,
    created_at     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- User roles junction table
CREATE TABLE customer_roles
(
    customer_id BIGINT,
    role_id     BIGINT,
    PRIMARY KEY (customer_id, role_id),
    FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- =============================================
-- PRODUCT CATALOG TABLES
-- =============================================

-- Categories table
CREATE TABLE categories
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    image_url   VARCHAR(255),
    is_active   BOOLEAN   DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Brands table
CREATE TABLE brands
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    logo_url    VARCHAR(255),
    website     VARCHAR(255),
    is_active   BOOLEAN   DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE products
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    sku            VARCHAR(50)    NOT NULL UNIQUE,
    name           VARCHAR(255)   NOT NULL,
    description    TEXT,
    price          DECIMAL(10, 2) NOT NULL,
    stock_quantity INT            NOT NULL DEFAULT 0,
    category_id    BIGINT,
    brand_id       BIGINT,
    scale          VARCHAR(20), -- For Gundam models: "1/144", "1/100", etc.
    color_code     VARCHAR(50), -- For paints: "TS-21", "X-1", etc.
    paint_type     VARCHAR(50), -- "Acrylic", "Enamel", "Lacquer"
    tool_type      VARCHAR(50), -- "Cutter", "Brush", "Airbrush", etc.
    image_url      VARCHAR(255),
    is_featured    BOOLEAN                 DEFAULT FALSE,
    is_active      BOOLEAN                 DEFAULT TRUE,
    created_at     TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL,
    FOREIGN KEY (brand_id) REFERENCES brands (id) ON DELETE SET NULL
);

-- Product images gallery
CREATE TABLE product_images
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id    BIGINT       NOT NULL,
    image_url     VARCHAR(255) NOT NULL,
    is_primary    BOOLEAN   DEFAULT FALSE,
    display_order INT       DEFAULT 0,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);

-- Product reviews
CREATE TABLE product_reviews
(
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id           BIGINT NOT NULL,
    customer_id          BIGINT NOT NULL,
    rating               INT    NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment              TEXT,
    is_verified_purchase BOOLEAN   DEFAULT FALSE,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE CASCADE
);

-- =============================================
-- SHOPPING CART TABLES
-- =============================================

-- Shopping cart
CREATE TABLE carts
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT UNIQUE,
    session_id  VARCHAR(100), -- For guest users
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE CASCADE
);

-- Cart items
CREATE TABLE cart_items
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id    BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity   INT    NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES carts (id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    UNIQUE KEY unique_cart_product (cart_id, product_id)
);

-- =============================================
-- ORDER MANAGEMENT TABLES
-- =============================================

-- Orders
CREATE TABLE orders
(
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_number         VARCHAR(50)    NOT NULL UNIQUE,
    customer_id          BIGINT,
    guest_email          VARCHAR(100),
    order_date           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    status               VARCHAR(50)    DEFAULT 'PENDING',
    payment_method       VARCHAR(50),
    payment_status       VARCHAR(50)    DEFAULT 'PENDING',
    subtotal             DECIMAL(10, 2) NOT NULL,
    tax                  DECIMAL(10, 2) DEFAULT 0.00,
    shipping_cost        DECIMAL(10, 2) DEFAULT 0.00,
    total_amount         DECIMAL(10, 2) NOT NULL,
    shipping_address     TEXT           NOT NULL,
    shipping_city        VARCHAR(100)   NOT NULL,
    shipping_postal_code VARCHAR(20)    NOT NULL,
    shipping_country     VARCHAR(50)    NOT NULL,
    tracking_number      VARCHAR(100),
    notes                TEXT,
    created_at           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE SET NULL
);

-- Order items
CREATE TABLE order_items
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id       BIGINT         NOT NULL,
    product_id     BIGINT,
    product_name   VARCHAR(255)   NOT NULL,
    product_sku    VARCHAR(50)    NOT NULL,
    quantity       INT            NOT NULL,
    price_per_unit DECIMAL(10, 2) NOT NULL,
    subtotal       DECIMAL(10, 2) NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE SET NULL
);

-- Order status history
CREATE TABLE order_status_history
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id   BIGINT      NOT NULL,
    status     VARCHAR(50) NOT NULL,
    comment    TEXT,
    changed_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

-- =============================================
-- INSERT INITIAL DATA
-- =============================================

-- Insert roles
INSERT INTO roles (name)
VALUES ('USER'),
       ('ADMIN');


-- Password for user: 'password123' (BCrypt encoded)
INSERT INTO customers (email, password, first_name, last_name, phone, address, city, postal_code, email_verified)
VALUES ('user@example.com', '$2a$10$kHzSQlviEvUEidG1Vaw/guBJf7GBrbZfXEOBXnxY8dYaQHAIqz8Q2', 'John', 'Doe', '555-0101',
        '123 Main St', 'Anytown', '12345', TRUE),
       ('admin@hobbyshop.com', '$2a$10$6/RRaFRO53JNQ9to/vPtUOy5UvAB0VvNljTNmKLlFAV56ZSl3zaSG', 'Admin', 'User',
        '555-0102', '456 Admin Ave', 'Admin City', '67890', TRUE);

-- Assign roles to users
INSERT INTO customer_roles (customer_id, role_id)
VALUES (1, 1), -- user@example.com gets ROLE_USER
       (2, 1),
       (2, 2);
-- admin gets ROLE_ADMIN

-- Insert categories
INSERT INTO categories (name, description)
VALUES ('Gundam Models', 'Plastic model kits from the Gundam universe'),
       ('Paints', 'Acrylic, enamel, and lacquer paints for models'),
       ('Tools', 'Hobby tools for building and painting models'),
       ('Accessories', 'Decals, topcoats, and other modeling accessories'),
       ('Airbrush Supplies', 'Airbrushes and related equipment');

-- Insert brands
INSERT INTO brands (name, description, website)
VALUES ('Bandai', 'Manufacturer of Gundam model kits', 'https://www.bandai.com'),
       ('Tamiya', 'Japanese manufacturer of plastic model kits and paints', 'https://www.tamiya.com'),
       ('Mr. Color', 'Professional grade paints by GSI Creos', 'https://www.mr-hobby.com'),
       ('GodHand', 'Premium quality hobby tools', 'https://godhand-tools.com'),
       ('Testors', 'Model paints and supplies', 'https://www.testors.com');

-- Insert sample products (Gundam Models)
INSERT INTO products (sku, name, description, price, stock_quantity, category_id, brand_id, scale, is_featured, image_url)
VALUES ('BAN-RG-001', 'RG 1/144 RX-78-2 Gundam', 'Real Grade version of the iconic RX-78-2 Gundam', 29.99, 15, 1, 1,
        '1/144', TRUE,'https://www.dalong.net/reviews/hg/h191/p/h191.jpg'),
       ('BAN-MG-001', 'MG 1/100 Freedom Gundam 2.0', 'Master Grade Freedom Gundam Ver. 2.0', 54.99, 8, 1, 1, '1/100',
        TRUE, 'https://www.dalong.net/reviews/mg/m192/p/m192.jpg'),
       ('BAN-PG-001', 'PG 1/60 Unleashed RX-78-2', 'Perfect Grade Unleashed RX-78-2 Gundam', 299.99, 3, 1, 1, '1/60',
        TRUE,'https://m.media-amazon.com/images/I/71fXBvrpKQL.jpg'),
       ('BAN-HG-001', 'HG 1/144 Wing Gundam', 'High Grade Wing Gundam TV version', 18.99, 25, 1, 1, '1/144', FALSE,
        'https://www.dalong.net/reviews/hg/h162/p/h162.jpg');

-- Insert sample products (Tamiya Paints)
INSERT INTO products (sku, name, description, price, stock_quantity, category_id, brand_id, color_code, paint_type,
                      is_featured,image_url)
VALUES ('TAM-XF-1', 'Tamiya XF-1 Flat Black', 'Acrylic paint - Flat Black 10ml', 3.99, 50, 2, 2, 'XF-1', 'Acrylic',
        TRUE,'https://m.media-amazon.com/images/I/61YMcBaWyTL.jpg'),
       ('TAM-XF-2', 'Tamiya XF-2 Flat White', 'Acrylic paint - Flat White 10ml', 3.99, 45, 2, 2, 'XF-2', 'Acrylic',
        FALSE,'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSoYMzq6A42aqwyj-MuOg1sZj0qblez0-4BYA&s'),
       ('TAM-X-7', 'Tamiya X-7 Red', 'Gloss acrylic paint - Red 10ml', 3.99, 30, 2, 2, 'X-7', 'Acrylic', FALSE,
        'https://m.media-amazon.com/images/I/41v8RIpBmvL._AC_UF894,1000_QL80_.jpg'),
       ('TAM-TS-21', 'Tamiya TS-21 Gold', 'Spray paint - Gold', 8.99, 20, 2, 2, 'TS-21', 'Lacquer', TRUE,
        'https://m.media-amazon.com/images/I/71rItSTY3IL.jpg');

-- Insert sample products (Mr. Color Paints)
INSERT INTO products (sku, name, description, price, stock_quantity, category_id, brand_id, color_code, paint_type,is_featured,image_url)
VALUES ('MRC-C-001', 'Mr. Color C1 White', 'Lacquer paint - White', 4.50, 40, 2, 3, 'C1', 'Lacquer',FALSE,
        'https://images.amain.com/cdn-cgi/image/f=auto,width=950/images/large/guz/guzc001.jpg'),
       ('MRC-C-002', 'Mr. Color C2 Black', 'Lacquer paint - Black', 4.50, 40, 2, 3, 'C2', 'Lacquer',FALSE,
        'https://m.media-amazon.com/images/I/61G2xbuPPRL._AC_UF894,1000_QL80_.jpg'),
       ('MRC-C-058', 'Mr. Color C58 Orange Yellow', 'Lacquer paint - Orange Yellow', 4.50, 25, 2, 3, 'C58', 'Lacquer',
        FALSE,'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTnQrANN-jtt94ZBA51BpdGSo1UjIIMzSkRAA&s'),
       ('MRC-C-068', 'Mr. Color C68 Red Madder', 'Lacquer paint - Red Madder', 4.50, 25, 2, 3, 'C68', 'Lacquer',FALSE,
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRaK_-94rX9TuAapRf3o44mnP13avX18B_zDQ&s');

-- Insert sample products (Hobby Tools)
INSERT INTO products (sku, name, description, price, stock_quantity, category_id, brand_id, tool_type, is_featured,image_url)
VALUES ('GDH-PN-120', 'GodHand Ultimate Nipper', 'Premium side cutters for plastic models', 69.99, 10, 3, 4, 'Cutter',
        TRUE,'https://m.media-amazon.com/images/I/6137OEOw1yL._AC_UF894,1000_QL80_.jpg'),
       ('TAM-TR-001', 'Tamiya Craft Glue Set', 'Basic modeling glue set', 12.99, 30, 3, 2, 'Adhesive', FALSE,
        'https://m.media-amazon.com/images/I/71lSWstECJL._AC_UF894,1000_QL80_.jpg'),
       ('TAM-FL-001', 'Tamiya File Set', 'Set of 3 precision files', 9.99, 25, 3, 2, 'File', FALSE,
        'https://m.media-amazon.com/images/I/31O6uUU+9yL._AC_UF894,1000_QL80_.jpg'),
       ('MRC-CL-001', 'Mr. Hobby Procon Airbrush', 'Double action airbrush', 89.99, 5, 5, 3, 'Airbrush', TRUE,
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRQg3bF0HMAYgZ6IUeHErks3X7uBMJOV_D57Q&s');

-- Insert sample product reviews
INSERT INTO product_reviews (product_id, customer_id, rating, comment, is_verified_purchase)
VALUES (1, 1, 5, 'Amazing kit! Great detail and articulation.', TRUE),
       (5, 1, 4, 'Good paint, goes on smooth.', TRUE),
       (13, 2, 5, 'Best nippers I''ve ever used!', TRUE);
-- =============================================
-- INSERT TEST ORDERS
-- =============================================

-- Orders for registered user (customer_id = 1 - John Doe)
INSERT INTO orders (order_number, customer_id, order_date, status, payment_method,
                    payment_status, subtotal, tax, shipping_cost, total_amount,
                    shipping_address, shipping_city, shipping_postal_code, shipping_country,
                    tracking_number, notes, created_at, updated_at)
VALUES
-- Order 1: Completed order with multiple items
('ORD-20250215-001', 1, DATE_SUB(NOW(), INTERVAL 15 DAY), 'DELIVERED', 'CREDIT_CARD',
 'PAID', 89.97, 8.99, 5.99, 104.95,
 '123 Main St', 'Anytown', '12345', 'USA',
 'TRACK123456789', 'Please leave at front door',
 DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),

-- Order 2: Processing order
('ORD-20250301-001', 1, DATE_SUB(NOW(), INTERVAL 8 DAY), 'PROCESSING', 'PAYPAL',
 'PAID', 54.99, 5.50, 4.99, 65.48,
 '123 Main St', 'Anytown', '12345', 'USA',
 NULL, NULL,
 DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY)),

-- Order 3: Shipped order with tracking
('ORD-20250305-001', 1, DATE_SUB(NOW(), INTERVAL 5 DAY), 'SHIPPED', 'CREDIT_CARD',
 'PAID', 33.98, 3.40, 5.99, 43.37,
 '123 Main St', 'Anytown', '12345', 'USA',
 'TRACK987654321', NULL,
 DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- Order 4: Pending payment order
('ORD-20250308-001', 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 'PENDING', 'BANK_TRANSFER',
 'PENDING', 149.98, 15.00, 9.99, 174.97,
 '123 Main St', 'Anytown', '12345', 'USA',
 NULL, 'Waiting for payment confirmation',
 DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- Order 5: Cancelled order
('ORD-20250210-001', 1, DATE_SUB(NOW(), INTERVAL 20 DAY), 'CANCELLED', 'CREDIT_CARD',
 'REFUNDED', 29.99, 3.00, 4.99, 37.98,
 '123 Main St', 'Anytown', '12345', 'USA',
 NULL, 'Customer requested cancellation',
 DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY)),

-- Guest orders (no customer_id, only guest_email)
-- Order 6: Guest order - Processing
('ORD-20250302-002', NULL, DATE_SUB(NOW(), INTERVAL 7 DAY), 'PROCESSING', 'PAYPAL',
 'PAID', 67.97, 6.80, 5.99, 80.76,
 '456 Oak Ave', 'Springfield', '67890', 'USA',
 NULL, NULL,
 DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),

-- Order 7: Guest order - Delivered
('ORD-20250220-003', NULL, DATE_SUB(NOW(), INTERVAL 12 DAY), 'DELIVERED', 'CREDIT_CARD',
 'PAID', 42.99, 4.30, 5.99, 53.28,
 '789 Pine St', 'Riverside', '54321', 'USA',
 'TRACK555777999', NULL,
 DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- Order 8: Guest order - Pending
('ORD-20250309-001', NULL, NOW(), 'PENDING', 'CREDIT_CARD',
 'PENDING', 89.98, 9.00, 7.99, 106.97,
 '321 Elm St', 'Hill Valley', '12321', 'USA',
 NULL, NULL,
 NOW(), NOW());

-- =============================================
-- INSERT ORDER ITEMS
-- =============================================

-- Order 1 items (DELIVERED order)
INSERT INTO order_items (order_id, product_id, product_name, product_sku, quantity, price_per_unit, subtotal,
                         created_at)
VALUES (1, 1, 'RG 1/144 RX-78-2 Gundam', 'BAN-RG-001', 2, 29.99, 59.98, DATE_SUB(NOW(), INTERVAL 15 DAY)),
       (1, 5, 'Tamiya XF-1 Flat Black', 'TAM-XF-1', 3, 3.99, 11.97, DATE_SUB(NOW(), INTERVAL 15 DAY)),
       (1, 13, 'GodHand Ultimate Nipper', 'GDH-PN-120', 1, 69.99, 69.99, DATE_SUB(NOW(), INTERVAL 15 DAY));

-- Order 2 items (PROCESSING order)
INSERT INTO order_items (order_id, product_id, product_name, product_sku, quantity, price_per_unit, subtotal,
                         created_at)
VALUES (2, 2, 'MG 1/100 Freedom Gundam 2.0', 'BAN-MG-001', 1, 54.99, 54.99, DATE_SUB(NOW(), INTERVAL 8 DAY));

-- Order 3 items (SHIPPED order)
INSERT INTO order_items (order_id, product_id, product_name, product_sku, quantity, price_per_unit, subtotal,
                         created_at)
VALUES (3, 4, 'HG 1/144 Wing Gundam', 'BAN-HG-001', 1, 18.99, 18.99, DATE_SUB(NOW(), INTERVAL 5 DAY)),
       (3, 9, 'Mr. Color C1 White', 'MRC-C-001', 2, 4.50, 9.00, DATE_SUB(NOW(), INTERVAL 5 DAY)),
       (3, 10, 'Mr. Color C2 Black', 'MRC-C-002', 1, 4.50, 4.50, DATE_SUB(NOW(), INTERVAL 5 DAY));

-- Order 4 items (PENDING order)
INSERT INTO order_items (order_id, product_id, product_name, product_sku, quantity, price_per_unit, subtotal,
                         created_at)
VALUES (4, 3, 'PG 1/60 Unleashed RX-78-2', 'BAN-PG-001', 1, 299.99, 299.99, DATE_SUB(NOW(), INTERVAL 2 DAY));

-- Order 5 items (CANCELLED order)
INSERT INTO order_items (order_id, product_id, product_name, product_sku, quantity, price_per_unit, subtotal,
                         created_at)
VALUES (5, 1, 'RG 1/144 RX-78-2 Gundam', 'BAN-RG-001', 1, 29.99, 29.99, DATE_SUB(NOW(), INTERVAL 20 DAY));

-- Order 6 items (Guest order - PROCESSING)
INSERT INTO order_items (order_id, product_id, product_name, product_sku, quantity, price_per_unit, subtotal,
                         created_at)
VALUES (6, 5, 'Tamiya XF-1 Flat Black', 'TAM-XF-1', 2, 3.99, 7.98, DATE_SUB(NOW(), INTERVAL 7 DAY)),
       (6, 6, 'Tamiya XF-2 Flat White', 'TAM-XF-2', 2, 3.99, 7.98, DATE_SUB(NOW(), INTERVAL 7 DAY)),
       (6, 8, 'Tamiya TS-21 Gold', 'TAM-TS-21', 1, 8.99, 8.99, DATE_SUB(NOW(), INTERVAL 7 DAY)),
       (6, 15, 'Tamiya File Set', 'TAM-FL-001', 2, 9.99, 19.98, DATE_SUB(NOW(), INTERVAL 7 DAY));

-- Order 7 items (Guest order - DELIVERED)
INSERT INTO order_items (order_id, product_id, product_name, product_sku, quantity, price_per_unit, subtotal,
                         created_at)
VALUES (7, 2, 'MG 1/100 Freedom Gundam 2.0', 'BAN-MG-001', 1, 54.99, 54.99, DATE_SUB(NOW(), INTERVAL 12 DAY)),
       (7, 15, 'Tamiya File Set', 'TAM-FL-001', 1, 9.99, 9.99, DATE_SUB(NOW(), INTERVAL 12 DAY));

-- Order 8 items (Guest order - PENDING)
INSERT INTO order_items (order_id, product_id, product_name, product_sku, quantity, price_per_unit, subtotal,
                         created_at)
VALUES (8, 13, 'GodHand Ultimate Nipper', 'GDH-PN-120', 1, 69.99, 69.99, NOW()),
       (8, 14, 'Tamiya Craft Glue Set', 'TAM-TR-001', 2, 12.99, 25.98, NOW());

-- =============================================
-- INSERT ORDER STATUS HISTORY
-- =============================================

-- Order 1 status history (DELIVERED)
INSERT INTO order_status_history (order_id, status, comment, changed_by, created_at)
VALUES (1, 'PENDING', 'Order placed successfully', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 15 DAY)),
       (1, 'PROCESSING', 'Order confirmed and processing', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 14 DAY)),
       (1, 'SHIPPED', 'Order shipped with tracking: TRACK123456789', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 10 DAY)),
       (1, 'DELIVERED', 'Order delivered successfully', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 5 DAY));

-- Order 2 status history (PROCESSING)
INSERT INTO order_status_history (order_id, status, comment, changed_by, created_at)
VALUES (2, 'PENDING', 'Order placed successfully', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 8 DAY)),
       (2, 'PROCESSING', 'Payment confirmed, preparing order', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 7 DAY));

-- Order 3 status history (SHIPPED)
INSERT INTO order_status_history (order_id, status, comment, changed_by, created_at)
VALUES (3, 'PENDING', 'Order placed successfully', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 5 DAY)),
       (3, 'PROCESSING', 'Payment confirmed, order in processing', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 4 DAY)),
       (3, 'SHIPPED', 'Order shipped with tracking: TRACK987654321', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- Order 4 status history (PENDING)
INSERT INTO order_status_history (order_id, status, comment, changed_by, created_at)
VALUES (4, 'PENDING', 'Order placed, awaiting payment confirmation', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- Order 5 status history (CANCELLED)
INSERT INTO order_status_history (order_id, status, comment, changed_by, created_at)
VALUES (5, 'PENDING', 'Order placed successfully', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 20 DAY)),
       (5, 'PROCESSING', 'Order confirmed', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 19 DAY)),
       (5, 'CANCELLED', 'Order cancelled: Customer requested cancellation', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 18 DAY));

-- Order 6 status history (Guest - PROCESSING)
INSERT INTO order_status_history (order_id, status, comment, changed_by, created_at)
VALUES (6, 'PENDING', 'Guest order placed successfully', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 7 DAY)),
       (6, 'PROCESSING', 'Payment confirmed, processing order', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 6 DAY));

-- Order 7 status history (Guest - DELIVERED)
INSERT INTO order_status_history (order_id, status, comment, changed_by, created_at)
VALUES (7, 'PENDING', 'Guest order placed successfully', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 12 DAY)),
       (7, 'PROCESSING', 'Order confirmed', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 11 DAY)),
       (7, 'SHIPPED', 'Order shipped with tracking: TRACK555777999', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 7 DAY)),
       (7, 'DELIVERED', 'Order delivered', 'SYSTEM', DATE_SUB(NOW(), INTERVAL 3 DAY));

-- Order 8 status history (Guest - PENDING)
INSERT INTO order_status_history (order_id, status, comment, changed_by, created_at)
VALUES (8, 'PENDING', 'Guest order placed, awaiting payment', 'SYSTEM', NOW());
-- Update guest orders with proper guest emails
UPDATE orders SET guest_email = 'guest1@example.com' WHERE order_number = 'ORD-20250302-002';
UPDATE orders SET guest_email = 'guest2@example.com' WHERE order_number = 'ORD-20250220-003';
UPDATE orders SET guest_email = 'guest3@example.com' WHERE order_number = 'ORD-20250309-001';


-- Create indexes for better performance
CREATE INDEX idx_products_category ON products (category_id);
CREATE INDEX idx_products_brand ON products (brand_id);
CREATE INDEX idx_products_price ON products (price);
CREATE INDEX idx_products_sku ON products (sku);
CREATE INDEX idx_orders_customer ON orders (customer_id);
CREATE INDEX idx_orders_order_number ON orders (order_number);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_cart_items_cart ON cart_items (cart_id);
CREATE INDEX idx_product_reviews_product ON product_reviews (product_id);
CREATE INDEX idx_product_reviews_customer ON product_reviews (customer_id);