package com.clothsphere.controller.HR;

import com.clothsphere.model.HR.TaskAssignment;
import com.clothsphere.model.SystemUser;
import com.clothsphere.service.HR.TaskAssignmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employee")
public class EmployeeTaskController {

    @Autowired
    private TaskAssignmentService taskAssignmentService;

    /**
     * Get tasks assigned to current employee
     */
    @GetMapping("/tasks")
    public ResponseEntity<List<Map<String, Object>>> getEmployeeTasks(HttpSession session) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"employee".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            // Get employee by username
            String username = currentUser.getUserName();

            // Get assignments for current employee
            List<TaskAssignment> assignments = taskAssignmentService.getAllAssignments().stream()
                    .filter(assignment ->
                            assignment.getEmployee() != null &&
                                    assignment.getEmployee().getUsername().equals(username))
                    .collect(Collectors.toList());

            // Convert to simplified format for frontend
            List<Map<String, Object>> taskList = assignments.stream().map(assignment -> {
                Map<String, Object> taskMap = new HashMap<>();
                taskMap.put("assignmentId", assignment.getAssignmentId());
                taskMap.put("taskName", assignment.getTask() != null ? assignment.getTask().getTaskName() : "N/A");
                taskMap.put("description", assignment.getTask() != null ? assignment.getTask().getDescription() : null);
                taskMap.put("departmentName", assignment.getDepartment() != null ?
                        assignment.getDepartment().getDepartmentName() : "N/A");
                taskMap.put("assignedDate", assignment.getAssignedDate());
                taskMap.put("deadline", assignment.getTask() != null ? assignment.getTask().getDeadline() : null);
                taskMap.put("estimatedHours", assignment.getEstimatedHours());
                taskMap.put("status", assignment.getStatus());
                taskMap.put("notes", assignment.getNotes());
                return taskMap;
            }).collect(Collectors.toList());

            return new ResponseEntity<>(taskList, HttpStatus.OK);

        } catch (Exception e) {
            System.out.println("Error fetching employee tasks: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update assignment status
     */
    @PutMapping("/assignments/{assignmentId}/status")
    public ResponseEntity<Map<String, Object>> updateAssignmentStatus(
            @PathVariable String assignmentId,
            @RequestBody Map<String, String> statusData,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"employee".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> response = new HashMap<>();

        try {
            String newStatus = statusData.get("status");
            if (newStatus == null || !isValidStatusTransition(newStatus)) {
                response.put("success", false);
                response.put("message", "Invalid status transition");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Verify the assignment belongs to the current employee
            String username = currentUser.getUserName();
            List<TaskAssignment> employeeAssignments = taskAssignmentService.getAllAssignments().stream()
                    .filter(assignment ->
                            assignment.getEmployee() != null &&
                                    assignment.getEmployee().getUsername().equals(username) &&
                                    assignment.getAssignmentId().equals(assignmentId))
                    .collect(Collectors.toList());

            if (employeeAssignments.isEmpty()) {
                response.put("success", false);
                response.put("message", "Assignment not found or access denied");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // Update assignment status
            TaskAssignment assignmentData = new TaskAssignment();
            assignmentData.setStatus(newStatus);

            TaskAssignment updatedAssignment = taskAssignmentService.updateAssignment(assignmentId, assignmentData);

            if (updatedAssignment != null) {
                response.put("success", true);
                response.put("message", "Status updated successfully");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "Failed to update status");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating status: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Validate status transitions
     */
    private boolean isValidStatusTransition(String newStatus) {
        return "IN_PROGRESS".equals(newStatus) || "COMPLETED".equals(newStatus);
    }
}