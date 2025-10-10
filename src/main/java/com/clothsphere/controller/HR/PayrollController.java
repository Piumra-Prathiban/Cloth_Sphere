// PayrollController.java
package com.clothsphere.controller.HR;

import com.clothsphere.service.HR.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/payroll")
public class PayrollController {

    @Autowired
    private PayrollService payrollService;

    // Generate monthly payroll for all employees
    @PostMapping("/generate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateMonthlyPayroll(
            @RequestParam int year,
            @RequestParam int month) {

        Map<String, Object> result = payrollService.generateMonthlyPayroll(year, month);
        return ResponseEntity.ok(result);
    }

    // Generate payroll for specific employee
    @PostMapping("/generate/employee")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateEmployeePayroll(
            @RequestParam String employeeId,
            @RequestParam int year,
            @RequestParam int month) {

        // This will call the service method indirectly through generateEmployeePayroll
        Map<String, Object> result = payrollService.getEmployeePayroll(employeeId, year, month);
        return ResponseEntity.ok(result);
    }

    // Get employee payroll
    @GetMapping("/employee")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getEmployeePayroll(
            @RequestParam String employeeId,
            @RequestParam int year,
            @RequestParam int month) {

        Map<String, Object> result = payrollService.getEmployeePayroll(employeeId, year, month);
        return ResponseEntity.ok(result);
    }

    // Get monthly payroll report
    @GetMapping("/monthly-report")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMonthlyPayrollReport(
            @RequestParam int year,
            @RequestParam int month) {

        Map<String, Object> result = payrollService.getMonthlyPayrollReport(year, month);
        return ResponseEntity.ok(result);
    }

    // Update OT rate
    @PutMapping("/ot-rate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateOtRate(
            @RequestParam String employeeId,
            @RequestParam String payrollMonth,
            @RequestParam Double otRate) {

        Map<String, Object> result = payrollService.updateOtRate(
                employeeId, payrollMonth, java.math.BigDecimal.valueOf(otRate));
        return ResponseEntity.ok(result);
    }

    // Mark payroll as paid
    @PutMapping("/mark-paid")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsPaid(
            @RequestParam String employeeId,
            @RequestParam String payrollMonth) {

        Map<String, Object> result = payrollService.markAsPaid(employeeId, payrollMonth);
        return ResponseEntity.ok(result);
    }

    // Payroll management page
    @GetMapping("/management")
    public String payrollManagementPage() {
        return "hr/payroll-management"; // You'll need to create this HTML template
    }

    // In PayrollController.java
    @GetMapping("/all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllPayrolls() {
        Map<String, Object> result = payrollService.getAllPayrolls();
        return ResponseEntity.ok(result);
    }

}