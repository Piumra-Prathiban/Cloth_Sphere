# 📊 Factory Manager (FM) Database Tables - Complete Analysis

## 🔍 Overview
This document analyzes **ALL database tables** that Factory Manager reads from and writes to in the Cloth Sphere system.

---

## 📋 **TABLES USED BY FACTORY MANAGER**

### ✅ **1. production_order** (PRIMARY TABLE)
**Table Name:** `production_order`
**Model:** `ProductionOrder.java`
**Repository:** `ProductionOrderRepository.java`

#### **Purpose:**
Stores all production orders that Factory Manager creates and manages.

#### **Operations:**
- ✅ **READ** - View all production orders
- ✅ **WRITE** - Create new production orders
- ✅ **UPDATE** - Update order details, status, completion
- ✅ **DELETE** - Delete production orders

#### **Columns:**
```sql
order_id VARCHAR(10) PRIMARY KEY          -- Auto-generated (po001, po002...)
product_name VARCHAR(200)                 -- Product being manufactured
product_type VARCHAR(100)                 -- Type of product (Garment, etc.)
quantity INT                              -- Total quantity to produce
order_date DATE                           -- When order was created
deadline DATE                             -- Due date (TRIGGERS ALERT!)
priority VARCHAR(20)                      -- HIGH, MEDIUM, LOW
status VARCHAR(30)                        -- PENDING, IN_PROGRESS, COMPLETED, DELAYED
customer_name VARCHAR(100)                -- Customer who placed order
customer_id VARCHAR(10)                   -- Customer reference
completed_quantity INT DEFAULT 0          -- How much is done
created_at DATETIME                       -- Record creation timestamp
updated_at DATETIME                       -- Last update timestamp
notes VARCHAR(500)                        -- Additional notes
```

#### **Trigger Connection:**
⚠️ **ALERT TRIGGER FIRES ON THIS TABLE!**
- When `deadline` is within 3 days → Creates alert in `production_alerts`
- When `deadline` is updated → May create new alert
- When `status` = 'COMPLETED' → No more alerts

---

### ✅ **2. production_schedule**
**Table Name:** `production_schedule`
**Model:** `ProductionSchedule.java`
**Repository:** `ProductionScheduleRepository.java`

#### **Purpose:**
Schedules production tasks for specific workstations and time slots.

#### **Operations:**
- ✅ **READ** - View all schedules
- ✅ **WRITE** - Create new schedules
- ✅ **UPDATE** - Update schedule details and progress
- ✅ **DELETE** - Delete schedules

#### **Columns:**
```sql
schedule_id VARCHAR(10) PRIMARY KEY       -- Auto-generated (sch001, sch002...)
order_id VARCHAR(10) FK                   -- Links to production_order
workstation_id VARCHAR(10) FK             -- Links to workstation
scheduled_date DATE                       -- When work is scheduled
start_time DATETIME                       -- Actual start time
end_time DATETIME                         -- Actual end time
estimated_hours DECIMAL                   -- Expected time
actual_hours DECIMAL                      -- Real time taken
assigned_quantity INT                     -- How much to produce
completed_quantity INT DEFAULT 0          -- How much is done
status VARCHAR(30)                        -- SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
shift VARCHAR(20)                         -- MORNING, AFTERNOON, NIGHT
created_at DATETIME
updated_at DATETIME
notes VARCHAR(500)
```

#### **Foreign Keys:**
- `order_id` → `production_order.order_id`
- `workstation_id` → `workstation.workstation_id`

---

### ✅ **3. workstation**
**Table Name:** `workstation`
**Model:** `WorkStation.java`
**Repository:** `WorkStationRepository.java`

#### **Purpose:**
Manages factory workstations where production happens.

#### **Operations:**
- ✅ **READ** - View all workstations
- ✅ **WRITE** - Create new workstations
- ✅ **UPDATE** - Update workstation status, capacity, load
- ✅ **DELETE** - Delete workstations (rare)

