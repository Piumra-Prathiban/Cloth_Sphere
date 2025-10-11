package com.clothsphere.strategy.HR;

import com.clothsphere.model.HR.Leave;
import com.clothsphere.model.HR.Employee;

/**
 * Strategy for Medium Leave Requests (4-7 days)
 * These leaves always require HR Manager approval
 */
public class MediumLeaveStrategy implements LeaveApprovalStrategy {

    @Override
    public boolean canApproveAutomatically(Leave leave, Employee employee) {
        // Medium duration leaves always require HR approval
        // Cannot be auto-approved due to potential impact on operations
        return false;
    }

    @Override
    public String getApprovalMessage(Leave leave) {
        return "Medium leave request (4-7 days) - Requires HR Manager approval. " +
                "Please provide detailed reason and ensure proper planning.";
    }

    @Override
    public int getMaxAllowedDays() {
        return 7;
    }
}