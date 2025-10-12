package com.clothsphere.model.FM;

/**
 * Data Transfer Object for Production Overview Statistics
 */
public class ProductionOverviewDTO {

    private Integer totalOrders;
    private Integer totalPlannedQuantity;
    private Integer totalCompletedQuantity;
    private Integer completedOrders;
    private Integer inProgressOrders;
    private Integer pendingOrders;
    private Integer delayedOrders;
    private Integer highPriorityOrders;
    private Integer mediumPriorityOrders;
    private Integer lowPriorityOrders;
    private Double overallCompletionRate;

    // Constructors
    public ProductionOverviewDTO() {
    }

    public ProductionOverviewDTO(Integer totalOrders, Integer totalPlannedQuantity,
                                Integer totalCompletedQuantity, Integer completedOrders,
                                Integer inProgressOrders, Integer pendingOrders,
                                Integer delayedOrders, Integer highPriorityOrders,
                                Integer mediumPriorityOrders, Integer lowPriorityOrders,
                                Double overallCompletionRate) {
        this.totalOrders = totalOrders;
        this.totalPlannedQuantity = totalPlannedQuantity;
        this.totalCompletedQuantity = totalCompletedQuantity;
        this.completedOrders = completedOrders;
        this.inProgressOrders = inProgressOrders;
        this.pendingOrders = pendingOrders;
        this.delayedOrders = delayedOrders;
        this.highPriorityOrders = highPriorityOrders;
        this.mediumPriorityOrders = mediumPriorityOrders;
        this.lowPriorityOrders = lowPriorityOrders;
        this.overallCompletionRate = overallCompletionRate;
    }

    // Static factory method to create from Object array
    public static ProductionOverviewDTO fromObjectArray(Object[] row) {
        return new ProductionOverviewDTO(
                (Integer) row[0],   // total_orders
                (Integer) row[1],   // total_planned_quantity
                (Integer) row[2],   // total_completed_quantity
                (Integer) row[3],   // completed_orders
                (Integer) row[4],   // in_progress_orders
                (Integer) row[5],   // pending_orders
                (Integer) row[6],   // delayed_orders
                (Integer) row[7],   // high_priority_orders
                (Integer) row[8],   // medium_priority_orders
                (Integer) row[9],   // low_priority_orders
                (Double) row[10]    // overall_completion_rate
        );
    }

    // Getters and Setters
    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Integer getTotalPlannedQuantity() {
        return totalPlannedQuantity;
    }

    public void setTotalPlannedQuantity(Integer totalPlannedQuantity) {
        this.totalPlannedQuantity = totalPlannedQuantity;
    }

    public Integer getTotalCompletedQuantity() {
        return totalCompletedQuantity;
    }

    public void setTotalCompletedQuantity(Integer totalCompletedQuantity) {
        this.totalCompletedQuantity = totalCompletedQuantity;
    }

    public Integer getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(Integer completedOrders) {
        this.completedOrders = completedOrders;
    }

    public Integer getInProgressOrders() {
        return inProgressOrders;
    }

    public void setInProgressOrders(Integer inProgressOrders) {
        this.inProgressOrders = inProgressOrders;
    }

    public Integer getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(Integer pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public Integer getDelayedOrders() {
        return delayedOrders;
    }

    public void setDelayedOrders(Integer delayedOrders) {
        this.delayedOrders = delayedOrders;
    }

    public Integer getHighPriorityOrders() {
        return highPriorityOrders;
    }

    public void setHighPriorityOrders(Integer highPriorityOrders) {
        this.highPriorityOrders = highPriorityOrders;
    }

    public Integer getMediumPriorityOrders() {
        return mediumPriorityOrders;
    }

    public void setMediumPriorityOrders(Integer mediumPriorityOrders) {
        this.mediumPriorityOrders = mediumPriorityOrders;
    }

    public Integer getLowPriorityOrders() {
        return lowPriorityOrders;
    }

    public void setLowPriorityOrders(Integer lowPriorityOrders) {
        this.lowPriorityOrders = lowPriorityOrders;
    }

    public Double getOverallCompletionRate() {
        return overallCompletionRate;
    }

    public void setOverallCompletionRate(Double overallCompletionRate) {
        this.overallCompletionRate = overallCompletionRate;
    }

    @Override
    public String toString() {
        return "ProductionOverviewDTO{" +
                "totalOrders=" + totalOrders +
                ", totalPlannedQuantity=" + totalPlannedQuantity +
                ", totalCompletedQuantity=" + totalCompletedQuantity +
                ", completedOrders=" + completedOrders +
                ", inProgressOrders=" + inProgressOrders +
                ", pendingOrders=" + pendingOrders +
                ", delayedOrders=" + delayedOrders +
                ", highPriorityOrders=" + highPriorityOrders +
                ", mediumPriorityOrders=" + mediumPriorityOrders +
                ", lowPriorityOrders=" + lowPriorityOrders +
                ", overallCompletionRate=" + overallCompletionRate +
                '}';
    }
}
