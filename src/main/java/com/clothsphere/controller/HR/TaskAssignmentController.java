package com.clothsphere.controller.HR;

import com.clothsphere.model.HR.TaskAssignment;
import com.clothsphere.model.HR.ProductionTask;
import com.clothsphere.model.SystemUser;
import com.clothsphere.service.HR.TaskAssignmentService;
import com.clothsphere.service.HR.productionTaskService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/assignments")
public class TaskAssignmentController {

    private static final Logger logger = LoggerFactory.getLogger(TaskAssignmentController.class);

    @Autowired
    private TaskAssignmentService taskAssignmentService;

    @Autowired
    private productionTaskService productionTaskService;

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
            logger.info("Loaded {} assignments with department details", allAssignments.size());
            return new ResponseEntity<>(allAssignments, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching assignments: {}", e.getMessage(), e);
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
            return assignment.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            logger.error("Error fetching assignment: {}", e.getMessage(), e);
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
            List<String> employeeIds = Collections.singletonList(employeeId);
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
     * Update assignment - UPDATED VERSION with automatic task status update
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

            // Update assignment with automatic task status update
            TaskAssignment updatedAssignment = taskAssignmentService.updateAssignmentWithTaskStatus(assignmentId, updatedData);

            if (updatedAssignment != null) {
                response.put("success", true);
                response.put("message", "Assignment updated successfully!");
                response.put("assignment", updatedAssignment);

                // Get updated task status
                String taskId = updatedAssignment.getTask().getTaskId();
                Optional<ProductionTask> taskOpt = productionTaskService.getTaskById(taskId);
                if (taskOpt.isPresent()) {
                    response.put("taskStatus", taskOpt.get().getStatus());
                    response.put("taskProgress", taskAssignmentService.getTaskCompletionProgress(taskId));
                }

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
            logger.error("Error fetching assignments by employee: {}", e.getMessage(), e);
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
            logger.error("Error fetching assignments by task: {}", e.getMessage(), e);
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
            logger.error("Error fetching assignments by status: {}", e.getMessage(), e);
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
            logger.error("Error fetching assignment statistics: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get task completion progress
     */
    @GetMapping("/task/{taskId}/progress")
    public ResponseEntity<Map<String, Object>> getTaskProgress(
            @PathVariable String taskId,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Map<String, Object> progress = taskAssignmentService.getTaskCompletionProgress(taskId);
            return new ResponseEntity<>(progress, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching task progress: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Employee update assignment status - with actual hours input
     */
    @PutMapping("/employee/{assignmentId}/status")
    public ResponseEntity<Map<String, Object>> updateEmployeeAssignmentStatus(
            @PathVariable String assignmentId,
            @RequestBody Map<String, Object> statusData,  // Changed to Object to accept numbers
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"employee".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> response = new HashMap<>();

        try {
            String status = (String) statusData.get("status");
            if (status == null || !taskAssignmentService.isValidStatus(status)) {
                response.put("success", false);
                response.put("message", "Valid status is required (ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED)");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Validate actual hours if provided
            Integer actualHours = null;
            if (statusData.get("actualHours") != null) {
                try {
                    // Handle both integer and decimal inputs
                    Object hoursObj = statusData.get("actualHours");
                    if (hoursObj instanceof Number) {
                        actualHours = ((Number) hoursObj).intValue();
                    } else {
                        actualHours = Integer.parseInt(hoursObj.toString());
                    }

                    if (actualHours < 1) {
                        response.put("success", false);
                        response.put("message", "Actual hours must be at least 1");
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }
                } catch (NumberFormatException e) {
                    response.put("success", false);
                    response.put("message", "Invalid actual hours format");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }

            // Verify the assignment belongs to the current employee
            Optional<TaskAssignment> assignmentOpt = taskAssignmentService.getAssignmentById(assignmentId);
            if (!assignmentOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Assignment not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            TaskAssignment assignment = assignmentOpt.get();
            if (assignment.getEmployee() == null || !assignment.getEmployee().getUsername().equals(currentUser.getUserName())) {
                response.put("success", false);
                response.put("message", "You can only update your own assignments");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }

            // Update assignment status with actual hours
            TaskAssignment updatedAssignment = taskAssignmentService.updateAssignmentStatus(
                    assignmentId, status, actualHours);

            if (updatedAssignment != null) {
                response.put("success", true);
                response.put("message", "Assignment status updated successfully!");
                response.put("assignment", updatedAssignment);

                // Include completion information if status is COMPLETED
                if ("COMPLETED".equals(status)) {
                    response.put("completionDate", updatedAssignment.getCompletionDate());
                    response.put("actualHours", updatedAssignment.getActualHours());
                }

                // Get updated task progress information
                String taskId = updatedAssignment.getTask().getTaskId();
                response.put("taskProgress", taskAssignmentService.getTaskCompletionProgress(taskId));

                Optional<ProductionTask> taskOpt = productionTaskService.getTaskById(taskId);
                if (taskOpt.isPresent()) {
                    response.put("taskStatus", taskOpt.get().getStatus());
                }

                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "Failed to update assignment status");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            logger.error("Error updating assignment status: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error updating assignment status: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get tasks for current employee
     */
    @GetMapping("/employee/tasks")
    public ResponseEntity<List<Map<String, Object>>> getEmployeeTasks(HttpSession session) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"employee".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            String username = currentUser.getUserName();
            List<TaskAssignment> assignments = taskAssignmentService.getAssignmentsByEmployeeUsername(username);

            // Convert to simplified DTO for frontend
            List<Map<String, Object>> taskList = assignments.stream().map(assignment -> {
                Map<String, Object> taskMap = new HashMap<>();
                taskMap.put("assignmentId", assignment.getAssignmentId());
                taskMap.put("taskName", assignment.getTask().getTaskName());
                taskMap.put("description", assignment.getTask().getDescription());
                taskMap.put("departmentName", assignment.getDepartment() != null ?
                        assignment.getDepartment().getDepartmentName() : "N/A");
                taskMap.put("assignedDate", assignment.getAssignedDate());
                taskMap.put("deadline", assignment.getTask().getDeadline());
                taskMap.put("estimatedHours", assignment.getEstimatedHours());
                taskMap.put("actualHours", assignment.getActualHours());
                taskMap.put("completionDate", assignment.getCompletionDate());
                taskMap.put("status", assignment.getStatus());
                taskMap.put("notes", assignment.getNotes());
                return taskMap;
            }).collect(Collectors.toList());

            logger.info("Loaded {} tasks for employee: {}", taskList.size(), username);
            return new ResponseEntity<>(taskList, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error fetching employee tasks: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}