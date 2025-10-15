package com.clothsphere.strategy.HR;

import com.clothsphere.model.HR.Leave;
import com.clothsphere.model.HR.Employee;

/**
 * Strategy Interface for Leave Approval
 * Defines the contract for different leave approval strategies
 */
public interface LeaveApprovalStrategy {

    boolean canApproveAutomatically(Leave leave, Employee employee);

    String getApprovalMessage(Leave leave);

    int getMaxAllowedDays();

    int getMaxAllowedHours();

    int getMaxPerDay();

    int getMaxPerMonth();

    String getLeaveType();
}