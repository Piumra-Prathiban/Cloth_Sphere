package com.clothsphere.model.HR;

import java.io.Serializable;
import java.util.Objects;

public class MonthlyAttendanceSummaryId implements Serializable {
    private String employeeId;
    private String monthYear;

    public MonthlyAttendanceSummaryId() {}

    public MonthlyAttendanceSummaryId(String employeeId, String monthYear) {
        this.employeeId = employeeId;
        this.monthYear = monthYear;
    }

    // Getters, Setters, equals, hashCode
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getMonthYear() { return monthYear; }
    public void setMonthYear(String monthYear) { this.monthYear = monthYear; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MonthlyAttendanceSummaryId that = (MonthlyAttendanceSummaryId) o;
        return Objects.equals(employeeId, that.employeeId) &&
                Objects.equals(monthYear, that.monthYear);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, monthYear);
    }
}