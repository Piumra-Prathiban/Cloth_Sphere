package com.clothsphere.controller.HR;

import com.clothsphere.model.HR.Leave;
import com.clothsphere.service.HR.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/hr/leave")
public class HRLeaveController {

    @Autowired
    private LeaveService leaveService;

    // Get all leave requests for HR management
    @GetMapping("/requests")
    public ResponseEntity<?> getAllLeaveRequests() {
        try {
            List<Leave> allLeaves = leaveService.getAllLeaveRequests();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("leaves", allLeaves);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching leave requests: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Update leave status (Approve/Reject)
    @PutMapping("/{leaveId}/status")
    public ResponseEntity<?> updateLeaveStatus(@PathVariable String leaveId,
                                               @RequestParam String status,
                                               @RequestParam(required = false) String comments) {
        try {
            boolean updated = leaveService.updateLeaveStatus(leaveId, status, comments);

            Map<String, Object> response = new HashMap<>();
            if (updated) {
                response.put("success", true);
                response.put("message", "Leave request " + status.toLowerCase() + " successfully");
            } else {
                response.put("success", false);
                response.put("message", "Failed to update leave status");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error updating leave status: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Get leave requests by employee ID
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> getLeavesByEmployee(@PathVariable String employeeId) {
        try {
            List<Leave> leaves = leaveService.getLeavesByEmployee(employeeId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("leaves", leaves);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching employee leaves: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Get leave statistics for dashboard
    @GetMapping("/statistics")
    public ResponseEntity<?> getLeaveStatistics() {
        try {
            Map<String, Long> stats = leaveService.getHRLeaveStatistics();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching leave statistics: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}