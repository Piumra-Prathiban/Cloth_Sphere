package com.clothsphere.service.HR;

import com.clothsphere.model.HR.Department;
import com.clothsphere.model.HR.ProductionTask;
import com.clothsphere.repository.HR.ProductionTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class productionTaskService {

    @Autowired
    private ProductionTaskRepository productionTaskRepository;

    /**
     * Generate the next task ID in format tk01, tk02, etc.
     */
    public String generateNextTaskId() {
        List<String> existingIds = productionTaskRepository.findAllTaskIds();

        if (existingIds.isEmpty()) {
            return "tk01";
        }

        int maxNumber = 0;
        for (String id : existingIds) {
            if (id != null && id.startsWith("tk") && id.length() == 4) {
                try {
                    String numberPart = id.substring(2); // Get part after "tk"
                    int number = Integer.parseInt(numberPart);
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid format IDs
                    continue;
                }
            }
        }

        // Generate next ID
        int nextNumber = maxNumber + 1;
        return String.format("tk%02d", nextNumber);
    }

    /**
     * Get all production tasks ordered by priority and deadline
     */
    @Transactional(readOnly = true)
    public List<ProductionTask> getAllTasks() {
        return productionTaskRepository.findAllOrderByCreatedDateDesc();
    }

    /**
     * Create a new production task with auto-generated ID
     */
    @Transactional
    public ProductionTask createTask(ProductionTask task) {
        task.setTaskId(generateNextTaskId());
        task.setCreatedDate(LocalDate.now());
        if (task.getStatus() == null || task.getStatus().isEmpty()) {
            task.setStatus("PENDING");
        }
        productionTaskRepository.insertTask(task);
        return task;
    }

    /**
     * Get production task by ID
     */
    @Transactional(readOnly = true)
    public Optional<ProductionTask> getTaskById(String taskId) {
        return productionTaskRepository.findById(taskId);
    }

    /**
     * Update existing production task
     */
    @Transactional
    public ProductionTask updateTask(String taskId, ProductionTask taskData) {
        Optional<ProductionTask> optionalTask = productionTaskRepository.findById(taskId);
        if (optionalTask.isPresent()) {
            ProductionTask existingTask = optionalTask.get();

            // Update only non-null fields
            if (taskData.getTaskName() != null) {
                existingTask.setTaskName(taskData.getTaskName());
            }
            if (taskData.getDescription() != null) {
                existingTask.setDescription(taskData.getDescription());
            }
            if (taskData.getDeadline() != null) {
                existingTask.setDeadline(taskData.getDeadline());
            }
            if (taskData.getPriority() != null) {
                existingTask.setPriority(taskData.getPriority());
            }
            if (taskData.getDepartment() != null) {
                existingTask.setDepartment(taskData.getDepartment());
            }
            if (taskData.getStatus() != null) {
                existingTask.setStatus(taskData.getStatus());
            }

            productionTaskRepository.updateTask(existingTask);
            return existingTask;
        }
        return null;
    }

    /**
     * Delete production task by ID
     */
    @Transactional
    public boolean deleteTask(String taskId) {
        try {
            if (productionTaskRepository.existsById(taskId)) {
                productionTaskRepository.deleteByTaskId(taskId);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("Error deleting task: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get tasks by department
     */
    @Transactional(readOnly = true)
    public List<ProductionTask> getTasksByDepartment(Department department) {
        return productionTaskRepository.findByDepartmentOrderByPriorityAndDeadline(department);
    }

    /**
     * Get tasks by department ID
     */
    @Transactional(readOnly = true)
    public List<ProductionTask> getTasksByDepartmentId(String departmentId) {
        return productionTaskRepository.findByDepartmentIdOrderByPriorityAndDeadline(departmentId);
    }

    /**
     * Get tasks by priority
     */
    @Transactional(readOnly = true)
    public List<ProductionTask> getTasksByPriority(String priority) {
        return productionTaskRepository.findByPriorityOrderByDeadline(priority);
    }

    /**
     * Get tasks by status
     */
    @Transactional(readOnly = true)
    public List<ProductionTask> getTasksByStatus(String status) {
        return productionTaskRepository.findByStatusOrderByPriorityAndDeadline(status);
    }

    /**
     * Get overdue tasks
     */
    @Transactional(readOnly = true)
    public List<ProductionTask> getOverdueTasks() {
        return productionTaskRepository.findOverdueTasks(LocalDate.now());
    }

    /**
     * Get task statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTaskStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Count by status
        stats.put("totalTasks", productionTaskRepository.countAllTasks());
        stats.put("pendingTasks", productionTaskRepository.countByStatus("PENDING"));
        stats.put("inProgressTasks", productionTaskRepository.countByStatus("IN_PROGRESS"));
        stats.put("completedTasks", productionTaskRepository.countByStatus("COMPLETED"));
        stats.put("cancelledTasks", productionTaskRepository.countByStatus("CANCELLED"));

        // Count by priority
        stats.put("criticalTasks", productionTaskRepository.countByPriority("P0"));
        stats.put("highPriorityTasks", productionTaskRepository.countByPriority("P1"));
        stats.put("mediumPriorityTasks", productionTaskRepository.countByPriority("P2"));
        stats.put("lowPriorityTasks", productionTaskRepository.countByPriority("P3"));

        // Overdue tasks
        List<ProductionTask> overdueTasks = getOverdueTasks();
        stats.put("overdueTasks", (long) overdueTasks.size());

        return stats;
    }

    /**
     * Get department task statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDepartmentTaskStatistics(Department department) {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalTasks", productionTaskRepository.countByDepartment(department));
        stats.put("pendingTasks", productionTaskRepository.countByDepartmentAndStatus(department, "PENDING"));
        stats.put("inProgressTasks", productionTaskRepository.countByDepartmentAndStatus(department, "IN_PROGRESS"));
        stats.put("completedTasks", productionTaskRepository.countByDepartmentAndStatus(department, "COMPLETED"));
        stats.put("cancelledTasks", productionTaskRepository.countByDepartmentAndStatus(department, "CANCELLED"));

        return stats;
    }

    /**
     * Search tasks by keyword
     */
    @Transactional(readOnly = true)
    public List<ProductionTask> searchTasks(String keyword) {
        return productionTaskRepository.findByTaskNameOrDescriptionContainingIgnoreCase(keyword);
    }

    /**
     * Update task status
     */
    @Transactional
    public boolean updateTaskStatus(String taskId, String status) {
        try {
            Optional<ProductionTask> optionalTask = productionTaskRepository.findById(taskId);
            if (optionalTask.isPresent()) {
                ProductionTask task = optionalTask.get();
                task.setStatus(status);
                productionTaskRepository.insertTask(task);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("Error updating task status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get tasks with deadline in the next N days
     */
    @Transactional(readOnly = true)
    public List<ProductionTask> getTasksDueSoon(int days) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(days);
        return productionTaskRepository.findTasksByDeadlineRange(today, futureDate);
    }

    /**
     * Validate priority value
     */
    public boolean isValidPriority(String priority) {
        return priority != null && (priority.equals("P0") || priority.equals("P1") ||
                priority.equals("P2") || priority.equals("P3"));
    }

    /**
     * Validate status value
     */
    public boolean isValidStatus(String status) {
        return status != null && (status.equals("PENDING") || status.equals("IN_PROGRESS") ||
                status.equals("COMPLETED") || status.equals("CANCELLED"));
    }
}