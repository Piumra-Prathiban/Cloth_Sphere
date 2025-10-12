package com.clothsphere.model.FM;

import java.time.LocalDate;

/**
 * Data Transfer Object for Production Summary Report
 */
public class ProductionReportDTO {

    private String status;
    private String priority;
    private Integer totalOrders;
    private Integer totalQuantity;
    private Integer completedQuantity;
    private Double avgCompletionRate;
    private Integer delayedCount;
    private LocalDate earliestOrder;
    private LocalDate latestDeadline;

    // Constructors
    public ProductionReportDTO() {
    }

    public ProductionReportDTO(String status, String priority, Integer totalOrders,
                              Integer totalQuantity, Integer completedQuantity,
                              Double avgCompletionRate, Integer delayedCount,
                              LocalDate earliestOrder, LocalDate latestDeadline) {
        this.status = status;
        this.priority = priority;
        this.totalOrders = totalOrders;
        this.totalQuantity = totalQuantity;
        this.completedQuantity = completedQuantity;
        this.avgCompletionRate = avgCompletionRate;
        this.delayedCount = delayedCount;
        this.earliestOrder = earliestOrder;
        this.latestDeadline = latestDeadline;
    }

    // Static factory method to create from Object array
    public static ProductionReportDTO fromObjectArray(Object[] row) {
        return new ProductionReportDTO(
                (String) row[0],           // status
                (String) row[1],           // priority
                (Integer) row[2],          // total_orders
                (Integer) row[3],          // total_quantity
                (Integer) row[4],          // completed_quantity
                (Double) row[5],           // avg_completion_rate
                (Integer) row[6],          // delayed_count
                row[7] != null ? ((java.sql.Date) row[7]).toLocalDate() : null,  // earliest_order
                row[8] != null ? ((java.sql.Date) row[8]).toLocalDate() : null   // latest_deadline
        );
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Integer getCompletedQuantity() {
        return completedQuantity;
    }

    public void setCompletedQuantity(Integer completedQuantity) {
        this.completedQuantity = completedQuantity;
    }

    public Double getAvgCompletionRate() {
        return avgCompletionRate;
    }

    public void setAvgCompletionRate(Double avgCompletionRate) {
        this.avgCompletionRate = avgCompletionRate;
    }

    public Integer getDelayedCount() {
        return delayedCount;
    }

    public void setDelayedCount(Integer delayedCount) {
        this.delayedCount = delayedCount;
    }

    public LocalDate getEarliestOrder() {
        return earliestOrder;
    }

    public void setEarliestOrder(LocalDate earliestOrder) {
        this.earliestOrder = earliestOrder;
    }

    public LocalDate getLatestDeadline() {
        return latestDeadline;
    }

    public void setLatestDeadline(LocalDate latestDeadline) {
        this.latestDeadline = latestDeadline;
    }

    @Override
    public String toString() {
        return "ProductionReportDTO{" +
                "status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                ", totalOrders=" + totalOrders +
                ", totalQuantity=" + totalQuantity +
                ", completedQuantity=" + completedQuantity +
                ", avgCompletionRate=" + avgCompletionRate +
                ", delayedCount=" + delayedCount +
                ", earliestOrder=" + earliestOrder +
                ", latestDeadline=" + latestDeadline +
                '}';
    }
}
