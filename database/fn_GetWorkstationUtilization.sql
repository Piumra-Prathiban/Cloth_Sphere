-- ============================================================================
-- Function: fn_GetWorkstationUtilization
-- Purpose: Calculate workstation utilization percentage
-- Returns: DECIMAL (0-100)
-- ============================================================================

-- Drop if exists
IF EXISTS (SELECT * FROM sys.objects WHERE name = 'fn_GetWorkstationUtilization')
    DROP FUNCTION fn_GetWorkstationUtilization;
GO

-- Create function
CREATE FUNCTION fn_GetWorkstationUtilization(@workstation_id VARCHAR(10))
RETURNS DECIMAL(5,2)
AS
BEGIN
    DECLARE @utilization DECIMAL(5,2);

    SELECT @utilization = (current_load * 100.0 / capacity)
    FROM workstation
    WHERE workstation_id = @workstation_id AND capacity > 0;

    RETURN ISNULL(@utilization, 0);
END;
GO

-- ============================================================================
-- USAGE
-- ============================================================================

-- Example 1: Get utilization for one workstation
SELECT dbo.fn_GetWorkstationUtilization('ws001') AS utilization_pct;

-- Example 2: Get utilization for all workstations
SELECT
    workstation_id,
    workstation_name,
    capacity,
    current_load,
    dbo.fn_GetWorkstationUtilization(workstation_id) AS utilization_pct,
    status
FROM workstation;

-- Example 3: Find overloaded workstations (>80%)
SELECT
    workstation_id,
    workstation_name,
    dbo.fn_GetWorkstationUtilization(workstation_id) AS utilization_pct
FROM workstation
WHERE dbo.fn_GetWorkstationUtilization(workstation_id) > 80;

-- Example 4: Find available workstations (<50%)
SELECT
    workstation_id,
    workstation_name,
    dbo.fn_GetWorkstationUtilization(workstation_id) AS utilization_pct
FROM workstation
WHERE dbo.fn_GetWorkstationUtilization(workstation_id) < 50
AND status = 'ACTIVE';

-- ============================================================================
-- TEST
-- ============================================================================

-- Insert test workstation
INSERT INTO workstation (
    workstation_id, workstation_name, workstation_type,
    capacity, current_load, status, location,
    created_at, updated_at
)
VALUES (
    'TEST_WS_001', 'Test Station', 'SEWING',
    10, 7, 'ACTIVE', 'Floor 1',
    GETDATE(), GETDATE()
);

-- Test function
SELECT dbo.fn_GetWorkstationUtilization('TEST_WS_001') AS utilization;
-- Expected: 70.00

-- Cleanup
DELETE FROM workstation WHERE workstation_id = 'TEST_WS_001';

-- ============================================================================
-- END
-- ============================================================================
