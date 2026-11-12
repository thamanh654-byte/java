-- Database Schema cho ung dung Quan Ly Cua Hang
CREATE DATABASE IF NOT EXISTS quanlycuahang CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE quanlycuahang;

-- 1. Bảng Danh mục sản phẩm (Khóa chính là category_id)
CREATE TABLE IF NOT EXISTS categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    UNIQUE KEY uk_categories_name (name)
);

-- 2. Bảng Sản phẩm (Khóa chính là product_id, Khóa ngoại category_id)
CREATE TABLE IF NOT EXISTS products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    price DOUBLE NOT NULL,
    quantity INT NOT NULL,
    category_id INT,
    status BOOLEAN DEFAULT TRUE,
    import_price DECIMAL(10,2) DEFAULT 0,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(category_id),
    CONSTRAINT chk_products_quantity CHECK (quantity >= 0),
    CONSTRAINT chk_products_price CHECK (price >= 0),
    KEY idx_products_status (status),
    KEY idx_products_category (category_id),
    KEY idx_products_name (name)
);

-- 3. Bảng Hóa đơn
CREATE TABLE IF NOT EXISTS orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(100) DEFAULT 'Khach vang lai',
    total_amount DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    KEY idx_orders_created_at (created_at)
);

-- 4. Bảng Chi tiết hóa đơn (PK kep: order_id + product_id)
CREATE TABLE IF NOT EXISTS order_details (
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DOUBLE NOT NULL,
    PRIMARY KEY (order_id, product_id),
    CONSTRAINT fk_order_details_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_order_details_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT chk_order_details_quantity CHECK (quantity > 0)
);

-- Dữ liệu mẫu khởi tạo ban đầu
-- (Dùng INSERT IGNORE để chạy lại file nhiều lần không bị nhân đôi dữ liệu)
INSERT IGNORE INTO categories (category_id, name) VALUES
(1, 'Điện thoại'),
(2, 'Phụ kiện');

INSERT IGNORE INTO products (product_id, name, price, quantity, category_id, status) VALUES
(1, 'iPhone 15 Pro Max', 29000000, 10, 1, TRUE),
(2, 'Samsung Galaxy S24', 21000000, 4, 1, TRUE),
(3, 'Củ sạc nhanh Anker 30W', 450000, 5, 2, TRUE),
(4, 'Tai nghe Bluetooth TWS', 790000, 2, 2, TRUE);

