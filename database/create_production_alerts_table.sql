-- ============================================================================
-- Production Alerts Table for Factory Manager
-- Stores deadline warnings and production alerts
-- ============================================================================

-- Drop table if exists (for clean reinstall)
-- DROP TABLE IF EXISTS production_alerts;

CREATE TABLE production_alerts (
    alert_id VARCHAR(15) PRIMARY KEY,
    order_id VARCHAR(10) NOT NULL,
    alert_type VARCHAR(50) NOT NULL DEFAULT 'DEADLINE_WARNING',
    -- 'DEADLINE_WARNING', 'OVERDUE', 'URGENT', 'QUALITY_ISSUE'
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    -- 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'
    message VARCHAR(500) NOT NULL,
    product_name VARCHAR(200),
    deadline DATE,
    days_remaining INT,
    is_read BIT NOT NULL DEFAULT 0,
    is_dismissed BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    read_at DATETIME,
    dismissed_at DATETIME,
    CONSTRAINT fk_alert_order FOREIGN KEY (order_id)
        REFERENCES production_order(order_id) ON DELETE CASCADE
);

-- ============================================================================
-- INDEXES for Performance Optimization
-- ============================================================================

-- Index for unread alerts (most common query)
CREATE INDEX idx_alerts_unread ON production_alerts(is_read, is_dismissed, created_at DESC);

-- Index for order lookup
CREATE INDEX idx_alerts_order ON production_alerts(order_id);

-- Index for alert type filtering
CREATE INDEX idx_alerts_type ON production_alerts(alert_type);

-- Index for severity filtering
CREATE INDEX idx_alerts_severity ON production_alerts(severity);

-- Index for date range queries
CREATE INDEX idx_alerts_created ON production_alerts(created_at DESC);

-- ============================================================================
-- COMMENTS
-- ============================================================================

EXEC sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'Stores production alerts and deadline warnings for factory manager dashboard',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE',  @level1name = N'production_alerts';

EXEC sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'Unique alert identifier (format: ALERT{timestamp}{seq})',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE',  @level1name = N'production_alerts',
    @level2type = N'COLUMN', @level2name = N'alert_id';

EXEC sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'Reference to production order that triggered the alert',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE',  @level1name = N'production_alerts',
    @level2type = N'COLUMN', @level2name = N'order_id';

-- ============================================================================
-- SAMPLE DATA (Optional - for testing)
-- ============================================================================

-- Uncomment to insert sample alerts for testing
/*
INSERT INTO production_alerts (alert_id, order_id, alert_type, severity, message, product_name, deadline, days_remaining, is_read)
VALUES
    ('ALERT001', 'po001', 'DEADLINE_WARNING', 'HIGH', 'Production deadline approaching in 2 days', 'Cotton T-Shirt', '2025-01-15', 2, 0),
    ('ALERT002', 'po002', 'DEADLINE_WARNING', 'CRITICAL', 'Production deadline approaching in 1 day', 'Denim Jeans', '2025-01-14', 1, 0),
    ('ALERT003', 'po003', 'OVERDUE', 'CRITICAL', 'Production order is overdue by 3 days', 'Summer Dress', '2025-01-10', -3, 0);
*/

-- ============================================================================
-- END OF SCHEMA
-- ============================================================================
