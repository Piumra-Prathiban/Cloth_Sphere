package com.clothsphere.service.HR;

import com.clothsphere.model.HR.Leave;
import com.clothsphere.model.HR.Employee;
import com.clothsphere.repository.HR.LeaveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class LeaveService {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private EmployeeService employeeService;

    // Request new leave using manual INSERT query
    @Transactional
    public Leave requestLeave(Leave leave, String employeeId) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            throw new RuntimeException("Employee not found");
        }

        // Validate dates
        if (leave.getStartDate().isAfter(leave.getEndDate())) {
            throw new RuntimeException("Start date cannot be after end date");
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

        // Generate leave ID manually
        Long nextIdNumber = leaveRepository.getNextLeaveIdNumber();
        String leaveId = "lev" + nextIdNumber;

        // Using updated manual INSERT query with leave_id
        int result = leaveRepository.insertLeave(
                leaveId,
                leave.getReason(),
                leave.getStartDate(),
                leave.getEndDate(),
                "PENDING",
                LocalDateTime.now(),
                employeeId,
                leave.getComments()
        );

        if (result > 0) {
            // Set the generated ID and return the leave object
            leave.setLeaveId(leaveId);
            leave.setEmployee(employee);
            leave.setStatus("PENDING");
            leave.setRequestDate(LocalDateTime.now());
            return leave;
        } else {
            throw new RuntimeException("Failed to save leave request");
        }
    }

    // Get all leaves for an employee
    public List<Leave> getLeavesByEmployee(String employeeId) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        return leaveRepository.findByEmployeeOrderByRequestDateDesc(employee);
    }

    // Get leave by ID
    public Optional<Leave> getLeaveById(String leaveId) {
        return leaveRepository.findById(leaveId);
    }

    // Cancel leave request using manual DELETE query
    @Transactional
    public boolean cancelLeaveRequest(String leaveId, String employeeId) {
        Optional<Leave> leaveOpt = leaveRepository.findById(leaveId);
        if (leaveOpt.isPresent()) {
            Leave leave = leaveOpt.get();
            if (leave.getEmployee().getId().equals(employeeId) &&
                    "PENDING".equals(leave.getStatus())) {

                // Using manual DELETE query
                int result = leaveRepository.deleteLeaveById(leaveId);
                return result > 0;
            }
        }
        return false;
    }

    // Get leave statistics for employee using manual count query
    public LeaveStatistics getLeaveStatistics(String employeeId) {
        long totalRequests = leaveRepository.findByEmployeeId(employeeId).size();
        long pending = leaveRepository.countByEmployeeIdAndStatus(employeeId, "PENDING");
        long approved = leaveRepository.countByEmployeeIdAndStatus(employeeId, "APPROVED");
        long rejected = leaveRepository.countByEmployeeIdAndStatus(employeeId, "REJECTED");

        return new LeaveStatistics(totalRequests, pending, approved, rejected);
    }

    // DTO for leave statistics
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

        // Getters
        public long getTotalRequests() { return totalRequests; }
        public long getPending() { return pending; }
        public long getApproved() { return approved; }
        public long getRejected() { return rejected; }
    }

    // Get all leave requests for HR
    public List<Leave> getAllLeaveRequests() {
        try {
            List<Leave> leaves = leaveRepository.findAllByOrderByRequestDateDesc();
            System.out.println("Repository returned " + leaves.size() + " leaves");

            // Ensure employee data is loaded
            for (Leave leave : leaves) {
                if (leave.getEmployee() != null) {
                    // This will force Hibernate to load the employee data
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

    // Update leave status using manual UPDATE query
    @Transactional
    public boolean updateLeaveStatus(String leaveId, String status, String comments) {
        Optional<Leave> leaveOpt = leaveRepository.findById(leaveId);
        if (leaveOpt.isPresent()) {
            Leave leave = leaveOpt.get();

            // Validate status
            if (!List.of("APPROVED", "REJECTED", "PENDING").contains(status)) {
                throw new RuntimeException("Invalid status: " + status);
            }

            // Using manual UPDATE query
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


    // Get HR dashboard statistics
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

    // Get leaves by status
    public List<Leave> getLeavesByStatus(String status) {
        return leaveRepository.findByStatusOrderByRequestDateDesc(status);
    }

}