-- ============================================================================
-- Buyer Shopping System Database Schema
-- Tables for Cart, Wishlist, Online Orders, and Order Items
-- ============================================================================

-- ============================================================================
-- 1. SHOPPING CART TABLE
-- Stores items added to cart by online buyers before checkout
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
-- Stores favorite products saved by online buyers for later
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
-- Stores orders placed by online buyers
-- ============================================================================
CREATE TABLE online_orders (
    order_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_number VARCHAR(20) NOT NULL UNIQUE,
    buyer_id VARCHAR(10) NOT NULL,

    -- Delivery Information
    delivery_name VARCHAR(100) NOT NULL,
    delivery_email VARCHAR(100) NOT NULL,
    delivery_phone VARCHAR(20) NOT NULL,
    delivery_address TEXT NOT NULL,
    delivery_city VARCHAR(50),
    delivery_postal_code VARCHAR(20),
    delivery_country VARCHAR(50) DEFAULT 'Sri Lanka',

    -- Order Details
    subtotal DECIMAL(10,2) NOT NULL,
    shipping_fee DECIMAL(10,2) DEFAULT 0.00,
    tax_amount DECIMAL(10,2) DEFAULT 0.00,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,

    -- Order Status & Tracking
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    -- Status values: PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED

    payment_method VARCHAR(20) DEFAULT 'COD',
    -- Payment methods: COD (Cash on Delivery), CARD, BANK_TRANSFER

    payment_status VARCHAR(20) DEFAULT 'PENDING',
    -- Payment status: PENDING, PAID, FAILED, REFUNDED

    tracking_number VARCHAR(50),

    -- Notes
    order_notes TEXT,
    admin_notes TEXT,

    -- Timestamps
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
-- Stores individual products within each online order
-- ============================================================================
CREATE TABLE online_order_items (
    order_item_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,

    -- Product snapshot at time of order
    product_name VARCHAR(200) NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    product_image_path VARCHAR(500),

    -- Pricing
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
-- 5. ORDER_STATUS_HISTORY TABLE (Optional - for tracking status changes)
-- Stores history of order status changes for audit trail
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
-- INDEXES for Performance Optimization
-- ============================================================================

-- Cart indexes
CREATE INDEX idx_cart_buyer ON cart(buyer_id);
CREATE INDEX idx_cart_product ON cart(product_id);
CREATE INDEX idx_cart_added_at ON cart(added_at);

-- Wishlist indexes
CREATE INDEX idx_wishlist_buyer ON wishlist(buyer_id);
CREATE INDEX idx_wishlist_product ON wishlist(product_id);
CREATE INDEX idx_wishlist_added_at ON wishlist(added_at);

-- Online Orders indexes
CREATE INDEX idx_online_orders_buyer ON online_orders(buyer_id);
CREATE INDEX idx_online_orders_status ON online_orders(status);
CREATE INDEX idx_online_orders_date ON online_orders(order_date);
CREATE INDEX idx_online_orders_number ON online_orders(order_number);
CREATE INDEX idx_online_orders_payment_status ON online_orders(payment_status);

-- Order Items indexes
CREATE INDEX idx_order_items_order ON online_order_items(order_id);
CREATE INDEX idx_order_items_product ON online_order_items(product_id);

-- Status History indexes
CREATE INDEX idx_status_history_order ON order_status_history(order_id);
CREATE INDEX idx_status_history_date ON order_status_history(changed_at);

-- ============================================================================
-- VIEWS for Common Queries
-- ============================================================================

-- View: Cart with Product Details
GO
CREATE VIEW vw_cart_details AS
SELECT
    c.cart_id,
    c.buyer_id,
    c.quantity,
    c.added_at,
    p.id as product_id,
    p.product_id as product_code,
    p.name as product_name,
    p.price as unit_price,
    p.stock,
    p.image_path,
    p.category,
    (c.quantity * p.price) as subtotal
FROM cart c
INNER JOIN products p ON c.product_id = p.id
WHERE p.stock > 0;

-- View: Wishlist with Product Details
GO
CREATE VIEW vw_wishlist_details AS
SELECT
    w.wishlist_id,
    w.buyer_id,
    w.added_at,
    p.id as product_id,
    p.product_id as product_code,
    p.name as product_name,
    p.price,
    p.stock,
    p.image_path,
    p.category,
    CASE WHEN p.stock > 0 THEN 1 ELSE 0 END as in_stock
FROM wishlist w
INNER JOIN products p ON w.product_id = p.id;

-- View: Order Summary
GO
CREATE VIEW vw_order_summary AS
SELECT
    o.order_id,
    o.order_number,
    o.buyer_id,
    b.customer_name,
    b.email,
    o.total_amount,
    o.status,
    o.payment_status,
    o.order_date,
    o.delivered_date,
    COUNT(oi.order_item_id) as total_items,
    SUM(oi.quantity) as total_quantity
FROM online_orders o
INNER JOIN online_buyer_login b ON o.buyer_id = b.buyer_id
LEFT JOIN online_order_items oi ON o.order_id = oi.order_id
GROUP BY
    o.order_id, o.order_number, o.buyer_id, b.customer_name,
    b.email, o.total_amount, o.status, o.payment_status,
    o.order_date, o.delivered_date;

-- ============================================================================
-- STORED PROCEDURES (Optional - for complex operations)
-- ============================================================================

-- Procedure: Add item to cart (or update quantity if exists)
GO
CREATE PROCEDURE sp_add_to_cart
    @buyer_id VARCHAR(10),
    @product_id BIGINT,
    @quantity INT = 1
AS
BEGIN
    SET NOCOUNT ON;

    -- Check if product exists and has stock
    IF NOT EXISTS (SELECT 1 FROM products WHERE id = @product_id AND stock > 0)
    BEGIN
        RAISERROR('Product not available', 16, 1);
        RETURN;
    END

    -- Check if item already in cart
    IF EXISTS (SELECT 1 FROM cart WHERE buyer_id = @buyer_id AND product_id = @product_id)
    BEGIN
        -- Update quantity
        UPDATE cart
        SET quantity = quantity + @quantity
        WHERE buyer_id = @buyer_id AND product_id = @product_id;
    END
    ELSE
    BEGIN
        -- Insert new cart item
        INSERT INTO cart (buyer_id, product_id, quantity)
        VALUES (@buyer_id, @product_id, @quantity);
    END
END
GO

-- Procedure: Clear cart after order placement
GO
CREATE PROCEDURE sp_clear_cart
    @buyer_id VARCHAR(10)
AS
BEGIN
    SET NOCOUNT ON;
    DELETE FROM cart WHERE buyer_id = @buyer_id;
END
GO

-- Procedure: Generate unique order number
GO
CREATE PROCEDURE sp_generate_order_number
    @order_number VARCHAR(20) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @date_prefix VARCHAR(8);
    DECLARE @sequence INT;

    -- Format: ORD-YYYYMMDD-NNNN
    SET @date_prefix = CONVERT(VARCHAR(8), GETDATE(), 112);

    -- Get next sequence for today
    SELECT @sequence = ISNULL(MAX(CAST(RIGHT(order_number, 4) AS INT)), 0) + 1
    FROM online_orders
    WHERE order_number LIKE 'ORD-' + @date_prefix + '%';

    SET @order_number = 'ORD-' + @date_prefix + '-' + RIGHT('0000' + CAST(@sequence AS VARCHAR(4)), 4);
END
GO

-- ============================================================================
-- TRIGGERS for Automation
-- ============================================================================

-- Trigger: Update online_orders.updated_at on any update
GO
CREATE TRIGGER trg_online_orders_update
ON online_orders
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE online_orders
    SET updated_at = GETDATE()
    WHERE order_id IN (SELECT order_id FROM inserted);
END
GO

-- Trigger: Create status history entry on status change
GO
CREATE TRIGGER trg_order_status_change
ON online_orders
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF UPDATE(status)
    BEGIN
        INSERT INTO order_status_history (order_id, old_status, new_status, changed_at)
        SELECT
            i.order_id,
            d.status as old_status,
            i.status as new_status,
            GETDATE()
        FROM inserted i
        INNER JOIN deleted d ON i.order_id = d.order_id
        WHERE i.status <> d.status;
    END
END
GO

-- ============================================================================
-- SAMPLE DATA (Optional - for testing)
-- ============================================================================

-- Note: Uncomment these if you want sample data for testing

/*
-- Sample Cart Items (assumes buyer OBY01 exists)
INSERT INTO cart (buyer_id, product_id, quantity)
SELECT 'OBY01', id, 2
FROM products
WHERE id IN (1, 2, 3);

-- Sample Wishlist Items
INSERT INTO wishlist (buyer_id, product_id)
SELECT 'OBY01', id
FROM products
WHERE id IN (4, 5);
*/

-- ============================================================================
-- END OF SCHEMA
-- ============================================================================
