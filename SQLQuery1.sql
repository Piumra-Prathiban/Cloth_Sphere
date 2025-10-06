 CREATE DATABASE Clothsphere
 
 USE Clothsphere
 
 create table calendar_notes (
        id bigint identity not null,
        created_at datetime2(6),
        note_date date not null,
        note_text varchar(2000) not null,
        updated_at datetime2(6),
        user_name varchar(50) not null,
        primary key (id)
    )


    create table department (
        id varchar(10) not null,
        department_name varchar(100) not null,
        description varchar(500),
        manager_id varchar(10),
        salary_budget float(53),
        primary key (id)
    )


    create table employee (
        id varchar(10) not null,
        address varchar(200),
        date_of_birth date,
        email varchar(100) not null,
        full_name varchar(100) not null,
        password varchar(100) not null,
        phone_number varchar(20),
        qualification1 varchar(100),
        qualification2 varchar(100),
        qualification3 varchar(100),
        username varchar(15) not null,
        department_id varchar(10),
        primary key (id)
    )


    create table performance_metrics (
        metric_id varchar(10) not null,
        actual_quantity int not null,
        created_at datetime2(6),
        defect_quantity int,
        delay_hours float(53),
        downtime_hours float(53),
        efficiency_rate float(53),
        employee_id varchar(10),
        order_id varchar(10),
        overtime_hours float(53),
        quality_rate float(53),
        record_date date not null,
        remarks varchar(500),
        schedule_id varchar(10),
        target_quantity int not null,
        updated_at datetime2(6),
        working_hours float(53),
        workstation_id varchar(10),
        primary key (metric_id)
    )


    create table production_order (
        order_id varchar(10) not null,
        completed_quantity int,
        created_at datetime2(6),
        customer_id varchar(10),
        customer_name varchar(100),
        deadline date not null,
        notes varchar(500),
        order_date date not null,
        priority varchar(20),
        product_name varchar(200) not null,
        product_type varchar(100),
        quantity int not null,
        status varchar(30),
        updated_at datetime2(6),
        primary key (order_id)
    )

 
    create table production_schedule (
        schedule_id varchar(10) not null,
        actual_hours float(53),
        assigned_quantity int not null,
        completed_quantity int,
        created_at datetime2(6),
        end_time datetime2(6),
        estimated_hours float(53),
        notes varchar(500),
        order_id varchar(10) not null,
        scheduled_date date not null,
        shift varchar(20),
        start_time datetime2(6),
        status varchar(30),
        updated_at datetime2(6),
        workstation_id varchar(10) not null,
        primary key (schedule_id)
    )


    create table production_task (
        task_id varchar(10) not null,
        created_date date not null,
        deadline date not null,
        description varchar(1000),
        priority varchar(2) not null,
        status varchar(20) not null,
        task_name varchar(200) not null,
        department_id varchar(10) not null,
        primary key (task_id)
    )

 
    create table staff_assignment (
        assignment_id varchar(10) not null,
        assigned_quantity int,
        assignment_date date not null,
        completed_quantity int,
        created_at datetime2(6),
        end_time datetime2(6),
        notes varchar(500),
        role varchar(50),
        schedule_id varchar(10) not null,
        shift varchar(20),
        start_time datetime2(6),
        status varchar(20),
        updated_at datetime2(6),
        workstation_id varchar(10) not null,
        employee_id varchar(10) not null,
        primary key (assignment_id)
    )

    create table system_user_login_details (
        user_name varchar(15) not null,
        created_at datetime2(6),
        email varchar(100),
        log_count int not null,
        password varchar(100) not null,
        phone_number varchar(20),
        role varchar(40) not null,
        primary key (user_name)
    )

    create table task_assignment (
        assignment_id varchar(10) not null,
        actual_hours int,
        assigned_date date not null,
        completion_date date,
        estimated_hours int,
        notes varchar(500),
        status varchar(20) not null,
        department_id varchar(10),
        employee_id varchar(10) not null,
        task_id varchar(10) not null,
        primary key (assignment_id)
    )

    create table workstation (
        workstation_id varchar(10) not null,
        capacity int not null,
        created_at datetime2(6),
        current_load int,
        equipment_details varchar(500),
        location varchar(100),
        status varchar(20),
        supervisor_id varchar(10),
        supervisor_name varchar(100),
        updated_at datetime2(6),
        workstation_name varchar(100) not null,
        workstation_type varchar(50),
        primary key (workstation_id)
    )

    create table leave_requests (
        leave_id varchar(255) not null,
        action_date datetime2(6),
        comments varchar(255),
        end_date date not null,
        reason varchar(255) not null,
        request_date datetime2(6),
        start_date date not null,
        status varchar(255) not null,
        employee_id varchar(10) not null,
        primary key (leave_id)
    )

    alter table employee 
       add constraint FKbejtwvg9bxus2mffsm3swj3u9 
       foreign key (department_id) 
       references department

    alter table production_task 
       add constraint FKgau98s6v7r7a540pwnsrsxeb5 
       foreign key (department_id) 
       references department
 
    alter table staff_assignment 
       add constraint FKqmst10pngbs2cbh3j1e1cb5ef 
       foreign key (employee_id) 
       references employee
 
    alter table task_assignment 
       add constraint FKldodkl3dkxcefht1f4n0g5plp 
       foreign key (department_id) 
       references department

 
    alter table task_assignment 
       add constraint FK9g6ly30skmgkrssxdby9w9wm4 
       foreign key (employee_id) 
       references employee

    alter table task_assignment 
       add constraint FKsfsf7dl697fju7fiboq7bfivr 
       foreign key (task_id) 
       references production_task

    alter table leave_requests 
       add constraint FKexiel1b0akn6l2gdv3d0io812 
       foreign key (employee_id) 
       references employee



	   INSERT INTO system_user_login_details (role, email, user_name, password, phone_number, log_count) 
	   VALUES 
	   ('hr-manager', 'rsith@gmail.com', 'rasith', '12345678', '0412245720', 0),
	   ('factory-manager', 'piumara@gmail.com', 'piumara', '12345678', '0413345720', 0),
	   ('inventory-manager', 'ama@gmail.com', 'ama', '12345678', '0413345720', 0),
	   ('customer-officer', 'reshani@gmail.com', 'reshani', '12345678', '0413345720', 0),
	   ('sales-executive', 'hiruni@gmail.com', 'hiruni', '12345678', '0413345720', 0);

