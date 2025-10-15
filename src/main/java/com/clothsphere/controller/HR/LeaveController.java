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
import java.time.LocalTime;
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

    // API to get leave balance
    @GetMapping("/balance")
    @ResponseBody
    public ResponseEntity<?> getLeaveBalance(HttpSession session) {
        try {
            String employeeId = getEmployeeIdFromSession(session);
            Map<String, Object> balance = leaveService.getLeaveBalance(employeeId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("balance", balance);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching leave balance: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API to submit leave request - WITH STRATEGY PATTERN FOR DIFFERENT TYPES
    @PostMapping("/request")
    public ResponseEntity<?> submitLeaveRequest(@RequestParam String reason,
                                                @RequestParam String leaveType,
                                                @RequestParam(required = false) String startDate,
                                                @RequestParam(required = false) String endDate,
                                                @RequestParam(required = false) String leaveDate,
                                                @RequestParam(required = false) String leaveTime,
                                                @RequestParam(required = false) Integer duration,
                                                HttpSession session) {
        try {
            String employeeId = getEmployeeIdFromSession(session);

            Leave leave = new Leave();
            leave.setReason(reason);
            leave.setLeaveType(leaveType);

            // Set dates and times based on leave type
            switch (leaveType) {
                case "SHORT_LEAVE":
                    LocalDate shortLeaveDate = LocalDate.parse(leaveDate);
                    LocalTime startTime = LocalTime.parse(leaveTime);
                    LocalTime endTime = startTime.plusHours(duration != null ? duration : 2);

                    leave.setStartDate(shortLeaveDate);
                    leave.setEndDate(shortLeaveDate);
                    leave.setStartTime(startTime);
                    leave.setEndTime(endTime);
                    leave.setTotalHours(duration != null ? duration : 2);
                    break;

                case "MEDIUM_LEAVE":
                    LocalDate mediumLeaveDate = LocalDate.parse(leaveDate);
                    leave.setStartDate(mediumLeaveDate);
                    leave.setEndDate(mediumLeaveDate);
                    leave.setTotalHours(8); // 8 hours for 1 day
                    break;

                case "LONG_LEAVE":
                    LocalDate longStartDate = LocalDate.parse(startDate);
                    LocalDate longEndDate = LocalDate.parse(endDate);
                    leave.setStartDate(longStartDate);
                    leave.setEndDate(longEndDate);
                    leave.setTotalHours(24); // 24 hours for 3 days
                    break;

                default:
                    throw new RuntimeException("Invalid leave type: " + leaveType);
            }

            // USE STRATEGY PATTERN - This will auto-approve eligible leaves
            Leave savedLeave = leaveService.requestLeave(leave, employeeId, leaveType);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("leaveId", savedLeave.getLeaveId());
            response.put("status", savedLeave.getStatus());
            response.put("leaveType", savedLeave.getLeaveType());

            // Different messages based on auto-approval
            if ("APPROVED".equals(savedLeave.getStatus())) {
                response.put("message", getLeaveTypeLabel(leaveType) + " AUTO-APPROVED! " +
                        savedLeave.getComments());
            } else {
                response.put("message", getLeaveTypeLabel(leaveType) + " request submitted successfully. " +
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

    // Helper method to get leave type label
    private String getLeaveTypeLabel(String leaveType) {
        switch (leaveType) {
            case "SHORT_LEAVE":
                return "2-Hour Leave";
            case "MEDIUM_LEAVE":
                return "1-Day Leave";
            case "LONG_LEAVE":
                return "3-Day Leave";
            default:
                return "Leave";
        }
    }
}