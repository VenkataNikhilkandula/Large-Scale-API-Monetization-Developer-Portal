-- Insert Roles
INSERT INTO roles (name) VALUES ('ROLE_ADMIN'), ('ROLE_DEVELOPER');

-- Insert Users (Password is 'password123' bcrypt hashed)
-- BCrypt: $2a$10$e0myzXyGP.13L096eS6T/O.c/4Jm5oTj7e5b3uGgXv1.B5aR/w.C2
INSERT INTO users (username, password, email) VALUES 
('admin', '$2a$10$e0myzXyGP.13L096eS6T/O.c/4Jm5oTj7e5b3uGgXv1.B5aR/w.C2', 'admin@enterprise.com'),
('developer', '$2a$10$e0myzXyGP.13L096eS6T/O.c/4Jm5oTj7e5b3uGgXv1.B5aR/w.C2', 'developer@enterprise.com');

-- Map Roles
INSERT INTO user_roles (user_id, role_id) VALUES 
(1, 1), -- admin is ROLE_ADMIN
(2, 2); -- developer is ROLE_DEVELOPER

-- Insert APIs
INSERT INTO apis (name, description, base_path, version, status, owner_id) VALUES 
('SMS Gateway API', 'High-throughput SMS delivery service', '/sms', 'v1', 'PUBLISHED', 1),
('Payment API', 'Credit card and bank transfer gateway', '/payments', 'v1', 'PUBLISHED', 1),
('Weather Analytics API', 'Advanced meteorological data', '/weather', 'v2', 'DRAFT', 1);

-- Insert Consumer Apps
INSERT INTO consumer_apps (name, description, developer_id) VALUES 
('Fintech App', 'Mobile banking dashboard app', 2),
('Marketing Suite', 'Automated campaign manager', 2);

-- Insert Subscriptions
INSERT INTO subscriptions (app_id, api_id, tier, status, starts_at) VALUES 
(1, 2, 'PREMIUM', 'ACTIVE', CURRENT_TIMESTAMP),
(2, 1, 'BASIC', 'ACTIVE', CURRENT_TIMESTAMP);

-- Insert API Keys (Plain-text/unhashed for simplicity in local demonstration, standard is to secure)
INSERT INTO api_keys (key_value, app_id, is_active, expires_at) VALUES 
('key_fintech_premium_123', 1, TRUE, '2030-01-01 00:00:00'),
('key_marketing_basic_456', 2, TRUE, '2030-01-01 00:00:00');

-- Insert Aggregated Usage
INSERT INTO api_usages (app_id, api_id, billing_month, request_count, overage_count) VALUES 
(1, 2, '2026-07', 9500, 0),
(2, 1, '2026-07', 1200, 200);

-- Insert Invoice
INSERT INTO invoices (app_id, billing_month, amount, status) VALUES 
(2, '2026-06', 45.00, 'PAID');
