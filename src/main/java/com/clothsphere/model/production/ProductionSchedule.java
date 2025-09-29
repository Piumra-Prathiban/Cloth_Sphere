package com.clothsphere.model.production;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_schedule")
public class ProductionSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_name", nullable = false, length = 120)
    private String scheduleName;

    @Column(name = "production_line", length = 100)
    private String productionLine;

    @Column(name = "target_quantity")
    private Integer targetQuantity;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductionTask> tasks = new ArrayList<>();

    public ProductionSchedule() {
    }

    public ProductionSchedule(String scheduleName, String productionLine, Integer targetQuantity, String status,
                              String notes, LocalDate startDate, LocalDate endDate) {
        this.scheduleName = scheduleName;
        this.productionLine = productionLine;
        this.targetQuantity = targetQuantity;
        this.status = status;
        this.notes = notes;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public String getProductionLine() {
        return productionLine;
    }

    public void setProductionLine(String productionLine) {
        this.productionLine = productionLine;
    }

    public Integer getTargetQuantity() {
        return targetQuantity;
    }

    public void setTargetQuantity(Integer targetQuantity) {
        this.targetQuantity = targetQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<ProductionTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<ProductionTask> tasks) {
        this.tasks = tasks;
    }

    public double getAverageProgress() {
        if (tasks == null || tasks.isEmpty()) {
            return 0.0;
        }
        int total = 0;
        int count = 0;
        for (ProductionTask task : tasks) {
            if (task.getProgressPercent() != null) {
                total += Math.max(0, Math.min(task.getProgressPercent(), 100));
                count++;
            }
        }
        return count == 0 ? 0.0 : (double) total / count;
    }
}
