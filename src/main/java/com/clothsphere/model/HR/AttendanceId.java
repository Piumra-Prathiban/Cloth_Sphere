package com.clothsphere.model.HR;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class AttendanceId implements Serializable {
    private String employeeId;
    private LocalDate attendanceDate;

    public AttendanceId() {}

    public AttendanceId(String employeeId, LocalDate attendanceDate) {
        this.employeeId = employeeId;
        this.attendanceDate = attendanceDate;
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttendanceId that = (AttendanceId) o;
        return Objects.equals(employeeId, that.employeeId) &&
                Objects.equals(attendanceDate, that.attendanceDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, attendanceDate);
    }
}