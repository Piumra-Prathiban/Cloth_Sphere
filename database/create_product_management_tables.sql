-- ============================================================================
-- Product Management Database Schema
-- For Customer & Product Management Officer Role
-- ============================================================================

-- ============================================================================
-- 1. PRODUCTS TABLE (Enhanced)
-- Stores all product information with categories and pricing
-- ============================================================================
CREATE TABLE products (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    description VARCHAR(MAX),
    category VARCHAR(50) NOT NULL,
    image_path VARCHAR(500),
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME NOT NULL DEFAULT GETDATE(),
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    CONSTRAINT chk_price_positive CHECK (price >= 0),
    CONSTRAINT chk_stock_positive CHECK (stock >= 0)
);

-- ============================================================================
-- 2. PRODUCT_CATEGORIES TABLE
-- Manage product categories and subcategories
-- ============================================================================
CREATE TABLE product_categories (
    category_id VARCHAR(10) PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    parent_category_id VARCHAR(10),
    description VARCHAR(500),
    display_order INT DEFAULT 0,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_parent_category FOREIGN KEY (parent_category_id)
        REFERENCES product_categories(category_id)
);

-- ============================================================================
-- 3. HOMEPAGE_BANNERS TABLE
-- Manage homepage promotional banners and content
-- ============================================================================
CREATE TABLE homepage_banners (
    banner_id VARCHAR(10) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    subtitle VARCHAR(300),
    image_path VARCHAR(500) NOT NULL,
    link_url VARCHAR(500),
    display_order INT DEFAULT 0,
    start_date DATETIME,
    end_date DATETIME,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    created_by VARCHAR(50),
    CONSTRAINT chk_banner_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

-- ============================================================================
-- 4. PROMOTIONAL_CONTENT TABLE
-- Manage promotional offers and campaigns
-- ============================================================================
CREATE TABLE promotional_content (
    promo_id VARCHAR(10) PRIMARY KEY,
    promo_name VARCHAR(200) NOT NULL,
    promo_type VARCHAR(50) NOT NULL, -- 'discount', 'sale', 'seasonal', 'clearance'
    description VARCHAR(MAX),
    discount_percentage DECIMAL(5,2),
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    created_by VARCHAR(50),
    CONSTRAINT chk_discount CHECK (discount_percentage >= 0 AND discount_percentage <= 100),
    CONSTRAINT chk_promo_dates CHECK (end_date >= start_date)
);

-- ============================================================================
-- 5. PRODUCT_PROMOTIONS TABLE (Junction Table)
-- Link products to promotional campaigns
-- ============================================================================
CREATE TABLE product_promotions (
    product_id VARCHAR(20) NOT NULL,
    promo_id VARCHAR(10) NOT NULL,
    discounted_price DECIMAL(10,2),
    PRIMARY KEY (product_id, promo_id),
    CONSTRAINT fk_product_promo FOREIGN KEY (product_id)
        REFERENCES products(product_id) ON DELETE CASCADE,
    CONSTRAINT fk_promotion FOREIGN KEY (promo_id)
        REFERENCES promotional_content(promo_id) ON DELETE CASCADE
);

-- ============================================================================
-- 6. BUYER_MESSAGES TABLE
-- Handle buyer messages, complaints, and support requests
-- ============================================================================
CREATE TABLE buyer_messages (
    message_id VARCHAR(15) PRIMARY KEY,
    buyer_id VARCHAR(10) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    message_text VARCHAR(MAX) NOT NULL,
    message_type VARCHAR(50) NOT NULL, -- 'inquiry', 'complaint', 'feedback', 'support'
    priority VARCHAR(20) DEFAULT 'normal', -- 'low', 'normal', 'high', 'urgent'
    status VARCHAR(20) NOT NULL DEFAULT 'open', -- 'open', 'in_progress', 'resolved', 'closed'
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    resolved_at DATETIME,
    resolved_by VARCHAR(50),
    CONSTRAINT fk_buyer_message FOREIGN KEY (buyer_id)
        REFERENCES online_buyer_login(buyer_id) ON DELETE CASCADE
);

-- ============================================================================
-- 7. MESSAGE_RESPONSES TABLE
-- Store responses to buyer messages
-- ============================================================================
CREATE TABLE message_responses (
    response_id VARCHAR(15) PRIMARY KEY,
    message_id VARCHAR(15) NOT NULL,
    response_text VARCHAR(MAX) NOT NULL,
    responded_by VARCHAR(50) NOT NULL,
    response_date DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_message_response FOREIGN KEY (message_id)
        REFERENCES buyer_messages(message_id) ON DELETE CASCADE
);

-- ============================================================================
-- 8. PRODUCT_IMAGES TABLE
-- Store multiple images per product (additional images)
-- ============================================================================
CREATE TABLE product_images (
    image_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id VARCHAR(20) NOT NULL,
    image_path VARCHAR(500) NOT NULL,
    image_type VARCHAR(20) DEFAULT 'additional', -- 'main', 'additional', 'thumbnail'
    display_order INT DEFAULT 0,
    is_active BIT NOT NULL DEFAULT 1,
    uploaded_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_product_image FOREIGN KEY (product_id)
        REFERENCES products(product_id) ON DELETE CASCADE
);

-- ============================================================================
-- 9. PRODUCT_PRICING_HISTORY TABLE
-- Track price changes for products
-- ============================================================================
CREATE TABLE product_pricing_history (
    history_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id VARCHAR(20) NOT NULL,
    old_price DECIMAL(10,2) NOT NULL,
    new_price DECIMAL(10,2) NOT NULL,
    change_reason VARCHAR(500),
    changed_by VARCHAR(50) NOT NULL,
    changed_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_product_pricing FOREIGN KEY (product_id)
        REFERENCES products(product_id) ON DELETE CASCADE
);

-- ============================================================================
-- INDEXES for Performance Optimization
-- ============================================================================

-- Products table indexes
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_is_active ON products(is_active);
CREATE INDEX idx_products_created_at ON products(created_at);
CREATE INDEX idx_products_price ON products(price);

-- Product categories indexes
CREATE INDEX idx_categories_parent ON product_categories(parent_category_id);
CREATE INDEX idx_categories_active ON product_categories(is_active);

-- Homepage banners indexes
CREATE INDEX idx_banners_active ON homepage_banners(is_active);
CREATE INDEX idx_banners_dates ON homepage_banners(start_date, end_date);

-- Promotional content indexes
CREATE INDEX idx_promo_active ON promotional_content(is_active);
CREATE INDEX idx_promo_dates ON promotional_content(start_date, end_date);

-- Buyer messages indexes
CREATE INDEX idx_messages_buyer ON buyer_messages(buyer_id);
CREATE INDEX idx_messages_status ON buyer_messages(status);
CREATE INDEX idx_messages_type ON buyer_messages(message_type);
CREATE INDEX idx_messages_created ON buyer_messages(created_at);

-- Product images indexes
CREATE INDEX idx_images_product ON product_images(product_id);
CREATE INDEX idx_images_type ON product_images(image_type);

-- ============================================================================
-- SAMPLE DATA (Optional - for testing)
-- ============================================================================

-- Sample product categories
INSERT INTO product_categories (category_id, category_name, parent_category_id, description, display_order)
VALUES
    ('CAT001', 'Men''s Clothing', NULL, 'All men''s fashion items', 1),
    ('CAT002', 'Women''s Clothing', NULL, 'All women''s fashion items', 2),
    ('CAT003', 'Accessories', NULL, 'Fashion accessories', 3),
    ('CAT004', 'Men''s Shirts', 'CAT001', 'Shirts for men', 1),
    ('CAT005', 'Men''s Pants', 'CAT001', 'Pants for men', 2),
    ('CAT006', 'Women''s Dresses', 'CAT002', 'Dresses for women', 1),
    ('CAT007', 'Women''s Tops', 'CAT002', 'Tops for women', 2);

-- Sample products (if products table is empty)
-- INSERT INTO products (product_id, name, code, price, stock, description, category, is_active)
-- VALUES
--     ('PROD001', 'Classic Cotton Shirt', 'CS-001', 29.99, 100, 'Comfortable cotton shirt for everyday wear', 'CAT004', 1),
--     ('PROD002', 'Slim Fit Denim Jeans', 'DJ-002', 49.99, 75, 'Modern slim fit denim jeans', 'CAT005', 1),
--     ('PROD003', 'Summer Floral Dress', 'FD-003', 59.99, 50, 'Beautiful floral pattern dress', 'CAT006', 1);

-- ============================================================================
-- END OF SCHEMA
-- ============================================================================
