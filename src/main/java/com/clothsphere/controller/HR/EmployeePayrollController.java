package com.clothsphere.controller.HR;

import com.clothsphere.service.HR.PayrollService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/employee/payroll")
public class EmployeePayrollController {

    @Autowired
    private PayrollService payrollService;

    // Get current employee's payroll for specific month
    @GetMapping("/my-paysheet")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMyPayroll(
            @RequestParam int year,
            @RequestParam int month,
            HttpSession session) {

        String employeeId = (String) session.getAttribute("employeeId");
        if (employeeId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Employee not logged in"));
        }

        Map<String, Object> result = payrollService.getCurrentEmployeePayroll(employeeId, year, month);
        return ResponseEntity.ok(result);
    }

    // Get current employee's payroll history - FIXED: Removed duplicate method
    @GetMapping("/my-paysheets")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMyPayrollHistory(HttpSession session) {
        String employeeId = (String) session.getAttribute("employeeId");
        if (employeeId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Employee not logged in"));
        }

        Map<String, Object> result = payrollService.getCurrentEmployeePayrollHistory(employeeId);
        return ResponseEntity.ok(result);
    }

    // Get payroll statistics for current employee
    @GetMapping("/my-statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMyPayrollStatistics(HttpSession session) {
        String employeeId = (String) session.getAttribute("employeeId");
        if (employeeId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Employee not logged in"));
        }

        Map<String, Object> result = payrollService.getCurrentEmployeePayrollStatistics(employeeId);
        return ResponseEntity.ok(result);
    }

    // Export payroll report for current employee
    @GetMapping("/export")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> exportMyPayrollReport(
            @RequestParam int year,
            @RequestParam int month,
            HttpSession session) {

        String employeeId = (String) session.getAttribute("employeeId");
        if (employeeId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Employee not logged in"));
        }

        try {
            Map<String, Object> payrollData = payrollService.getCurrentEmployeePayroll(employeeId, year, month);

            if (Boolean.TRUE.equals(payrollData.get("success"))) {
                // Generate export data
                Map<String, Object> exportData = generateExportData(payrollData);
                return ResponseEntity.ok(exportData);
            } else {
                return ResponseEntity.ok(payrollData);
            }

        } catch (Exception e) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("message", "Error exporting payroll: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    private Map<String, Object> generateExportData(Map<String, Object> payrollData) {
        Map<String, Object> exportData = new java.util.HashMap<>();
        exportData.put("success", true);
        exportData.put("message", "Payroll data ready for export");
        exportData.put("exportFormat", "PDF");
        exportData.put("payrollData", payrollData);
        exportData.put("generatedAt", java.time.LocalDateTime.now().toString());
        return exportData;
    }
}