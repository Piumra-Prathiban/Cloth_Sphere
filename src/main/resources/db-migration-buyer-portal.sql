-- Buyer Portal Database Migration Script
-- Run this script manually in SQL Server Management Studio or using sqlcmd

USE Clothsphere;
GO

-- Add new columns to buyers table if they don't exist
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'buyers') AND name = 'password')
    ALTER TABLE buyers ADD password NVARCHAR(255) NULL;
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'buyers') AND name = 'phone')
    ALTER TABLE buyers ADD phone NVARCHAR(255) NULL;
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'buyers') AND name = 'address')
    ALTER TABLE buyers ADD address NVARCHAR(500) NULL;
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'buyers') AND name = 'city')
    ALTER TABLE buyers ADD city NVARCHAR(255) NULL;
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'buyers') AND name = 'postal_code')
    ALTER TABLE buyers ADD postal_code NVARCHAR(255) NULL;
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'buyers') AND name = 'country')
    ALTER TABLE buyers ADD country NVARCHAR(255) NULL;
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'buyers') AND name = 'company_name')
    ALTER TABLE buyers ADD company_name NVARCHAR(255) NULL;
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'buyers') AND name = 'created_at')
    ALTER TABLE buyers ADD created_at DATETIME2 NULL;
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'buyers') AND name = 'updated_at')
    ALTER TABLE buyers ADD updated_at DATETIME2 NULL;
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'buyers') AND name = 'is_active')
    ALTER TABLE buyers ADD is_active BIT DEFAULT 1;
GO

-- Update existing orders table to support new structure
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'orders') AND type = 'U')
BEGIN
    -- Add new columns to orders table
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'order_number')
        ALTER TABLE orders ADD order_number NVARCHAR(50) NULL;

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'total_amount')
        ALTER TABLE orders ADD total_amount DECIMAL(18,2) NULL;

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'delivery_address')
        ALTER TABLE orders ADD delivery_address NVARCHAR(500) NULL;

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'delivery_city')
        ALTER TABLE orders ADD delivery_city NVARCHAR(255) NULL;

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'delivery_postal_code')
        ALTER TABLE orders ADD delivery_postal_code NVARCHAR(255) NULL;

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'delivery_country')
        ALTER TABLE orders ADD delivery_country NVARCHAR(255) NULL;

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'delivery_phone')
        ALTER TABLE orders ADD delivery_phone NVARCHAR(255) NULL;

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'special_instructions')
        ALTER TABLE orders ADD special_instructions NVARCHAR(1000) NULL;

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'estimated_delivery_date')
        ALTER TABLE orders ADD estimated_delivery_date DATETIME2 NULL;

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'actual_delivery_date')
        ALTER TABLE orders ADD actual_delivery_date DATETIME2 NULL;

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'tracking_number')
        ALTER TABLE orders ADD tracking_number NVARCHAR(255) NULL;

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'payment_status')
        ALTER TABLE orders ADD payment_status NVARCHAR(50) NULL;

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'created_at')
        ALTER TABLE orders ADD created_at DATETIME2 NULL;

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'updated_at')
        ALTER TABLE orders ADD updated_at DATETIME2 NULL;

    -- Drop old product_id column if exists (no longer needed)
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'product_id')
        ALTER TABLE orders DROP CONSTRAINT IF EXISTS FK_orders_product_id;
        ALTER TABLE orders DROP COLUMN product_id;

    -- Drop old quantity column if exists (moved to order_items)
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'orders') AND name = 'quantity')
        ALTER TABLE orders DROP COLUMN quantity;
END
GO

-- Update order_items table structure
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'order_items') AND type = 'U')
BEGIN
    -- Add order_id if doesn't exist
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'order_items') AND name = 'order_id')
        ALTER TABLE order_items ADD order_id BIGINT NULL;

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'order_items') AND name = 'price')
        ALTER TABLE order_items ADD price DECIMAL(18,2) NULL;

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'order_items') AND name = 'product_name')
        ALTER TABLE order_items ADD product_name NVARCHAR(255) NULL;

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'order_items') AND name = 'product_size')
        ALTER TABLE order_items ADD product_size NVARCHAR(50) NULL;

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'order_items') AND name = 'product_material')
        ALTER TABLE order_items ADD product_material NVARCHAR(50) NULL;
END
GO

-- Create inquiries table if it doesn't exist
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'inquiries') AND type = 'U')
BEGIN
    CREATE TABLE inquiries (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        buyer_id BIGINT NOT NULL,
        subject NVARCHAR(200) NOT NULL,
        message NVARCHAR(2000) NOT NULL,
        inquiry_type NVARCHAR(50),
        related_order_id BIGINT,
        status NVARCHAR(50),
        response NVARCHAR(2000),
        responded_by NVARCHAR(255),
        responded_at DATETIME2,
        created_at DATETIME2,
        updated_at DATETIME2,
        CONSTRAINT FK_inquiries_buyer FOREIGN KEY (buyer_id) REFERENCES buyers(id)
    );
END
GO

PRINT 'Buyer Portal migration completed successfully!';
GO
