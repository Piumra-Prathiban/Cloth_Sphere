# Deadline Warning Trigger - Deployment Instructions

## 📋 Overview
This document provides step-by-step instructions for deploying the Production Deadline Warning Trigger system for Factory Manager.

---

## 🗄️ Phase 1: Database Setup

### Step 1: Create Production Alerts Table

1. Open **SQL Server Management Studio (SSMS)**
2. Connect to your SQL Server instance
3. Select the database: `Final`
4. Open and execute: `database/create_production_alerts_table.sql`

**Verify:**
```sql
-- Check if table was created
SELECT * FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME = 'production_alerts';

-- Check indexes
EXEC sp_helpindex 'production_alerts';
```

---

### Step 2: Create Deadline Warning Trigger

1. Open and execute: `database/create_deadline_warning_trigger.sql`

**Verify:**
```sql
-- Check if trigger was created
SELECT name, is_disabled
FROM sys.triggers
WHERE name = 'trg_production_deadline_warning';

-- Should return: trg_production_deadline_warning | 0 (enabled)
```

---

## ✅ Phase 2: Test Database Trigger

### Test 1: Insert Production Order with Deadline in 2 Days
```sql
-- Insert test order
INSERT INTO production_order (
    order_id, product_name, product_type, quantity, order_date,
    deadline, priority, status, customer_name, customer_id, completed_quantity
)
VALUES (
    'po999', 'Test Product', 'Garment', 100, GETDATE(),
    DATEADD(day, 2, CAST(GETDATE() AS DATE)),
    'HIGH', 'PENDING', 'Test Customer', 'CUST001', 0
);

-- Check if alert was created
SELECT * FROM production_alerts WHERE order_id = 'po999';
-- Should return 1 alert with severity 'MEDIUM'
```

### Test 2: Update Order to Today's Deadline
```sql
-- Update deadline to today
UPDATE production_order
SET deadline = CAST(GETDATE() AS DATE)
WHERE order_id = 'po999';

-- Check alerts
SELECT * FROM production_alerts WHERE order_id = 'po999' ORDER BY created_at DESC;
-- Should return 2 alerts, newest one with severity 'CRITICAL' and type 'URGENT'
```

### Test 3: Mark Order as Completed
```sql
-- Mark as completed
UPDATE production_order
SET status = 'COMPLETED'
WHERE order_id = 'po999';

-- Try to trigger alert (should NOT create new alert)
UPDATE production_order
SET deadline = DATEADD(day, 1, CAST(GETDATE() AS DATE))
WHERE order_id = 'po999';

-- Check alerts (should still be 2 alerts, no new one)
SELECT COUNT(*) FROM production_alerts WHERE order_id = 'po999';
```

### Cleanup Test Data
```sql
DELETE FROM production_alerts WHERE order_id = 'po999';
DELETE FROM production_order WHERE order_id = 'po999';
```

---

## 🚀 Phase 3: Deploy Backend Code

### Step 1: Verify Files Created
Check that the following files exist:
- ✅ `src/main/java/com/clothsphere/model/FM/ProductionAlert.java`
- ✅ `src/main/java/com/clothsphere/repository/FM/ProductionAlertRepository.java`
- ✅ `src/main/java/com/clothsphere/service/FM/AlertService.java`
- ✅ `src/main/java/com/clothsphere/controller/FM/AlertController.java`

### Step 2: Build Project
```bash
# Navigate to project directory
cd "Cloth_Sphere"

# Build with Maven (if using Maven wrapper)
./mvnw clean install

# Or with Maven
mvn clean install
```

### Step 3: Start Application
```bash
# Run Spring Boot application
./mvnw spring-boot:run

# Or
java -jar target/cloth-sphere-0.0.1-SNAPSHOT.jar
```

**Check logs for:**
- ✅ No compilation errors
- ✅ JPA entity mapping successful
- ✅ Controller endpoints registered

---

## 🌐 Phase 4: Test REST API Endpoints

### Using Browser or Postman:

#### 1. Get Unread Alerts
```
GET http://localhost:8080/factory/api/alerts/unread
```

#### 2. Get Alert Count
```
GET http://localhost:8080/factory/api/alerts/count/unread
```

#### 3. Mark Alert as Read
```
PUT http://localhost:8080/factory/api/alerts/{alertId}/read
```

#### 4. Dismiss Alert
```
PUT http://localhost:8080/factory/api/alerts/{alertId}/dismiss
```

#### 5. Get Dashboard Summary
```
GET http://localhost:8080/factory/api/alerts/dashboard-summary
```

---

## 🖥️ Phase 5: Test Frontend UI

### Step 1: Login as Factory Manager
1. Navigate to: `http://localhost:8080/systemUserLogin`
2. Login with Factory Manager credentials
3. Should redirect to: `http://localhost:8080/factory/dashboard`

### Step 2: Verify Alert Bell Icon
- ✅ Bell icon visible in dashboard header (top right)
- ✅ Badge shows alert count (if alerts exist)
- ✅ Badge is hidden (if no alerts)

### Step 3: Test Alert Dropdown
1. Click the bell icon
2. Dropdown should appear with alerts
3. Each alert should show:
   - Severity badge (color-coded)
   - Alert message
   - Order ID and deadline
   - Days remaining
   - Action buttons (Dismiss, View Order)

