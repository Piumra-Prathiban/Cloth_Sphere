package com.clothsphere.service.HR;

import com.clothsphere.model.HR.Department;
import com.clothsphere.model.HR.Employee;
import com.clothsphere.model.HR.ProductionTask;
import com.clothsphere.model.HR.TaskAssignment;
import com.clothsphere.repository.HR.TaskAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskAssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(TaskAssignmentService.class);

    @Autowired
    private TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    @Lazy
    private productionTaskService productionTaskService;

    /**
     * Generate the next assignment ID in format asg01, asg02, etc.
     */
    public String generateNextAssignmentId() {
        List<String> existingIds = taskAssignmentRepository.findAllAssignmentIds();

        if (existingIds.isEmpty()) {
            return "asg01";
        }

        int maxNumber = 0;
        for (String id : existingIds) {
            if (id != null && id.startsWith("asg") && id.length() == 5) {
                try {
                    String numberPart = id.substring(3);
                    int number = Integer.parseInt(numberPart);
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }

        int nextNumber = maxNumber + 1;
        return String.format("asg%02d", nextNumber);
    }

    /**
     * Create a new task assignment - FIXED VERSION
     */
    @Transactional
    public TaskAssignment createAssignment(TaskAssignment assignment) {
        try {
            assignment.setAssignmentId(generateNextAssignmentId());
            assignment.setAssignedDate(LocalDate.now());

            if (assignment.getStatus() == null || assignment.getStatus().isEmpty()) {
                assignment.setStatus("ASSIGNED");
            }

            int result = taskAssignmentRepository.insertAssignment(assignment);
            if (result > 0) {
                return assignment;
            } else {
                throw new RuntimeException("Failed to insert assignment into database");
            }
        } catch (Exception e) {
            logger.error("Error creating assignment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create assignment: " + e.getMessage(), e);
        }
    }

    /**
     * Get assignment by ID
     */
    @Transactional(readOnly = true)
    public Optional<TaskAssignment> getAssignmentById(String assignmentId) {
        return taskAssignmentRepository.findById(assignmentId);
    }

    /**
     * Update task assignment
     */
    @Transactional
    public TaskAssignment updateAssignment(String assignmentId, TaskAssignment assignmentData) {
        Optional<TaskAssignment> optionalAssignment = taskAssignmentRepository.findById(assignmentId);
        if (optionalAssignment.isPresent()) {
            TaskAssignment existingAssignment = optionalAssignment.get();

            if (assignmentData.getEmployee() != null) {
                existingAssignment.setEmployee(assignmentData.getEmployee());
            }
            if (assignmentData.getTask() != null) {
                existingAssignment.setTask(assignmentData.getTask());
            }
            if (assignmentData.getEstimatedHours() != null) {
                existingAssignment.setEstimatedHours(assignmentData.getEstimatedHours());
            }
            if (assignmentData.getActualHours() != null) {
                existingAssignment.setActualHours(assignmentData.getActualHours());
            }
            if (assignmentData.getStatus() != null) {
                existingAssignment.setStatus(assignmentData.getStatus());

                // Set completion date if status is COMPLETED
                if ("COMPLETED".equals(assignmentData.getStatus())) {
                    existingAssignment.setCompletionDate(LocalDate.now());
                }
            }
            if (assignmentData.getNotes() != null) {
                existingAssignment.setNotes(assignmentData.getNotes());
            }

            taskAssignmentRepository.updateAssignment(existingAssignment);
            return existingAssignment;
        }
        return null;
    }

    /**
     * Delete assignment
     */
    @Transactional
    public boolean deleteAssignment(String assignmentId) {
        try {
            if (taskAssignmentRepository.existsById(assignmentId)) {
                taskAssignmentRepository.deleteByAssignmentId(assignmentId);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("Error deleting assignment: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get assignments by employee
     */
    @Transactional(readOnly = true)
    public List<TaskAssignment> getAssignmentsByEmployee(Employee employee) {
        return taskAssignmentRepository.findByEmployee(employee);
    }

    /**
     * Get assignments by employee ID
     */
    @Transactional(readOnly = true)
    public List<TaskAssignment> getAssignmentsByEmployeeId(String employeeId) {
        return taskAssignmentRepository.findByEmployeeId(employeeId);
    }

    /**
     * Get assignments by task
     */
    @Transactional(readOnly = true)
    public List<TaskAssignment> getAssignmentsByTask(ProductionTask task) {
        return taskAssignmentRepository.findByTask(task);
    }

    /**
     * Get assignments by task ID
     */
    @Transactional(readOnly = true)
    public List<TaskAssignment> getAssignmentsByTaskId(String taskId) {
        return taskAssignmentRepository.findByTaskId(taskId);
    }

    /**
     * Get active assignments by employee (not completed or cancelled)
     */
    @Transactional(readOnly = true)
    public List<TaskAssignment> getActiveAssignmentsByEmployee(Employee employee) {
        return taskAssignmentRepository.findActiveAssignmentsByEmployee(employee);
    }

    /**
     * Get active assignments by employee ID
     */
    @Transactional(readOnly = true)
    public List<TaskAssignment> getActiveAssignmentsByEmployeeId(String employeeId) {
        return taskAssignmentRepository.findActiveAssignmentsByEmployeeId(employeeId);
    }

    /**
     * Get assignments by status
     */
    @Transactional(readOnly = true)
    public List<TaskAssignment> getAssignmentsByStatus(String status) {
        return taskAssignmentRepository.findByStatus(status);
    }

    /**
     * Get assignments by department ID - Make sure this method exists
     */
    @Transactional(readOnly = true)
    public List<TaskAssignment> getAssignmentsByDepartmentId(String departmentId) {
        try {
            return taskAssignmentRepository.findByDepartmentId(departmentId);
        } catch (Exception e) {
            System.out.println("Error loading assignments for department " + departmentId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Check if employee has active assignments
     */
    @Transactional(readOnly = true)
    public boolean hasActiveAssignments(Employee employee) {
        Long count = taskAssignmentRepository.countActiveAssignmentsByEmployee(employee);
        return count != null && count > 0;
    }

    /**
     * Check if task has active assignments
     */
    @Transactional(readOnly = true)
    public boolean hasActiveAssignments(ProductionTask task) {
        Long count = taskAssignmentRepository.countActiveAssignmentsByTask(task);
        return count != null && count > 0;
    }

    /**
     * Get assignment statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAssignmentStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Count by status
        stats.put("totalAssignments", taskAssignmentRepository.countAllAssignments());
        stats.put("assignedTasks", taskAssignmentRepository.findByStatus("ASSIGNED").size());
        stats.put("inProgressTasks", taskAssignmentRepository.findByStatus("IN_PROGRESS").size());
        stats.put("completedTasks", taskAssignmentRepository.findByStatus("COMPLETED").size());
        stats.put("cancelledTasks", taskAssignmentRepository.findByStatus("CANCELLED").size());

        return stats;
    }

    /**
     * Validate status value
     */
    public boolean isValidStatus(String status) {
        return status != null && (status.equals("ASSIGNED") || status.equals("IN_PROGRESS") ||
                status.equals("COMPLETED") || status.equals("CANCELLED"));
    }

    /**
     * Assign task to multiple employees
     */
    /**
     * Assign task to multiple employees - FIXED VERSION
     */
    @Transactional
    public Map<String, Object> assignTaskToEmployees(String taskId, List<String> employeeIds, Integer estimatedHours, String notes) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        List<String> errorMessages = new ArrayList<>();

        // Validate task exists first
        Optional<ProductionTask> taskOpt = productionTaskService.getTaskById(taskId);
        if (!taskOpt.isPresent()) {
            result.put("success", false);
            result.put("message", "Task not found");
            return result;
        }

        ProductionTask task = taskOpt.get();

        // Validate all employees exist before starting assignments
        List<Employee> validEmployees = new ArrayList<>();
        for (String employeeId : employeeIds) {
            try {
                Employee employee = employeeService.getEmployeeById(employeeId);
                if (employee == null) {
                    errorMessages.add("Employee " + employeeId + " not found");
                    continue;
                }

                // Check if already assigned
                List<TaskAssignment> existingAssignments = getActiveAssignmentsByEmployeeId(employeeId);
                boolean alreadyAssigned = existingAssignments.stream()
                        .anyMatch(assignment -> assignment.getTask().getTaskId().equals(taskId));

                if (alreadyAssigned) {
                    errorMessages.add("Employee " + employee.getFullName() + " already has this task assigned");
                    continue;
                }

                validEmployees.add(employee);
            } catch (Exception e) {
                errorMessages.add("Error validating employee " + employeeId + ": " + e.getMessage());
            }
        }

        // If no valid employees, return early
        if (validEmployees.isEmpty()) {
            result.put("success", false);
            result.put("message", "No valid employees found: " + String.join(", ", errorMessages));
            result.put("assignedCount", 0);
            result.put("totalCount", employeeIds.size());
            return result;
        }

        // Create assignments for valid employees
        for (Employee employee : validEmployees) {
            try {
                TaskAssignment assignment = new TaskAssignment();
                assignment.setEmployee(employee);
                assignment.setTask(task);
                assignment.setDepartment(task.getDepartment());
                assignment.setEstimatedHours(estimatedHours);
                assignment.setNotes(notes);
                assignment.setStatus("ASSIGNED");

                TaskAssignment savedAssignment = createAssignment(assignment);
                if (savedAssignment != null) {
                    successCount++;
                    logger.info("Successfully assigned task {} to employee {}", taskId, employee.getId());
                }
            } catch (Exception e) {
                String errorMsg = "Error assigning task to employee " + employee.getId() + ": " + e.getMessage();
                errorMessages.add(errorMsg);
                logger.error(errorMsg, e);
                // Continue with other employees instead of failing the entire batch
            }
        }

        // Build result
        result.put("success", successCount > 0);
        result.put("assignedCount", successCount);
        result.put("totalCount", employeeIds.size());

        String message;
        if (successCount == employeeIds.size()) {
            message = "Successfully assigned task to all " + successCount + " employees";
        } else if (successCount > 0) {
            message = "Assigned to " + successCount + " out of " + employeeIds.size() + " employees. " +
                    String.join(" ", errorMessages);
        } else {
            message = "Failed to assign task to any employees: " + String.join(" ", errorMessages);
        }

        result.put("message", message);
        return result;
    }

    /**
     * Get all assignments (simple version) - FIXED VERSION
     */
    @Transactional(readOnly = true)
    public List<TaskAssignment> getAllAssignments() {
        try {
            return taskAssignmentRepository.findAllWithDepartmentDetails();
        } catch (Exception e) {
            System.out.println("Error loading all assignments: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get all assignments with employee and task details loaded
     */
    @Transactional(readOnly = true)
    public List<TaskAssignment> getAllAssignmentsWithDetails() {
        List<TaskAssignment> assignments = taskAssignmentRepository.findAll();

        // Eagerly load employee and task details to avoid LazyLoadingException
        for (TaskAssignment assignment : assignments) {
            if (assignment.getEmployee() != null) {
                // Initialize employee details
                assignment.getEmployee().getFullName();
                if (assignment.getEmployee().getDepartment() != null) {
                    assignment.getEmployee().getDepartment().getDepartmentName();
                }
            }
            if (assignment.getTask() != null) {
                // Initialize task details
                assignment.getTask().getTaskName();
                if (assignment.getTask().getDepartment() != null) {
                    assignment.getTask().getDepartment().getDepartmentName();
                }
            }
        }

        return assignments;
    }

    /**
     * Get assignments by employee username
     */
    @Transactional(readOnly = true)
    public List<TaskAssignment> getAssignmentsByEmployeeUsername(String username) {
        try {
            // First get employee by username
            Employee employee = employeeService.getEmployeeByUsername(username);
            if (employee == null) {
                return new ArrayList<>();
            }

            // Then get assignments by employee
            return taskAssignmentRepository.findByEmployee(employee);
        } catch (Exception e) {
            logger.error("Error getting assignments for username {}: {}", username, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get task completion progress
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTaskCompletionProgress(String taskId) {
        Map<String, Object> progress = new HashMap<>();

        try {
            List<TaskAssignment> assignments = getAssignmentsByTaskId(taskId);
            long totalAssignments = assignments.size();
            long completedAssignments = assignments.stream()
                    .filter(assignment -> "COMPLETED".equals(assignment.getStatus()))
                    .count();

            progress.put("totalAssignments", totalAssignments);
            progress.put("completedAssignments", completedAssignments);
            progress.put("completionPercentage", totalAssignments > 0 ?
                    (completedAssignments * 100) / totalAssignments : 0);
            progress.put("isFullyCompleted", completedAssignments == totalAssignments && totalAssignments > 0);

        } catch (Exception e) {
            System.out.println("Error getting task completion progress: " + e.getMessage());
            progress.put("totalAssignments", 0);
            progress.put("completedAssignments", 0);
            progress.put("completionPercentage", 0);
            progress.put("isFullyCompleted", false);
        }

        return progress;
    }

    /**
     * Update production task status based on assignment completions
     */
    @Transactional
    public void updateTaskStatusBasedOnAssignments(String taskId) {
        try {
            Optional<ProductionTask> taskOpt = productionTaskService.getTaskById(taskId);
            if (!taskOpt.isPresent()) {
                return;
            }

            ProductionTask task = taskOpt.get();
            List<TaskAssignment> assignments = getAssignmentsByTaskId(taskId);

            if (assignments.isEmpty()) {
                // No assignments, set to PENDING
                task.setStatus("PENDING");
                productionTaskService.updateTask(taskId, task);
                return;
            }

            // Count assignments by status
            long totalAssignments = assignments.size();
            long completedAssignments = assignments.stream()
                    .filter(assignment -> "COMPLETED".equals(assignment.getStatus()))
                    .count();
            long inProgressAssignments = assignments.stream()
                    .filter(assignment -> "IN_PROGRESS".equals(assignment.getStatus()))
                    .count();
            long assignedAssignments = assignments.stream()
                    .filter(assignment -> "ASSIGNED".equals(assignment.getStatus()))
                    .count();

            String newTaskStatus = determineTaskStatus(totalAssignments, completedAssignments,
                    inProgressAssignments, assignedAssignments);

            // Update task status if changed
            if (!newTaskStatus.equals(task.getStatus())) {
                task.setStatus(newTaskStatus);
                productionTaskService.updateTask(taskId, task);
                System.out.println("Updated task " + taskId + " status to: " + newTaskStatus);
            }
        } catch (Exception e) {
            System.out.println("Error updating task status based on assignments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Determine task status based on assignment progress
     */
    private String determineTaskStatus(long total, long completed, long inProgress, long assigned) {
        if (total == 0) {
            return "PENDING";
        }

        if (completed == total) {
            // All employees completed their assignments
            return "COMPLETED";
        } else if (completed > 0 || inProgress > 0) {
            // At least one employee started or completed work
            return "IN_PROGRESS";
        } else if (assigned == total) {
            // All assignments are assigned but no one started
            return "PENDING";
        }

        return "PENDING";
    }

    @Transactional
    public TaskAssignment updateAssignmentWithTaskStatus(String assignmentId, TaskAssignment assignmentData) {
        TaskAssignment updatedAssignment = updateAssignment(assignmentId, assignmentData);

        if (updatedAssignment != null && assignmentData.getStatus() != null) {
            // Update the task status based on all assignments
            String taskId = updatedAssignment.getTask().getTaskId();
            updateTaskStatusBasedOnAssignments(taskId);
        }

        return updatedAssignment;
    }

    @Transactional
    public TaskAssignment updateAssignmentStatus(String assignmentId, String newStatus, Integer actualHours) {
        Optional<TaskAssignment> optionalAssignment = taskAssignmentRepository.findById(assignmentId);
        if (!optionalAssignment.isPresent()) {
            return null;
        }

        TaskAssignment assignment = optionalAssignment.get();
        String oldStatus = assignment.getStatus();

        // Set completion date if status changed to COMPLETED
        java.time.LocalDate completionDate = assignment.getCompletionDate();
        if ("COMPLETED".equals(newStatus) && !"COMPLETED".equals(oldStatus)) {
            completionDate = java.time.LocalDate.now();
        } else if (!"COMPLETED".equals(newStatus) && "COMPLETED".equals(oldStatus)) {
            completionDate = null;
        }

        // Use the custom update method
        taskAssignmentRepository.updateAssignmentStatus(assignmentId, newStatus, completionDate, actualHours);

        // Reload the assignment to return updated entity
        Optional<TaskAssignment> updatedAssignment = taskAssignmentRepository.findById(assignmentId);

        // Update the task status based on all assignments
        if (updatedAssignment.isPresent() && updatedAssignment.get().getTask() != null) {
            updateTaskStatusBasedOnAssignments(updatedAssignment.get().getTask().getTaskId());
            return updatedAssignment.get();
        }

        return null;
    }

}