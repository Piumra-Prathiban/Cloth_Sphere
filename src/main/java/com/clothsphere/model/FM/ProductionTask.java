package com.clothsphere.model.FM;

import com.clothsphere.model.HR.Department;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_task")
public class ProductionTask {

    @Id
    @Column(name = "task_id", length = 10, nullable = false)
    private String taskId;

    @Column(name = "task_name", nullable = false, length = 200)
    private String taskName;

    @Column(name = "deadline", nullable = false)
    private LocalDate deadline;

    @Column(name = "priority", nullable = false, length = 2)
    private String priority; // P0, P1, P2, P3

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", referencedColumnName = "id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "productionTasks", "employees"})
    private Department department;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "PENDING"; // PENDING, IN_PROGRESS, COMPLETED, CANCELLED

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    @Column(name = "description", length = 1000)
    private String description;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TaskAssignment> assignments = new ArrayList<>();

    // Default constructor
    public ProductionTask() {
        this.createdDate = LocalDate.now();
        this.status = "PENDING";
    }

    // Parameterized constructor
    public ProductionTask(String taskName, LocalDate deadline, String priority, Department department, String description) {
        this();
        this.taskName = taskName;
        this.deadline = deadline;
        this.priority = priority;
        this.department = department;
        this.description = description;
    }

    // Getters and setters
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<TaskAssignment> getAssignments() { return assignments; }
    public void setAssignments(List<TaskAssignment> assignments) { this.assignments = assignments; }

    // Utility methods
    public String getPriorityLabel() {
        switch (priority) {
            case "P0": return "Critical";
            case "P1": return "High";
            case "P2": return "Medium";
            case "P3": return "Low";
            default: return "Unknown";
        }
    }

    public String getPriorityDescription() {
        switch (priority) {
            case "P0": return "Critical issues";
            case "P1": return "High-priority tasks";
            case "P2": return "Medium-priority";
            case "P3": return "Low-priority";
            default: return "Unknown priority";
        }
    }

    public boolean isOverdue() {
        return deadline.isBefore(LocalDate.now()) && !"COMPLETED".equals(status) && !"CANCELLED".equals(status);
    }

    @Override
    public String toString() {
        return "ProductionTask{" +
                "taskId='" + taskId + '\'' +
                ", taskName='" + taskName + '\'' +
                ", deadline=" + deadline +
                ", priority='" + priority + '\'' +
                ", status='" + status + '\'' +
                ", department='" + (department != null ? department.getDepartmentName() : "None") + '\'' +
                '}';
    }
}