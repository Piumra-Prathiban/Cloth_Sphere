package com.clothsphere.controller.HR;

import com.clothsphere.model.HR.TaskAssignment;
import com.clothsphere.model.SystemUser;
import com.clothsphere.service.HR.TaskAssignmentService;
import com.clothsphere.service.HR.DepartmentService; // Add this import
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/assignments")
public class TaskAssignmentController {

    @Autowired
    private TaskAssignmentService taskAssignmentService;

    @Autowired
    private DepartmentService departmentService; // Fixed variable name

    /**
     * Get all task assignments - CORRECTED VERSION
     */
    /**
     * Get all task assignments - SIMPLIFIED VERSION
     */
    @GetMapping
    public ResponseEntity<List<TaskAssignment>> getAllAssignments(HttpSession session) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            List<TaskAssignment> allAssignments = taskAssignmentService.getAllAssignments();
            System.out.println("Loaded " + allAssignments.size() + " assignments with department details");
            return new ResponseEntity<>(allAssignments, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error fetching assignments: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get assignment by ID
     */
    @GetMapping("/{assignmentId}")
    public ResponseEntity<TaskAssignment> getAssignment(@PathVariable String assignmentId, HttpSession session) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Optional<TaskAssignment> assignment = taskAssignmentService.getAssignmentById(assignmentId);
            if (assignment.isPresent()) {
                return new ResponseEntity<>(assignment.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.out.println("Error fetching assignment: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create new assignment
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAssignment(
            @RequestBody Map<String, Object> assignmentData,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate required fields
            String employeeId = (String) assignmentData.get("employeeId");
            String taskId = (String) assignmentData.get("taskId");

            if (employeeId == null || employeeId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Employee ID is required");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            if (taskId == null || taskId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Task ID is required");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Create assignment using service method for multiple employees
            List<String> employeeIds = java.util.Arrays.asList(employeeId);
            Integer estimatedHours = assignmentData.get("estimatedHours") != null ?
                    Integer.parseInt(assignmentData.get("estimatedHours").toString()) : null;
            String notes = (String) assignmentData.get("notes");

            Map<String, Object> assignmentResult = taskAssignmentService.assignTaskToEmployees(
                    taskId, employeeIds, estimatedHours, notes);

            if ((Boolean) assignmentResult.get("success")) {
                response.put("success", true);
                response.put("message", assignmentResult.get("message"));
                response.put("assignmentId", "Created successfully");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", assignmentResult.get("message"));
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating assignment: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Assign task to multiple employees
     */
    @PostMapping("/bulk-assign")
    public ResponseEntity<Map<String, Object>> bulkAssignTask(
            @RequestBody Map<String, Object> assignmentData,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> response = new HashMap<>();

        try {
            String taskId = (String) assignmentData.get("taskId");
            @SuppressWarnings("unchecked")
            List<String> employeeIds = (List<String>) assignmentData.get("employeeIds");

            if (taskId == null || taskId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Task ID is required");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            if (employeeIds == null || employeeIds.isEmpty()) {
                response.put("success", false);
                response.put("message", "At least one employee ID is required");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            Integer estimatedHours = assignmentData.get("estimatedHours") != null ?
                    Integer.parseInt(assignmentData.get("estimatedHours").toString()) : null;
            String notes = (String) assignmentData.get("notes");

            Map<String, Object> assignmentResult = taskAssignmentService.assignTaskToEmployees(
                    taskId, employeeIds, estimatedHours, notes);

            response.putAll(assignmentResult);
            return new ResponseEntity<>(response,
                    (Boolean) assignmentResult.get("success") ? HttpStatus.OK : HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error bulk assigning task: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update assignment
     */
    @PutMapping("/{assignmentId}")
    public ResponseEntity<Map<String, Object>> updateAssignment(
            @PathVariable String assignmentId,
            @RequestBody Map<String, Object> assignmentData,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<TaskAssignment> existingAssignmentOpt = taskAssignmentService.getAssignmentById(assignmentId);
            if (!existingAssignmentOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Assignment not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // Validate status if provided
            String status = (String) assignmentData.get("status");
            if (status != null && !taskAssignmentService.isValidStatus(status)) {
                response.put("success", false);
                response.put("message", "Valid status is required (ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED)");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Create updated assignment data
            TaskAssignment updatedData = new TaskAssignment();
            if (assignmentData.get("estimatedHours") != null) {
                updatedData.setEstimatedHours(Integer.parseInt(assignmentData.get("estimatedHours").toString()));
            }
            if (assignmentData.get("actualHours") != null) {
                updatedData.setActualHours(Integer.parseInt(assignmentData.get("actualHours").toString()));
            }
            if (status != null) {
                updatedData.setStatus(status);
            }
            if (assignmentData.get("notes") != null) {
                updatedData.setNotes((String) assignmentData.get("notes"));
            }

            // Update assignment
            TaskAssignment updatedAssignment = taskAssignmentService.updateAssignment(assignmentId, updatedData);

            if (updatedAssignment != null) {
                response.put("success", true);
                response.put("message", "Assignment updated successfully!");
                response.put("assignment", updatedAssignment);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "Failed to update assignment");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating assignment: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete assignment
     */
    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<Map<String, Object>> deleteAssignment(
            @PathVariable String assignmentId,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> response = new HashMap<>();

        try {
            boolean success = taskAssignmentService.deleteAssignment(assignmentId);

            if (success) {
                response.put("success", true);
                response.put("message", "Assignment deleted successfully!");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "Assignment not found or failed to delete");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting assignment: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get assignments by employee
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<TaskAssignment>> getAssignmentsByEmployee(
            @PathVariable String employeeId,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            List<TaskAssignment> assignments = taskAssignmentService.getAssignmentsByEmployeeId(employeeId);
            return new ResponseEntity<>(assignments, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error fetching assignments by employee: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get assignments by task
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<TaskAssignment>> getAssignmentsByTask(
            @PathVariable String taskId,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            List<TaskAssignment> assignments = taskAssignmentService.getAssignmentsByTaskId(taskId);
            return new ResponseEntity<>(assignments, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error fetching assignments by task: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get assignments by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskAssignment>> getAssignmentsByStatus(
            @PathVariable String status,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            List<TaskAssignment> assignments = taskAssignmentService.getAssignmentsByStatus(status);
            return new ResponseEntity<>(assignments, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error fetching assignments by status: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get assignment statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getAssignmentStatistics(HttpSession session) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Map<String, Object> stats = taskAssignmentService.getAssignmentStatistics();
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error fetching assignment statistics: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}