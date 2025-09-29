-- Production management schema for Cloth Sphere
-- Designed for Microsoft SQL Server 2022

CREATE TABLE production_schedule (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    schedule_name NVARCHAR(120) NOT NULL,
    production_line NVARCHAR(100) NULL,
    target_quantity INT NULL,
    status NVARCHAR(30) NULL,
    notes NVARCHAR(500) NULL,
    start_date DATE NULL,
    end_date DATE NULL
);

CREATE TABLE production_task (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    schedule_id BIGINT NOT NULL,
    employee_id NVARCHAR(10) NULL,
    task_name NVARCHAR(150) NOT NULL,
    task_description NVARCHAR(600) NULL,
    status NVARCHAR(30) NULL,
    priority NVARCHAR(20) NULL,
    progress_percent INT NULL,
    due_date DATE NULL,
    urgent_flag BIT NOT NULL DEFAULT 0,
    issue_notes NVARCHAR(500) NULL,
    last_updated DATETIME2 NULL,
    CONSTRAINT fk_task_schedule FOREIGN KEY (schedule_id) REFERENCES production_schedule(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_employee FOREIGN KEY (employee_id) REFERENCES employee(id)
);

CREATE TABLE production_message (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    schedule_id BIGINT NULL,
    subject NVARCHAR(120) NOT NULL,
    message_body NVARCHAR(1000) NOT NULL,
    severity NVARCHAR(20) NULL,
    created_by NVARCHAR(80) NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT fk_message_schedule FOREIGN KEY (schedule_id) REFERENCES production_schedule(id) ON DELETE SET NULL
);

CREATE TABLE production_performance_report (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    schedule_id BIGINT NULL,
    period_start DATE NULL,
    period_end DATE NULL,
    tasks_completed INT NOT NULL,
    tasks_pending INT NOT NULL,
    tasks_overdue INT NOT NULL,
    productivity_score DECIMAL(5,2) NOT NULL,
    generated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT fk_report_schedule FOREIGN KEY (schedule_id) REFERENCES production_schedule(id) ON DELETE SET NULL
);

CREATE INDEX idx_task_schedule ON production_task(schedule_id);
CREATE INDEX idx_task_due_date ON production_task(due_date);
CREATE INDEX idx_message_schedule ON production_message(schedule_id);
CREATE INDEX idx_report_schedule ON production_performance_report(schedule_id);
