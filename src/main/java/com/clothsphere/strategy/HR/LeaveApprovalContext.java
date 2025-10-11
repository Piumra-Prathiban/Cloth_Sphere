package com.clothsphere.strategy.HR;
import com.clothsphere.model.HR.Leave;
import com.clothsphere.model.HR.Employee;

/**
 * Context class that manages strategy selection
 * This is the main class that decides which strategy to use
 */
public class LeaveApprovalContext {
    private LeaveApprovalStrategy strategy;

    /**
     * Automatically selects the appropriate strategy based on leave details
     * @param leave The leave request
     */
    public void setStrategy(Leave leave) {
        int days = leave.getTotalDays();
        String reason = leave.getReason().toLowerCase();

        // Priority 1: Check if it's an emergency
        if (isEmergencyLeave(reason)) {
            this.strategy = new EmergencyLeaveStrategy();
            System.out.println("Selected Strategy: EmergencyLeaveStrategy");
        }
        // Priority 2: Select based on duration
        else if (days <= 3) {
            this.strategy = new ShortLeaveStrategy();
            System.out.println("Selected Strategy: ShortLeaveStrategy (" + days + " days)");
        } else if (days <= 7) {
            this.strategy = new MediumLeaveStrategy();
            System.out.println("Selected Strategy: MediumLeaveStrategy (" + days + " days)");
        } else {
            this.strategy = new LongLeaveStrategy();
            System.out.println("Selected Strategy: LongLeaveStrategy (" + days + " days)");
        }
    }

    /**
     * Manually set a specific strategy (optional)
     * @param strategy The strategy to use
     */
    public void setStrategy(LeaveApprovalStrategy strategy) {
        this.strategy = strategy;
        System.out.println("Manually set strategy: " + strategy.getClass().getSimpleName());
    }

    /**
     * Evaluates if the leave request can be auto-approved
     * @param leave The leave request
     * @param employee The employee
     * @return true if can be auto-approved
     */
    public boolean evaluateLeaveRequest(Leave leave, Employee employee) {
        if (strategy == null) {
            throw new IllegalStateException("Strategy not set. Call setStrategy() first.");
        }
        return strategy.canApproveAutomatically(leave, employee);
    }

    /**
     * Gets the approval message from the current strategy
     * @param leave The leave request
     * @return The approval message
     */
    public String getApprovalMessage(Leave leave) {
        if (strategy == null) {
            throw new IllegalStateException("Strategy not set. Call setStrategy() first.");
        }
        return strategy.getApprovalMessage(leave);
    }

    /**
     * Gets the maximum allowed days from the current strategy
     * @return Maximum days
     */
    public int getMaxAllowedDays() {
        if (strategy == null) {
            throw new IllegalStateException("Strategy not set. Call setStrategy() first.");
        }
        return strategy.getMaxAllowedDays();
    }

    /**
     * Gets the name of the current strategy
     * @return Strategy name
     */
    public String getStrategyName() {
        if (strategy == null) {
            return "No Strategy Set";
        }
        return strategy.getClass().getSimpleName();
    }

    /**
     * Helper method to check if leave is emergency
     * @param reason The leave reason
     * @return true if emergency
     */
    private boolean isEmergencyLeave(String reason) {
        return reason.contains("emergency") ||
                reason.contains("medical") ||
                reason.contains("hospital") ||
                reason.contains("death") ||
                reason.contains("accident") ||
                reason.contains("urgent") ||
                reason.contains("critical");
    }
}