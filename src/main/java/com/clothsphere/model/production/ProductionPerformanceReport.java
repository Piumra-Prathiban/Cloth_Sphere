package com.clothsphere.model.production;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "production_performance_report")
public class ProductionPerformanceReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private ProductionSchedule schedule;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "tasks_completed")
    private int tasksCompleted;

    @Column(name = "tasks_pending")
    private int tasksPending;

    @Column(name = "tasks_overdue")
    private int tasksOverdue;

    @Column(name = "productivity_score")
    private double productivityScore;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    public ProductionPerformanceReport() {
    }

    public ProductionPerformanceReport(ProductionSchedule schedule, LocalDate periodStart, LocalDate periodEnd,
                                       int tasksCompleted, int tasksPending, int tasksOverdue, double productivityScore) {
        this.schedule = schedule;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.tasksCompleted = tasksCompleted;
        this.tasksPending = tasksPending;
        this.tasksOverdue = tasksOverdue;
        this.productivityScore = productivityScore;
    }

    @PrePersist
    public void onCreate() {
        this.generatedAt = LocalDateTime.now();
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

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public int getTasksCompleted() {
        return tasksCompleted;
    }

    public void setTasksCompleted(int tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
    }

    public int getTasksPending() {
        return tasksPending;
    }

    public void setTasksPending(int tasksPending) {
        this.tasksPending = tasksPending;
    }

    public int getTasksOverdue() {
        return tasksOverdue;
    }

    public void setTasksOverdue(int tasksOverdue) {
        this.tasksOverdue = tasksOverdue;
    }

    public double getProductivityScore() {
        return productivityScore;
    }

    public void setProductivityScore(double productivityScore) {
        this.productivityScore = productivityScore;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
