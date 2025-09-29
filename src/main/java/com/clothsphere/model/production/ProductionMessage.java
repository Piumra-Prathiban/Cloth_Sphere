package com.clothsphere.model.production;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "production_message")
public class ProductionMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private ProductionSchedule schedule;

    @Column(name = "subject", nullable = false, length = 120)
    private String subject;

    @Column(name = "message_body", nullable = false, length = 1000)
    private String messageBody;

    @Column(name = "severity", length = 20)
    private String severity;

    @Column(name = "created_by", length = 80)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ProductionMessage() {
    }

    public ProductionMessage(ProductionSchedule schedule, String subject, String messageBody, String severity, String createdBy) {
        this.schedule = schedule;
        this.subject = subject;
        this.messageBody = messageBody;
        this.severity = severity;
        this.createdBy = createdBy;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