#### **Columns:**
```sql
workstation_id VARCHAR(10) PRIMARY KEY    -- Auto-generated (ws001, ws002...)
workstation_name VARCHAR(100)             -- Display name
workstation_type VARCHAR(50)              -- CUTTING, SEWING, FINISHING, QUALITY_CHECK, PACKING
capacity INT                              -- Max workers
current_load INT DEFAULT 0                -- Current workers assigned
status VARCHAR(20)                        -- ACTIVE, INACTIVE, MAINTENANCE
location VARCHAR(100)                     -- Physical location
supervisor_id VARCHAR(10)                 -- Supervisor employee ID
supervisor_name VARCHAR(100)              -- Supervisor name
equipment_details VARCHAR(500)            -- Equipment information
created_at DATETIME
updated_at DATETIME
```

---

### ✅ **4. staff_assignment**
**Table Name:** `staff_assignment`
**Model:** `StaffAssignment.java`
**Repository:** `StaffAssignmentRepository.java`

#### **Purpose:**
Assigns employees to production schedules and workstations.

#### **Operations:**
- ✅ **READ** - View all assignments
- ✅ **WRITE** - Create new assignments
- ✅ **UPDATE** - Update assignment status and progress
- ✅ **DELETE** - Delete assignments

#### **Columns:**
```sql
assignment_id VARCHAR(10) PRIMARY KEY     -- Auto-generated (sa001, sa002...)
schedule_id VARCHAR(10) FK                -- Links to production_schedule
employee_id VARCHAR(10) FK                -- Links to employee (HR table)
workstation_id VARCHAR(10) FK             -- Links to workstation
assignment_date DATE                      -- Date of assignment
shift VARCHAR(20)                         -- MORNING, AFTERNOON, NIGHT
role VARCHAR(50)                          -- OPERATOR, QUALITY_CHECKER, SUPERVISOR
status VARCHAR(20)                        -- ASSIGNED, WORKING, COMPLETED, ABSENT
assigned_quantity INT                     -- Target quantity
completed_quantity INT DEFAULT 0          -- Actual completed
start_time DATETIME
end_time DATETIME
created_at DATETIME
updated_at DATETIME
notes VARCHAR(500)
```

#### **Foreign Keys:**
- `schedule_id` → `production_schedule.schedule_id`
- `employee_id` → `employee.id` (HR table)
- `workstation_id` → `workstation.workstation_id`

---

### ✅ **5. performance_metrics**
**Table Name:** `performance_metrics`
**Model:** `PerformanceMetrics.java`
**Repository:** `PerformanceMetricsRepository.java`

#### **Purpose:**
Records daily performance metrics for production analysis.

#### **Operations:**
- ✅ **READ** - View all metrics
- ✅ **WRITE** - Create new performance records
- ❌ **UPDATE** - Rarely updated (historical data)
- ❌ **DELETE** - Rarely deleted (historical data)

#### **Columns:**
```sql
metric_id VARCHAR(10) PRIMARY KEY         -- Auto-generated (pm001, pm002...)
record_date DATE                          -- Date of record
workstation_id VARCHAR(10) FK             -- Links to workstation
employee_id VARCHAR(10) FK                -- Links to employee
order_id VARCHAR(10) FK                   -- Links to production_order
schedule_id VARCHAR(10) FK                -- Links to production_schedule
target_quantity INT                       -- Expected output
actual_quantity INT                       -- Real output
defect_quantity INT DEFAULT 0             -- Defective items
efficiency_rate DECIMAL                   -- Auto-calculated %
quality_rate DECIMAL                      -- Auto-calculated %
downtime_hours DECIMAL DEFAULT 0          -- Machine downtime
working_hours DECIMAL                     -- Total hours worked
overtime_hours DECIMAL DEFAULT 0          -- Extra hours
delay_hours DECIMAL DEFAULT 0             -- Delay time
remarks VARCHAR(500)                      -- Notes
created_at DATETIME
updated_at DATETIME
```

#### **Foreign Keys:**
- `workstation_id` → `workstation.workstation_id`
- `employee_id` → `employee.id`
- `order_id` → `production_order.order_id`
- `schedule_id` → `production_schedule.schedule_id`

---

### ✅ **6. production_alerts** ⭐ **NEW TABLE**
**Table Name:** `production_alerts`
**Model:** `ProductionAlert.java`
**Repository:** `ProductionAlertRepository.java`

#### **Purpose:**
Stores deadline warning alerts automatically created by trigger.

#### **Operations:**
- ✅ **READ** - View alerts in dashboard
- ✅ **WRITE** - Automatically created by trigger (rarely manual)
- ✅ **UPDATE** - Mark as read, dismiss alerts
- ✅ **DELETE** - Delete old/dismissed alerts

