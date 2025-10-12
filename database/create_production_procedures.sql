-- =====================================================
-- Factory Manager Production Summary Report Procedure
-- =====================================================
-- Database: SQL Server
-- Purpose: Generate comprehensive production summary for analysis
-- =====================================================

USE Clothsphere;
GO

-- Drop procedure if exists
IF OBJECT_ID('dbo.generate_production_report', 'P') IS NOT NULL
    DROP PROCEDURE dbo.generate_production_report;
GO

-- Create the stored procedure
CREATE PROCEDURE dbo.generate_production_report
    @start_date DATE,
    @end_date DATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Production Summary Report
    SELECT
        po.status,
        po.priority,
        COUNT(*) as total_orders,
        SUM(po.quantity) as total_quantity,
        SUM(po.completed_quantity) as completed_quantity,
        CASE
            WHEN SUM(po.quantity) > 0
            THEN ROUND(CAST(SUM(po.completed_quantity) AS FLOAT) / SUM(po.quantity) * 100, 2)
            ELSE 0
        END as avg_completion_rate,
        SUM(CASE
            WHEN po.deadline < CAST(GETDATE() AS DATE) AND po.status != 'COMPLETED'
            THEN 1
            ELSE 0
        END) as delayed_count,
        MIN(po.order_date) as earliest_order,
        MAX(po.deadline) as latest_deadline
    FROM production_order po
    WHERE po.order_date BETWEEN @start_date AND @end_date
    GROUP BY po.status, po.priority
    ORDER BY
        CASE po.priority
            WHEN 'HIGH' THEN 1
            WHEN 'MEDIUM' THEN 2
            WHEN 'LOW' THEN 3
            ELSE 4
        END,
        po.status;
END;
GO

-- =====================================================
-- Additional Helper Procedure: Get Production Overview
-- =====================================================

IF OBJECT_ID('dbo.get_production_overview', 'P') IS NOT NULL
    DROP PROCEDURE dbo.get_production_overview;
GO

CREATE PROCEDURE dbo.get_production_overview
    @start_date DATE,
    @end_date DATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Overall production statistics
    SELECT
        COUNT(*) as total_orders,
        SUM(quantity) as total_planned_quantity,
        SUM(completed_quantity) as total_completed_quantity,
        SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_orders,
        SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress_orders,
        SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pending_orders,
        SUM(CASE WHEN status = 'DELAYED' THEN 1 ELSE 0 END) as delayed_orders,
        SUM(CASE WHEN priority = 'HIGH' THEN 1 ELSE 0 END) as high_priority_orders,
        SUM(CASE WHEN priority = 'MEDIUM' THEN 1 ELSE 0 END) as medium_priority_orders,
        SUM(CASE WHEN priority = 'LOW' THEN 1 ELSE 0 END) as low_priority_orders,
        CASE
            WHEN SUM(quantity) > 0
            THEN ROUND(CAST(SUM(completed_quantity) AS FLOAT) / SUM(quantity) * 100, 2)
            ELSE 0
        END as overall_completion_rate
    FROM production_order
    WHERE order_date BETWEEN @start_date AND @end_date;
END;
GO

-- =====================================================
-- Test the procedures
-- =====================================================

-- Example usage:
-- EXEC dbo.generate_production_report @start_date = '2025-01-01', @end_date = '2025-12-31';
-- EXEC dbo.get_production_overview @start_date = '2025-01-01', @end_date = '2025-12-31';

PRINT 'Production procedures created successfully!';
GO
