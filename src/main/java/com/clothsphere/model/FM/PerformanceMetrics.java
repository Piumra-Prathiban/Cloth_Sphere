package com.clothsphere.model.FM;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_metrics")
public class PerformanceMetrics {

    @Id
    @Column(name = "metric_id", length = 10, nullable = false)
    private String metricId;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "workstation_id", length = 10)
    private String workstationId;

    @Column(name = "employee_id", length = 10)
    private String employeeId;

    @Column(name = "order_id", length = 10)
    private String orderId;

    @Column(name = "schedule_id", length = 10)
    private String scheduleId;

    @Column(name = "target_quantity", nullable = false)
    private Integer targetQuantity;

    @Column(name = "actual_quantity", nullable = false)
    private Integer actualQuantity;

    @Column(name = "defect_quantity")
    private Integer defectQuantity = 0;

    @Column(name = "efficiency_rate")
    private Double efficiencyRate; // Percentage

    @Column(name = "quality_rate")
    private Double qualityRate; // Percentage

    @Column(name = "downtime_hours")
    private Double downtimeHours = 0.0;

    @Column(name = "working_hours")
    private Double workingHours;

    @Column(name = "overtime_hours")
    private Double overtimeHours = 0.0;

    @Column(name = "delay_hours")
    private Double delayHours = 0.0;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default constructor
    public PerformanceMetrics() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.defectQuantity = 0;
        this.downtimeHours = 0.0;
        this.overtimeHours = 0.0;
        this.delayHours = 0.0;
    }

    // Parameterized constructor
    public PerformanceMetrics(String metricId, LocalDate recordDate, String workstationId,
                             String employeeId, String orderId, Integer targetQuantity,
                             Integer actualQuantity, Double workingHours) {
        this.metricId = metricId;
        this.recordDate = recordDate;
        this.workstationId = workstationId;
        this.employeeId = employeeId;
        this.orderId = orderId;
        this.targetQuantity = targetQuantity;
        this.actualQuantity = actualQuantity;
        this.workingHours = workingHours;
        this.defectQuantity = 0;
        this.downtimeHours = 0.0;
        this.overtimeHours = 0.0;
        this.delayHours = 0.0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        calculateMetrics();
    }

    // Calculate efficiency and quality metrics
    public void calculateMetrics() {
        // Efficiency Rate = (Actual Quantity / Target Quantity) * 100
        if (targetQuantity != null && targetQuantity > 0) {
            this.efficiencyRate = (actualQuantity * 100.0) / targetQuantity;
        }

        // Quality Rate = ((Actual Quantity - Defects) / Actual Quantity) * 100
        if (actualQuantity != null && actualQuantity > 0) {
            int goodQuantity = actualQuantity - (defectQuantity != null ? defectQuantity : 0);
            this.qualityRate = (goodQuantity * 100.0) / actualQuantity;
        }

        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getMetricId() { return metricId; }
    public void setMetricId(String metricId) { this.metricId = metricId; }

    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }

    public String getWorkstationId() { return workstationId; }
    public void setWorkstationId(String workstationId) { this.workstationId = workstationId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getScheduleId() { return scheduleId; }
    public void setScheduleId(String scheduleId) { this.scheduleId = scheduleId; }

    public Integer getTargetQuantity() { return targetQuantity; }
    public void setTargetQuantity(Integer targetQuantity) {
        this.targetQuantity = targetQuantity;
        calculateMetrics();
    }

    public Integer getActualQuantity() { return actualQuantity; }
    public void setActualQuantity(Integer actualQuantity) {
        this.actualQuantity = actualQuantity;
        calculateMetrics();
    }

    public Integer getDefectQuantity() { return defectQuantity; }
    public void setDefectQuantity(Integer defectQuantity) {
        this.defectQuantity = defectQuantity;
        calculateMetrics();
    }

    public Double getEfficiencyRate() { return efficiencyRate; }
    public void setEfficiencyRate(Double efficiencyRate) { this.efficiencyRate = efficiencyRate; }

    public Double getQualityRate() { return qualityRate; }
    public void setQualityRate(Double qualityRate) { this.qualityRate = qualityRate; }

    public Double getDowntimeHours() { return downtimeHours; }
    public void setDowntimeHours(Double downtimeHours) { this.downtimeHours = downtimeHours; }

    public Double getWorkingHours() { return workingHours; }
    public void setWorkingHours(Double workingHours) { this.workingHours = workingHours; }

    public Double getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(Double overtimeHours) { this.overtimeHours = overtimeHours; }

    public Double getDelayHours() { return delayHours; }
    public void setDelayHours(Double delayHours) { this.delayHours = delayHours; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Calculated field for overall performance score
    @Transient
    public Double getOverallPerformanceScore() {
        if (efficiencyRate == null || qualityRate == null) return 0.0;
        // Weighted average: 60% efficiency, 40% quality
        return (efficiencyRate * 0.6) + (qualityRate * 0.4);
    }

    @Override
    public String toString() {
        return "PerformanceMetrics{" +
                "metricId='" + metricId + '\'' +
                ", recordDate=" + recordDate +
                ", workstationId='" + workstationId + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", efficiencyRate=" + efficiencyRate +
                ", qualityRate=" + qualityRate +
                '}';
    }
}