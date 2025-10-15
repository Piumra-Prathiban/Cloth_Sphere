package com.clothsphere.strategy.HR;

import com.clothsphere.model.HR.Leave;
import com.clothsphere.model.HR.Employee;

/**
 * Strategy for Medium Leave Requests (1 day)
 * These leaves require HR approval with monthly limits
 */
public class MediumLeaveStrategy implements LeaveApprovalStrategy {

    @Override
    public boolean canApproveAutomatically(Leave leave, Employee employee) {
        // 1-day leaves always require HR approval
        return false;
    }

    @Override
    public String getApprovalMessage(Leave leave) {
        return "1-day leave request - Requires HR approval. " +
                "Maximum 2 one-day leaves per month allowed.";
    }

    @Override
    public int getMaxAllowedDays() {
        return 1;
    }

    @Override
    public int getMaxAllowedHours() {
        return 8; // 8 hours for 1 day
    }

    @Override
    public int getMaxPerDay() {
        return 1;
    }

    @Override
    public int getMaxPerMonth() {
        return 2; // Maximum 2 one-day leaves per month
    }

    @Override
    public String getLeaveType() {
        return "MEDIUM_LEAVE";
    }
}