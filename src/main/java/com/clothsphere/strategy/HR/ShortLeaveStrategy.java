package com.clothsphere.strategy.HR;

import com.clothsphere.model.HR.Leave;
import com.clothsphere.model.HR.Employee;

/**
 * Strategy for Short Leave Requests (2 hours)
 * These leaves can be taken multiple times per day
 */
public class ShortLeaveStrategy implements LeaveApprovalStrategy {

    @Override
    public boolean canApproveAutomatically(Leave leave, Employee employee) {
        // Auto-approve 2-hour leaves if within daily and monthly limits
        return isWithinDailyLimit(employee) && isWithinMonthlyLimit(employee);
    }

    @Override
    public String getApprovalMessage(Leave leave) {
        return "2-hour short leave - Approved automatically. " +
                "You can take up to 3 short leaves per month, maximum 2 hours each.";
    }

    @Override
    public int getMaxAllowedDays() {
        return 0; // Not applicable for hourly leaves
    }

    @Override
    public int getMaxAllowedHours() {
        return 2; // Maximum 2 hours per short leave
    }

    @Override
    public int getMaxPerDay() {
        return 1; // Only 1 short leave per day allowed
    }

    @Override
    public int getMaxPerMonth() {
        return 3; // Maximum 3 short leaves per month
    }

    @Override
    public String getLeaveType() {
        return "SHORT_LEAVE";
    }

    private boolean isWithinDailyLimit(Employee employee) {
        // Implementation would check database for today's short leave count
        // For now, return true (will be implemented in service layer)
        return true;
    }

    private boolean isWithinMonthlyLimit(Employee employee) {
        // Implementation would check database for monthly short leave count
        // For now, return true (will be implemented in service layer)
        return true;
    }
}