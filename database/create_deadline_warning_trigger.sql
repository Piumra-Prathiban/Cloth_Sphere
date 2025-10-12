-- ============================================================================
-- Deadline Warning Trigger for Production Orders
-- Automatically creates alerts when production deadlines are approaching
-- ============================================================================

-- Drop trigger if exists (for clean reinstall)
IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'trg_production_deadline_warning')
    DROP TRIGGER trg_production_deadline_warning;
GO

CREATE TRIGGER trg_production_deadline_warning
ON production_order
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Declare variables for alert generation
    DECLARE @CurrentDate DATE = CAST(GETDATE() AS DATE);

    -- Insert deadline warning alerts for orders approaching deadline
    INSERT INTO production_alerts (
        alert_id,
        order_id,
        alert_type,
        severity,
        message,
        product_name,
        deadline,
        days_remaining,
        is_read,
        is_dismissed,
        created_at
    )
    SELECT
        -- Generate unique alert ID: ALERT + timestamp + sequence
        CONCAT('ALERT', FORMAT(GETDATE(), 'yyyyMMddHHmmss'),
               RIGHT('000' + CAST(ROW_NUMBER() OVER (ORDER BY i.order_id) AS VARCHAR(3)), 3)) AS alert_id,
        i.order_id,
        CASE
            WHEN DATEDIFF(day, @CurrentDate, i.deadline) < 0 THEN 'OVERDUE'
            WHEN DATEDIFF(day, @CurrentDate, i.deadline) = 0 THEN 'URGENT'
            ELSE 'DEADLINE_WARNING'
        END AS alert_type,
        CASE
            WHEN DATEDIFF(day, @CurrentDate, i.deadline) < 0 THEN 'CRITICAL'
            WHEN DATEDIFF(day, @CurrentDate, i.deadline) = 0 THEN 'CRITICAL'
            WHEN DATEDIFF(day, @CurrentDate, i.deadline) = 1 THEN 'HIGH'
            WHEN DATEDIFF(day, @CurrentDate, i.deadline) <= 3 THEN 'MEDIUM'
            ELSE 'LOW'
        END AS severity,
        CASE
            WHEN DATEDIFF(day, @CurrentDate, i.deadline) < 0 THEN
                CONCAT('Production order ', i.order_id, ' for "', i.product_name, '" is OVERDUE by ',
                       ABS(DATEDIFF(day, @CurrentDate, i.deadline)), ' day(s)!')
            WHEN DATEDIFF(day, @CurrentDate, i.deadline) = 0 THEN
                CONCAT('URGENT: Production order ', i.order_id, ' for "', i.product_name,
                       '" deadline is TODAY!')
            WHEN DATEDIFF(day, @CurrentDate, i.deadline) = 1 THEN
                CONCAT('Production order ', i.order_id, ' for "', i.product_name,
                       '" deadline is TOMORROW!')
            ELSE
                CONCAT('Production order ', i.order_id, ' for "', i.product_name,
                       '" deadline approaching in ', DATEDIFF(day, @CurrentDate, i.deadline), ' day(s)')
        END AS message,
        i.product_name,
        i.deadline,
        DATEDIFF(day, @CurrentDate, i.deadline) AS days_remaining,
        0 AS is_read,
        0 AS is_dismissed,
        GETDATE() AS created_at
    FROM inserted i
    WHERE
        -- Only create alerts for orders within 3 days of deadline or overdue
        DATEDIFF(day, @CurrentDate, i.deadline) <= 3
        -- Don't create alerts for completed or cancelled orders
        AND i.status NOT IN ('COMPLETED', 'CANCELLED')
        -- Prevent duplicate alerts: check if alert already exists for today
        AND NOT EXISTS (
            SELECT 1
            FROM production_alerts pa
            WHERE pa.order_id = i.order_id
            AND CAST(pa.created_at AS DATE) = @CurrentDate
            AND pa.is_dismissed = 0
        );

    -- Auto-update status to DELAYED for overdue orders
    UPDATE production_order
    SET status = 'DELAYED',
        updated_at = GETDATE()
    WHERE order_id IN (
        SELECT order_id
        FROM inserted
        WHERE deadline < @CurrentDate
        AND status NOT IN ('COMPLETED', 'CANCELLED', 'DELAYED')
    );

END;
GO

-- ============================================================================
-- Trigger Information and Testing
-- ============================================================================

-- View trigger details
SELECT
    name AS TriggerName,
    OBJECT_NAME(parent_id) AS TableName,
    is_disabled AS IsDisabled,
    create_date AS CreatedDate,
    modify_date AS ModifiedDate
FROM sys.triggers
WHERE name = 'trg_production_deadline_warning';

-- ============================================================================
-- Test Script (Optional - uncomment to test)
-- ============================================================================

/*
-- Test 1: Insert a production order with deadline in 2 days
DECLARE @TestDate DATE = DATEADD(day, 2, CAST(GETDATE() AS DATE));
INSERT INTO production_order (
    order_id, product_name, product_type, quantity, order_date,
    deadline, priority, status, customer_name, customer_id, completed_quantity
)
VALUES (
    'po999', 'Test Product', 'Garment', 100, GETDATE(),
    @TestDate, 'HIGH', 'PENDING', 'Test Customer', 'CUST001', 0
);

-- Check if alert was created
SELECT * FROM production_alerts WHERE order_id = 'po999';

-- Test 2: Update order deadline to today (should create urgent alert)
UPDATE production_order
SET deadline = CAST(GETDATE() AS DATE)
WHERE order_id = 'po999';

-- Check alerts
SELECT * FROM production_alerts WHERE order_id = 'po999' ORDER BY created_at DESC;

-- Test 3: Mark order as completed (should stop creating alerts)
UPDATE production_order
SET status = 'COMPLETED'
WHERE order_id = 'po999';

-- Try to create alert (should not create)
UPDATE production_order
SET deadline = DATEADD(day, 1, CAST(GETDATE() AS DATE))
WHERE order_id = 'po999';

-- Cleanup test data
DELETE FROM production_order WHERE order_id = 'po999';
*/

-- ============================================================================
-- Trigger Management Commands
-- ============================================================================

-- Disable trigger
-- DISABLE TRIGGER trg_production_deadline_warning ON production_order;

-- Enable trigger
-- ENABLE TRIGGER trg_production_deadline_warning ON production_order;

-- Drop trigger
-- DROP TRIGGER trg_production_deadline_warning;

-- ============================================================================
-- END OF TRIGGER
-- ============================================================================
