package com.clothsphere.strategy.HR;

import com.clothsphere.model.HR.Leave;
import com.clothsphere.model.HR.Employee;

/**
 * Strategy for Long Leave Requests (8+ days)
 * These leaves require strict approval and advance notice
 */
public class LongLeaveStrategy implements LeaveApprovalStrategy {

    @Override
    public boolean canApproveAutomatically(Leave leave, Employee employee) {
        // Long leaves always require manual approval
        // May need department manager AND HR approval
        return false;
    }

    @Override
    public String getApprovalMessage(Leave leave) {
        return "Long leave request (8+ days) - Requires department manager and HR approval. " +
                "Please submit at least 2 weeks in advance with proper documentation.";
    }

    @Override
    public int getMaxAllowedDays() {
        return Integer.MAX_VALUE; // No upper limit, but requires approval
    }
}