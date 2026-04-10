-- Insert demo user for testing password reset
MERGE INTO users (id, email, password_hash, role) KEY(email) VALUES
(1, 'demo@example.com', '$2a$10$X9XG5tV7b3q8zL8q8Zq8Z.H8q8Zq8Zq8Zq8Zq8Zq8Zq8Zq8Zq8Zq', 'USER');

-- Insert demo products (existing baseline)
MERGE INTO products (id, title, description, price_cents, currency, stock, image_url, active) KEY(id) VALUES
(1, 'Wireless Mouse', 'Ergonomic wireless mouse', 2999, 'CNY', 100, '/images/mouse.jpg', true),
(2, 'Mechanical Keyboard', 'RGB mechanical keyboard', 8999, 'CNY', 50, '/images/keyboard.jpg', true);
