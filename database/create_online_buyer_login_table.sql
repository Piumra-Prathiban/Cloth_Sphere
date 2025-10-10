-- Create online_buyer_login table for buyer authentication
-- This is a separate table from the 'buyers' table
-- 'buyers' table is for local/offline buyers (LBY prefix)
-- 'online_buyer_login' table is for online buyers who register through the website (OBY prefix)

CREATE TABLE online_buyer_login (
    buyer_id VARCHAR(10) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    customer_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    company VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'buyer',
    log_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    is_active BIT NOT NULL DEFAULT 1
);

-- Create indexes for better performance
CREATE INDEX idx_online_buyer_username ON online_buyer_login(username);
CREATE INDEX idx_online_buyer_email ON online_buyer_login(email);
CREATE INDEX idx_online_buyer_is_active ON online_buyer_login(is_active);

-- Sample insert (optional - for testing)
-- Password is 'password123' encrypted with BCrypt
-- INSERT INTO online_buyer_login (buyer_id, username, password, email, customer_name, phone, address, company, role, log_count, created_at, is_active)
-- VALUES ('OBY01', 'testbuyer', '$2a$10$N9qo8uLOickgx2ZMRZoMye1234567890abcdefghijklmnopqrstuv', 'testbuyer@example.com', 'Test Buyer', '1234567890', '123 Test St', 'Test Company', 'buyer', 0, GETDATE(), 1);
