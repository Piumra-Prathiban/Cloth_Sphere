package com.clothsphere.service.production;

import com.clothsphere.model.HR.Employee;
import com.clothsphere.model.production.ProductionMessage;
import com.clothsphere.model.production.ProductionPerformanceReport;
import com.clothsphere.model.production.ProductionSchedule;
import com.clothsphere.model.production.ProductionTask;
import com.clothsphere.service.HR.EmployeeService;
import com.clothsphere.repository.production.ProductionMessageRepository;
import com.clothsphere.repository.production.ProductionPerformanceReportRepository;
import com.clothsphere.repository.production.ProductionScheduleRepository;
import com.clothsphere.repository.production.ProductionTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class ProductionManagementService {

    private final ProductionScheduleRepository scheduleRepository;
    private final ProductionTaskRepository taskRepository;
    private final ProductionMessageRepository messageRepository;
    private final ProductionPerformanceReportRepository reportRepository;
    private final EmployeeService employeeService;

    @Autowired
    public ProductionManagementService(ProductionScheduleRepository scheduleRepository,
                                       ProductionTaskRepository taskRepository,
                                       ProductionMessageRepository messageRepository,
                                       ProductionPerformanceReportRepository reportRepository,
                                       EmployeeService employeeService) {
        this.scheduleRepository = scheduleRepository;
        this.taskRepository = taskRepository;
        this.messageRepository = messageRepository;
        this.reportRepository = reportRepository;
        this.employeeService = employeeService;
    }

    // ========================= SCHEDULES =========================
    public List<ProductionSchedule> getAllSchedules() {
        List<ProductionSchedule> schedules = scheduleRepository.findAllOrdered();
        schedules.forEach(schedule -> schedule.getTasks().size());
        return schedules;
    }

    public Optional<ProductionSchedule> getSchedule(Long scheduleId) {
        return scheduleRepository.findById(scheduleId);
    }

    public ProductionSchedule createSchedule(String scheduleName, String productionLine, Integer targetQuantity,
                                             String status, String notes, LocalDate startDate, LocalDate endDate) {
        ProductionSchedule schedule = new ProductionSchedule(scheduleName, productionLine, targetQuantity,
                status != null ? status : "PLANNED", notes, startDate, endDate);
        return scheduleRepository.save(schedule);
    }

    public ProductionSchedule updateScheduleStatus(Long scheduleId, String status) {
        ProductionSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
        schedule.setStatus(status);
        return scheduleRepository.save(schedule);
    }

    // ========================= TASKS =========================
    public ProductionTask createTask(Long scheduleId, String employeeId, String taskName, String taskDescription,
                                     String status, String priority, Integer progressPercent, LocalDate dueDate,
                                     boolean urgent, String issueNotes) {
        ProductionSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
        Employee employee = employeeId != null ? employeeService.getEmployeeById(employeeId) : null;
        int initialProgress = progressPercent != null ? Math.max(0, Math.min(progressPercent, 100)) : 0;
        ProductionTask task = new ProductionTask(schedule, employee, taskName, taskDescription,
                status != null ? status : "PLANNED", priority, initialProgress,
                dueDate, urgent, issueNotes);
        return taskRepository.save(task);
    }

    public ProductionTask updateTask(Long taskId, Integer progressPercent, String status, Boolean urgent,
                                     String issueNotes) {
        ProductionTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        if (progressPercent != null) {
            task.setProgressPercent(Math.max(0, Math.min(progressPercent, 100)));
        }
        if (status != null && !status.isBlank()) {
            task.setStatus(status);
        }
        if (urgent != null) {
            task.setUrgent(urgent);
        }
        if (issueNotes != null) {
            task.setIssueNotes(issueNotes);
        }
        task.setLastUpdated(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public List<ProductionTask> getTasksForSchedule(Long scheduleId) {
        return taskRepository.findByScheduleId(scheduleId);
    }

    public List<ProductionTask> getUrgentTasks() {
        return taskRepository.findByUrgentTrueOrderByDueDateAsc();
    }

    public List<ProductionTask> getUpcomingTasks() {
        return taskRepository.findTop10ByOrderByDueDateAsc();
    }

    // ========================= MESSAGES =========================
    public ProductionMessage createMessage(Long scheduleId, String subject, String body, String severity, String createdBy) {
        ProductionSchedule schedule = null;
        if (scheduleId != null) {
            schedule = scheduleRepository.findById(scheduleId).orElse(null);
        }
        ProductionMessage message = new ProductionMessage(schedule, subject, body,
                severity != null ? severity : "INFO", createdBy);
        return messageRepository.save(message);
    }

    public List<ProductionMessage> getRecentMessages() {
        return messageRepository.findTop6ByOrderByCreatedAtDesc();
    }

    // ========================= REPORTS =========================
    public ProductionPerformanceReport generateProductivityReport(Long scheduleId, LocalDate periodStart, LocalDate periodEnd) {
        ProductionSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));

        LocalDate start = periodStart != null ? periodStart : schedule.getStartDate();
        LocalDate end = periodEnd != null ? periodEnd : LocalDate.now();
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("Invalid period");
        }

        List<ProductionTask> tasks = (start != null && end != null)
                ? taskRepository.findByScheduleIdAndDueDateBetween(scheduleId, start, end)
                : taskRepository.findByScheduleId(scheduleId);

        int totalTasks = tasks.size();
        int completed = (int) tasks.stream().filter(ProductionTask::isCompleted).count();
        int overdue = (int) tasks.stream().filter(ProductionTask::isOverdue).count();
        int pending = totalTasks - completed;

        double productivity = totalTasks == 0 ? 0.0 : (completed * 100.0) / totalTasks;
        // penalize overdue items to highlight urgency
        productivity = Math.max(0.0, productivity - overdue * 5.0);

        ProductionPerformanceReport report = new ProductionPerformanceReport(schedule, start, end, completed, pending, overdue,
                Math.round(productivity * 10.0) / 10.0);
        return reportRepository.save(report);
    }

    public List<ProductionPerformanceReport> getRecentReports() {
        return reportRepository.findTop6ByOrderByGeneratedAtDesc();
    }

    public List<ProductionPerformanceReport> getReportsForSchedule(Long scheduleId) {
        return reportRepository.findByScheduleIdOrderByGeneratedAtDesc(scheduleId);
    }

    // ========================= DASHBOARD HELPERS =========================
    public Map<Long, Double> calculateProgressBySchedule(Collection<ProductionSchedule> schedules) {
        Map<Long, Double> progressMap = new HashMap<>();
        for (ProductionSchedule schedule : schedules) {
            progressMap.put(schedule.getId(), Math.round(schedule.getAverageProgress() * 10.0) / 10.0);
        }
        return progressMap;
    }

    public Map<Long, Long> calculateOpenTasksBySchedule(Collection<ProductionSchedule> schedules) {
        Map<Long, Long> openMap = new HashMap<>();
        for (ProductionSchedule schedule : schedules) {
            long open = schedule.getTasks().stream().filter(task -> !task.isCompleted()).count();
            openMap.put(schedule.getId(), open);
        }
        return openMap;
    }

    public List<Employee> getAssignableEmployees() {
        return employeeService.getAllEmployeesSortedByName();
    }
}
