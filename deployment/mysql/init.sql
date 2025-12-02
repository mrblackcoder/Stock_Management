-- Initial database setup (executed on first run)

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);
CREATE INDEX IF NOT EXISTS idx_transactions_date ON stock_transactions(transaction_date);

-- Insert default admin user (password: admin123)
INSERT IGNORE INTO users (username, email, password, full_name, role, enabled, created_at)
VALUES ('admin', 'admin@local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIrssjO7urlDJf9FGh7HB0k2OQZ9k5zC', 
        'System Administrator', 'ADMIN', true, NOW());

-- Insert sample categories
INSERT IGNORE INTO categories (name, description, created_at)
VALUES 
    ('Electronics', 'Electronic devices and accessories', NOW()),
    ('Office Supplies', 'Office equipment and supplies', NOW()),
    ('Furniture', 'Office and home furniture', NOW());

-- Insert sample suppliers  
INSERT IGNORE INTO suppliers (name, email, phone, address, created_at)
VALUES 
    ('TechSupply Inc', 'contact@techsupply.com', '+1-555-0100', '123 Tech Street, NY', NOW()),
    ('Office World', 'sales@officeworld.com', '+1-555-0200', '456 Supply Ave, CA', NOW());
