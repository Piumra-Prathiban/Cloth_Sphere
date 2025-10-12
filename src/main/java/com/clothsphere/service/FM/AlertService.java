package com.clothsphere.service.FM;

import com.clothsphere.model.FM.ProductionAlert;
import com.clothsphere.repository.FM.ProductionAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AlertService {

    @Autowired
    private ProductionAlertRepository alertRepository;

    // ===================== ALERT RETRIEVAL =====================

    /**
     * Get all alerts ordered by creation date
     */
    public List<ProductionAlert> getAllAlerts() {
        return alertRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get alert by ID
     */
    public ProductionAlert getAlertById(String alertId) {
        return alertRepository.findById(alertId).orElse(null);
    }

    /**
     * Get unread alerts (not dismissed)
     */
    public List<ProductionAlert> getUnreadAlerts() {
        return alertRepository.findUnreadAlerts();
    }

    /**
     * Get unread alerts by severity
     */
    public List<ProductionAlert> getUnreadAlertsBySeverity(String severity) {
        return alertRepository.findUnreadAlertsBySeverity(severity);
    }

    /**
     * Get critical unread alerts
     */
    public List<ProductionAlert> getCriticalAlerts() {
        return alertRepository.findUnreadAlertsBySeverity("CRITICAL");
    }

    /**
     * Get today's alerts
     */
    public List<ProductionAlert> getTodayAlerts() {
        return alertRepository.findTodayAlerts(LocalDate.now());
    }

    /**
     * Get alerts by order ID
     */
    public List<ProductionAlert> getAlertsByOrderId(String orderId) {
        return alertRepository.findByOrderId(orderId);
    }

    /**
     * Get active (not dismissed) alerts
     */
    public List<ProductionAlert> getActiveAlerts() {
        return alertRepository.findActiveAlerts();
    }

    /**
     * Get dismissed alerts
     */
    public List<ProductionAlert> getDismissedAlerts() {
        return alertRepository.findDismissedAlerts();
    }

    /**
     * Get alerts within date range
     */
    public List<ProductionAlert> getAlertsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return alertRepository.findByDateRange(startDate, endDate);
    }

    /**
     * Get overdue alerts
     */
    public List<ProductionAlert> getOverdueAlerts() {
        return alertRepository.findOverdueAlerts();
    }

    /**
     * Get alerts within specified days
     */
    public List<ProductionAlert> getAlertsWithinDays(int days) {
        return alertRepository.findAlertsWithinDays(days);
    }

    // ===================== ALERT COUNTS =====================

    /**
     * Count unread alerts
     */
    public Long countUnreadAlerts() {
        return alertRepository.countUnreadAlerts();
    }

    /**
     * Count critical unread alerts
     */
    public Long countCriticalUnreadAlerts() {
        return alertRepository.countCriticalUnreadAlerts();
    }

    /**
     * Count all alerts
     */
    public Long countAllAlerts() {
        return alertRepository.count();
    }

    // ===================== ALERT ACTIONS =====================

    /**
     * Mark alert as read
     */
    @Transactional
    public boolean markAsRead(String alertId) {
        try {
            int updated = alertRepository.markAsRead(alertId, LocalDateTime.now());
            return updated > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mark multiple alerts as read
     */
    @Transactional
    public boolean markMultipleAsRead(List<String> alertIds) {
        try {
            for (String alertId : alertIds) {
                alertRepository.markAsRead(alertId, LocalDateTime.now());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mark all alerts as read
     */
    @Transactional
    public int markAllAsRead() {
        return alertRepository.markAllAsRead(LocalDateTime.now());
    }

    /**
     * Dismiss alert
     */
    @Transactional
    public boolean dismissAlert(String alertId) {
        try {
            int updated = alertRepository.dismissAlert(alertId, LocalDateTime.now());
            return updated > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Dismiss multiple alerts
     */
    @Transactional
    public boolean dismissMultipleAlerts(List<String> alertIds) {
        try {
            LocalDateTime now = LocalDateTime.now();
            for (String alertId : alertIds) {
                alertRepository.dismissAlert(alertId, now);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Dismiss all alerts for a specific order
     */
    @Transactional
    public int dismissAlertsByOrderId(String orderId) {
        return alertRepository.dismissAlertsByOrderId(orderId, LocalDateTime.now());
    }

    /**
     * Delete alert permanently
     */
    @Transactional
    public boolean deleteAlert(String alertId) {
        try {
            alertRepository.deleteById(alertId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete old alerts (older than specified days)
     */
    @Transactional
    public int deleteOldAlerts(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        return alertRepository.deleteOldAlerts(cutoffDate);
    }

    /**
     * Delete dismissed alerts older than specified days
     */
    @Transactional
    public int cleanupDismissedAlerts(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        return alertRepository.deleteDismissedAlerts(cutoffDate);
    }

    // ===================== ALERT STATISTICS =====================

    /**
     * Get alert statistics
     */
    public Map<String, Object> getAlertStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Total counts
        stats.put("totalAlerts", alertRepository.count());
        stats.put("unreadAlerts", alertRepository.countUnreadAlerts());
        stats.put("criticalAlerts", alertRepository.countCriticalUnreadAlerts());

        // Count by severity
        Map<String, Long> severityCounts = new HashMap<>();
        List<Object[]> severityData = alertRepository.countAlertsBySeverity();
        for (Object[] row : severityData) {
            severityCounts.put((String) row[0], (Long) row[1]);
        }
        stats.put("bySeverity", severityCounts);

        // Count by type
        Map<String, Long> typeCounts = new HashMap<>();
        List<Object[]> typeData = alertRepository.countAlertsByType();
        for (Object[] row : typeData) {
            typeCounts.put((String) row[0], (Long) row[1]);
        }
        stats.put("byType", typeCounts);

        // Recent counts
        stats.put("todayAlerts", getTodayAlerts().size());
        stats.put("overdueAlerts", getOverdueAlerts().size());

        return stats;
    }

    /**
     * Get alert summary for dashboard
     */
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();

        // Key metrics
        summary.put("unreadCount", countUnreadAlerts());
        summary.put("criticalCount", countCriticalUnreadAlerts());
        summary.put("overdueCount", (long) getOverdueAlerts().size());

        // Recent critical alerts (top 5)
        List<ProductionAlert> criticalAlerts = alertRepository.findRecentCriticalAlerts();
        summary.put("recentCriticalAlerts", criticalAlerts.size() > 5 ?
                criticalAlerts.subList(0, 5) : criticalAlerts);

        // Recent unread alerts (top 10)
        List<ProductionAlert> unreadAlerts = getUnreadAlerts();
        summary.put("recentUnreadAlerts", unreadAlerts.size() > 10 ?
                unreadAlerts.subList(0, 10) : unreadAlerts);

        return summary;
    }

    /**
     * Get alert distribution by severity
     */
    public Map<String, Long> getAlertDistributionBySeverity() {
        Map<String, Long> distribution = new HashMap<>();
        List<Object[]> data = alertRepository.countAlertsBySeverity();
        for (Object[] row : data) {
            distribution.put((String) row[0], (Long) row[1]);
        }
        return distribution;
    }

    /**
     * Get alert distribution by type
     */
    public Map<String, Long> getAlertDistributionByType() {
        Map<String, Long> distribution = new HashMap<>();
        List<Object[]> data = alertRepository.countAlertsByType();
        for (Object[] row : data) {
            distribution.put((String) row[0], (Long) row[1]);
        }
        return distribution;
    }

    // ===================== UTILITY METHODS =====================

    /**
     * Generate next alert ID
     */
    public String generateNextAlertId() {
        List<String> existingIds = alertRepository.findAllAlertIds();
        if (existingIds.isEmpty()) {
            return "ALERT001";
        }

        int maxNumber = 0;
        for (String id : existingIds) {
            if (id != null && id.startsWith("ALERT") && id.length() >= 8) {
                try {
                    int number = Integer.parseInt(id.substring(5));
                    maxNumber = Math.max(maxNumber, number);
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return String.format("ALERT%03d", maxNumber + 1);
    }

    /**
     * Check if alert exists for order today
     */
    public boolean hasAlertForOrderToday(String orderId) {
        return alertRepository.existsAlertForOrderToday(orderId, LocalDate.now());
    }

    /**
     * Create manual alert
     */
    @Transactional
    public ProductionAlert createManualAlert(ProductionAlert alert) {
        if (alert.getAlertId() == null || alert.getAlertId().isEmpty()) {
            alert.setAlertId(generateNextAlertId());
        }
        return alertRepository.save(alert);
    }

    /**
     * Get alerts requiring attention (critical and unread)
     */
    public List<ProductionAlert> getAlertsRequiringAttention() {
        return alertRepository.findUnreadAlertsBySeverity("CRITICAL");
    }
}
