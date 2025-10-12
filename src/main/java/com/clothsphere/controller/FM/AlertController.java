package com.clothsphere.controller.FM;

import com.clothsphere.model.FM.ProductionAlert;
import com.clothsphere.service.FM.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/factory/api/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    // ===================== GET ALERTS =====================

    /**
     * Get all alerts
     */
    @GetMapping
    public ResponseEntity<List<ProductionAlert>> getAllAlerts() {
        try {
            List<ProductionAlert> alerts = alertService.getAllAlerts();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get alert by ID
     */
    @GetMapping("/{alertId}")
    public ResponseEntity<ProductionAlert> getAlertById(@PathVariable String alertId) {
        try {
            ProductionAlert alert = alertService.getAlertById(alertId);
            if (alert != null) {
                return ResponseEntity.ok(alert);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get unread alerts
     */
    @GetMapping("/unread")
    public ResponseEntity<List<ProductionAlert>> getUnreadAlerts() {
        try {
            List<ProductionAlert> alerts = alertService.getUnreadAlerts();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get critical alerts
     */
    @GetMapping("/critical")
    public ResponseEntity<List<ProductionAlert>> getCriticalAlerts() {
        try {
            List<ProductionAlert> alerts = alertService.getCriticalAlerts();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get today's alerts
     */
    @GetMapping("/today")
    public ResponseEntity<List<ProductionAlert>> getTodayAlerts() {
        try {
            List<ProductionAlert> alerts = alertService.getTodayAlerts();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get active (not dismissed) alerts
     */
    @GetMapping("/active")
    public ResponseEntity<List<ProductionAlert>> getActiveAlerts() {
        try {
            List<ProductionAlert> alerts = alertService.getActiveAlerts();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get overdue alerts
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<ProductionAlert>> getOverdueAlerts() {
        try {
            List<ProductionAlert> alerts = alertService.getOverdueAlerts();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get alerts by order ID
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<ProductionAlert>> getAlertsByOrderId(@PathVariable String orderId) {
        try {
            List<ProductionAlert> alerts = alertService.getAlertsByOrderId(orderId);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get alerts by severity
     */
    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<ProductionAlert>> getAlertsBySeverity(@PathVariable String severity) {
        try {
            List<ProductionAlert> alerts = alertService.getUnreadAlertsBySeverity(severity);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===================== ALERT COUNTS =====================

    /**
     * Get unread alert count
     */
    @GetMapping("/count/unread")
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("count", alertService.countUnreadAlerts());
            response.put("critical", alertService.countCriticalUnreadAlerts());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get alert statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getAlertStatistics() {
        try {
            Map<String, Object> stats = alertService.getAlertStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get dashboard summary
     */
    @GetMapping("/dashboard-summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        try {
            Map<String, Object> summary = alertService.getDashboardSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===================== ALERT ACTIONS =====================

    /**
     * Mark alert as read
     */
    @PutMapping("/{alertId}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable String alertId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = alertService.markAsRead(alertId);
            if (success) {
                response.put("success", true);
                response.put("message", "Alert marked as read");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Alert not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error marking alert as read: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Mark multiple alerts as read
     */
    @PutMapping("/read/multiple")
    public ResponseEntity<Map<String, Object>> markMultipleAsRead(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            @SuppressWarnings("unchecked")
            List<String> alertIds = (List<String>) requestData.get("alertIds");

            if (alertIds == null || alertIds.isEmpty()) {
                response.put("success", false);
                response.put("message", "No alert IDs provided");
                return ResponseEntity.badRequest().body(response);
            }

            boolean success = alertService.markMultipleAsRead(alertIds);
            response.put("success", success);
            response.put("message", success ? "Alerts marked as read" : "Failed to mark alerts as read");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error marking alerts as read: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Mark all alerts as read
     */
    @PutMapping("/read/all")
    public ResponseEntity<Map<String, Object>> markAllAsRead() {
        Map<String, Object> response = new HashMap<>();
        try {
            int count = alertService.markAllAsRead();
            response.put("success", true);
            response.put("message", "All alerts marked as read");
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error marking all alerts as read: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Dismiss alert
     */
    @PutMapping("/{alertId}/dismiss")
    public ResponseEntity<Map<String, Object>> dismissAlert(@PathVariable String alertId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = alertService.dismissAlert(alertId);
            if (success) {
                response.put("success", true);
                response.put("message", "Alert dismissed");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Alert not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error dismissing alert: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Dismiss multiple alerts
     */
    @PutMapping("/dismiss/multiple")
    public ResponseEntity<Map<String, Object>> dismissMultipleAlerts(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            @SuppressWarnings("unchecked")
            List<String> alertIds = (List<String>) requestData.get("alertIds");

            if (alertIds == null || alertIds.isEmpty()) {
                response.put("success", false);
                response.put("message", "No alert IDs provided");
                return ResponseEntity.badRequest().body(response);
            }

            boolean success = alertService.dismissMultipleAlerts(alertIds);
            response.put("success", success);
            response.put("message", success ? "Alerts dismissed" : "Failed to dismiss alerts");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error dismissing alerts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Dismiss all alerts for an order
     */
    @PutMapping("/dismiss/order/{orderId}")
    public ResponseEntity<Map<String, Object>> dismissAlertsByOrderId(@PathVariable String orderId) {
        Map<String, Object> response = new HashMap<>();
        try {
            int count = alertService.dismissAlertsByOrderId(orderId);
            response.put("success", true);
            response.put("message", "Alerts dismissed for order");
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error dismissing alerts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete alert
     */
    @DeleteMapping("/{alertId}")
    public ResponseEntity<Map<String, Object>> deleteAlert(@PathVariable String alertId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = alertService.deleteAlert(alertId);
            if (success) {
                response.put("success", true);
                response.put("message", "Alert deleted");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Alert not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting alert: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Cleanup old alerts
     */
    @DeleteMapping("/cleanup/{daysOld}")
    public ResponseEntity<Map<String, Object>> cleanupOldAlerts(@PathVariable int daysOld) {
        Map<String, Object> response = new HashMap<>();
        try {
            int count = alertService.deleteOldAlerts(daysOld);
            response.put("success", true);
            response.put("message", "Old alerts cleaned up");
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error cleaning up alerts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Create manual alert
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createManualAlert(@RequestBody ProductionAlert alert) {
        Map<String, Object> response = new HashMap<>();
        try {
            ProductionAlert created = alertService.createManualAlert(alert);
            response.put("success", true);
            response.put("message", "Alert created successfully");
            response.put("alert", created);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating alert: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
