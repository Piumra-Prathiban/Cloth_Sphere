-- ============================================================================
-- Trigger: trg_AutoCompleteSchedule
-- Purpose: Auto-update schedule status to COMPLETED when 100% done
-- ============================================================================

-- Drop if exists
IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'trg_AutoCompleteSchedule')
    DROP TRIGGER trg_AutoCompleteSchedule;
GO

-- Create trigger
CREATE TRIGGER trg_AutoCompleteSchedule
ON production_schedule
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE production_schedule
    SET status = 'COMPLETED',
        updated_at = GETDATE()
    WHERE schedule_id IN (
        SELECT schedule_id FROM inserted
        WHERE completed_quantity >= assigned_quantity
        AND status != 'COMPLETED'
    );
END;
GO

-- ============================================================================
-- USAGE & TEST
-- ============================================================================

-- Test: Insert schedule
INSERT INTO production_schedule (
    schedule_id, order_id, workstation_id, scheduled_date,
    assigned_quantity, completed_quantity, status, shift,
    created_at, updated_at
)
VALUES (
    'TEST_SCH_001', 'po001', 'ws001', CAST(GETDATE() AS DATE),
    100, 0, 'IN_PROGRESS', 'MORNING',
    GETDATE(), GETDATE()
);

-- Check status
SELECT schedule_id, assigned_quantity, completed_quantity, status
FROM production_schedule WHERE schedule_id = 'TEST_SCH_001';

-- Update to 100% (trigger fires!)
UPDATE production_schedule
SET completed_quantity = 100
WHERE schedule_id = 'TEST_SCH_001';

-- Check status (should be COMPLETED!)
SELECT schedule_id, assigned_quantity, completed_quantity, status
FROM production_schedule WHERE schedule_id = 'TEST_SCH_001';

-- Cleanup
DELETE FROM production_schedule WHERE schedule_id = 'TEST_SCH_001';

PRINT '✓ Trigger test complete';

-- ============================================================================
-- END
-- ============================================================================
