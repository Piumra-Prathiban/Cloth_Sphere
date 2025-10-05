package com.clothsphere.controller.HR;

import com.clothsphere.service.HR.PayrollService;
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

    /**
     * Get employee's own payroll for a specific month
     */
    @GetMapping("/my-paysheet")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMyPayroll(
            @RequestParam int year,
            @RequestParam int month) {

        // In a real application, you would get the employee ID from the session/authentication
        String employeeId = getCurrentEmployeeId(); // You need to implement this method

        Map<String, Object> result = payrollService.getEmployeePayroll(employeeId, year, month);
        return ResponseEntity.ok(result);
    }

    /**
     * Get all payroll records for the current employee
     */
    @GetMapping("/my-paysheets")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMyAllPayrolls() {
        String employeeId = getCurrentEmployeeId();

        try {
            // Get all payrolls and filter for current employee
            Map<String, Object> allPayrolls = payrollService.getAllPayrolls();

            if (Boolean.TRUE.equals(allPayrolls.get("success"))) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> payrollDetails =
                        (java.util.List<Map<String, Object>>) allPayrolls.get("payrolls");

                // Filter for current employee
                java.util.List<Map<String, Object>> myPayrolls = payrollDetails.stream()
                        .filter(detail -> {
                            com.clothsphere.model.HR.Payroll payroll =
                                    (com.clothsphere.model.HR.Payroll) detail.get("payroll");
                            return payroll.getEmployeeId().equals(employeeId);
                        })
                        .collect(java.util.stream.Collectors.toList());

                Map<String, Object> result = new java.util.HashMap<>();
                result.put("success", true);
                result.put("payrolls", myPayrolls);
                result.put("employeeId", employeeId);
                result.put("employeeName", getEmployeeName(employeeId));
                result.put("totalRecords", myPayrolls.size());

                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.ok(allPayrolls);
            }

        } catch (Exception e) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("message", "Error fetching payroll records: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * Export payroll report for current employee
     */
    @GetMapping("/export")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> exportMyPayrollReport(
            @RequestParam int year,
            @RequestParam int month) {

        String employeeId = getCurrentEmployeeId();

        try {
            Map<String, Object> payrollData = payrollService.getEmployeePayroll(employeeId, year, month);

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

    /**
     * Get payroll statistics for current employee
     */
    @GetMapping("/my-statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMyPayrollStatistics() {
        String employeeId = getCurrentEmployeeId();

        try {
            Map<String, Object> allPayrolls = payrollService.getAllPayrolls();

            if (Boolean.TRUE.equals(allPayrolls.get("success"))) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> payrollDetails =
                        (java.util.List<Map<String, Object>>) allPayrolls.get("payrolls");

                // Filter for current employee and calculate statistics
                java.util.List<Map<String, Object>> myPayrolls = payrollDetails.stream()
                        .filter(detail -> {
                            com.clothsphere.model.HR.Payroll payroll =
                                    (com.clothsphere.model.HR.Payroll) detail.get("payroll");
                            return payroll.getEmployeeId().equals(employeeId);
                        })
                        .collect(java.util.stream.Collectors.toList());

                // Calculate statistics
                double totalNetSalary = 0.0;
                double totalGrossSalary = 0.0;
                double totalDeductions = 0.0;
                int paidCount = 0;
                int pendingCount = 0;

                for (Map<String, Object> detail : myPayrolls) {
                    com.clothsphere.model.HR.Payroll payroll =
                            (com.clothsphere.model.HR.Payroll) detail.get("payroll");

                    totalNetSalary += payroll.getNetSalary().doubleValue();
                    totalGrossSalary += payroll.getGrossSalary().doubleValue();
                    totalDeductions += payroll.getDeductions().doubleValue();

                    if ("PAID".equals(payroll.getStatus())) {
                        paidCount++;
                    } else {
                        pendingCount++;
                    }
                }

                Map<String, Object> statistics = new java.util.HashMap<>();
                statistics.put("totalPayrolls", myPayrolls.size());
                statistics.put("paidCount", paidCount);
                statistics.put("pendingCount", pendingCount);
                statistics.put("totalNetSalary", totalNetSalary);
                statistics.put("totalGrossSalary", totalGrossSalary);
                statistics.put("totalDeductions", totalDeductions);
                statistics.put("averageNetSalary", myPayrolls.size() > 0 ? totalNetSalary / myPayrolls.size() : 0);

                Map<String, Object> result = new java.util.HashMap<>();
                result.put("success", true);
                result.put("statistics", statistics);
                result.put("employeeName", getEmployeeName(employeeId));

                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.ok(allPayrolls);
            }

        } catch (Exception e) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("message", "Error calculating statistics: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    // Helper methods
    private String getCurrentEmployeeId() {
        // In a real application, get from Spring Security context
        // For now, return a placeholder - you'll need to implement proper authentication
        return "emp01"; // Replace with actual employee ID from session
    }

    private String getEmployeeName(String employeeId) {
        // Implement employee name lookup
        return "Employee Name"; // Replace with actual implementation
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