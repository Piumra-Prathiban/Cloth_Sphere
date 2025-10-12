-- ============================================================================
-- Function: fn_GetOrderProgress
-- Purpose: Calculate completion percentage for a production order
-- Returns: DECIMAL (0-100)
-- ============================================================================

-- Drop if exists
IF EXISTS (SELECT * FROM sys.objects WHERE name = 'fn_GetOrderProgress')
    DROP FUNCTION fn_GetOrderProgress;
GO

-- Create function
CREATE FUNCTION fn_GetOrderProgress(@order_id VARCHAR(10))
RETURNS DECIMAL(5,2)
AS
BEGIN
    DECLARE @progress DECIMAL(5,2);

    SELECT @progress = (completed_quantity * 100.0 / quantity)
    FROM production_order
    WHERE order_id = @order_id AND quantity > 0;

    RETURN ISNULL(@progress, 0);
END;
GO

-- ============================================================================
-- USAGE
-- ============================================================================

-- Example 1: Get progress for one order
SELECT dbo.fn_GetOrderProgress('po001') AS progress_pct;

-- Example 2: Get progress for all orders
SELECT
    order_id,
    product_name,
    quantity,
    completed_quantity,
    dbo.fn_GetOrderProgress(order_id) AS progress_pct,
    status
FROM production_order;

-- Example 3: Find orders above 50%
SELECT
    order_id,
    product_name,
    dbo.fn_GetOrderProgress(order_id) AS progress_pct
FROM production_order
WHERE dbo.fn_GetOrderProgress(order_id) >= 50;

-- ============================================================================
-- TEST
-- ============================================================================

-- Insert test order
INSERT INTO production_order (
    order_id, product_name, product_type, quantity,
    order_date, deadline, priority, status,
    customer_name, customer_id, completed_quantity,
    created_at, updated_at
)
VALUES (
    'TEST_FN_001', 'Test', 'Garment', 100,
    GETDATE(), DATEADD(day, 7, GETDATE()),
    'HIGH', 'IN_PROGRESS', 'Test', 'C001', 75,
    GETDATE(), GETDATE()
);

-- Test function
SELECT dbo.fn_GetOrderProgress('TEST_FN_001') AS progress;
-- Expected: 75.00

-- Cleanup
DELETE FROM production_alerts WHERE order_id = 'TEST_FN_001';
DELETE FROM production_order WHERE order_id = 'TEST_FN_001';

-- ============================================================================
-- END
-- ============================================================================
