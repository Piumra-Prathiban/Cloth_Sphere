-- ============================================================================
-- Trigger: trg_UpdateProductionOrderTimestamp
-- Purpose: Auto-update updated_at timestamp on production_order changes
-- ============================================================================

-- Drop if exists
IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'trg_UpdateProductionOrderTimestamp')
    DROP TRIGGER trg_UpdateProductionOrderTimestamp;
GO

-- Create trigger
CREATE TRIGGER trg_UpdateProductionOrderTimestamp
ON production_order
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE production_order
    SET updated_at = GETDATE()
    WHERE order_id IN (SELECT order_id FROM inserted);
END;
GO

-- ============================================================================
-- USAGE & TEST
-- ============================================================================

-- Test: Insert order
INSERT INTO production_order (
    order_id, product_name, product_type, quantity,
    order_date, deadline, priority, status,
    customer_name, customer_id, completed_quantity,
    created_at, updated_at
)
VALUES (
    'TEST_TRG_001', 'Test Product', 'Garment', 100,
    GETDATE(), DATEADD(day, 7, GETDATE()),
    'HIGH', 'PENDING', 'Test Customer', 'C001', 0,
    GETDATE(), GETDATE()
);

-- Check timestamp
SELECT order_id, updated_at FROM production_order WHERE order_id = 'TEST_TRG_001';

-- Wait 2 seconds
WAITFOR DELAY '00:00:02';

-- Update order (trigger fires)
UPDATE production_order
SET completed_quantity = 50
WHERE order_id = 'TEST_TRG_001';

-- Check timestamp (should be updated!)
SELECT order_id, updated_at, completed_quantity FROM production_order WHERE order_id = 'TEST_TRG_001';

-- Cleanup
DELETE FROM production_alerts WHERE order_id = 'TEST_TRG_001';
DELETE FROM production_order WHERE order_id = 'TEST_TRG_001';

PRINT '✓ Trigger test complete';

-- ============================================================================
-- END
-- ============================================================================
