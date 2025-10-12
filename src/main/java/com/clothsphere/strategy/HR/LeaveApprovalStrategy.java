package com.clothsphere.strategy.HR;

import com.clothsphere.model.HR.Leave;
import com.clothsphere.model.HR.Employee;

/**
 * Strategy Interface for Leave Approval
 * Defines the contract for different leave approval strategies
 */
public interface LeaveApprovalStrategy {
    /**
     * Determines if a leave request can be automatically approved
     * @param leave The leave request
     * @param employee The employee requesting leave
     * @return true if can be auto-approved, false otherwise
     */
    boolean canApproveAutomatically(Leave leave, Employee employee);

    /**
     * Gets the approval message for this strategy
     * @param leave The leave request
     * @return Message explaining the approval decision
     */
    String getApprovalMessage(Leave leave);

    /**
     * Gets the maximum allowed days for this strategy
     * @return Maximum number of days
     */
    int getMaxAllowedDays();
}