#### **Columns:**
```sql
alert_id VARCHAR(15) PRIMARY KEY          -- Auto-generated (ALERT20250112...)
order_id VARCHAR(10) FK                   -- Links to production_order
alert_type VARCHAR(50)                    -- DEADLINE_WARNING, OVERDUE, URGENT, QUALITY_ISSUE
severity VARCHAR(20)                      -- LOW, MEDIUM, HIGH, CRITICAL
message VARCHAR(500)                      -- Alert message text
product_name VARCHAR(200)                 -- Product name (copied from order)
deadline DATE                             -- Deadline date (copied from order)
days_remaining INT                        -- Calculated days until deadline
is_read BIT DEFAULT 0                     -- Has FM read this alert?
is_dismissed BIT DEFAULT 0                -- Has FM dismissed this alert?
created_at DATETIME                       -- When alert was created
read_at DATETIME                          -- When it was marked read
dismissed_at DATETIME                     -- When it was dismissed
```

#### **Foreign Keys:**
- `order_id` → `production_order.order_id` (CASCADE DELETE)

#### **Automatically Created By:**
- Trigger: `trg_production_deadline_warning` on `production_order` table

---

### ⚠️ **7. employee** (READ ONLY - from HR)
**Table Name:** `employee`
**Model:** `Employee.java` (HR module)
**Repository:** `EmployeeRepository.java` (HR module)

#### **Purpose:**
Stores employee information managed by HR department.

#### **Operations by FM:**
- ✅ **READ** - View employees for assignment
- ❌ **WRITE** - Cannot create employees (HR only)
- ❌ **UPDATE** - Cannot update employees (HR only)
- ❌ **DELETE** - Cannot delete employees (HR only)

#### **Usage:**
Factory Manager reads this table to:
- Assign employees to production tasks
- View employee names in staff assignments
- Check employee availability

---

## 📊 **SUMMARY TABLE**

| # | Table Name | Read | Write | Update | Delete | Trigger Source | New? |
|---|-----------|------|-------|--------|--------|----------------|------|
| 1 | production_order | ✅ | ✅ | ✅ | ✅ | **YES (Creates Alerts)** | ❌ |
| 2 | production_schedule | ✅ | ✅ | ✅ | ✅ | No | ❌ |
| 3 | workstation | ✅ | ✅ | ✅ | ✅ | No | ❌ |
| 4 | staff_assignment | ✅ | ✅ | ✅ | ✅ | No | ❌ |
| 5 | performance_metrics | ✅ | ✅ | Rare | Rare | No | ❌ |
| 6 | **production_alerts** | ✅ | Auto | ✅ | ✅ | No | **✅ NEW** |
| 7 | employee (HR) | ✅ | ❌ | ❌ | ❌ | No | ❌ |

---

## 🔄 **DATA FLOW DIAGRAM**

```
Factory Manager Actions:
         |
         v
1. CREATE production_order
         |
         v
   [TRIGGER FIRES] ← If deadline ≤ 3 days
         |
         v
   INSERT INTO production_alerts
         |
         v
2. CREATE production_schedule (linked to order)
         |
         v
3. ASSIGN workstation to schedule
         |
         v
4. CREATE staff_assignment (assign employees)
         |
         v
5. RECORD performance_metrics (daily)
         |
         v
6. UPDATE production_order.completed_quantity
         |
         v
   [TRIGGER FIRES AGAIN] ← If status != COMPLETED
         |
         v
   MAY CREATE NEW ALERT
         |
         v
7. VIEW production_alerts in Dashboard
         |
         v
8. MARK alert as read / DISMISS alert
```

---

## ⚡ **TRIGGER BEHAVIOR SIMULATION**

### **Scenario 1: New Order with Deadline in 2 Days**
```sql
-- FM creates order
INSERT INTO production_order (
    order_id = 'po001',
    product_name = 'Cotton T-Shirt',
    deadline = '2025-01-14',  -- 2 days from today (2025-01-12)
    status = 'PENDING'
);

-- Trigger automatically fires:
INSERT INTO production_alerts (
    alert_id = 'ALERT20250112120000001',
    order_id = 'po001',
    alert_type = 'DEADLINE_WARNING',
    severity = 'MEDIUM',  -- 2 days = MEDIUM
    message = 'Production order po001 for "Cotton T-Shirt" deadline approaching in 2 days',
    days_remaining = 2,
    is_read = 0
);
```

