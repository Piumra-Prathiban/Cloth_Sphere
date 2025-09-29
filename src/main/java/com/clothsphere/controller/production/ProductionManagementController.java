package com.clothsphere.controller.production;

import com.clothsphere.model.SystemUser;
import com.clothsphere.model.production.ProductionPerformanceReport;
import com.clothsphere.model.production.ProductionSchedule;
import com.clothsphere.model.production.ProductionTask;
import com.clothsphere.service.production.ProductionManagementService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/production")
public class ProductionManagementController {

    private final ProductionManagementService productionService;

    @Autowired
    public ProductionManagementController(ProductionManagementService productionService) {
        this.productionService = productionService;
    }

    // ========================= DASHBOARD =========================
    @GetMapping("/dashboard")
    public String viewDashboard(@RequestParam(value = "scheduleId", required = false) Long scheduleId,
                                HttpSession session,
                                Model model) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"factory-manager".equalsIgnoreCase(currentUser.getRole())) {
            return "redirect:/systemUserLogin";
        }

        List<ProductionSchedule> schedules = productionService.getAllSchedules();
        Map<Long, Double> progressMap = productionService.calculateProgressBySchedule(schedules);
        Map<Long, Long> openTasksMap = productionService.calculateOpenTasksBySchedule(schedules);

        model.addAttribute("user", currentUser);
        model.addAttribute("schedules", schedules);
        model.addAttribute("scheduleProgress", progressMap);
        model.addAttribute("scheduleOpenTasks", openTasksMap);
        model.addAttribute("urgentTasks", productionService.getUrgentTasks());
        model.addAttribute("upcomingTasks", productionService.getUpcomingTasks());
        model.addAttribute("recentMessages", productionService.getRecentMessages());
        model.addAttribute("recentReports", productionService.getRecentReports());
        model.addAttribute("employees", productionService.getAssignableEmployees());

        if (scheduleId == null && !schedules.isEmpty()) {
            scheduleId = schedules.get(0).getId();
        }

        if (scheduleId != null) {
            model.addAttribute("selectedScheduleId", scheduleId);
            List<ProductionTask> tasks = productionService.getTasksForSchedule(scheduleId);
            model.addAttribute("selectedScheduleTasks", tasks);
            List<ProductionPerformanceReport> reports = productionService.getReportsForSchedule(scheduleId);
            model.addAttribute("selectedScheduleReports", reports);
        }

        return "productionDashboard";
    }

    // ========================= SCHEDULE =========================
    @PostMapping("/schedules")
    public String createSchedule(@RequestParam String scheduleName,
                                 @RequestParam(required = false) String productionLine,
                                 @RequestParam(required = false) Integer targetQuantity,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) String notes,
                                 @RequestParam(required = false) String startDate,
                                 @RequestParam(required = false) String endDate,
                                 RedirectAttributes redirectAttributes) {
        try {
            LocalDate start = parseDate(startDate);
            LocalDate end = parseDate(endDate);
            productionService.createSchedule(scheduleName, productionLine, targetQuantity, status, notes, start, end);
            redirectAttributes.addFlashAttribute("dashboardMessage", "Production schedule created successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("dashboardError", "Failed to create schedule: " + ex.getMessage());
        }
        return "redirect:/production/dashboard";
    }

    // ========================= TASK =========================
    @PostMapping("/tasks")
    public String assignTask(@RequestParam Long scheduleId,
                             @RequestParam String taskName,
                             @RequestParam(required = false) String employeeId,
                             @RequestParam(required = false) String taskDescription,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) String priority,
                             @RequestParam(required = false) Integer progressPercent,
                             @RequestParam(required = false) String dueDate,
                             @RequestParam(required = false, defaultValue = "false") boolean urgent,
                             @RequestParam(required = false) String issueNotes,
                             RedirectAttributes redirectAttributes) {
        try {
            LocalDate due = parseDate(dueDate);
            productionService.createTask(scheduleId, employeeId, taskName, taskDescription, status, priority,
                    progressPercent, due, urgent, issueNotes);
            redirectAttributes.addFlashAttribute("dashboardMessage", "Task assigned successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("dashboardError", "Failed to assign task: " + ex.getMessage());
        }
        return "redirect:/production/dashboard?scheduleId=" + scheduleId;
    }

    @PostMapping("/tasks/{taskId}/progress")
    public String updateTaskProgress(@PathVariable Long taskId,
                                     @RequestParam Long scheduleId,
                                     @RequestParam(required = false) Integer progressPercent,
                                     @RequestParam(required = false) String status,
                                     @RequestParam(required = false) Boolean urgent,
                                     @RequestParam(required = false) String issueNotes,
                                     RedirectAttributes redirectAttributes) {
        try {
            productionService.updateTask(taskId, progressPercent, status, urgent, issueNotes);
            redirectAttributes.addFlashAttribute("dashboardMessage", "Task updated successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("dashboardError", "Failed to update task: " + ex.getMessage());
        }
        return "redirect:/production/dashboard?scheduleId=" + scheduleId;
    }

    // ========================= MESSAGE =========================
    @PostMapping("/messages")
    public String postMessage(@RequestParam(required = false) Long scheduleId,
                              @RequestParam String subject,
                              @RequestParam("messageBody") String messageBody,
                              @RequestParam(required = false) String severity,
                              @RequestParam(required = false) String createdBy,
                              RedirectAttributes redirectAttributes) {
        try {
            productionService.createMessage(scheduleId, subject, messageBody, severity, createdBy);
            redirectAttributes.addFlashAttribute("dashboardMessage", "Message shared with production teams.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("dashboardError", "Failed to send message: " + ex.getMessage());
        }
        return "redirect:/production/dashboard";
    }

    // ========================= REPORT =========================
    @PostMapping("/reports/generate")
    public String generateReport(@RequestParam Long scheduleId,
                                 @RequestParam(required = false) String periodStart,
                                 @RequestParam(required = false) String periodEnd,
                                 RedirectAttributes redirectAttributes) {
        try {
            LocalDate start = parseDate(periodStart);
            LocalDate end = parseDate(periodEnd);
            ProductionPerformanceReport report = productionService.generateProductivityReport(scheduleId, start, end);
            redirectAttributes.addFlashAttribute("dashboardMessage",
                    "Productivity report generated (Score: " + report.getProductivityScore() + "%).");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("dashboardError", "Failed to generate report: " + ex.getMessage());
        }
        return "redirect:/production/dashboard?scheduleId=" + scheduleId;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format: " + value);
        }
    }
}
