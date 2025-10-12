package com.clothsphere.model.FM;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "production_alerts")
public class ProductionAlert {

    @Id
    @Column(name = "alert_id", length = 15, nullable = false)
    private String alertId;

    @Column(name = "order_id", nullable = false, length = 10)
    private String orderId;

    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType; // DEADLINE_WARNING, OVERDUE, URGENT, QUALITY_ISSUE

    @Column(name = "severity", nullable = false, length = 20)
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "days_remaining")
    private Integer daysRemaining;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "is_dismissed", nullable = false)
    private Boolean isDismissed = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "dismissed_at")
    private LocalDateTime dismissedAt;

    // Default constructor
    public ProductionAlert() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
        this.isDismissed = false;
    }

    // Parameterized constructor
    public ProductionAlert(String alertId, String orderId, String alertType, String severity,
                          String message, String productName, LocalDate deadline, Integer daysRemaining) {
        this.alertId = alertId;
        this.orderId = orderId;
        this.alertType = alertType;
        this.severity = severity;
        this.message = message;
        this.productName = productName;
        this.deadline = deadline;
        this.daysRemaining = daysRemaining;
        this.isRead = false;
        this.isDismissed = false;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public Integer getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(Integer daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
        if (isRead && this.readAt == null) {
            this.readAt = LocalDateTime.now();
        }
    }

    public Boolean getIsDismissed() {
        return isDismissed;
    }

    public void setIsDismissed(Boolean isDismissed) {
        this.isDismissed = isDismissed;
        if (isDismissed && this.dismissedAt == null) {
            this.dismissedAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public LocalDateTime getDismissedAt() {
        return dismissedAt;
    }

    public void setDismissedAt(LocalDateTime dismissedAt) {
        this.dismissedAt = dismissedAt;
    }

    // Utility methods
    @Transient
    public String getSeverityClass() {
        switch (severity) {
            case "CRITICAL":
                return "danger";
            case "HIGH":
                return "warning";
            case "MEDIUM":
                return "info";
            case "LOW":
                return "secondary";
            default:
                return "secondary";
        }
    }

    @Transient
    public String getAlertIcon() {
        switch (alertType) {
            case "OVERDUE":
                return "exclamation-triangle";
            case "URGENT":
                return "exclamation-circle";
            case "DEADLINE_WARNING":
                return "clock";
            case "QUALITY_ISSUE":
                return "tools";
            default:
                return "bell";
        }
    }

    @Override
    public String toString() {
        return "ProductionAlert{" +
                "alertId='" + alertId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", alertType='" + alertType + '\'' +
                ", severity='" + severity + '\'' +
                ", message='" + message + '\'' +
                ", daysRemaining=" + daysRemaining +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
}
