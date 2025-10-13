package com.clothsphere.model.FM;

import com.clothsphere.model.HR.Department;
import com.clothsphere.model.HR.Employee;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

@Entity
@Table(name = "task_assignment")
public class TaskAssignment {

    @Id
    @Column(name = "assignment_id", length = 10, nullable = false)
    private String assignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "department", "taskAssignments"})
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", referencedColumnName = "task_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "department", "assignments"})
    private ProductionTask task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", referencedColumnName = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "employees", "productionTasks"})
    private Department department;

    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "actual_hours")
    private Integer actualHours;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "ASSIGNED"; // ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED

    @Column(name = "notes", length = 500)
    private String notes;

    // Default constructor
    public TaskAssignment() {
        this.assignedDate = LocalDate.now();
        this.status = "ASSIGNED";
    }

    // Parameterized constructor
    public TaskAssignment(Employee employee, ProductionTask task, Integer estimatedHours, String notes) {
        this();
        this.employee = employee;
        this.task = task;
        this.estimatedHours = estimatedHours;
        this.notes = notes;
    }

    // Getters and setters
    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public ProductionTask getTask() { return task; }
    public void setTask(ProductionTask task) { this.task = task; }

    public LocalDate getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDate assignedDate) { this.assignedDate = assignedDate; }

    public Integer getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Integer estimatedHours) { this.estimatedHours = estimatedHours; }

    public Integer getActualHours() { return actualHours; }
    public void setActualHours(Integer actualHours) { this.actualHours = actualHours; }

    public LocalDate getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // Utility methods
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isOverdue() {
        return task.getDeadline().isBefore(LocalDate.now()) && !isCompleted() && !"CANCELLED".equals(status);
    }

    public String getStatusLabel() {
        switch (status) {
            case "ASSIGNED": return "Assigned";
            case "IN_PROGRESS": return "In Progress";
            case "COMPLETED": return "Completed";
            case "CANCELLED": return "Cancelled";
            default: return status;
        }
    }

    @Override
    public String toString() {
        return "TaskAssignment{" +
                "assignmentId='" + assignmentId + '\'' +
                ", employee=" + (employee != null ? employee.getFullName() : "None") +
                ", task=" + (task != null ? task.getTaskName() : "None") +
                ", status='" + status + '\'' +
                '}';
    }
}