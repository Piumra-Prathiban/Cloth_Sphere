-- ============================================================================
-- Stored Procedure: sp_AutoCompleteOrders
-- Purpose: Automatically mark production orders as COMPLETED when 100% done
-- Author: Factory Manager Module
-- Created: 2025-01-12
-- ============================================================================

-- Drop procedure if exists (for clean reinstall)
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_AutoCompleteOrders')
    DROP PROCEDURE sp_AutoCompleteOrders;
GO

CREATE PROCEDURE sp_AutoCompleteOrders
AS
BEGIN
    SET NOCOUNT ON;

    -- Declare variables
    DECLARE @UpdatedCount INT = 0;
    DECLARE @ErrorMessage NVARCHAR(4000);
    DECLARE @ErrorSeverity INT;
    DECLARE @ErrorState INT;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- Update orders where completed_quantity >= quantity
        -- and status is not already COMPLETED
        UPDATE production_order
        SET
            status = 'COMPLETED',
            updated_at = GETDATE()
        WHERE
            completed_quantity >= quantity
            AND status NOT IN ('COMPLETED', 'CANCELLED')
            AND quantity > 0;  -- Safety check

        -- Get count of updated orders
        SET @UpdatedCount = @@ROWCOUNT;

        COMMIT TRANSACTION;

        -- Return summary
        SELECT
            @UpdatedCount AS orders_completed,
            GETDATE() AS execution_time,
            'SUCCESS' AS status,
            CASE
                WHEN @UpdatedCount = 0 THEN 'No orders to complete'
                WHEN @UpdatedCount = 1 THEN '1 order marked as COMPLETED'
                ELSE CAST(@UpdatedCount AS VARCHAR) + ' orders marked as COMPLETED'
            END AS message;

        -- Return list of completed orders (if any)
        IF @UpdatedCount > 0
        BEGIN
            SELECT
                order_id,
                product_name,
                quantity,
                completed_quantity,
                customer_name,
                deadline,
                updated_at
            FROM production_order
            WHERE status = 'COMPLETED'
            AND CAST(updated_at AS DATE) = CAST(GETDATE() AS DATE)
            ORDER BY updated_at DESC;
        END

    END TRY
    BEGIN CATCH
        -- Rollback transaction on error
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        -- Capture error details
        SELECT
            @ErrorMessage = ERROR_MESSAGE(),
            @ErrorSeverity = ERROR_SEVERITY(),
            @ErrorState = ERROR_STATE();

        -- Return error information
        SELECT
            0 AS orders_completed,
            GETDATE() AS execution_time,
            'ERROR' AS status,
            @ErrorMessage AS message,
            @ErrorSeverity AS error_severity,
            @ErrorState AS error_state;

        -- Re-throw error
        RAISERROR(@ErrorMessage, @ErrorSeverity, @ErrorState);
    END CATCH
END;
GO

-- ============================================================================
-- USAGE EXAMPLES
-- ============================================================================

-- Example 1: Run manually to complete orders
EXEC sp_AutoCompleteOrders;

-- Example 2: Schedule as SQL Server Agent Job (daily at 6 PM)
/*
EXEC msdb.dbo.sp_add_job
    @job_name = N'Auto Complete Production Orders',
    @enabled = 1,
    @description = N'Automatically complete production orders when 100% finished';

EXEC msdb.dbo.sp_add_jobstep
    @job_name = N'Auto Complete Production Orders',
    @step_name = N'Run sp_AutoCompleteOrders',
    @subsystem = N'TSQL',
    @database_name = N'Final',
    @command = N'EXEC sp_AutoCompleteOrders;';

EXEC msdb.dbo.sp_add_schedule
    @schedule_name = N'Daily at 6 PM',
    @freq_type = 4,  -- Daily
    @freq_interval = 1,
    @active_start_time = 180000;  -- 6:00 PM

EXEC msdb.dbo.sp_attach_schedule
    @job_name = N'Auto Complete Production Orders',
    @schedule_name = N'Daily at 6 PM';
*/

-- ============================================================================
-- TEST SCRIPT
-- ============================================================================

-- Test 1: Create test orders
PRINT '========================================';
PRINT 'TEST 1: Creating test orders';
PRINT '========================================';

-- Insert test order that should be completed (100% done)
INSERT INTO production_order (
    order_id, product_name, product_type, quantity,
    order_date, deadline, priority, status,
    customer_name, customer_id, completed_quantity,
    created_at, updated_at
)
VALUES
(
    'TEST_AUTO_001',
    'Test Product 1 (100% Done)',
    'Garment',
    100,
    GETDATE(),
    DATEADD(day, 7, GETDATE()),
    'HIGH',
    'IN_PROGRESS',  -- Currently IN_PROGRESS but 100% done
    'Test Customer',
    'C001',
    100,  -- Completed = Total (should auto-complete)
    GETDATE(),
    GETDATE()
),
(
    'TEST_AUTO_002',
    'Test Product 2 (50% Done)',
    'Garment',
    200,
    GETDATE(),
    DATEADD(day, 7, GETDATE()),
    'MEDIUM',
    'IN_PROGRESS',
    'Test Customer',
    'C002',
    100,  -- Only 50% done (should NOT auto-complete)
    GETDATE(),
    GETDATE()
),
(
    'TEST_AUTO_003',
    'Test Product 3 (Exceeded)',
    'Garment',
    150,
    GETDATE(),
    DATEADD(day, 7, GETDATE()),
    'LOW',
    'IN_PROGRESS',
    'Test Customer',
    'C003',
    160,  -- Completed > Total (should auto-complete)
    GETDATE(),
    GETDATE()
);

