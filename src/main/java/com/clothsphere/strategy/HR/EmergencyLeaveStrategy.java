package com.clothsphere.strategy.HR;
import com.clothsphere.model.HR.Leave;
import com.clothsphere.model.HR.Employee;

/**
 * Strategy for Emergency Leave Requests
 * Auto-approves leaves for medical emergencies, family emergencies, etc.
 */
public class EmergencyLeaveStrategy implements LeaveApprovalStrategy {

    @Override
    public boolean canApproveAutomatically(Leave leave, Employee employee) {
        String reason = leave.getReason().toLowerCase();

        // Auto-approve if reason contains emergency keywords
        boolean isEmergency = reason.contains("emergency") ||
                reason.contains("medical") ||
                reason.contains("hospital") ||
                reason.contains("death") ||
                reason.contains("accident") ||
                reason.contains("urgent") ||
                reason.contains("critical");

        return isEmergency;
    }

    @Override
    public String getApprovalMessage(Leave leave) {
        return "Emergency leave - Approved immediately. " +
                "Please submit supporting documents (medical certificate, etc.) within 48 hours.";
    }

    @Override
    public int getMaxAllowedDays() {
        return 5; // Emergency leaves typically short-term
    }
}