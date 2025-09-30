package com.clothsphere.model.SalesOrder;

public enum OrderStatus {
    PENDING("Pending", "Order has been placed and is awaiting confirmation", "#ffc107", "fas fa-clock"),
    CONFIRMED("Confirmed", "Order has been confirmed and is ready for production", "#17a2b8", "fas fa-check-circle"),
    IN_PRODUCTION("In Production", "Order is currently being manufactured", "#007bff", "fas fa-cogs"),
    QUALITY_CHECK("Quality Check", "Products are undergoing quality assurance", "#6f42c1", "fas fa-search"),
    READY_TO_SHIP("Ready to Ship", "Order is packed and ready for shipping", "#28a745", "fas fa-box"),
    SHIPPED("Shipped", "Order has been dispatched and is in transit", "#343a40", "fas fa-truck"),
    DELIVERED("Delivered", "Order has been successfully delivered", "#28a745", "fas fa-check-square"),
    CANCELLED("Cancelled", "Order has been cancelled", "#dc3545", "fas fa-times-circle"),
    RETURNED("Returned", "Order has been returned by customer", "#fd7e14", "fas fa-undo"),
    REFUNDED("Refunded", "Order amount has been refunded", "#6c757d", "fas fa-money-bill-wave");

    private final String displayName;
    private final String description;
    private final String colorCode;
    private final String iconClass;

