package com.clothsphere.strategy.HR;

import com.clothsphere.model.HR.Leave;
import com.clothsphere.model.HR.Employee;

/**
 * Strategy for Short Leave Requests (1-2 days)
 * These leaves can be auto-approved if conditions are met
 */
public class ShortLeaveStrategy implements LeaveApprovalStrategy {

    @Override
    public boolean canApproveAutomatically(Leave leave, Employee employee) {
        int leaveDays = leave.getTotalDays();

        // Auto-approve if leave is between 1-3 days
        // Additional conditions can be added:
        // - Check employee's attendance record
        // - Check remaining leave balance
        // - Check department workload

        return leaveDays >= 1 && leaveDays <= 2;
    }

    @Override
    public String getApprovalMessage(Leave leave) {
        return "Short leave request (1-3 days) - Approved automatically. " +
                "Please ensure work handover is complete.";
    }

    @Override
    public int getMaxAllowedDays() {
        return 2;
    }
}