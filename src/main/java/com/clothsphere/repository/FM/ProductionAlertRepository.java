package com.clothsphere.repository.FM;

import com.clothsphere.model.FM.ProductionAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductionAlertRepository extends JpaRepository<ProductionAlert, String> {

    // ===================== BASIC QUERIES =====================

    /**
     * Find all alerts ordered by creation date (newest first)
     */
    List<ProductionAlert> findAllByOrderByCreatedAtDesc();

    /**
     * Find alerts by order ID
     */
    List<ProductionAlert> findByOrderId(String orderId);

    /**
     * Find alerts by alert type
     */
    List<ProductionAlert> findByAlertType(String alertType);

    /**
     * Find alerts by severity
     */
    List<ProductionAlert> findBySeverity(String severity);

    // ===================== UNREAD ALERTS =====================

    /**
     * Find all unread and not dismissed alerts
     */
    @Query("SELECT a FROM ProductionAlert a WHERE a.isRead = false AND a.isDismissed = false ORDER BY a.createdAt DESC")
    List<ProductionAlert> findUnreadAlerts();

    /**
     * Find unread alerts by severity
     */
    @Query("SELECT a FROM ProductionAlert a WHERE a.isRead = false AND a.isDismissed = false AND a.severity = :severity ORDER BY a.createdAt DESC")
    List<ProductionAlert> findUnreadAlertsBySeverity(@Param("severity") String severity);

    /**
     * Count unread alerts
     */
    @Query("SELECT COUNT(a) FROM ProductionAlert a WHERE a.isRead = false AND a.isDismissed = false")
    Long countUnreadAlerts();

    /**
     * Count critical unread alerts
     */
    @Query("SELECT COUNT(a) FROM ProductionAlert a WHERE a.isRead = false AND a.isDismissed = false AND a.severity = 'CRITICAL'")
    Long countCriticalUnreadAlerts();

    // ===================== DATE RANGE QUERIES =====================

    /**
     * Find alerts created between dates
     */
    @Query("SELECT a FROM ProductionAlert a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<ProductionAlert> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Find today's alerts
     */
    @Query("SELECT a FROM ProductionAlert a WHERE CAST(a.createdAt AS date) = :today ORDER BY a.createdAt DESC")
    List<ProductionAlert> findTodayAlerts(@Param("today") LocalDate today);

    // ===================== DISMISSED ALERTS =====================

    /**
     * Find dismissed alerts
     */
    @Query("SELECT a FROM ProductionAlert a WHERE a.isDismissed = true ORDER BY a.dismissedAt DESC")
    List<ProductionAlert> findDismissedAlerts();

    /**
     * Find active (not dismissed) alerts
     */
    @Query("SELECT a FROM ProductionAlert a WHERE a.isDismissed = false ORDER BY a.createdAt DESC")
    List<ProductionAlert> findActiveAlerts();

    // ===================== UPDATE OPERATIONS =====================

    /**
     * Mark alert as read
     */
    @Modifying
    @Transactional
    @Query("UPDATE ProductionAlert a SET a.isRead = true, a.readAt = :readAt WHERE a.alertId = :alertId")
    int markAsRead(@Param("alertId") String alertId, @Param("readAt") LocalDateTime readAt);

    /**
     * Mark all alerts as read
     */
    @Modifying
    @Transactional
    @Query("UPDATE ProductionAlert a SET a.isRead = true, a.readAt = :readAt WHERE a.isRead = false AND a.isDismissed = false")
    int markAllAsRead(@Param("readAt") LocalDateTime readAt);

    /**
     * Dismiss alert
     */
    @Modifying
    @Transactional
    @Query("UPDATE ProductionAlert a SET a.isDismissed = true, a.dismissedAt = :dismissedAt WHERE a.alertId = :alertId")
    int dismissAlert(@Param("alertId") String alertId, @Param("dismissedAt") LocalDateTime dismissedAt);

    /**
     * Dismiss all alerts for a specific order
     */
    @Modifying
    @Transactional
    @Query("UPDATE ProductionAlert a SET a.isDismissed = true, a.dismissedAt = :dismissedAt WHERE a.orderId = :orderId AND a.isDismissed = false")
    int dismissAlertsByOrderId(@Param("orderId") String orderId, @Param("dismissedAt") LocalDateTime dismissedAt);

    // ===================== DELETE OPERATIONS =====================

    /**
     * Delete alerts older than specified date
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ProductionAlert a WHERE a.createdAt < :cutoffDate")
    int deleteOldAlerts(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Delete dismissed alerts older than specified date
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ProductionAlert a WHERE a.isDismissed = true AND a.dismissedAt < :cutoffDate")
    int deleteDismissedAlerts(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ===================== STATISTICS =====================

    /**
     * Count alerts by severity
     */
    @Query("SELECT a.severity, COUNT(a) FROM ProductionAlert a WHERE a.isDismissed = false GROUP BY a.severity")
    List<Object[]> countAlertsBySeverity();

    /**
     * Count alerts by type
     */
    @Query("SELECT a.alertType, COUNT(a) FROM ProductionAlert a WHERE a.isDismissed = false GROUP BY a.alertType")
    List<Object[]> countAlertsByType();

    /**
     * Get recent critical alerts
     */
    @Query("SELECT a FROM ProductionAlert a WHERE a.severity = 'CRITICAL' AND a.isDismissed = false ORDER BY a.createdAt DESC")
    List<ProductionAlert> findRecentCriticalAlerts();

    // ===================== CUSTOM QUERY METHODS =====================

    /**
     * Find alerts for orders with deadline within days
     */
    @Query("SELECT a FROM ProductionAlert a WHERE a.daysRemaining <= :days AND a.daysRemaining >= 0 AND a.isDismissed = false ORDER BY a.daysRemaining ASC, a.createdAt DESC")
    List<ProductionAlert> findAlertsWithinDays(@Param("days") Integer days);

    /**
     * Find overdue alerts
     */
    @Query("SELECT a FROM ProductionAlert a WHERE a.daysRemaining < 0 AND a.isDismissed = false ORDER BY a.daysRemaining ASC")
    List<ProductionAlert> findOverdueAlerts();

    /**
     * Get alert IDs for all alerts
     */
    @Query("SELECT a.alertId FROM ProductionAlert a")
    List<String> findAllAlertIds();

    /**
     * Check if alert exists for order today
     */
    @Query("SELECT COUNT(a) > 0 FROM ProductionAlert a WHERE a.orderId = :orderId AND CAST(a.createdAt AS date) = :today AND a.isDismissed = false")
    boolean existsAlertForOrderToday(@Param("orderId") String orderId, @Param("today") LocalDate today);
}
