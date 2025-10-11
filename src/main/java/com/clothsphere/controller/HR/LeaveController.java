package com.clothsphere.controller.HR;

import com.clothsphere.model.HR.Leave;
import com.clothsphere.model.SystemUser;
import com.clothsphere.service.HR.EmployeeService;
import com.clothsphere.service.HR.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/leave")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private EmployeeService employeeService;

    // Helper method to get employee ID from session
    private String getEmployeeIdFromSession(HttpSession session) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            throw new RuntimeException("User not logged in");
        }

        // Get employee ID using username
        String username = currentUser.getUserName();
        var employee = employeeService.getEmployeeByUsername(username);
        if (employee == null) {
            throw new RuntimeException("Employee record not found for username: " + username);
        }

        return employee.getId();
    }

    // Show leave request page
    @GetMapping("/request")
    public String showLeaveRequestPage(Model model) {
        return "employeeDashboard :: leave-content";
    }

    // API to submit leave request - WITH STRATEGY PATTERN
    @PostMapping("/request")
    public ResponseEntity<?> submitLeaveRequest(@RequestParam String reason,
                                                @RequestParam String startDate,
                                                @RequestParam String endDate,
                                                HttpSession session) {
        try {
            String employeeId = getEmployeeIdFromSession(session);

            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            Leave leave = new Leave();
            leave.setReason(reason);
            leave.setStartDate(start);
            leave.setEndDate(end);

            // USE STRATEGY PATTERN - This will auto-approve eligible leaves
            Leave savedLeave = leaveService.requestLeave(leave, employeeId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("leaveId", savedLeave.getLeaveId());
            response.put("status", savedLeave.getStatus());

            // Different messages based on auto-approval
            if ("APPROVED".equals(savedLeave.getStatus())) {
                response.put("message", "🎉 Leave request AUTO-APPROVED! " +
                        savedLeave.getComments());
            } else {
                response.put("message", "Leave request submitted successfully. " +
                        savedLeave.getComments());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API to get employee's leave history
    @GetMapping("/history")
    public ResponseEntity<?> getLeaveHistory(HttpSession session) {
        try {
            String employeeId = getEmployeeIdFromSession(session);

            List<Leave> leaves = leaveService.getLeavesByEmployee(employeeId);
            LeaveService.LeaveStatistics stats = leaveService.getLeaveStatistics(employeeId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("leaves", leaves);
            response.put("statistics", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching leave history: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API to cancel leave request
    @DeleteMapping("/cancel/{leaveId}")
    public ResponseEntity<?> cancelLeaveRequest(@PathVariable String leaveId,
                                                HttpSession session) {
        try {
            String employeeId = getEmployeeIdFromSession(session);

            boolean cancelled = leaveService.cancelLeaveRequest(leaveId, employeeId);

            Map<String, Object> response = new HashMap<>();
            if (cancelled) {
                response.put("success", true);
                response.put("message", "Leave request cancelled successfully");
            } else {
                response.put("success", false);
                response.put("message", "Cannot cancel this leave request");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error cancelling leave request: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}