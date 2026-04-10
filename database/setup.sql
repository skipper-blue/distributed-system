-- Distributed Drinks System Database Setup
-- MySQL Script

CREATE DATABASE IF NOT EXISTS drinks_system;
USE drinks_system;

-- Drinks table
CREATE TABLE IF NOT EXISTS drinks (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    price DECIMAL(10,2) NOT NULL
);

-- Stock table (per branch)
CREATE TABLE IF NOT EXISTS stock (
    id INT PRIMARY KEY AUTO_INCREMENT,
    branch VARCHAR(50) NOT NULL,
    drink_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    threshold INT NOT NULL DEFAULT 10,
    FOREIGN KEY (drink_id) REFERENCES drinks(id),
    UNIQUE KEY unique_branch_drink (branch, drink_id)
);

-- Restock audit table
CREATE TABLE IF NOT EXISTS restocks (
    id INT PRIMARY KEY AUTO_INCREMENT,
    drink_id INT NOT NULL,
    branch VARCHAR(50) NOT NULL,
    quantity_added INT NOT NULL,
    restock_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (drink_id) REFERENCES drinks(id),
    INDEX idx_restocks_branch_date (branch, restock_date),
    INDEX idx_restocks_drink_date (drink_id, restock_date)
);

-- Stock transfer tracking (Nairobi → branches)
CREATE TABLE IF NOT EXISTS stock_transfers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    drink_id INT NOT NULL,
    from_branch VARCHAR(50),
    to_branch VARCHAR(50),
    quantity INT NOT NULL,
    transfer_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (drink_id) REFERENCES drinks(id),
    INDEX idx_transfers_date (transfer_date)
);

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(100) NOT NULL,
    branch VARCHAR(50) NOT NULL,
    total_cost DECIMAL(10,2) NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Order items table
CREATE TABLE IF NOT EXISTS order_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    drink_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Insert sample drinks (prices in KSH - Kenyan Shilling)
INSERT INTO drinks (name, price) VALUES
('Coke', 50.00),
('Fanta', 45.00),
('Pepsi', 48.00);

-- Insert stock for all branches (initial stock 100, threshold 10)
INSERT INTO stock (branch, drink_id, quantity, threshold) VALUES
('NAIROBI', 1, 100, 10),
('NAIROBI', 2, 100, 10),
('NAIROBI', 3, 100, 10),
('NAKURU', 1, 100, 10),
('NAKURU', 2, 100, 10),
('NAKURU', 3, 100, 10),
('MOMBASA', 1, 100, 10),
('MOMBASA', 2, 100, 10),
('MOMBASA', 3, 100, 10),
('KISUMU', 1, 100, 10),
('KISUMU', 2, 100, 10),
('KISUMU', 3, 100, 10);

-- Sample orders (optional)
INSERT INTO orders (customer_name, branch, total_cost) VALUES
('John Doe', 'NAIROBI', 150.00),
('Jane Smith', 'NAKURU', 95.00);

INSERT INTO order_items (order_id, drink_name, quantity, price) VALUES
(1, 'Coke', 2, 50.00),
(1, 'Fanta', 1, 45.00),
(2, 'Pepsi', 2, 48.00);


-- Currency normalization helper for older USD-based seed data
UPDATE drinks SET price = price * 130;

-- Update order item prices from USD to KSH
UPDATE order_items SET price = price * 130;

-- Update order total costs from USD to KSH
UPDATE orders SET total_cost = total_cost * 130;
