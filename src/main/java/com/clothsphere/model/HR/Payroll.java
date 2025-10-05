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

    // Business logic methods
    public void calculateSalary() {
        if (basicSalary == null || actualWorkHours == null || maxWorkHours == null) {
            return;
        }

        // Calculate hourly rate
        BigDecimal hourlyRate = basicSalary.divide(
                new BigDecimal(maxWorkHours.toString()), 2, BigDecimal.ROUND_HALF_UP
        );

        // Calculate regular hours (up to max work hours)
        Double regularHours = Math.min(actualWorkHours, maxWorkHours);
        BigDecimal regularPay = hourlyRate.multiply(new BigDecimal(regularHours.toString()));

        // Calculate overtime
        this.otHours = Math.max(0.0, actualWorkHours - maxWorkHours);
        if (otHours > 0) {
            BigDecimal otHourlyRate = hourlyRate.multiply(otRate);
            this.otAmount = otHourlyRate.multiply(new BigDecimal(otHours.toString()));
        } else {
            this.otAmount = BigDecimal.ZERO;
        }

        // Calculate gross salary
        this.grossSalary = regularPay.add(otAmount);

        // Apply deductions based on attendance rate
        if (attendanceRate < 80.0) {
            double deductionPercentage = (80.0 - attendanceRate) / 100.0;
            this.deductions = grossSalary.multiply(new BigDecimal(deductionPercentage));
        } else {
            this.deductions = BigDecimal.ZERO;
        }

        // Calculate net salary
        this.netSalary = grossSalary.subtract(deductions);
        this.status = "CALCULATED";
    }
}