### **Scenario 2: Update Order to Today**
```sql
-- FM updates deadline
UPDATE production_order
SET deadline = '2025-01-12'  -- Today!
WHERE order_id = 'po001';

-- Trigger fires again:
INSERT INTO production_alerts (
    alert_id = 'ALERT20250112140000002',
    order_id = 'po001',
    alert_type = 'URGENT',
    severity = 'CRITICAL',  -- Today = CRITICAL
    message = 'URGENT: Production order po001 for "Cotton T-Shirt" deadline is TODAY!',
    days_remaining = 0,
    is_read = 0
);
```

### **Scenario 3: Mark Order Complete**
```sql
-- FM completes order
UPDATE production_order
SET status = 'COMPLETED'
WHERE order_id = 'po001';

-- Trigger checks status:
-- status = 'COMPLETED' → NO NEW ALERT CREATED ✓
-- Existing alerts remain but won't create new ones
```

### **Scenario 4: Order Becomes Overdue**
```sql
-- Time passes, deadline is now in the past
UPDATE production_order
SET deadline = '2025-01-09'  -- 3 days ago
WHERE order_id = 'po001';

-- Trigger fires:
INSERT INTO production_alerts (
    alert_id = 'ALERT20250112160000003',
    order_id = 'po001',
    alert_type = 'OVERDUE',
    severity = 'CRITICAL',
    message = 'Production order po001 for "Cotton T-Shirt" is OVERDUE by 3 day(s)!',
    days_remaining = -3,
    is_read = 0
);

-- ALSO updates order status:
UPDATE production_order
SET status = 'DELAYED'
WHERE order_id = 'po001';
```

---

## 🎯 **CRITICAL DEPENDENCIES**

### **Must Exist Before Testing Trigger:**

1. ✅ `production_order` table must exist
2. ✅ `production_alerts` table must exist (NEW)
3. ✅ `trg_production_deadline_warning` trigger must be created and enabled
4. ✅ Foreign key constraint from `production_alerts.order_id` → `production_order.order_id`

### **Optional But Recommended:**

5. `production_schedule` table (for full FM workflow)
6. `workstation` table (for scheduling)
7. `staff_assignment` table (for assignments)
8. `performance_metrics` table (for reporting)
9. `employee` table (for staff assignments)

---

## 🧪 **TESTING THE TRIGGER**

### **Prerequisite Check:**
```sql
-- 1. Check production_order exists
SELECT COUNT(*) FROM production_order;

-- 2. Check production_alerts exists
SELECT COUNT(*) FROM production_alerts;

-- 3. Check trigger exists and enabled
SELECT name, is_disabled FROM sys.triggers
WHERE name = 'trg_production_deadline_warning';
-- Should return: is_disabled = 0
```

### **Simple Test:**
```sql
-- Insert order with deadline in 2 days
INSERT INTO production_order (
    order_id, product_name, product_type, quantity,
    order_date, deadline, priority, status,
    customer_name, customer_id, completed_quantity,
    created_at, updated_at
) VALUES (
    'TEST001', 'Test Product', 'Garment', 100,
    GETDATE(), DATEADD(day, 2, CAST(GETDATE() AS DATE)),
    'HIGH', 'PENDING', 'Test Customer', 'C001', 0,
    GETDATE(), GETDATE()
);

-- Check if alert was created
SELECT * FROM production_alerts WHERE order_id = 'TEST001';
-- Expected: 1 row with severity='MEDIUM', days_remaining=2
```

---

## 📝 **NOTES**

1. **Trigger is AUTOMATIC** - No manual code needed to create alerts
2. **Trigger fires on INSERT and UPDATE** of production_order
3. **Duplicate prevention** - Won't create multiple alerts for same order on same day
4. **CASCADE DELETE** - Deleting production_order deletes its alerts
5. **Read-only from HR** - Employee table is managed by HR module
6. **New table added** - production_alerts is a new table specifically for this feature

---

**Document Created:** 2025-01-12
**Purpose:** Database table analysis for Factory Manager trigger implementation
**Status:** Complete and Verified ✅