### Step 4: Test Alert Actions
1. **Mark as Read**: Click alert → badge count decreases
2. **Mark All as Read**: Click "Mark all as read" → all alerts marked
3. **Dismiss Alert**: Click "Dismiss" → alert removed from list
4. **View Order**: Click "View Order" → navigates to production section

### Step 5: Test Auto-Refresh
- Alerts should refresh automatically every 5 minutes
- Create a new alert in database and wait 5 minutes to verify

---

## 🧪 Phase 6: End-to-End Testing

### Test Scenario 1: Create Order Approaching Deadline
```sql
INSERT INTO production_order (
    order_id, product_name, product_type, quantity, order_date,
    deadline, priority, status, customer_name, customer_id, completed_quantity
)
VALUES (
    'po_test_001', 'Summer T-Shirt', 'Garment', 500, GETDATE(),
    DATEADD(day, 2, CAST(GETDATE() AS DATE)),
    'HIGH', 'IN_PROGRESS', 'ABC Company', 'CUST123', 200
);
```

**Expected Result:**
1. ✅ Alert created in database
2. ✅ Alert appears in dashboard within 5 minutes (or refresh page)
3. ✅ Badge count increases
4. ✅ Alert severity is MEDIUM
5. ✅ Message says "2 days remaining"

### Test Scenario 2: Update to Urgent
```sql
UPDATE production_order
SET deadline = CAST(GETDATE() AS DATE)
WHERE order_id = 'po_test_001';
```

**Expected Result:**
1. ✅ New alert created with severity CRITICAL
2. ✅ Alert type is URGENT
3. ✅ Message says "deadline is TODAY!"

### Test Scenario 3: Complete Order
```sql
UPDATE production_order
SET status = 'COMPLETED', completed_quantity = 500
WHERE order_id = 'po_test_001';
```

**Expected Result:**
1. ✅ No new alerts created
2. ✅ Existing alerts remain (until dismissed)

---

## 🔧 Troubleshooting

### Issue 1: Trigger Not Firing
**Check:**
```sql
-- Verify trigger is enabled
SELECT name, is_disabled FROM sys.triggers
WHERE name = 'trg_production_deadline_warning';

-- If disabled, enable it
ENABLE TRIGGER trg_production_deadline_warning ON production_order;
```

### Issue 2: Alerts Not Showing in Frontend
**Check:**
1. Browser console for JavaScript errors
2. Network tab: verify API calls succeed
3. Check if `/js/alerts.js` is loading (view source)

**Debug:**
```javascript
// Open browser console and run:
fetch('/factory/api/alerts/unread')
  .then(r => r.json())
  .then(d => console.log(d));
```

### Issue 3: Badge Not Updating
**Check:**
1. `alertBadge` element exists in HTML
2. `updateAlertBadge()` function is called
3. Check browser console for errors

### Issue 4: 404 Error on API Calls
**Check:**
1. Application is running
2. Controller is mapped correctly: `/factory/api/alerts`
3. Check application logs for errors

---

## 📊 Monitoring

### Check Alert Statistics
```sql
-- Total alerts
SELECT COUNT(*) as total_alerts FROM production_alerts;

-- Unread alerts
SELECT COUNT(*) as unread_alerts FROM production_alerts
WHERE is_read = 0 AND is_dismissed = 0;

-- Critical alerts
SELECT COUNT(*) as critical_alerts FROM production_alerts
WHERE severity = 'CRITICAL' AND is_dismissed = 0;

-- Alerts by severity
SELECT severity, COUNT(*) as count
FROM production_alerts
WHERE is_dismissed = 0
GROUP BY severity;

-- Recent alerts (last 7 days)
SELECT * FROM production_alerts
WHERE created_at >= DATEADD(day, -7, GETDATE())
ORDER BY created_at DESC;
```

---

## 🗑️ Maintenance

### Cleanup Old Alerts (Older than 30 days)
```sql
-- Via API
DELETE http://localhost:8080/factory/api/alerts/cleanup/30

-- Or via SQL
DELETE FROM production_alerts
WHERE created_at < DATEADD(day, -30, GETDATE());
```

### Cleanup Dismissed Alerts (Older than 7 days)
```sql
DELETE FROM production_alerts
WHERE is_dismissed = 1
AND dismissed_at < DATEADD(day, -7, GETDATE());
```

---

## ✅ Deployment Checklist

- [ ] Database table created
- [ ] Database trigger created and enabled
- [ ] Backend code compiled without errors
- [ ] Application starts successfully
- [ ] API endpoints accessible
- [ ] Frontend UI updated
- [ ] JavaScript file loaded
- [ ] Alert bell icon visible
- [ ] Test data verified trigger works
- [ ] End-to-end test passed
- [ ] Documentation reviewed

---

## 📝 Notes

- **Auto-refresh**: Alerts refresh every 5 minutes
- **Badge limit**: Shows "99+" if count > 99
- **Trigger frequency**: Fires on INSERT/UPDATE of production_order
- **Duplicate prevention**: Trigger prevents duplicate alerts for same order on same day

---

## 🆘 Support

If you encounter issues:
1. Check application logs
2. Check database trigger logs
3. Check browser console for JavaScript errors
4. Verify all files are in correct locations
5. Restart application after code changes

---

**Deployment Date:** _________
**Deployed By:** _________
**Status:** [ ] Success  [ ] Failed  [ ] Partial
