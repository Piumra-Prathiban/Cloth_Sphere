package com.clothsphere.controller.FM;

import com.clothsphere.model.HR.Department;
import com.clothsphere.model.FM.ProductionTask;
import com.clothsphere.model.SystemUser;
import com.clothsphere.service.HR.DepartmentService;
import com.clothsphere.service.FM.productionTaskService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/api/workload")
public class WorkloadController {

    @Autowired
    private productionTaskService productionTaskService;

    @Autowired
    private DepartmentService departmentService;

    /**
     * Get all production tasks
     */
    @GetMapping("/tasks")
    @ResponseBody
    public ResponseEntity<List<ProductionTask>> getAllTasks(HttpSession session) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            List<ProductionTask> tasks = productionTaskService.getAllTasks();
            System.out.println("Retrieved " + tasks.size() + " production tasks");
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error fetching production tasks: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get single production task by ID
     */
    @GetMapping("/tasks/{taskId}")
    @ResponseBody
    public ResponseEntity<ProductionTask> getTask(@PathVariable String taskId, HttpSession session) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Optional<ProductionTask> task = productionTaskService.getTaskById(taskId);
            if (task.isPresent()) {
                return new ResponseEntity<>(task.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.out.println("Error fetching task: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create new production task
     */
    @PostMapping("/tasks")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createTask(
            @RequestBody Map<String, String> taskData,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate required fields
            String taskName = taskData.get("taskName");
            String deadlineStr = taskData.get("deadline");
            String priority = taskData.get("priority");
            String departmentId = taskData.get("departmentId");

            if (taskName == null || taskName.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Task name is required");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            if (deadlineStr == null || deadlineStr.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Deadline is required");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            if (priority == null || !productionTaskService.isValidPriority(priority)) {
                response.put("success", false);
                response.put("message", "Valid priority is required (P0, P1, P2, P3)");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            if (departmentId == null || departmentId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Department is required");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Parse deadline
            LocalDate deadline;
            try {
                deadline = LocalDate.parse(deadlineStr, DateTimeFormatter.ISO_DATE);
            } catch (Exception e) {
                response.put("success", false);
                response.put("message", "Invalid deadline format. Use YYYY-MM-DD");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Validate deadline is not in the past
            if (deadline.isBefore(LocalDate.now())) {
                response.put("success", false);
                response.put("message", "Deadline cannot be in the past");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Get department
            Optional<Department> departmentOpt = departmentService.getDepartmentById(departmentId);
            if (!departmentOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Department not found");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Create production task object
            ProductionTask task = new ProductionTask();
            task.setTaskName(taskName.trim());
            task.setDescription(taskData.get("description"));
            task.setDeadline(deadline);
            task.setPriority(priority);
            task.setDepartment(departmentOpt.get());

            // Save task
            ProductionTask savedTask = productionTaskService.createTask(task);

            response.put("success", true);
            response.put("message", "Production task created successfully!");
            response.put("taskId", savedTask.getTaskId());
            response.put("task", savedTask);

            System.out.println("Created new production task: " + savedTask.getTaskId() + " - " + savedTask.getTaskName());
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating task: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update existing production task
     */
    @PutMapping("/tasks/{taskId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateTask(
            @PathVariable String taskId,
            @RequestBody Map<String, String> taskData,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<ProductionTask> existingTaskOpt = productionTaskService.getTaskById(taskId);
            if (!existingTaskOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Task not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // Validate and parse data
            String taskName = taskData.get("taskName");
            String deadlineStr = taskData.get("deadline");
            String priority = taskData.get("priority");
            String departmentId = taskData.get("departmentId");
            String status = taskData.get("status");

            if (taskName == null || taskName.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Task name is required");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            if (priority != null && !productionTaskService.isValidPriority(priority)) {
                response.put("success", false);
                response.put("message", "Valid priority is required (P0, P1, P2, P3)");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            if (status != null && !productionTaskService.isValidStatus(status)) {
                response.put("success", false);
                response.put("message", "Valid status is required (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            LocalDate deadline = null;
            if (deadlineStr != null && !deadlineStr.trim().isEmpty()) {
                try {
                    deadline = LocalDate.parse(deadlineStr, DateTimeFormatter.ISO_DATE);
                } catch (Exception e) {
                    response.put("success", false);
                    response.put("message", "Invalid deadline format. Use YYYY-MM-DD");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }

            // Get department if provided
            Department department = null;
            if (departmentId != null && !departmentId.trim().isEmpty()) {
                Optional<Department> departmentOpt = departmentService.getDepartmentById(departmentId);
                if (!departmentOpt.isPresent()) {
                    response.put("success", false);
                    response.put("message", "Department not found");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
                department = departmentOpt.get();
            }

            // Create updated task data
            ProductionTask updatedTaskData = new ProductionTask();
            updatedTaskData.setTaskName(taskName.trim());
            updatedTaskData.setDescription(taskData.get("description"));
            if (deadline != null) updatedTaskData.setDeadline(deadline);
            if (priority != null) updatedTaskData.setPriority(priority);
            if (department != null) updatedTaskData.setDepartment(department);
            if (status != null) updatedTaskData.setStatus(status);

            // Update task
            ProductionTask updatedTask = productionTaskService.updateTask(taskId, updatedTaskData);

            if (updatedTask != null) {
                response.put("success", true);
                response.put("message", "Task updated successfully!");
                response.put("task", updatedTask);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "Failed to update task");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating task: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete production task
     */
    @DeleteMapping("/tasks/{taskId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteTask(
            @PathVariable String taskId,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> response = new HashMap<>();

        try {
            boolean success = productionTaskService.deleteTask(taskId);

            if (success) {
                response.put("success", true);
                response.put("message", "Task deleted successfully!");
                System.out.println("Deleted production task: " + taskId);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "Task not found or failed to delete");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting task: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update task status only
     */
    @PatchMapping("/tasks/{taskId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateTaskStatus(
            @PathVariable String taskId,
            @RequestBody Map<String, String> statusData,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> response = new HashMap<>();

        try {
            String status = statusData.get("status");
            if (status == null || !productionTaskService.isValidStatus(status)) {
                response.put("success", false);
                response.put("message", "Valid status is required (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            boolean success = productionTaskService.updateTaskStatus(taskId, status);

            if (success) {
                response.put("success", true);
                response.put("message", "Task status updated successfully!");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "Task not found or failed to update status");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating task status: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get tasks by department
     */
    @GetMapping("/tasks/department/{departmentId}")
    @ResponseBody
    public ResponseEntity<List<ProductionTask>> getTasksByDepartment(
            @PathVariable String departmentId,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            List<ProductionTask> tasks = productionTaskService.getTasksByDepartmentId(departmentId);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error fetching tasks by department: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get tasks by status
     */
    @GetMapping("/tasks/status/{status}")
    @ResponseBody
    public ResponseEntity<List<ProductionTask>> getTasksByStatus(
            @PathVariable String status,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            List<ProductionTask> tasks = productionTaskService.getTasksByStatus(status);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error fetching tasks by status: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get tasks by priority
     */
    @GetMapping("/tasks/priority/{priority}")
    @ResponseBody
    public ResponseEntity<List<ProductionTask>> getTasksByPriority(
            @PathVariable String priority,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            List<ProductionTask> tasks = productionTaskService.getTasksByPriority(priority);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error fetching tasks by priority: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get overdue tasks
     */
    @GetMapping("/tasks/overdue")
    @ResponseBody
    public ResponseEntity<List<ProductionTask>> getOverdueTasks(HttpSession session) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            List<ProductionTask> tasks = productionTaskService.getOverdueTasks();
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error fetching overdue tasks: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get task statistics
     */
    @GetMapping("/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTaskStatistics(HttpSession session) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Map<String, Object> stats = productionTaskService.getTaskStatistics();
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error fetching task statistics: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Search tasks
     */
    @GetMapping("/tasks/search")
    @ResponseBody
    public ResponseEntity<List<ProductionTask>> searchTasks(
            @RequestParam String keyword,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            List<ProductionTask> tasks = productionTaskService.searchTasks(keyword);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error searching tasks: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    

}