    OrderStatus(String displayName, String description, String colorCode, String iconClass) {
        this.displayName = displayName;
        this.description = description;
        this.colorCode = colorCode;
        this.iconClass = iconClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getIconClass() {
        return iconClass;
    }

    // Method to get status by display name
    public static OrderStatus fromDisplayName(String displayName) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.displayName.equals(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No enum constant with display name: " + displayName);
    }

    // Method to check if order can be cancelled
    public boolean isCancellable() {
        return this == PENDING || this == CONFIRMED;
    }

    // Method to check if order can be modified
    public boolean isModifiable() {
        return this == PENDING || this == CONFIRMED;
    }

    // Method to check if order is in progress
    public boolean isInProgress() {
        return this == IN_PRODUCTION || this == QUALITY_CHECK || this == READY_TO_SHIP || this == SHIPPED;
    }

    // Method to check if order is completed
    public boolean isCompleted() {
        return this == DELIVERED;
    }

    // Method to check if order is final (cannot be changed)
    public boolean isFinalStatus() {
        return this == DELIVERED || this == CANCELLED || this == REFUNDED;
    }

    // Method to check if order is active (not cancelled or refunded)
    public boolean isActive() {
        return this != CANCELLED && this != REFUNDED;
    }

    // Method to check if order affects inventory
    public boolean affectsInventory() {
        return this == CONFIRMED || this == IN_PRODUCTION || this == QUALITY_CHECK ||
                this == READY_TO_SHIP || this == SHIPPED || this == DELIVERED;
    }

    // Method to get next possible statuses based on business logic
    public OrderStatus[] getNextPossibleStatuses() {
        switch (this) {
            case PENDING:
                return new OrderStatus[]{CONFIRMED, CANCELLED};
            case CONFIRMED:
                return new OrderStatus[]{IN_PRODUCTION, CANCELLED};
            case IN_PRODUCTION:
                return new OrderStatus[]{QUALITY_CHECK, CANCELLED};
            case QUALITY_CHECK:
                return new OrderStatus[]{READY_TO_SHIP, IN_PRODUCTION}; // Can go back to production if quality fails
            case READY_TO_SHIP:
                return new OrderStatus[]{SHIPPED, CANCELLED};
            case SHIPPED:
                return new OrderStatus[]{DELIVERED, RETURNED};
            case DELIVERED:
                return new OrderStatus[]{RETURNED}; // Customer can return delivered items
            case RETURNED:
                return new OrderStatus[]{REFUNDED, CONFIRMED}; // Can be refunded or resent
            case CANCELLED:
                return new OrderStatus[]{REFUNDED}; // Cancelled orders might need refund
            case REFUNDED:
                return new OrderStatus[]{}; // Final status - no further transitions
            default:
                return new OrderStatus[]{};
        }
    }

    // Method to get all statuses that come before current status
    public OrderStatus[] getPreviousStatuses() {
        switch (this) {
            case PENDING:
                return new OrderStatus[]{};
            case CONFIRMED:
                return new OrderStatus[]{PENDING};
            case IN_PRODUCTION:
                return new OrderStatus[]{PENDING, CONFIRMED};
            case QUALITY_CHECK:
                return new OrderStatus[]{PENDING, CONFIRMED, IN_PRODUCTION};
            case READY_TO_SHIP:
                return new OrderStatus[]{PENDING, CONFIRMED, IN_PRODUCTION, QUALITY_CHECK};
            case SHIPPED:
                return new OrderStatus[]{PENDING, CONFIRMED, IN_PRODUCTION, QUALITY_CHECK, READY_TO_SHIP};
            case DELIVERED:
                return new OrderStatus[]{PENDING, CONFIRMED, IN_PRODUCTION, QUALITY_CHECK, READY_TO_SHIP, SHIPPED};
            case CANCELLED:
                return new OrderStatus[]{}; // Can be cancelled from any status
            case RETURNED:
                return new OrderStatus[]{DELIVERED};
            case REFUNDED:
                return new OrderStatus[]{CANCELLED, RETURNED};
            default:
                return new OrderStatus[]{};
        }
    }

    // Method to check if transition to new status is valid
    public boolean canTransitionTo(OrderStatus newStatus) {
        if (this == newStatus) return false; // Same status
        if (this.isFinalStatus() && newStatus != RETURNED && newStatus != REFUNDED) {
            return false; // Cannot change final statuses except for returns/refunds
        }

        OrderStatus[] nextStatuses = this.getNextPossibleStatuses();
        for (OrderStatus status : nextStatuses) {
            if (status == newStatus) return true;
        }
        return false;
    }

    // Method to get Bootstrap CSS class for styling
    public String getBootstrapClass() {
        switch (this) {
            case PENDING:
                return "warning";
            case CONFIRMED:
                return "info";
            case IN_PRODUCTION:
                return "primary";
            case QUALITY_CHECK:
                return "secondary";
            case READY_TO_SHIP:
                return "success";
            case SHIPPED:
                return "dark";
            case DELIVERED:
                return "success";
            case CANCELLED:
                return "danger";
            case RETURNED:
                return "warning";
            case REFUNDED:
                return "secondary";
            default:
                return "secondary";
        }
    }

    // Method to get priority level (higher number = higher priority)
    public int getPriorityLevel() {
        switch (this) {
            case PENDING:
                return 8; // High priority - needs immediate attention
            case CONFIRMED:
                return 7; // High priority - needs to start production
            case IN_PRODUCTION:
                return 6; // Medium-high priority
            case QUALITY_CHECK:
                return 5; // Medium priority
            case READY_TO_SHIP:
                return 9; // Very high priority - ready for dispatch
            case SHIPPED:
                return 4; // Medium-low priority - in transit
            case DELIVERED:
                return 1; // Low priority - completed
            case CANCELLED:
                return 2; // Low priority - completed
            case RETURNED:
                return 8; // High priority - needs handling
            case REFUNDED:
                return 1; // Low priority - completed
            default:
                return 3;
        }
    }

    // Method to get estimated processing time in days
    public int getEstimatedProcessingDays() {
        switch (this) {
            case PENDING:
                return 1; // Should be confirmed within 1 day
            case CONFIRMED:
                return 2; // Should start production within 2 days
            case IN_PRODUCTION:
                return 7; // Production typically takes a week
            case QUALITY_CHECK:
                return 2; // Quality check takes 1-2 days
            case READY_TO_SHIP:
                return 1; // Should ship within 1 day
            case SHIPPED:
                return 5; // Typical shipping time
            case DELIVERED:
                return 0; // Already delivered
            case CANCELLED:
                return 0; // No processing needed
            case RETURNED:
                return 3; // Return processing time
            case REFUNDED:
                return 0; // Already processed
            default:
                return 1;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}