// MonthlyAttendanceSummaryController.java
package com.clothsphere.controller.HR;

import com.clothsphere.service.HR.MonthlyAttendanceSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/attendance-summary")
public class MonthlyAttendanceSummaryController {

    @Autowired
    private MonthlyAttendanceSummaryService summaryService;

    /**
     * Calculate monthly attendance summary for all employees
     * This endpoint is called BEFORE generating payroll
     */
    @PostMapping("/calculate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> calculateMonthlySummary(
            @RequestParam int year,
            @RequestParam int month) {

        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("=== CALCULATE ATTENDANCE SUMMARY ENDPOINT ===");
            System.out.println("Received request - Year: " + year + ", Month: " + month);

            // Validate parameters
            if (year < 2000 || year > 2100) {
                response.put("success", false);
                response.put("message", "Invalid year: " + year);
                return ResponseEntity.badRequest().body(response);
            }

            if (month < 1 || month > 12) {
                response.put("success", false);
                response.put("message", "Invalid month: " + month);
                return ResponseEntity.badRequest().body(response);
            }

            // Calculate summaries
            Map<String, Object> result = summaryService.calculateAndGetMonthlySummary(year, month);

            // Check if any employees were processed
            @SuppressWarnings("unchecked")
            java.util.List<?> allEmployees = (java.util.List<?>) result.get("allEmployees");

            if (allEmployees == null || allEmployees.isEmpty()) {
                System.out.println("⚠ No attendance data found for " + year + "-" + String.format("%02d", month));
                response.put("success", false);
                response.put("message", "No attendance records found for " + year + "-" + String.format("%02d", month) +
                        ". Please ensure employees have checked in/out during this month.");
                response.put("year", year);
                response.put("month", month);
                return ResponseEntity.ok(response);
            }

            System.out.println("✓ Successfully calculated summaries for " + allEmployees.size() + " employees");

            response.put("success", true);
            response.put("message", "Attendance summary calculated successfully");
            response.put("year", year);
            response.put("month", month);
            response.put("employeeCount", allEmployees.size());
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("ERROR in calculateMonthlySummary:");
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "Error calculating attendance summary: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get monthly attendance report (read-only)
     */
    @GetMapping("/report")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMonthlyReport(
            @RequestParam int year,
            @RequestParam int month) {

        try {
            Map<String, Object> report = summaryService.getMonthlyAttendanceReport(year, month);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", report);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching report: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get individual employee monthly summary
     */
    @GetMapping("/employee")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getEmployeeSummary(
            @RequestParam String employeeId,
            @RequestParam int year,
            @RequestParam int month) {

        try {
            Map<String, Object> summary = summaryService.getEmployeeMonthlySummary(employeeId, year, month);

            Map<String, Object> response = new HashMap<>();

            if (summary.containsKey("error")) {
                response.put("success", false);
                response.put("message", summary.get("error"));
            } else {
                response.put("success", true);
                response.put("data", summary);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching employee summary: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Search employees by attendance criteria
     */
    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchEmployees(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "ALL") String criteria,
            @RequestParam(required = false) Double threshold) {

        try {
            Map<String, Object> result = summaryService.searchEmployeesByAttendance(
                    year, month, criteria, threshold);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error searching employees: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }
}