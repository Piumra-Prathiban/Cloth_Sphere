package com.clothsphere.model.CPM;

import com.clothsphere.model.SOM.OnlineBuyerLogin;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "buyer_messages")
public class BuyerMessage {

    @Id
    @Column(name = "message_id", length = 15)
    private String messageId;

    @Column(name = "buyer_id", nullable = false)
    private String buyerId;

    @Column(name = "subject", length = 200, nullable = false)
    private String subject;

    @Column(name = "message_text", columnDefinition = "VARCHAR(MAX)", nullable = false)
    private String messageText;

    @Column(name = "message_type", length = 50, nullable = false)
    private String messageType; // 'inquiry', 'complaint', 'feedback', 'support'

    @Column(name = "priority", length = 20)
    private String priority = "normal"; // 'low', 'normal', 'high', 'urgent'

    @Column(name = "status", length = 20, nullable = false)
    private String status = "open"; // 'open', 'in_progress', 'resolved', 'closed'

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by", length = 50)
    private String resolvedBy;

    // Constructors
    public BuyerMessage() {
        this.createdAt = LocalDateTime.now();
    }

    public BuyerMessage(String messageId, String buyerId, String subject, String messageText,
                       String messageType, String priority) {
        this.messageId = messageId;
        this.buyerId = buyerId;
        this.subject = subject;
        this.messageText = messageText;
        this.messageType = messageType;
        this.priority = priority;
        this.status = "open";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    @Override
    public String toString() {
        return "BuyerMessage{" +
                "messageId='" + messageId + '\'' +
                ", buyerId='" + buyerId + '\'' +
                ", subject='" + subject + '\'' +
                ", messageType='" + messageType + '\'' +
                ", priority='" + priority + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