Select *
from attendance

SELECT *
FROM system_user_login_details

INSERT INTO attendance (employee_id, attendance_date, check_in_time, check_out_time, status, work_hours, notes, created_at, updated_at)
VALUES 
('emp01', '2025-10-01', '08:30:00', '17:15:00', 'PRESENT', 8.75, 'On time', '2025-10-01 08:30:00', '2025-10-01 17:15:00'),
('emp01', '2025-10-02', '08:45:00', '17:30:00', 'PRESENT', 8.75, 'Slightly late', '2025-10-02 08:45:00', '2025-10-02 17:30:00'),
('emp01', '2025-10-03', '09:00:00', '17:00:00', 'PRESENT', 8.00, 'On time', '2025-10-03 09:00:00', '2025-10-03 17:00:00'),
('emp02', '2025-10-01', '09:00:00', '17:00:00', 'PRESENT', 8.00, 'On time', '2025-10-01 09:00:00', '2025-10-03 17:00:00'),
('emp02', '2025-10-03', '09:00:00', '17:00:00', 'PRESENT', 8.00, 'On time', '2025-10-03 09:00:00', '2025-10-03 17:00:00'),
('emp03', '2025-10-01', '08:30:00', '17:15:00', 'PRESENT', 8.75, 'On time', '2025-10-01 08:30:00', '2025-10-01 17:15:00'),
('emp03', '2025-10-02', '08:45:00', '17:30:00', 'PRESENT', 8.75, 'Slightly late', '2025-10-02 08:45:00', '2025-10-02 17:30:00'),
('emp03', '2025-10-03', '09:00:00', '17:00:00', 'PRESENT', 8.00, 'On time', '2025-10-03 09:00:00', '2025-10-03 17:00:00');

DELETE FROM attendance
WHERE  attendance_id = '17';

SElect *
FROM attendance

SElect *
FROM leave_requests


DELETE FROM employee
WHERE id = 'emp02';

ALTER TABLE employee
ADD CONSTRAINT emailUniq UNIQUE (email);

INSERT INTO system_user_login_details (role, email, user_name, password, phone_number, log_count, created_at)
VALUES
('hr-manager', 'rsith@gmail.com', 'rasith', '12345678', '0412245720', 0, CURRENT_TIMESTAMP),
('factory-manager', 'piumara@gmail.com', 'piumara', '12345678', '0413345720', 0, CURRENT_TIMESTAMP),
('inventory-manager', 'ama@gmail.com', 'ama', '12345678', '0413345720', 0, CURRENT_TIMESTAMP),
('customer-officer', 'reshani@gmail.com', 'reshani', '12345678', '0413345720', 0, CURRENT_TIMESTAMP),
('sales-executive', 'hiruni@gmail.com', 'hiruni', '12345678', '0413345720', 0, CURRENT_TIMESTAMP);

