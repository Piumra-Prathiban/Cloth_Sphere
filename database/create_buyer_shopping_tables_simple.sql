-- ============================================================================
-- Buyer Shopping System Database Schema (Simplified Version)
-- Tables for Cart, Wishlist, Online Orders, and Order Items
-- ============================================================================

-- ============================================================================
-- 1. SHOPPING CART TABLE
-- ============================================================================
CREATE TABLE cart (
    cart_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    buyer_id VARCHAR(10) NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    added_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_cart_buyer FOREIGN KEY (buyer_id)
        REFERENCES online_buyer_login(buyer_id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT chk_cart_quantity CHECK (quantity > 0),
    CONSTRAINT uq_cart_buyer_product UNIQUE (buyer_id, product_id)
);

-- ============================================================================
-- 2. WISHLIST TABLE
-- ============================================================================
CREATE TABLE wishlist (
    wishlist_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    buyer_id VARCHAR(10) NOT NULL,
    product_id BIGINT NOT NULL,
    added_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_wishlist_buyer FOREIGN KEY (buyer_id)
        REFERENCES online_buyer_login(buyer_id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT uq_wishlist_buyer_product UNIQUE (buyer_id, product_id)
);

-- ============================================================================
-- 3. ONLINE_ORDERS TABLE
-- ============================================================================
CREATE TABLE online_orders (
    order_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_number VARCHAR(20) NOT NULL UNIQUE,
    buyer_id VARCHAR(10) NOT NULL,
    delivery_name VARCHAR(100) NOT NULL,
    delivery_email VARCHAR(100) NOT NULL,
    delivery_phone VARCHAR(20) NOT NULL,
    delivery_address TEXT NOT NULL,
    delivery_city VARCHAR(50),
    delivery_postal_code VARCHAR(20),
    delivery_country VARCHAR(50) DEFAULT 'Sri Lanka',
    subtotal DECIMAL(10,2) NOT NULL,
    shipping_fee DECIMAL(10,2) DEFAULT 0.00,
    tax_amount DECIMAL(10,2) DEFAULT 0.00,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(20) DEFAULT 'COD',
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    tracking_number VARCHAR(50),
    order_notes TEXT,
    admin_notes TEXT,
    order_date DATETIME NOT NULL DEFAULT GETDATE(),
    confirmed_date DATETIME,
    shipped_date DATETIME,
    delivered_date DATETIME,
    cancelled_date DATETIME,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_online_order_buyer FOREIGN KEY (buyer_id)
        REFERENCES online_buyer_login(buyer_id) ON DELETE NO ACTION,
    CONSTRAINT chk_order_amounts CHECK (total_amount >= 0)
);

-- ============================================================================
-- 4. ONLINE_ORDER_ITEMS TABLE
-- ============================================================================
CREATE TABLE online_order_items (
    order_item_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    product_image_path VARCHAR(500),
    unit_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    discount_percentage DECIMAL(5,2) DEFAULT 0.00,
    item_total DECIMAL(10,2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id)
        REFERENCES online_orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE NO ACTION,
    CONSTRAINT chk_order_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_item_total CHECK (item_total >= 0)
);

-- ============================================================================
-- 5. ORDER_STATUS_HISTORY TABLE
-- ============================================================================
CREATE TABLE order_status_history (
    history_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    changed_by VARCHAR(50),
    change_reason TEXT,
    changed_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_status_history_order FOREIGN KEY (order_id)
        REFERENCES online_orders(order_id) ON DELETE CASCADE
);

-- ============================================================================
-- INDEXES
-- ============================================================================
CREATE INDEX idx_cart_buyer ON cart(buyer_id);
CREATE INDEX idx_cart_product ON cart(product_id);
CREATE INDEX idx_wishlist_buyer ON wishlist(buyer_id);
CREATE INDEX idx_wishlist_product ON wishlist(product_id);
CREATE INDEX idx_online_orders_buyer ON online_orders(buyer_id);
CREATE INDEX idx_online_orders_status ON online_orders(status);
CREATE INDEX idx_online_orders_date ON online_orders(order_date);
CREATE INDEX idx_order_items_order ON online_order_items(order_id);
CREATE INDEX idx_order_items_product ON online_order_items(product_id);

-- ============================================================================
-- END OF SCHEMA
-- ============================================================================
