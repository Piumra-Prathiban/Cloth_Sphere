package com.clothsphere.model.HR;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_attendance_summary")
@IdClass(MonthlyAttendanceSummaryId.class)
public class MonthlyAttendanceSummary {

    @Id
    @Column(name = "employee_id")
    private String employeeId;

    @Id
    @Column(name = "month_year")
    private String monthYear; // Format: "2025-10"

    @Column(name = "total_days_in_month")
    private int totalDaysInMonth;

    @Column(name = "present_days")
    private int presentDays;

    @Column(name = "absent_days")
    private int absentDays;

    @Column(name = "total_work_hours")
    private double totalWorkHours;

    @Column(name = "attendance_rate")
    private double attendanceRate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and Setters
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getMonthYear() { return monthYear; }
    public void setMonthYear(String monthYear) { this.monthYear = monthYear; }

    public int getTotalDaysInMonth() { return totalDaysInMonth; }
    public void setTotalDaysInMonth(int totalDaysInMonth) { this.totalDaysInMonth = totalDaysInMonth; }

    public int getPresentDays() { return presentDays; }
    public void setPresentDays(int presentDays) { this.presentDays = presentDays; }

    public int getAbsentDays() { return absentDays; }
    public void setAbsentDays(int absentDays) { this.absentDays = absentDays; }

    public double getTotalWorkHours() { return totalWorkHours; }
    public void setTotalWorkHours(double totalWorkHours) { this.totalWorkHours = totalWorkHours; }

    public double getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(double attendanceRate) { this.attendanceRate = attendanceRate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}