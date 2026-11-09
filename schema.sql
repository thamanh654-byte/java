-- Database Schema cho ung dung Quan Ly Cua Hang
CREATE DATABASE IF NOT EXISTS quanlycuahang CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE quanlycuahang;

-- 1. Bảng Danh mục sản phẩm (Khóa chính là category_id)
CREATE TABLE IF NOT EXISTS categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

-- 2. Bảng Sản phẩm (Khóa chính là product_id, Khóa ngoại category_id)
CREATE TABLE IF NOT EXISTS products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    price DOUBLE NOT NULL,
    quantity INT NOT NULL,
    category_id INT,
    status BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

-- 3. Bảng Hóa đơn
CREATE TABLE IF NOT EXISTS orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(100) NOT NULL,
    total_amount DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Bảng Chi tiết hóa đơn
CREATE TABLE IF NOT EXISTS order_details (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DOUBLE NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- Dữ liệu mẫu khởi tạo ban đầu
INSERT INTO categories (name, description) VALUES 
('Điện thoại', 'Thiết bị di động thông minh'),
('Phụ kiện', 'Cáp sạc, tai nghe, ốp lưng')
ON DUPLICATE KEY UPDATE name=name;

INSERT INTO products (name, price, quantity, category_id, status) VALUES 
('iPhone 15 Pro Max', 29000000, 10, 1, TRUE),
('Samsung Galaxy S24', 21000000, 4, 1, TRUE),
('Củ sạc nhanh Anker 30W', 450000, 5, 2, TRUE),
('Tai nghe Bluetooth TWS', 790000, 2, 2, TRUE)
ON DUPLICATE KEY UPDATE name=name;
