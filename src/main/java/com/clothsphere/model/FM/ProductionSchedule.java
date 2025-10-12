package com.clothsphere.model.FM;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "production_schedule")
public class ProductionSchedule {

    @Id
    @Column(name = "schedule_id", length = 10, nullable = false)
    private String scheduleId;

    @Column(name = "order_id", nullable = false, length = 10)
    private String orderId;

    @Column(name = "workstation_id", nullable = false, length = 10)
    private String workstationId;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "estimated_hours")
    private Double estimatedHours;

    @Column(name = "actual_hours")
    private Double actualHours;

    @Column(name = "assigned_quantity", nullable = false)
    private Integer assignedQuantity;

    @Column(name = "completed_quantity")
    private Integer completedQuantity = 0;

    @Column(name = "status", length = 30)
    private String status; // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED

    @Column(name = "shift", length = 20)
    private String shift; // MORNING, AFTERNOON, NIGHT

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "notes", length = 500)
    private String notes;

    // Default constructor
    public ProductionSchedule() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.completedQuantity = 0;
    }

    // Parameterized constructor
    public ProductionSchedule(String scheduleId, String orderId, String workstationId,
                             LocalDate scheduledDate, Integer assignedQuantity,
                             Double estimatedHours, String shift, String status) {
        this.scheduleId = scheduleId;
        this.orderId = orderId;
        this.workstationId = workstationId;
        this.scheduledDate = scheduledDate;
        this.assignedQuantity = assignedQuantity;
        this.estimatedHours = estimatedHours;
        this.shift = shift;
        this.status = status;
        this.completedQuantity = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getScheduleId() { return scheduleId; }
    public void setScheduleId(String scheduleId) { this.scheduleId = scheduleId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getWorkstationId() { return workstationId; }
    public void setWorkstationId(String workstationId) { this.workstationId = workstationId; }

    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }

    public Double getActualHours() { return actualHours; }
    public void setActualHours(Double actualHours) { this.actualHours = actualHours; }

    public Integer getAssignedQuantity() { return assignedQuantity; }
    public void setAssignedQuantity(Integer assignedQuantity) { this.assignedQuantity = assignedQuantity; }

    public Integer getCompletedQuantity() { return completedQuantity; }
    public void setCompletedQuantity(Integer completedQuantity) {
        this.completedQuantity = completedQuantity;
        this.updatedAt = LocalDateTime.now();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // Calculated field for completion percentage
    @Transient
    public Double getCompletionPercentage() {
        if (assignedQuantity == null || assignedQuantity == 0) return 0.0;
        return (completedQuantity * 100.0) / assignedQuantity;
    }

    @Override
    public String toString() {
        return "ProductionSchedule{" +
                "scheduleId='" + scheduleId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", workstationId='" + workstationId + '\'' +
                ", scheduledDate=" + scheduledDate +
                ", status='" + status + '\'' +
                ", shift='" + shift + '\'' +
                '}';
    }
}