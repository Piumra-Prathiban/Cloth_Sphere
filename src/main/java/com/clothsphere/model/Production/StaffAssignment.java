package com.clothsphere.model.Production;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_assignment")
public class StaffAssignment {

    @Id
    @Column(name = "assignment_id", length = 10, nullable = false)
    private String assignmentId;

    @Column(name = "schedule_id", nullable = false, length = 10)
    private String scheduleId;

    @Column(name = "employee_id", nullable = false, length = 10)
    private String employeeId;

    @Column(name = "employee_name", length = 100)
    private String employeeName;

    @Column(name = "workstation_id", nullable = false, length = 10)
    private String workstationId;

    @Column(name = "assignment_date", nullable = false)
    private LocalDate assignmentDate;

    @Column(name = "shift", length = 20)
    private String shift; // MORNING, AFTERNOON, NIGHT

    @Column(name = "role", length = 50)
    private String role; // OPERATOR, QUALITY_CHECKER, SUPERVISOR

    @Column(name = "status", length = 20)
    private String status; // ASSIGNED, WORKING, COMPLETED, ABSENT

    @Column(name = "assigned_quantity")
    private Integer assignedQuantity;

    @Column(name = "completed_quantity")
    private Integer completedQuantity = 0;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "notes", length = 500)
    private String notes;

    // Default constructor
    public StaffAssignment() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.completedQuantity = 0;
    }

    // Parameterized constructor
    public StaffAssignment(String assignmentId, String scheduleId, String employeeId,
                          String employeeName, String workstationId, LocalDate assignmentDate,
                          String shift, String role, String status, Integer assignedQuantity) {
        this.assignmentId = assignmentId;
        this.scheduleId = scheduleId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.workstationId = workstationId;
        this.assignmentDate = assignmentDate;
        this.shift = shift;
        this.role = role;
        this.status = status;
        this.assignedQuantity = assignedQuantity;
        this.completedQuantity = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }

    public String getScheduleId() { return scheduleId; }
    public void setScheduleId(String scheduleId) { this.scheduleId = scheduleId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getWorkstationId() { return workstationId; }
    public void setWorkstationId(String workstationId) { this.workstationId = workstationId; }

    public LocalDate getAssignmentDate() { return assignmentDate; }
    public void setAssignmentDate(LocalDate assignmentDate) { this.assignmentDate = assignmentDate; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getAssignedQuantity() { return assignedQuantity; }
    public void setAssignedQuantity(Integer assignedQuantity) { this.assignedQuantity = assignedQuantity; }

    public Integer getCompletedQuantity() { return completedQuantity; }
    public void setCompletedQuantity(Integer completedQuantity) {
        this.completedQuantity = completedQuantity;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // Calculated field for performance percentage
    @Transient
    public Double getPerformancePercentage() {
        if (assignedQuantity == null || assignedQuantity == 0) return 0.0;
        return (completedQuantity * 100.0) / assignedQuantity;
    }

    @Override
    public String toString() {
        return "StaffAssignment{" +
                "assignmentId='" + assignmentId + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", workstationId='" + workstationId + '\'' +
                ", status='" + status + '\'' +
                ", shift='" + shift + '\'' +
                '}';
    }
}