SELECT *
FROM system_user_login_details


SELECT *
FROM payroll

SELECT *
FROM monthly_attendance_summary

DELETE FROM payroll
WHERE employee_id = 'emp01';





INSERT INTO attendance (employee_id, attendance_date, check_in_time, check_out_time, status, work_hours, notes, created_at, updated_at)
VALUES
('emp01', '2025-09-01', '08:35:00', '17:20:00', 'PRESENT', 8.75, 'On time', '2025-09-01 08:35:00', '2025-09-01 17:20:00'),
('emp01', '2025-09-02', '08:50:00', '17:30:00', 'PRESENT', 8.67, 'Slightly late', '2025-09-02 08:50:00', '2025-09-02 17:30:00'),
('emp01', '2025-09-03', '09:05:00', '17:10:00', 'PRESENT', 8.08, 'Arrived late', '2025-09-03 09:05:00', '2025-09-03 17:10:00'),
('emp01', '2025-09-04', '08:40:00', '17:25:00', 'PRESENT', 8.75, 'Good performance', '2025-09-04 08:40:00', '2025-09-04 17:25:00'),
('emp01', '2025-09-05', '08:30:00', '17:00:00', 'PRESENT', 8.50, 'On time', '2025-09-05 08:30:00', '2025-09-05 17:00:00'),

('emp01', '2025-09-08', '09:00:00', '17:10:00', 'PRESENT', 8.17, 'Late by 30 min', '2025-09-08 09:00:00', '2025-09-08 17:10:00'),
('emp01', '2025-09-09', '08:25:00', '17:15:00', 'PRESENT', 8.83, 'Early check-in', '2025-09-09 08:25:00', '2025-09-09 17:15:00'),
('emp01', '2025-09-10', '08:45:00', '17:00:00', 'PRESENT', 8.25, 'Normal day', '2025-09-10 08:45:00', '2025-09-10 17:00:00'),
('emp01', '2025-09-11', '08:40:00', '17:20:00', 'PRESENT', 8.67, 'Good day', '2025-09-11 08:40:00', '2025-09-11 17:20:00'),
('emp01', '2025-09-12', '08:30:00', '17:00:00', 'PRESENT', 8.50, 'On time', '2025-09-12 08:30:00', '2025-09-12 17:00:00'),

('emp01', '2025-09-15', '08:55:00', '17:10:00', 'PRESENT', 8.25, 'Slightly late', '2025-09-15 08:55:00', '2025-09-15 17:10:00'),
('emp01', '2025-09-16', '08:35:00', '17:25:00', 'PRESENT', 8.83, 'Productive day', '2025-09-16 08:35:00', '2025-09-16 17:25:00'),
('emp01', '2025-09-17', '08:45:00', '17:05:00', 'PRESENT', 8.33, 'Normal schedule', '2025-09-17 08:45:00', '2025-09-17 17:05:00'),
('emp01', '2025-09-18', '08:40:00', '17:30:00', 'PRESENT', 8.83, 'Stayed late', '2025-09-18 08:40:00', '2025-09-18 17:30:00'),
('emp01', '2025-09-19', '08:50:00', '17:00:00', 'PRESENT', 8.17, 'Slightly late', '2025-09-19 08:50:00', '2025-09-19 17:00:00'),

('emp01', '2025-09-22', '08:30:00', '17:15:00', 'PRESENT', 8.75, 'On time', '2025-09-22 08:30:00', '2025-09-22 17:15:00'),
('emp01', '2025-09-23', '09:05:00', '17:20:00', 'PRESENT', 8.25, 'Late arrival', '2025-09-23 09:05:00', '2025-09-23 17:20:00'),
('emp01', '2025-09-24', '08:40:00', '17:30:00', 'PRESENT', 8.83, 'Good work hours', '2025-09-24 08:40:00', '2025-09-24 17:30:00'),
('emp01', '2025-09-25', '08:45:00', '17:10:00', 'PRESENT', 8.42, 'Normal day', '2025-09-25 08:45:00', '2025-09-25 17:10:00'),
('emp01', '2025-09-26', '08:35:00', '17:25:00', 'PRESENT', 8.83, 'On time', '2025-09-26 08:35:00', '2025-09-26 17:25:00'),