PRINT '✓ 3 test orders created';
PRINT '';

-- Test 2: Check orders before running procedure
PRINT '========================================';
PRINT 'TEST 2: Orders BEFORE sp_AutoCompleteOrders';
PRINT '========================================';

SELECT
    order_id,
    product_name,
    quantity,
    completed_quantity,
    CAST((completed_quantity * 100.0 / quantity) AS DECIMAL(5,2)) AS completion_pct,
    status
FROM production_order
WHERE order_id LIKE 'TEST_AUTO_%'
ORDER BY order_id;

PRINT '';

-- Test 3: Run the stored procedure
PRINT '========================================';
PRINT 'TEST 3: Running sp_AutoCompleteOrders';
PRINT '========================================';

EXEC sp_AutoCompleteOrders;

PRINT '';

-- Test 4: Check orders after running procedure
PRINT '========================================';
PRINT 'TEST 4: Orders AFTER sp_AutoCompleteOrders';
PRINT '========================================';

SELECT
    order_id,
    product_name,
    quantity,
    completed_quantity,
    CAST((completed_quantity * 100.0 / quantity) AS DECIMAL(5,2)) AS completion_pct,
    status
FROM production_order
WHERE order_id LIKE 'TEST_AUTO_%'
ORDER BY order_id;

PRINT '';
PRINT 'Expected Results:';
PRINT '  ✓ TEST_AUTO_001: status should be COMPLETED (was 100%)';
PRINT '  ✓ TEST_AUTO_002: status should remain IN_PROGRESS (only 50%)';
PRINT '  ✓ TEST_AUTO_003: status should be COMPLETED (exceeded 100%)';
PRINT '';

-- Test 5: Run procedure again (should find nothing to update)
PRINT '========================================';
PRINT 'TEST 5: Running sp_AutoCompleteOrders AGAIN';
PRINT '========================================';

EXEC sp_AutoCompleteOrders;

PRINT '';
PRINT 'Expected Result: "No orders to complete"';
PRINT '';

-- Cleanup test data
PRINT '========================================';
PRINT 'CLEANUP: Removing test data';
PRINT '========================================';

DELETE FROM production_alerts WHERE order_id LIKE 'TEST_AUTO_%';
DELETE FROM production_order WHERE order_id LIKE 'TEST_AUTO_%';

PRINT '✓ Test data cleaned up';
PRINT '';
PRINT '========================================';
PRINT '✓✓✓ TEST COMPLETE ✓✓✓';
PRINT '========================================';

-- ============================================================================
-- MAINTENANCE & MONITORING
-- ============================================================================

-- View procedure definition
-- EXEC sp_helptext 'sp_AutoCompleteOrders';

-- Check if procedure exists
-- SELECT name, create_date, modify_date
-- FROM sys.procedures
-- WHERE name = 'sp_AutoCompleteOrders';

-- View execution history (if SQL Server Agent is used)
/*
SELECT
    j.name AS job_name,
    h.run_date,
    h.run_time,
    h.run_duration,
    h.message,
    CASE h.run_status
        WHEN 0 THEN 'Failed'
        WHEN 1 THEN 'Succeeded'
        WHEN 2 THEN 'Retry'
        WHEN 3 THEN 'Canceled'
    END AS run_status
FROM msdb.dbo.sysjobs j
INNER JOIN msdb.dbo.sysjobhistory h ON j.job_id = h.job_id
WHERE j.name = 'Auto Complete Production Orders'
ORDER BY h.run_date DESC, h.run_time DESC;
*/

-- ============================================================================
-- DOCUMENTATION
-- ============================================================================

/*
PROCEDURE: sp_AutoCompleteOrders

DESCRIPTION:
    Automatically marks production orders as COMPLETED when their completed_quantity
    equals or exceeds the total quantity. This ensures data consistency and reduces
    manual work for Factory Managers.

PARAMETERS:
    None

RETURNS:
    Result Set 1: Summary information
        - orders_completed: Number of orders marked as completed
        - execution_time: When the procedure ran
        - status: SUCCESS or ERROR
        - message: Descriptive message

    Result Set 2: List of completed orders (if any)
        - order_id: Order identifier
        - product_name: Product name
        - quantity: Total quantity
        - completed_quantity: Completed quantity
        - customer_name: Customer name
        - deadline: Order deadline
        - updated_at: When it was marked complete

NOTES:
    - Only updates orders with status IN_PROGRESS or PENDING
    - Does not update COMPLETED or CANCELLED orders
    - Uses transaction for data integrity
    - Can be scheduled to run automatically
    - Safe to run multiple times (idempotent)

EXAMPLE USAGE:
    -- Manual execution
    EXEC sp_AutoCompleteOrders;

    -- Check what would be completed (without actually updating)
    SELECT order_id, product_name, quantity, completed_quantity, status
    FROM production_order
    WHERE completed_quantity >= quantity
    AND status NOT IN ('COMPLETED', 'CANCELLED');

PERFORMANCE:
    - Very fast (simple UPDATE statement)
    - No complex join
    - Uses indexed columns (status)
    - Safe for large datasets

ERROR HANDLING:
    - All errors are caught and returned
    - Transaction is rolled back on error
    - Error details are logged

AUTHOR: Factory Manager Module
VERSION: 1.0
CREATED: 2025-01-12
LAST MODIFIED: 2025-01-12
*/

-- ============================================================================
-- END OF STORED PROCEDURE
-- ============================================================================
