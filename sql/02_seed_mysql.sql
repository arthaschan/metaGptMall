USE ecommerce;

INSERT IGNORE INTO products
  (id, title, description, price_cents, currency, stock, image_url, active)
VALUES
  (1, 'Wireless Mouse', '2.4G wireless mouse', 1999, 'USD', 100, 'https://example.com/mouse.jpg', 1),
  (2, 'Mechanical Keyboard', 'Blue switches keyboard', 6999, 'USD', 50, 'https://example.com/keyboard.jpg', 1),
  (3, 'Clean Code', 'A Handbook of Agile Software Craftsmanship', 2599, 'USD', 200, 'https://example.com/cleancode.jpg', 1),
  (4, 'Basic T-Shirt', '100% cotton t-shirt', 1299, 'USD', 300, 'https://example.com/tshirt.jpg', 1);