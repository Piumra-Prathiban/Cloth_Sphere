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
        // Auto-approve 2-hour leaves if within daily limit
        return isWithinDailyLimit(employee) && isWithinMonthlyLimit(employee);
    }

    @Override
    public String getApprovalMessage(Leave leave) {
        return "2-hour short leave - Approved automatically. " +
                "You can take up to 3 short leaves per day, maximum 2 hours each.";
    }

    @Override
    public int getMaxAllowedDays() {
        return 0; // Not applicable for hourly leaves
    }

    @Override
    public int getMaxAllowedHours() {
        return 2;
    }

    @Override
    public int getMaxPerDay() {
        return 3;
    }

    @Override
    public int getMaxPerMonth() {
        return 3; // Maximum 20 short leaves per month
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