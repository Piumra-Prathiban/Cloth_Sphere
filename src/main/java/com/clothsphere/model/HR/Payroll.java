// Payroll.java
package com.clothsphere.model.HR;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll")
@IdClass(PayrollId.class)
public class Payroll {

    @Id
    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Id
    @Column(name = "payroll_month", nullable = false)
    private String payrollMonth; // Format: "2025-10"

    @Column(name = "basic_salary", nullable = false)
    private BigDecimal basicSalary;

    @Column(name = "actual_work_hours")
    private Double actualWorkHours;

    @Column(name = "max_work_hours")
    private Double maxWorkHours = 180.0; // Default 180 hours per month

    @Column(name = "ot_rate")
    private BigDecimal otRate = new BigDecimal("1.5"); // Default 1.5x

    @Column(name = "ot_hours")
    private Double otHours = 0.0;

    @Column(name = "ot_amount")
    private BigDecimal otAmount = BigDecimal.ZERO;

    @Column(name = "gross_salary")
    private BigDecimal grossSalary = BigDecimal.ZERO;

    @Column(name = "epf_rate")
    private BigDecimal epfRate = new BigDecimal("8.0");

    @Column(name = "etf_rate")
    private BigDecimal etfRate = new BigDecimal("4.0");

    @Column(name = "epf_amount")
    private BigDecimal epfAmount = BigDecimal.ZERO;

    @Column(name = "etf_amount")
    private BigDecimal etfAmount = BigDecimal.ZERO;

    @Column(name = "deductions")
    private BigDecimal deductions = BigDecimal.ZERO;

    @Column(name = "net_salary")
    private BigDecimal netSalary = BigDecimal.ZERO;

    @Column(name = "attendance_rate")
    private Double attendanceRate = 0.0;

    @Column(name = "status")
    private String status = "PENDING"; // PENDING, CALCULATED, PAID

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Payroll() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Payroll(String employeeId, String payrollMonth, BigDecimal basicSalary) {
        this();
        this.employeeId = employeeId;
        this.payrollMonth = payrollMonth;
        this.basicSalary = basicSalary;
    }

    // Getters and Setters
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getPayrollMonth() { return payrollMonth; }
    public void setPayrollMonth(String payrollMonth) { this.payrollMonth = payrollMonth; }

    public BigDecimal getBasicSalary() { return basicSalary; }
    public void setBasicSalary(BigDecimal basicSalary) { this.basicSalary = basicSalary; }

    public Double getActualWorkHours() { return actualWorkHours; }
    public void setActualWorkHours(Double actualWorkHours) { this.actualWorkHours = actualWorkHours; }

    public Double getMaxWorkHours() { return maxWorkHours; }
    public void setMaxWorkHours(Double maxWorkHours) { this.maxWorkHours = maxWorkHours; }

    public BigDecimal getOtRate() { return otRate; }
    public void setOtRate(BigDecimal otRate) { this.otRate = otRate; }

    public Double getOtHours() { return otHours; }
    public void setOtHours(Double otHours) { this.otHours = otHours; }

    public BigDecimal getOtAmount() { return otAmount; }
    public void setOtAmount(BigDecimal otAmount) { this.otAmount = otAmount; }

    public BigDecimal getGrossSalary() { return grossSalary; }
    public void setGrossSalary(BigDecimal grossSalary) { this.grossSalary = grossSalary; }

    public BigDecimal getEpfRate() { return epfRate; }
    public void setEpfRate(BigDecimal epfRate) { this.epfRate = epfRate; }

    public BigDecimal getEtfRate() { return etfRate; }
    public void setEtfRate(BigDecimal etfRate) { this.etfRate = etfRate; }

    public BigDecimal getEpfAmount() { return epfAmount; }
    public void setEpfAmount(BigDecimal epfAmount) { this.epfAmount = epfAmount; }

    public BigDecimal getEtfAmount() { return etfAmount; }
    public void setEtfAmount(BigDecimal etfAmount) { this.etfAmount = etfAmount; }

    public BigDecimal getDeductions() { return deductions; }
    public void setDeductions(BigDecimal deductions) { this.deductions = deductions; }

    public BigDecimal getNetSalary() { return netSalary; }
    public void setNetSalary(BigDecimal netSalary) { this.netSalary = netSalary; }

    public Double getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(Double attendanceRate) { this.attendanceRate = attendanceRate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // In Payroll.java - Update the calculateSalary method to clarify OT rate usage
    /**
     * Calculate salary with EPF/ETF deductions
     * Formula:
     * - Basic Salary = Department Budget (full amount, not divided)
     * - Hourly Rate = Basic Salary / Max Work Hours
     * - Regular Hours Pay = Hourly Rate × Min(Actual Hours, Max Hours)
     * - OT Amount = Hourly Rate × OT Hours × OT Rate (OT Rate is multiplier, e.g., 1.5 for time-and-a-half)
     * - Gross Salary = Regular Hours Pay + OT Amount
     * - EPF = Gross Salary × EPF Rate (e.g., 8%)
     * - ETF = Gross Salary × ETF Rate (e.g., 3%)
     * - Total Deductions = EPF + ETF
     * - Net Salary = Gross Salary - Total Deductions
     */
    public void calculateSalary() {
        if (basicSalary == null || actualWorkHours == null || maxWorkHours == null) {
            return;
        }

        // Calculate hourly rate from basic salary
        BigDecimal hourlyRate = basicSalary.divide(
                new BigDecimal(maxWorkHours.toString()), 2, BigDecimal.ROUND_HALF_UP
        );

        // Calculate regular hours (up to max work hours)
        Double regularHours = Math.min(actualWorkHours, maxWorkHours);
        BigDecimal regularPay = hourlyRate.multiply(new BigDecimal(regularHours.toString()));

        // Calculate overtime
        this.otHours = Math.max(0.0, actualWorkHours - maxWorkHours);
        if (otHours > 0) {
            // OT Rate is a multiplier (e.g., 1.5 means time-and-a-half)
            BigDecimal otHourlyRate = hourlyRate.multiply(otRate);
            this.otAmount = otHourlyRate.multiply(new BigDecimal(otHours.toString()));
        } else {
            this.otAmount = BigDecimal.ZERO;
        }

        // Calculate gross salary (Regular Pay + OT Amount)
        this.grossSalary = regularPay.add(otAmount);

        // Calculate EPF (Employee Provident Fund)
        this.epfAmount = grossSalary.multiply(epfRate.divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP));

        // Calculate ETF (Employees' Trust Fund)
        this.etfAmount = grossSalary.multiply(etfRate.divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP));

        // Total deductions = EPF + ETF
        this.deductions = epfAmount.add(etfAmount);

        // Calculate net salary
        this.netSalary = grossSalary.subtract(deductions);
        this.status = "CALCULATED";
    }
}