package com.clothsphere.model.HR;

import com.clothsphere.model.HR.Employee;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    private LocalDateTime requestDate;

    private LocalDateTime actionDate;

    private String comments;

    @ManyToOne(fetch = FetchType.EAGER) // Changed from LAZY to EAGER
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
    public String getLeaveId() { return leaveId; } // Changed return type
    public void setLeaveId(String leaveId) { this.leaveId = leaveId; } // Changed parameter type

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

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
}