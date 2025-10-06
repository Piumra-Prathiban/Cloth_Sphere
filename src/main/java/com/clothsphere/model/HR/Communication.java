package com.clothsphere.model.HR;

import com.clothsphere.model.SystemUser;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "internal_communications")
public class Communication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Integer messageId;

    @Column(name = "sender_email", nullable = false, length = 100)
    private String senderEmail;

    @Column(name = "receiver_email", nullable = false, length = 100)
    private String receiverEmail;

    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @Column(name = "message_text", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String messageText;

    @Column(name = "sent_date")
    private LocalDateTime sentDate;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_email", referencedColumnName = "email", insertable = false, updatable = false)
    private SystemUser sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_email", referencedColumnName = "email", insertable = false, updatable = false)
    private SystemUser receiver;

    // Constructors
    public Communication() {
        this.sentDate = LocalDateTime.now();
        this.isRead = false;
        this.isDeleted = false;
    }

    public Communication(String senderEmail, String receiverEmail, String subject, String messageText) {
        this();
        this.senderEmail = senderEmail;
        this.receiverEmail = receiverEmail;
        this.subject = subject;
        this.messageText = messageText;
    }

    // Getters and Setters
    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
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

    public LocalDateTime getSentDate() {
        return sentDate;
    }

    public void setSentDate(LocalDateTime sentDate) {
        this.sentDate = sentDate;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public SystemUser getSender() {
        return sender;
    }

    public void setSender(SystemUser sender) {
        this.sender = sender;
    }

    public SystemUser getReceiver() {
        return receiver;
    }

    public void setReceiver(SystemUser receiver) {
        this.receiver = receiver;
    }

    // Utility methods
    public String getMessagePreview() {
        if (messageText == null || messageText.length() <= 100) {
            return messageText;
        }
        return messageText.substring(0, 100) + "...";
    }

    public boolean canEmployeeSendToFactoryManager() {
        // Check if sender is employee and receiver is factory manager
        if (sender != null && receiver != null) {
            return !("Employee".equals(sender.getRole()) && "Factory Manager".equals(receiver.getRole()));
        }
        return true;
    }

    @Override
    public String toString() {
        return "Communication{" +
                "messageId=" + messageId +
                ", senderEmail='" + senderEmail + '\'' +
                ", receiverEmail='" + receiverEmail + '\'' +
                ", subject='" + subject + '\'' +
                ", sentDate=" + sentDate +
                ", isRead=" + isRead +
                '}';
    }
}