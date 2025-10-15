package com.clothsphere.strategy.HR;

import com.clothsphere.model.HR.Leave;
import com.clothsphere.model.HR.Employee;

/**
 * Strategy for Long Leave Requests (3 days)
 * These require strict HR approval with monthly limits
 */
public class LongLeaveStrategy implements LeaveApprovalStrategy {

    @Override
    public boolean canApproveAutomatically(Leave leave, Employee employee) {
        // 3-day leaves always require HR approval
        return false;
    }

    @Override
    public String getApprovalMessage(Leave leave) {
        return "3-day leave request - Requires HR approval. " +
                "Maximum 1 three-day leave per month allowed.";
    }

    @Override
    public int getMaxAllowedDays() {
        return 3;
    }

    @Override
    public int getMaxAllowedHours() {
        return 24; // 24 hours for 3 days
    }

    @Override
    public int getMaxPerDay() {
        return 1;
    }

    @Override
    public int getMaxPerMonth() {
        return 1; // Maximum 1 three-day leave per month
    }

    @Override
    public String getLeaveType() {
        return "LONG_LEAVE";
    }
}