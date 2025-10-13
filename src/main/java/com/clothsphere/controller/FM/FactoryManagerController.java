package com.clothsphere.controller.FM;

import com.clothsphere.model.FM.ProductionTask;
import com.clothsphere.model.FM.TaskAssignment;
import com.clothsphere.model.HR.*;
import com.clothsphere.model.SystemUser;
import com.clothsphere.repository.FM.ProductionTaskRepository;
import com.clothsphere.repository.FM.TaskAssignmentRepository;
import com.clothsphere.repository.HR.*;
import com.clothsphere.service.FM.TaskAssignmentService;
import com.clothsphere.service.FM.productionTaskService;
import com.clothsphere.service.HR.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/factory")
public class FactoryManagerController {

    @Autowired
    private productionTaskService productionTaskService;

    @Autowired
    private TaskAssignmentService taskAssignmentService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ProductionTaskRepository productionTaskRepository;

    @Autowired
    private TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    // ==================== DASHBOARD ====================

    /**
     * Factory Manager Dashboard - Main View
     */
    @GetMapping("/dashboard")
    public String showFactoryDashboard(HttpSession session, Model model) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/systemUserLogin";
        }

        try {
            // Get dashboard statistics
            Map<String, Object> taskStats = productionTaskService.getTaskStatistics();
            Map<String, Object> assignmentStats = taskAssignmentService.getAssignmentStatistics();

            // Get active tasks
            List<ProductionTask> activeTasks = productionTaskService.getTasksByStatus("IN_PROGRESS");
            List<ProductionTask> pendingTasks = productionTaskService.getTasksByStatus("PENDING");
            List<ProductionTask> overdueTasks = productionTaskService.getOverdueTasks();

            // Get recent assignments
            List<TaskAssignment> recentAssignments = taskAssignmentService.getAllAssignments()
                    .stream()
                    .limit(10)
                    .collect(Collectors.toList());

            // Get all departments
            List<Department> departments = departmentService.getAllDepartments();

            // Get employee count
            long totalEmployees = employeeRepository.count();

            // Calculate productivity metrics
            long totalTasks = (Long) taskStats.get("totalTasks");
            long completedTasks = (Long) taskStats.get("completedTasks");
            double completionRate = totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0;

            // Add data to model
            model.addAttribute("user", currentUser);
            model.addAttribute("taskStats", taskStats);
            model.addAttribute("assignmentStats", assignmentStats);
            model.addAttribute("activeTasks", activeTasks);
            model.addAttribute("pendingTasks", pendingTasks);
            model.addAttribute("overdueTasks", overdueTasks);
            model.addAttribute("recentAssignments", recentAssignments);
            model.addAttribute("departments", departments);
            model.addAttribute("totalEmployees", totalEmployees);
            model.addAttribute("completionRate", String.format("%.1f", completionRate));

            return "factoryManagerDashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
            return "error";
        }
    }

    // ==================== PRODUCTION TASK MANAGEMENT ====================

    /**
     * Get all production tasks
     */
    @GetMapping("/api/tasks")
    @ResponseBody
    public ResponseEntity<List<ProductionTask>> getAllTasks() {
        try {
            List<ProductionTask> tasks = productionTaskService.getAllTasks();
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get task by ID
     */
    @GetMapping("/api/tasks/{taskId}")
    @ResponseBody
    public ResponseEntity<ProductionTask> getTaskById(@PathVariable String taskId) {
        try {
            Optional<ProductionTask> task = productionTaskService.getTaskById(taskId);
            return task.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create new production task
     */
    @PostMapping("/api/tasks")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createTask(@RequestBody Map<String, Object> taskData) {
        Map<String, Object> response = new HashMap<>();
        try {
            ProductionTask task = new ProductionTask();
            task.setTaskName((String) taskData.get("taskName"));
            task.setDescription((String) taskData.get("description"));
            task.setDeadline(LocalDate.parse((String) taskData.get("deadline")));
            task.setPriority((String) taskData.get("priority"));
            task.setStatus("PENDING");

            // Get department
            String departmentId = (String) taskData.get("departmentId");
            Optional<Department> departmentOpt = departmentService.getDepartmentById(departmentId);
            if (!departmentOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Department not found");
                return ResponseEntity.badRequest().body(response);
            }
            task.setDepartment(departmentOpt.get());

            ProductionTask savedTask = productionTaskService.createTask(task);
            response.put("success", true);
            response.put("message", "Task created successfully");
            response.put("task", savedTask);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating task: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update production task
     */
    @PutMapping("/api/tasks/{taskId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateTask(@PathVariable String taskId,
                                                          @RequestBody Map<String, Object> taskData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<ProductionTask> existingTask = productionTaskService.getTaskById(taskId);
            if (!existingTask.isPresent()) {
                response.put("success", false);
                response.put("message", "Task not found");
                return ResponseEntity.notFound().build();
            }

            ProductionTask task = existingTask.get();
            if (taskData.containsKey("taskName")) task.setTaskName((String) taskData.get("taskName"));
            if (taskData.containsKey("description")) task.setDescription((String) taskData.get("description"));
            if (taskData.containsKey("deadline")) task.setDeadline(LocalDate.parse((String) taskData.get("deadline")));
            if (taskData.containsKey("priority")) task.setPriority((String) taskData.get("priority"));
            if (taskData.containsKey("status")) task.setStatus((String) taskData.get("status"));

            ProductionTask updatedTask = productionTaskService.updateTask(taskId, task);
            response.put("success", true);
            response.put("message", "Task updated successfully");
            response.put("task", updatedTask);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating task: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete production task
     */
    @DeleteMapping("/api/tasks/{taskId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteTask(@PathVariable String taskId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean deleted = productionTaskService.deleteTask(taskId);
            if (deleted) {
                response.put("success", true);
                response.put("message", "Task deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Task not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting task: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get tasks by status
     */
    @GetMapping("/api/tasks/status/{status}")
    @ResponseBody
    public ResponseEntity<List<ProductionTask>> getTasksByStatus(@PathVariable String status) {
        try {
            List<ProductionTask> tasks = productionTaskService.getTasksByStatus(status);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get overdue tasks
     */
    @GetMapping("/api/tasks/overdue")
    @ResponseBody
    public ResponseEntity<List<ProductionTask>> getOverdueTasks() {
        try {
            List<ProductionTask> tasks = productionTaskService.getOverdueTasks();
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== STAFF ASSIGNMENT MANAGEMENT ====================

    /**
     * Get all assignments
     */
    @GetMapping("/api/assignments")
    @ResponseBody
    public ResponseEntity<List<TaskAssignment>> getAllAssignments() {
        try {
            List<TaskAssignment> assignments = taskAssignmentService.getAllAssignments();
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Assign task to employees
     */
    @PostMapping("/api/assignments/assign")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> assignTaskToEmployees(@RequestBody Map<String, Object> assignmentData) {
        try {
            String taskId = (String) assignmentData.get("taskId");
            @SuppressWarnings("unchecked")
            List<String> employeeIds = (List<String>) assignmentData.get("employeeIds");
            Integer estimatedHours = (Integer) assignmentData.get("estimatedHours");
            String notes = (String) assignmentData.get("notes");

            Map<String, Object> result = taskAssignmentService.assignTaskToEmployees(
                    taskId, employeeIds, estimatedHours, notes);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error assigning task: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Update assignment status
     */
    @PutMapping("/api/assignments/{assignmentId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateAssignmentStatus(
            @PathVariable String assignmentId,
            @RequestBody Map<String, Object> statusData) {
        Map<String, Object> response = new HashMap<>();
        try {
            String newStatus = (String) statusData.get("status");
            Integer actualHours = statusData.containsKey("actualHours") ?
                    (Integer) statusData.get("actualHours") : null;

            TaskAssignment updated = taskAssignmentService.updateAssignmentStatus(
                    assignmentId, newStatus, actualHours);

            if (updated != null) {
                response.put("success", true);
                response.put("message", "Assignment status updated successfully");
                response.put("assignment", updated);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Assignment not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating assignment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get assignments by employee
     */
    @GetMapping("/api/assignments/employee/{employeeId}")
    @ResponseBody
    public ResponseEntity<List<TaskAssignment>> getAssignmentsByEmployee(@PathVariable String employeeId) {
        try {
            List<TaskAssignment> assignments = taskAssignmentService.getAssignmentsByEmployeeId(employeeId);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get assignments by task
     */
    @GetMapping("/api/assignments/task/{taskId}")
    @ResponseBody
    public ResponseEntity<List<TaskAssignment>> getAssignmentsByTask(@PathVariable String taskId) {
        try {
            List<TaskAssignment> assignments = taskAssignmentService.getAssignmentsByTaskId(taskId);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== EMPLOYEE MANAGEMENT ====================

    /**
     * Get all employees
     */
    @GetMapping("/api/employees")
    @ResponseBody
    public ResponseEntity<List<Employee>> getAllEmployees() {
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get employees by department
     */
    @GetMapping("/api/employees/department/{departmentId}")
    @ResponseBody
    public ResponseEntity<List<Employee>> getEmployeesByDepartment(@PathVariable String departmentId) {
        try {
            Optional<Department> departmentOpt = departmentService.getDepartmentById(departmentId);
            if (!departmentOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            List<Employee> employees = employeeRepository.findByDepartment(departmentOpt.get());
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== DEPARTMENT MANAGEMENT ====================

    /**
     * Get all departments
     */
    @GetMapping("/api/departments")
    @ResponseBody
    public ResponseEntity<List<Department>> getAllDepartments() {
        try {
            List<Department> departments = departmentService.getAllDepartments();
            return ResponseEntity.ok(departments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== PERFORMANCE REPORTS ====================

    /**
     * Get task statistics
     */
    @GetMapping("/api/reports/task-stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTaskStatistics() {
        try {
            Map<String, Object> stats = productionTaskService.getTaskStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get assignment statistics
     */
    @GetMapping("/api/reports/assignment-stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAssignmentStatistics() {
        try {
            Map<String, Object> stats = taskAssignmentService.getAssignmentStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get employee workload report
     */
    @GetMapping("/api/reports/workload")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getEmployeeWorkload() {
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            List<Map<String, Object>> workloadData = new ArrayList<>();

            for (Employee employee : employees) {
                Map<String, Object> data = new HashMap<>();
                data.put("employeeId", employee.getId());
                data.put("employeeName", employee.getFullName());
                data.put("department", employee.getDepartment() != null ?
                        employee.getDepartment().getDepartmentName() : "N/A");

                List<TaskAssignment> activeAssignments =
                        taskAssignmentService.getActiveAssignmentsByEmployeeId(employee.getId());
                data.put("activeTasksCount", activeAssignments.size());

                int totalEstimatedHours = activeAssignments.stream()
                        .mapToInt(a -> a.getEstimatedHours() != null ? a.getEstimatedHours() : 0)
                        .sum();
                data.put("totalEstimatedHours", totalEstimatedHours);

                workloadData.add(data);
            }

            return ResponseEntity.ok(workloadData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get department performance report
     */
    @GetMapping("/api/reports/department-performance")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getDepartmentPerformance() {
        try {
            List<Department> departments = departmentService.getAllDepartments();
            List<Map<String, Object>> performanceData = new ArrayList<>();

            for (Department department : departments) {
                Map<String, Object> data = new HashMap<>();
                data.put("departmentId", department.getId());
                data.put("departmentName", department.getDepartmentName());

                List<ProductionTask> tasks = productionTaskService.getTasksByDepartmentId(department.getId());
                data.put("totalTasks", tasks.size());

                long completedTasks = tasks.stream()
                        .filter(t -> "COMPLETED".equals(t.getStatus()))
                        .count();
                data.put("completedTasks", completedTasks);

                double completionRate = tasks.size() > 0 ?
                        (completedTasks * 100.0 / tasks.size()) : 0;
                data.put("completionRate", String.format("%.1f", completionRate));

                long employeeCount = employeeRepository.countByDepartment(department);
                data.put("employeeCount", employeeCount);

                performanceData.add(data);
            }

            return ResponseEntity.ok(performanceData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Add this method to FactoryManagerController.java

    /**
     * Get task progress statistics
     */
    @GetMapping("/api/reports/task-progress")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTaskProgressStats() {
        try {
            Map<String, Object> taskStats = productionTaskService.getTaskStatistics();

            long totalTasks = (Long) taskStats.get("totalTasks");
            long completedTasks = (Long) taskStats.get("completedTasks");
            long inProgressTasks = (Long) taskStats.get("inProgressTasks");
            long pendingTasks = (Long) taskStats.get("pendingTasks");

            // Calculate percentages
            double completedPercentage = totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0;
            double inProgressPercentage = totalTasks > 0 ? (inProgressTasks * 100.0 / totalTasks) : 0;
            double pendingPercentage = totalTasks > 0 ? (pendingTasks * 100.0 / totalTasks) : 0;

            Map<String, Object> progressStats = new HashMap<>();
            progressStats.put("completedPercentage", Math.round(completedPercentage));
            progressStats.put("inProgressPercentage", Math.round(inProgressPercentage));
            progressStats.put("pendingPercentage", Math.round(pendingPercentage));
            progressStats.put("completedTasks", completedTasks);
            progressStats.put("inProgressTasks", inProgressTasks);
            progressStats.put("pendingTasks", pendingTasks);
            progressStats.put("totalTasks", totalTasks);

            return ResponseEntity.ok(progressStats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
