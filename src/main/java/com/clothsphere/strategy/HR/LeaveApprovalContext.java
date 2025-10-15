package com.clothsphere.strategy.HR;

import com.clothsphere.model.HR.Leave;
import com.clothsphere.model.HR.Employee;

/**
 * Context class that manages strategy selection based on leave type
 */
public class LeaveApprovalContext {
    private LeaveApprovalStrategy strategy;

    /**
     * Automatically selects the appropriate strategy based on leave type
     */
    public void setStrategy(String leaveType) {
        switch (leaveType) {
            case "SHORT_LEAVE":
                this.strategy = new ShortLeaveStrategy();
                System.out.println("Selected Strategy: ShortLeaveStrategy (2 hours)");
                break;
            case "MEDIUM_LEAVE":
                this.strategy = new MediumLeaveStrategy();
                System.out.println("Selected Strategy: MediumLeaveStrategy (1 day)");
                break;
            case "LONG_LEAVE":
                this.strategy = new LongLeaveStrategy();
                System.out.println("Selected Strategy: LongLeaveStrategy (3 days)");
                break;
            default:
                // Default to short leave for backward compatibility
                this.strategy = new ShortLeaveStrategy();
                System.out.println("Selected Strategy: Default ShortLeaveStrategy");
        }
    }

    /**
     * Manually set a specific strategy (optional)
     */
    public void setStrategy(LeaveApprovalStrategy strategy) {
        this.strategy = strategy;
        System.out.println("Manually set strategy: " + strategy.getClass().getSimpleName());
    }

    /**
     * Evaluates if the leave request can be auto-approved
     */
    public boolean evaluateLeaveRequest(Leave leave, Employee employee) {
        if (strategy == null) {
            throw new IllegalStateException("Strategy not set. Call setStrategy() first.");
        }
        return strategy.canApproveAutomatically(leave, employee);
    }

    /**
     * Gets the approval message from the current strategy
     */
    public String getApprovalMessage(Leave leave) {
        if (strategy == null) {
            throw new IllegalStateException("Strategy not set. Call setStrategy() first.");
        }
        return strategy.getApprovalMessage(leave);
    }

    /**
     * Gets the maximum allowed days from the current strategy
     */
    public int getMaxAllowedDays() {
        if (strategy == null) {
            throw new IllegalStateException("Strategy not set. Call setStrategy() first.");
        }
        return strategy.getMaxAllowedDays();
    }

    /**
     * Gets the maximum allowed hours from the current strategy
     */
    public int getMaxAllowedHours() {
        if (strategy == null) {
            throw new IllegalStateException("Strategy not set. Call setStrategy() first.");
        }
        return strategy.getMaxAllowedHours();
    }

    /**
     * Gets the maximum per day from the current strategy
     */
    public int getMaxPerDay() {
        if (strategy == null) {
            throw new IllegalStateException("Strategy not set. Call setStrategy() first.");
        }
        return strategy.getMaxPerDay();
    }

    /**
     * Gets the maximum per month from the current strategy
     */
    public int getMaxPerMonth() {
        if (strategy == null) {
            throw new IllegalStateException("Strategy not set. Call setStrategy() first.");
        }
        return strategy.getMaxPerMonth();
    }

    /**
     * Gets the leave type from the current strategy
     */
    public String getLeaveType() {
        if (strategy == null) {
            throw new IllegalStateException("Strategy not set. Call setStrategy() first.");
        }
        return strategy.getLeaveType();
    }

    /**
     * Gets the name of the current strategy
     */
    public String getStrategyName() {
        if (strategy == null) {
            return "No Strategy Set";
        }
        return strategy.getClass().getSimpleName();
    }
}