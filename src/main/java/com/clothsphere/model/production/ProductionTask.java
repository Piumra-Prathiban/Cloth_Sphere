package com.clothsphere.model.production;

import com.clothsphere.model.HR.Employee;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "production_task")
public class ProductionTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ProductionSchedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee assignedEmployee;

    @Column(name = "task_name", nullable = false, length = 150)
    private String taskName;

    @Column(name = "task_description", length = 600)
    private String taskDescription;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "priority", length = 20)
    private String priority;

    @Column(name = "progress_percent")
    private Integer progressPercent;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "urgent_flag")
    private boolean urgent;

    @Column(name = "issue_notes", length = 500)
    private String issueNotes;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    public ProductionTask() {
    }

    public ProductionTask(ProductionSchedule schedule, Employee assignedEmployee, String taskName,
                          String taskDescription, String status, String priority, Integer progressPercent,
                          LocalDate dueDate, boolean urgent, String issueNotes) {
        this.schedule = schedule;
        this.assignedEmployee = assignedEmployee;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.status = status;
        this.priority = priority;
        this.progressPercent = progressPercent;
        this.dueDate = dueDate;
        this.urgent = urgent;
        this.issueNotes = issueNotes;
    }

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProductionSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(ProductionSchedule schedule) {
        this.schedule = schedule;
    }

    public Employee getAssignedEmployee() {
        return assignedEmployee;
    }

    public void setAssignedEmployee(Employee assignedEmployee) {
        this.assignedEmployee = assignedEmployee;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Integer getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Integer progressPercent) {
        this.progressPercent = progressPercent;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    public String getIssueNotes() {
        return issueNotes;
    }

    public void setIssueNotes(String issueNotes) {
        this.issueNotes = issueNotes;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isCompleted() {
        if (status != null && status.equalsIgnoreCase("COMPLETED")) {
            return true;
        }
        return progressPercent != null && progressPercent >= 100;
    }

    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now()) && !isCompleted();
    }
}
