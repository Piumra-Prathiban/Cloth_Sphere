package com.clothsphere.service.HR;

import com.clothsphere.model.HR.Payroll;
import com.clothsphere.repository.HR.PayrollRepository;
import com.clothsphere.repository.HR.MonthlyAttendanceSummaryRepository;
import com.clothsphere.repository.HR.EmployeeRepository;
import com.clothsphere.repository.HR.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PayrollService {

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private MonthlyAttendanceSummaryRepository monthlySummaryRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * Generate payroll for all employees for a specific month
     */
    @Transactional
    public Map<String, Object> generateMonthlyPayroll(int year, int month) {
        Map<String, Object> result = new HashMap<>();
        String payrollMonth = String.format("%d-%02d", year, month);

        try {
            System.out.println("=== GENERATING MONTHLY PAYROLL ===");
            System.out.println("Year: " + year + ", Month: " + month);

            // Get all employees
            List<String> employeeIds = employeeRepository.findAllEmployeeIds();
            System.out.println("Found " + employeeIds.size() + " employees");

            List<Payroll> generatedPayrolls = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (String employeeId : employeeIds) {
                try {
                    System.out.println("\n--- Processing employee: " + employeeId + " ---");
                    Payroll payroll = generateEmployeePayroll(employeeId, year, month);
                    if (payroll != null) {
                        generatedPayrolls.add(payroll);
                        System.out.println("✓ Payroll generated successfully for: " + employeeId);
                    } else {
                        errors.add("Failed to generate payroll for: " + employeeId);
                        System.out.println("✗ Failed to generate payroll for: " + employeeId);
                    }
                } catch (Exception e) {
                    errors.add("Error for " + employeeId + ": " + e.getMessage());
                    System.err.println("✗ Error processing " + employeeId + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            result.put("success", true);
            result.put("message", "Payroll generated for " + generatedPayrolls.size() + " employees");
            result.put("payrollMonth", payrollMonth);
            result.put("generatedPayrolls", generatedPayrolls);
            result.put("errors", errors);

            System.out.println("\n=== PAYROLL GENERATION COMPLETE ===");
            System.out.println("Success: " + generatedPayrolls.size() + " / " + employeeIds.size());

        } catch (Exception e) {
            System.err.println("=== PAYROLL GENERATION FAILED ===");
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error generating payroll: " + e.getMessage());
        }

        return result;
    }

    /**
     * Generate payroll for a specific employee
     */
    @Transactional
    public Payroll generateEmployeePayroll(String employeeId, int year, int month) {
        String payrollMonth = String.format("%d-%02d", year, month);
        String monthYear = payrollMonth;

        try {
            System.out.println("Step 1: Getting basic salary for " + employeeId);
            // Get employee's basic salary from department
            BigDecimal basicSalary = getEmployeeBasicSalary(employeeId);
            if (basicSalary == null) {
                System.err.println("ERROR: No basic salary found for employee: " + employeeId);
                return null;
            }
            System.out.println("✓ Basic Salary: " + basicSalary);

            System.out.println("Step 2: Getting monthly attendance summary");
            // Get monthly attendance summary
            Optional<com.clothsphere.model.HR.MonthlyAttendanceSummary> summaryOpt =
                    monthlySummaryRepository.findByEmployeeAndMonth(employeeId, monthYear);

            if (summaryOpt.isEmpty()) {
                System.err.println("ERROR: No attendance summary found for employee: " + employeeId + " for month: " + monthYear);
                return null;
            }

            com.clothsphere.model.HR.MonthlyAttendanceSummary summary = summaryOpt.get();
            System.out.println("✓ Attendance Summary found:");
            System.out.println("  - Total Work Hours: " + summary.getTotalWorkHours());
            System.out.println("  - Attendance Rate: " + summary.getAttendanceRate() + "%");

            System.out.println("Step 3: Creating payroll object");
            // Create payroll object
            Payroll payroll = new Payroll(employeeId, payrollMonth, basicSalary);
            payroll.setActualWorkHours(summary.getTotalWorkHours());
            payroll.setAttendanceRate(summary.getAttendanceRate());

            System.out.println("Step 4: Calculating salary");
            // Calculate salary
            payroll.calculateSalary();
            System.out.println("✓ Salary calculated:");
            System.out.println("  - Gross Salary: " + payroll.getGrossSalary());
            System.out.println("  - Deductions: " + payroll.getDeductions());
            System.out.println("  - Net Salary: " + payroll.getNetSalary());

            System.out.println("Step 5: Checking for existing payroll");
            // Check if payroll already exists
            Optional<Payroll> existingPayroll = payrollRepository.findByEmployeeAndMonth(employeeId, payrollMonth);

            if (existingPayroll.isPresent()) {
                System.out.println("✓ Updating existing payroll record");
                // Update existing payroll
                payrollRepository.updatePayroll(
                        employeeId, payrollMonth,
                        payroll.getBasicSalary().doubleValue(),
                        payroll.getActualWorkHours(),
                        payroll.getMaxWorkHours(),
                        payroll.getOtRate().doubleValue(),
                        payroll.getOtHours(),
                        payroll.getOtAmount().doubleValue(),
                        payroll.getGrossSalary().doubleValue(),
                        payroll.getEpfRate().doubleValue(),
                        payroll.getEtfRate().doubleValue(),
                        payroll.getEpfAmount().doubleValue(),
                        payroll.getEtfAmount().doubleValue(),
                        payroll.getDeductions().doubleValue(),
                        payroll.getNetSalary().doubleValue(),
                        payroll.getAttendanceRate(),
                        payroll.getStatus()
                );
                System.out.println("✓ Payroll updated successfully");
            } else {
                System.out.println("✓ Inserting new payroll record");
                // Insert new payroll
                payrollRepository.insertPayroll(
                        employeeId, payrollMonth,
                        payroll.getBasicSalary().doubleValue(),
                        payroll.getActualWorkHours(),
                        payroll.getMaxWorkHours(),
                        payroll.getOtRate().doubleValue(),
                        payroll.getOtHours(),
                        payroll.getOtAmount().doubleValue(),
                        payroll.getGrossSalary().doubleValue(),
                        payroll.getEpfRate().doubleValue(),
                        payroll.getEtfRate().doubleValue(),
                        payroll.getEpfAmount().doubleValue(),
                        payroll.getEtfAmount().doubleValue(),
                        payroll.getDeductions().doubleValue(),
                        payroll.getNetSalary().doubleValue(),
                        payroll.getAttendanceRate(),
                        payroll.getStatus()
                );
                System.out.println("✓ Payroll inserted successfully");
            }

            return payroll;

        } catch (Exception e) {
            System.err.println("ERROR: Exception in generateEmployeePayroll for " + employeeId);
            System.err.println("Exception message: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get employee's basic salary from their department
     */
    private BigDecimal getEmployeeBasicSalary(String employeeId) {
        try {
            System.out.println("  - Looking up employee: " + employeeId);
            // Get employee's department
            Optional<com.clothsphere.model.HR.Employee> employeeOpt = employeeRepository.findEmployeeById(employeeId);
            if (employeeOpt.isPresent()) {
                String departmentName = String.valueOf(employeeOpt.get().getDepartment());
                System.out.println("  - Employee department: " + departmentName);

                if (departmentName != null && !departmentName.trim().isEmpty() && !"null".equals(departmentName)) {
                    // Get department's salary budget
                    Optional<com.clothsphere.model.HR.Department> deptOpt =
                            Optional.ofNullable(departmentRepository.findByDepartmentName(departmentName));
                    if (deptOpt.isPresent()) {
                        Double salaryBudget = deptOpt.get().getSalaryBudget();
                        System.out.println("  - Department salary budget: " + salaryBudget);
                        if (salaryBudget != null && salaryBudget > 0) {
                            // Return FULL salary budget as basic salary (NOT divided by 12)
                            BigDecimal basicSalary = BigDecimal.valueOf(salaryBudget);
                            System.out.println("  - Basic salary (full budget): " + basicSalary);
                            return basicSalary;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("  - ERROR getting basic salary for employee " + employeeId + ": " + e.getMessage());
            e.printStackTrace();
        }

        // Return default basic salary if not found
        System.out.println("  - Using default salary: 50000.00");
        return new BigDecimal("50000.00");
    }

    /**
     * Get payroll for a specific employee and month
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getEmployeePayroll(String employeeId, int year, int month) {
        Map<String, Object> result = new HashMap<>();
        String payrollMonth = String.format("%d-%02d", year, month);

        try {
            Optional<Payroll> payrollOpt = payrollRepository.findByEmployeeAndMonth(employeeId, payrollMonth);

            if (payrollOpt.isPresent()) {
                Payroll payroll = payrollOpt.get();

                // Get employee details
                String employeeName = getEmployeeName(employeeId);

                result.put("success", true);
                result.put("payroll", payroll);
                result.put("employeeName", employeeName);
            } else {
                result.put("success", false);
                result.put("message", "No payroll found for employee " + employeeId + " for month " + payrollMonth);
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error fetching payroll: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Get all payrolls for a specific month
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMonthlyPayrollReport(int year, int month) {
        Map<String, Object> result = new HashMap<>();
        String payrollMonth = String.format("%d-%02d", year, month);

        try {
            System.out.println("=== FETCHING MONTHLY PAYROLL REPORT ===");
            System.out.println("Month: " + payrollMonth);

            List<Payroll> payrolls = payrollRepository.findAllByMonth(payrollMonth);
            System.out.println("Found " + payrolls.size() + " payroll records");

            List<Map<String, Object>> payrollDetails = new ArrayList<>();

            double totalGrossSalary = 0.0;
            double totalDeductions = 0.0;
            double totalNetSalary = 0.0;

            for (Payroll payroll : payrolls) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("payroll", payroll);
                detail.put("employeeName", getEmployeeName(payroll.getEmployeeId()));

                payrollDetails.add(detail);

                totalGrossSalary += payroll.getGrossSalary().doubleValue();
                totalDeductions += payroll.getDeductions().doubleValue();
                totalNetSalary += payroll.getNetSalary().doubleValue();
            }

            result.put("success", true);
            result.put("payrollMonth", payrollMonth);
            result.put("payrolls", payrollDetails);
            result.put("totalGrossSalary", totalGrossSalary);
            result.put("totalDeductions", totalDeductions);
            result.put("totalNetSalary", totalNetSalary);
            result.put("employeeCount", payrolls.size());

            System.out.println("✓ Report generated successfully");

        } catch (Exception e) {
            System.err.println("ERROR: Failed to fetch payroll report");
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error fetching payroll report: " + e.getMessage());
        }

        return result;
    }

    /**
     * Update OT rate for payroll calculation
     */
    @Transactional
    public Map<String, Object> updateOtRate(String employeeId, String payrollMonth, BigDecimal otRate) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("=== UPDATING OT RATE ===");
            System.out.println("Employee: " + employeeId + ", Month: " + payrollMonth + ", New OT Rate: " + otRate);

            Optional<Payroll> payrollOpt = payrollRepository.findByEmployeeAndMonth(employeeId, payrollMonth);

            if (payrollOpt.isPresent()) {
                Payroll payroll = payrollOpt.get();
                payroll.setOtRate(otRate);
                payroll.calculateSalary();

                payrollRepository.updatePayroll(
                        employeeId, payrollMonth,
                        payroll.getBasicSalary().doubleValue(),
                        payroll.getActualWorkHours(),
                        payroll.getMaxWorkHours(),
                        payroll.getOtRate().doubleValue(),
                        payroll.getOtHours(),
                        payroll.getOtAmount().doubleValue(),
                        payroll.getGrossSalary().doubleValue(),
                        payroll.getEpfRate().doubleValue(),
                        payroll.getEtfRate().doubleValue(),
                        payroll.getEpfAmount().doubleValue(),
                        payroll.getEtfAmount().doubleValue(),
                        payroll.getDeductions().doubleValue(),
                        payroll.getNetSalary().doubleValue(),
                        payroll.getAttendanceRate(),
                        payroll.getStatus()
                );

                result.put("success", true);
                result.put("message", "OT rate updated successfully");
                result.put("payroll", payroll);
                System.out.println("✓ OT rate updated successfully");
            } else {
                result.put("success", false);
                result.put("message", "Payroll not found");
                System.out.println("✗ Payroll not found");
            }

        } catch (Exception e) {
            System.err.println("ERROR: Failed to update OT rate");
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error updating OT rate: " + e.getMessage());
        }

        return result;
    }

    /**
     * Mark payroll as paid
     */
    @Transactional
    public Map<String, Object> markAsPaid(String employeeId, String payrollMonth) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("=== MARKING PAYROLL AS PAID ===");
            System.out.println("Employee: " + employeeId + ", Month: " + payrollMonth);

            payrollRepository.updatePayrollStatus(employeeId, payrollMonth, "PAID");
            result.put("success", true);
            result.put("message", "Payroll marked as paid");
            System.out.println("✓ Payroll marked as paid");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to mark payroll as paid");
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error updating payroll status: " + e.getMessage());
        }

        return result;
    }

    private String getEmployeeName(String employeeId) {
        try {
            return employeeRepository.findEmployeeById(employeeId)
                    .map(com.clothsphere.model.HR.Employee::getFullName)
                    .orElse("Unknown Employee");
        } catch (Exception e) {
            return "Unknown Employee";
        }
    }

    /**
     * Get all payroll records across all months
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAllPayrolls() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Use the injected repository instance
            List<Payroll> allPayrolls = payrollRepository.findAll();

            List<Map<String, Object>> payrollDetails = new ArrayList<>();

            for (Payroll payroll : allPayrolls) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("payroll", payroll);
                detail.put("employeeName", getEmployeeName(payroll.getEmployeeId()));
                payrollDetails.add(detail);
            }

            result.put("success", true);
            result.put("payrolls", payrollDetails);
            result.put("totalRecords", allPayrolls.size());

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error fetching all payrolls: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

}