('emp01', '2025-09-29', '08:30:00', '17:00:00', 'PRESENT', 8.50, 'On time', '2025-09-29 08:30:00', '2025-09-29 17:00:00'),
('emp01', '2025-09-30', '09:00:00', '17:15:00', 'PRESENT', 8.25, 'Slightly late', '2025-09-30 09:00:00', '2025-09-30 17:15:00');

SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'payroll'
ORDER BY ORDINAL_POSITION;

SELECT *
FROM internal_communications

-- Communication table
CREATE TABLE internal_communications (
    message_id INT IDENTITY(1,1) PRIMARY KEY,
    sender_email varchar(100) NOT NULL,
    receiver_email varchar(100) NOT NULL,
    subject NVARCHAR(500) NOT NULL,
    message_text NVARCHAR(MAX) NOT NULL,
    sent_date DATETIME2 DEFAULT GETDATE(),
    is_read BIT DEFAULT 0,
    is_deleted BIT DEFAULT 0,
);

-- Create index for better performance
CREATE INDEX idx_sender_receiver ON internal_communications(sender_email, receiver_email);
CREATE INDEX idx_sent_date ON internal_communications(sent_date);

select * 
from system_user_login_details
WHERE role = 'Employee';

--/////////////////////////////////////////////////////////////////////////////////////////////////
-- 1. SUBQUERY EXAMPLE: Get users who have sent more than 5 messages
SELECT su.user_name, su.role, su.email
FROM system_user_login_details su
WHERE su.email IN (
    SELECT sender_email 
    FROM internal_communications 
    GROUP BY sender_email 
    HAVING COUNT(*) > 5
);

-- 2. GROUPING WITH HAVING: Get message statistics by role
SELECT 
    su.role,
    COUNT(ic.message_id) as total_messages_sent,
    AVG(LEN(ic.message_text)) as avg_message_length
FROM system_user_login_details su
JOIN internal_communications ic ON su.email = ic.sender_email
GROUP BY su.role
HAVING COUNT(ic.message_id) > 0
ORDER BY total_messages_sent DESC;

-- 3. STORED PROCEDURE: Send message with validation
CREATE PROCEDURE sp_SendInternalMessage
    @sender_email NVARCHAR(255),
    @receiver_email NVARCHAR(255),
    @subject NVARCHAR(500),
    @message_text NVARCHAR(MAX),
    @result INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Check if sender exists and is active
    IF NOT EXISTS (SELECT 1 FROM system_user_login_details WHERE email = @sender_email)
    BEGIN
        SET @result = 0; -- Sender not found
        RETURN;
    END
    
    -- Check if receiver exists and is active
    IF NOT EXISTS (SELECT 1 FROM system_user_login_details WHERE email = @receiver_email)
    BEGIN
        SET @result = -1; -- Receiver not found
        RETURN;
    END
    
    -- Check if employee is trying to message factory manager
    DECLARE @sender_role NVARCHAR(50), @receiver_role NVARCHAR(50);
    
    SELECT @sender_role = role FROM system_user_login_details WHERE email = @sender_email;
    SELECT @receiver_role = role FROM system_user_login_details WHERE email = @receiver_email;
    
    IF @sender_role = 'Employee' AND @receiver_role = 'Factory Manager'
    BEGIN
        SET @result = -2; -- Employee cannot message Factory Manager directly
        RETURN;
    END
    
    -- Insert the message
    INSERT INTO internal_communications (sender_email, receiver_email, subject, message_text)
    VALUES (@sender_email, @receiver_email, @subject, @message_text);
    
    SET @result = 1; -- Success
