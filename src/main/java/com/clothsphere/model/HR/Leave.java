package com.clothsphere.model.HR;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "leave_requests")
public class Leave {

    @Id
    @GeneratedValue(generator = "leave_id_generator")
    @GenericGenerator(name = "leave_id_generator",
            type = com.clothsphere.util.LeaveIdGenerator.class)
    private String leaveId;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column
    private LocalTime startTime;

    @Column
    private LocalTime endTime;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(nullable = false)
    private String leaveType = "SHORT_LEAVE"; // SHORT_LEAVE, MEDIUM_LEAVE, LONG_LEAVE

    @Column
    private Integer totalHours;

    private LocalDateTime requestDate;

    private LocalDateTime actionDate;

    private String comments;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // Constructors
    public Leave() {
        this.requestDate = LocalDateTime.now();
    }

    public Leave(String reason, LocalDate startDate, LocalDate endDate, Employee employee) {
        this();
        this.reason = reason;
        this.startDate = startDate;
        this.endDate = endDate;
        this.employee = employee;
    }

    // Getters and Setters
    public String getLeaveId() { return leaveId; }
    public void setLeaveId(String leaveId) { this.leaveId = leaveId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public Integer getTotalHours() { return totalHours; }
    public void setTotalHours(Integer totalHours) { this.totalHours = totalHours; }

    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }

    public LocalDateTime getActionDate() { return actionDate; }
    public void setActionDate(LocalDateTime actionDate) { this.actionDate = actionDate; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    // Utility method to calculate total days
    public int getTotalDays() {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    // Utility method to calculate total hours
    public int calculateTotalHours() {
        if (totalHours != null) {
            return totalHours;
        }

        if ("SHORT_LEAVE".equals(leaveType)) {
            // For short leaves, calculate based on time difference
            if (startTime != null && endTime != null) {
                return (int) java.time.temporal.ChronoUnit.HOURS.between(startTime, endTime);
            }
            return 2; // Default 2 hours for short leaves
        } else if ("MEDIUM_LEAVE".equals(leaveType)) {
            return 8; // 8 hours for 1 day
        } else if ("LONG_LEAVE".equals(leaveType)) {
            return getTotalDays() * 8; // 8 hours per day for multi-day leaves
        }

        return 0;
    }
}