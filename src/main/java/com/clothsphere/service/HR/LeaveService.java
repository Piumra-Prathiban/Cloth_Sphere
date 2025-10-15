package com.clothsphere.service.HR;

import com.clothsphere.model.HR.Leave;
import com.clothsphere.model.HR.Employee;
import com.clothsphere.repository.HR.LeaveRepository;
import com.clothsphere.strategy.HR.LeaveApprovalContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class LeaveService {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private EmployeeService employeeService;

    // ========================================
    // NEW METHOD WITH STRATEGY PATTERN FOR DIFFERENT LEAVE TYPES
    // ========================================
    @Transactional
    public Leave requestLeave(Leave leave, String employeeId, String leaveType) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            throw new RuntimeException("Employee not found");
        }

        System.out.println("=== APPLYING STRATEGY PATTERN ===");
        System.out.println("Employee: " + employee.getFullName() + " (" + employeeId + ")");
        System.out.println("Leave Type: " + leaveType);
        System.out.println("Leave Duration: " + leave.getTotalDays() + " days");
        System.out.println("Leave Reason: " + leave.getReason());

        // Validate dates based on leave type
        if (!validateLeaveDates(leave, leaveType)) {
            throw new RuntimeException("Invalid dates for leave type: " + leaveType);
        }

        if (leave.getStartDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Start date cannot be in the past");
        }

        // Check for overlapping leaves
        List<Leave> overlappingLeaves = leaveRepository.findOverlappingLeaves(
                employee, leave.getStartDate(), leave.getEndDate());

        if (!overlappingLeaves.isEmpty()) {
            throw new RuntimeException("You already have a leave request for the selected dates");
        }

        // Check leave limits based on type
        if (!checkLeaveLimits(employee, leaveType)) {
            throw new RuntimeException("Leave limit exceeded for type: " + leaveType);
        }

        // ========================================
        // APPLY STRATEGY PATTERN
        // ========================================
        LeaveApprovalContext approvalContext = new LeaveApprovalContext();
        approvalContext.setStrategy(leaveType);

        boolean canAutoApprove = approvalContext.evaluateLeaveRequest(leave, employee);
        String approvalMessage = approvalContext.getApprovalMessage(leave);

        System.out.println("Strategy Used: " + approvalContext.getStrategyName());
        System.out.println("Can Auto-Approve: " + canAutoApprove);
        System.out.println("Message: " + approvalMessage);

        String status;
        String comments;

        if (canAutoApprove) {
            status = "APPROVED";
            comments = "Auto-approved by system. " + approvalMessage;
            leave.setActionDate(LocalDateTime.now());
            System.out.println("✓ Leave AUTO-APPROVED");
        } else {
            status = "PENDING";
            comments = "Pending HR approval. " + approvalMessage;
            System.out.println("⏳ Leave set to PENDING");
        }

        // Set leave type and calculate hours
        leave.setLeaveType(leaveType);
        leave.setTotalHours(leave.calculateTotalHours());

        // Generate leave ID
        Long nextIdNumber = leaveRepository.getNextLeaveIdNumber();
        String leaveId = "lev" + nextIdNumber;

        // Save leave request
        int result = leaveRepository.insertLeaveWithType(
                leaveId,
                leave.getReason(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getStartTime(),
                leave.getEndTime(),
                status,
                leaveType,
                leave.getTotalHours(),
                LocalDateTime.now(),
                employeeId,
                comments
        );

        if (result > 0) {
            leave.setLeaveId(leaveId);
            leave.setEmployee(employee);
            leave.setStatus(status);
            leave.setComments(comments);
            leave.setRequestDate(LocalDateTime.now());

            System.out.println("✓ Leave saved - ID: " + leaveId + ", Type: " + leaveType + ", Status: " + status);
            System.out.println("=================================");

            return leave;
        } else {
            throw new RuntimeException("Failed to save leave request");
        }
    }

    private boolean validateLeaveDates(Leave leave, String leaveType) {
        switch (leaveType) {
            case "SHORT_LEAVE":
                return leave.getStartDate().equals(leave.getEndDate()) &&
                        leave.getStartTime() != null && leave.getEndTime() != null &&
                        leave.getStartTime().isBefore(leave.getEndTime());

            case "MEDIUM_LEAVE":
                return leave.getStartDate().equals(leave.getEndDate()) &&
                        !leave.getStartDate().isBefore(LocalDate.now());

            case "LONG_LEAVE":
                return !leave.getStartDate().isBefore(LocalDate.now()) &&
                        !leave.getEndDate().isBefore(leave.getStartDate()) &&
                        leave.getTotalDays() == 3;

            default:
                return false;
        }
    }

    private boolean checkLeaveLimits(Employee employee, String leaveType) {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        List<Leave> monthlyLeaves = leaveRepository.findByEmployeeAndLeaveTypeAndDateRange(
                employee, leaveType, startOfMonth, endOfMonth);

        // Count only approved and pending leaves
        long count = monthlyLeaves.stream()
                .filter(leave -> "APPROVED".equals(leave.getStatus()) || "PENDING".equals(leave.getStatus()))
                .count();

        LeaveApprovalContext context = new LeaveApprovalContext();
        context.setStrategy(leaveType);

        return count < context.getMaxPerMonth();
    }

    // ========================================
    // GET LEAVE BALANCE METHOD
    // ========================================
    public Map<String, Object> getLeaveBalance(String employeeId) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            throw new RuntimeException("Employee not found");
        }

        Map<String, Object> balance = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        // Short Leave Balance
        List<Leave> todayShortLeaves = leaveRepository.findByEmployeeAndLeaveTypeAndDate(
                employee, "SHORT_LEAVE", today);
        List<Leave> monthlyShortLeaves = leaveRepository.findByEmployeeAndLeaveTypeAndDateRange(
                employee, "SHORT_LEAVE", startOfMonth, endOfMonth);

        LeaveApprovalContext shortContext = new LeaveApprovalContext();
        shortContext.setStrategy("SHORT_LEAVE");

        Map<String, Object> shortLeaveBalance = new HashMap<>();
        shortLeaveBalance.put("usedToday", todayShortLeaves.size());
        shortLeaveBalance.put("maxPerDay", shortContext.getMaxPerDay());
        shortLeaveBalance.put("remainingToday", shortContext.getMaxPerDay() - todayShortLeaves.size());
        shortLeaveBalance.put("usedThisMonth", monthlyShortLeaves.size());
        shortLeaveBalance.put("maxPerMonth", shortContext.getMaxPerMonth());
        shortLeaveBalance.put("remainingThisMonth", shortContext.getMaxPerMonth() - monthlyShortLeaves.size());
        balance.put("shortLeave", shortLeaveBalance);

        // Medium Leave Balance
        List<Leave> monthlyMediumLeaves = leaveRepository.findByEmployeeAndLeaveTypeAndDateRange(
                employee, "MEDIUM_LEAVE", startOfMonth, endOfMonth);

        LeaveApprovalContext mediumContext = new LeaveApprovalContext();
        mediumContext.setStrategy("MEDIUM_LEAVE");

        Map<String, Object> mediumLeaveBalance = new HashMap<>();
        mediumLeaveBalance.put("usedThisMonth", monthlyMediumLeaves.size());
        mediumLeaveBalance.put("maxPerMonth", mediumContext.getMaxPerMonth());
        mediumLeaveBalance.put("remainingThisMonth", mediumContext.getMaxPerMonth() - monthlyMediumLeaves.size());
        balance.put("mediumLeave", mediumLeaveBalance);

        // Long Leave Balance
        List<Leave> monthlyLongLeaves = leaveRepository.findByEmployeeAndLeaveTypeAndDateRange(
                employee, "LONG_LEAVE", startOfMonth, endOfMonth);

        LeaveApprovalContext longContext = new LeaveApprovalContext();
        longContext.setStrategy("LONG_LEAVE");

        Map<String, Object> longLeaveBalance = new HashMap<>();
        longLeaveBalance.put("usedThisMonth", monthlyLongLeaves.size());
        longLeaveBalance.put("maxPerMonth", longContext.getMaxPerMonth());
        longLeaveBalance.put("remainingThisMonth", longContext.getMaxPerMonth() - monthlyLongLeaves.size());
        balance.put("longLeave", longLeaveBalance);

        return balance;
    }

    // ========================================
    // OTHER METHODS (keep existing functionality)
    // ========================================

    public List<Leave> getLeavesByEmployee(String employeeId) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        return leaveRepository.findByEmployeeOrderByRequestDateDesc(employee);
    }

    public Optional<Leave> getLeaveById(String leaveId) {
        return leaveRepository.findById(leaveId);
    }

    @Transactional
    public boolean cancelLeaveRequest(String leaveId, String employeeId) {
        Optional<Leave> leaveOpt = leaveRepository.findById(leaveId);
        if (leaveOpt.isPresent()) {
            Leave leave = leaveOpt.get();
            if (leave.getEmployee().getId().equals(employeeId) &&
                    "PENDING".equals(leave.getStatus())) {
                int result = leaveRepository.deleteLeaveById(leaveId);
                return result > 0;
            }
        }
        return false;
    }

    public LeaveStatistics getLeaveStatistics(String employeeId) {
        long totalRequests = leaveRepository.findByEmployeeId(employeeId).size();
        long pending = leaveRepository.countByEmployeeIdAndStatus(employeeId, "PENDING");
        long approved = leaveRepository.countByEmployeeIdAndStatus(employeeId, "APPROVED");
        long rejected = leaveRepository.countByEmployeeIdAndStatus(employeeId, "REJECTED");

        return new LeaveStatistics(totalRequests, pending, approved, rejected);
    }

    public static class LeaveStatistics {
        private long totalRequests;
        private long pending;
        private long approved;
        private long rejected;

        public LeaveStatistics(long totalRequests, long pending, long approved, long rejected) {
            this.totalRequests = totalRequests;
            this.pending = pending;
            this.approved = approved;
            this.rejected = rejected;
        }

        public long getTotalRequests() { return totalRequests; }
        public long getPending() { return pending; }
        public long getApproved() { return approved; }
        public long getRejected() { return rejected; }
    }

    public List<Leave> getAllLeaveRequests() {
        try {
            List<Leave> leaves = leaveRepository.findAllByOrderByRequestDateDesc();
            System.out.println("Repository returned " + leaves.size() + " leaves");

            for (Leave leave : leaves) {
                if (leave.getEmployee() != null) {
                    leave.getEmployee().getId();
                    leave.getEmployee().getFullName();
                }
            }

            return leaves;
        } catch (Exception e) {
            System.out.println("Error in getAllLeaveRequests service: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Transactional
    public boolean updateLeaveStatus(String leaveId, String status, String comments) {
        Optional<Leave> leaveOpt = leaveRepository.findById(leaveId);
        if (leaveOpt.isPresent()) {
            if (!List.of("APPROVED", "REJECTED", "PENDING").contains(status)) {
                throw new RuntimeException("Invalid status: " + status);
            }

            int result = leaveRepository.updateLeaveStatus(
                    leaveId,
                    status,
                    comments,
                    LocalDateTime.now()
            );

            return result > 0;
        }
        return false;
    }

    public Map<String, Long> getHRLeaveStatistics() {
        Map<String, Long> stats = new HashMap<>();

        long totalLeaves = leaveRepository.count();
        long pendingLeaves = leaveRepository.countByStatus("PENDING");
        long approvedLeaves = leaveRepository.countByStatus("APPROVED");
        long rejectedLeaves = leaveRepository.countByStatus("REJECTED");

        stats.put("total", totalLeaves);
        stats.put("pending", pendingLeaves);
        stats.put("approved", approvedLeaves);
        stats.put("rejected", rejectedLeaves);

        return stats;
    }

    public List<Leave> getLeavesByStatus(String status) {
        return leaveRepository.findByStatusOrderByRequestDateDesc(status);
    }
}