END;
GO
--///////////////////////////////
-- Create the stored procedure for sending messages
CREATE OR ALTER PROCEDURE SendInternalMessage
    @sender_email NVARCHAR(255),
    @receiver_email NVARCHAR(255),
    @subject NVARCHAR(500),
    @message_text NVARCHAR(MAX)
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @result INT = 0;
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- Check if sender exists
        IF NOT EXISTS (SELECT 1 FROM system_user_login_details WHERE email = @sender_email)
        BEGIN
            SET @result = -1; -- Sender not found
            SELECT @result AS Result;
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        -- Check if receiver exists
        IF NOT EXISTS (SELECT 1 FROM system_user_login_details WHERE email = @receiver_email)
        BEGIN
            SET @result = -1; -- Receiver not found
            SELECT @result AS Result;
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        -- Prevent self-messaging
        IF @sender_email = @receiver_email
        BEGIN
            SET @result = -3; -- Cannot message yourself
            SELECT @result AS Result;
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        -- Check if employee is trying to message factory manager
        DECLARE @sender_role NVARCHAR(50), @receiver_role NVARCHAR(50);
        
        SELECT @sender_role = role FROM system_user_login_details WHERE email = @sender_email;
        SELECT @receiver_role = role FROM system_user_login_details WHERE email = @receiver_email;
        
        IF @sender_role = 'Employee' AND @receiver_role = 'Factory Manager'
        BEGIN
            SET @result = -2; -- Employee cannot message Factory Manager directly
            SELECT @result AS Result;
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        -- Insert the message with all required fields
        INSERT INTO internal_communications 
        (sender_email, receiver_email, subject, message_text, sent_date, is_read, is_deleted)
        VALUES 
        (@sender_email, @receiver_email, @subject, @message_text, GETDATE(), 0, 0);
        
        COMMIT TRANSACTION;
        
        SET @result = 1; -- Success
        SELECT @result AS Result;
        
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        
        SET @result = -99; -- General error
        SELECT @result AS Result;
        
        -- Re-throw the error for debugging
        THROW;
    END CATCH;
END;
GO

-- 4. FUNCTION: Get unread message count for a user
CREATE FUNCTION fn_GetUnreadMessageCount(@user_email NVARCHAR(255))
RETURNS INT
AS
BEGIN
    DECLARE @count INT;
    
    SELECT @count = COUNT(*)
    FROM internal_communications 
    WHERE receiver_email = @user_email 
    AND is_read = 0 
    AND is_deleted = 0;
    
    RETURN @count;
END;
GO

-- 5. TRIGGER: Log message activity and prevent self-messaging
CREATE TRIGGER tr_PreventSelfMessage
ON internal_communications
INSTEAD OF INSERT
AS
BEGIN
    SET NOCOUNT ON;
    
    IF EXISTS (SELECT 1 FROM inserted WHERE sender_email = receiver_email)
    BEGIN
        RAISERROR('Cannot send message to yourself', 16, 1);
        RETURN;
    END
    
    INSERT INTO internal_communications (sender_email, receiver_email, subject, message_text, sent_date)
    SELECT sender_email, receiver_email, subject, message_text, GETDATE()
    FROM inserted;
END;
GO
--////////////////
-- 5. TRIGGER: Prevent self-messaging with proper boolean defaults
CREATE OR ALTER TRIGGER tr_PreventSelfMessage
ON internal_communications
INSTEAD OF INSERT
AS
BEGIN
    SET NOCOUNT ON;
    
    IF EXISTS (SELECT 1 FROM inserted WHERE sender_email = receiver_email)
    BEGIN
        RAISERROR('Cannot send message to yourself', 16, 1);
        RETURN;
    END
    
    INSERT INTO internal_communications 
    (sender_email, receiver_email, subject, message_text, sent_date, is_read, is_deleted)
    SELECT 
        sender_email, 
        receiver_email, 
        subject, 
        message_text, 
        ISNULL(sent_date, GETDATE()),
        0, -- is_read = false (unread)
        0  -- is_deleted = false (not deleted)
    FROM inserted;
END;
GO
--/////////////////////////////////////////////////////////////////////////////////////////////////
-- Create the function for unread message count
CREATE FUNCTION GetUnreadMessageCount(@user_email NVARCHAR(255))
RETURNS INT
AS
BEGIN
    DECLARE @count INT;
    
    SELECT @count = COUNT(*)
    FROM internal_communications 
    WHERE receiver_email = @user_email 
    AND is_read = 0 
    AND is_deleted = 0;
    
    RETURN ISNULL(@count, 0);
END;
GO

-- Test data insertion (optional)
INSERT INTO internal_communications (sender_email, receiver_email, subject, message_text, sent_date, is_read, is_deleted)
VALUES 
('piumara@gmail.com', 'rsith@gmail.com', 'Welcome to ClothSphere', 'Welcome to our team! We are excited to have you onboard.', GETDATE(), 0, 0),
('piumara@gmail.com', 'rsith@gmail.com', 'Meeting Schedule', 'Lets schedule a meeting to discuss the new projects.', GETDATE(), 0, 0);
GO

-- Check if trigger exists and drop it
IF OBJECT_ID('tr_PreventSelfMessage', 'TR') IS NOT NULL
    DROP TRIGGER tr_PreventSelfMessage;
GO

-- Check available users in the system
SELECT email, user_name, role 
FROM system_user_login_details;