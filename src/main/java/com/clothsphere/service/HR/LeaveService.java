package com.clothsphere.service.HR;

import com.clothsphere.model.HR.Leave;
import com.clothsphere.model.HR.Employee;
import com.clothsphere.repository.HR.LeaveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveService {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private EmployeeService employeeService;

    // Request new leave
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

        leave.setEmployee(employee);
        leave.setStatus("PENDING");
        leave.setRequestDate(LocalDateTime.now());

        return leaveRepository.save(leave);
    }

    // Get all leaves for an employee
    public List<Leave> getLeavesByEmployee(String employeeId) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        return leaveRepository.findByEmployeeOrderByRequestDateDesc(employee);
    }

    // Get leave by ID
    public Optional<Leave> getLeaveById(Long leaveId) {
        return leaveRepository.findById(leaveId);
    }

    // Cancel leave request (only if pending)
    public boolean cancelLeaveRequest(Long leaveId, String employeeId) {
        Optional<Leave> leaveOpt = leaveRepository.findById(leaveId);
        if (leaveOpt.isPresent()) {
            Leave leave = leaveOpt.get();
            if (leave.getEmployee().getId().equals(employeeId) &&
                    "PENDING".equals(leave.getStatus())) {
                leaveRepository.delete(leave);
                return true;
            }
        }
        return false;
    }

    // Get leave statistics for employee
    public LeaveStatistics getLeaveStatistics(String employeeId) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        List<Leave> allLeaves = leaveRepository.findByEmployeeOrderByRequestDateDesc(employee);

        long totalRequests = allLeaves.size();
        long pending = allLeaves.stream().filter(l -> "PENDING".equals(l.getStatus())).count();
        long approved = allLeaves.stream().filter(l -> "APPROVED".equals(l.getStatus())).count();
        long rejected = allLeaves.stream().filter(l -> "REJECTED".equals(l.getStatus())).count();

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
}