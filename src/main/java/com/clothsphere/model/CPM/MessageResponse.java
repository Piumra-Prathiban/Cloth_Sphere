package com.clothsphere.model.CPM;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message_responses")
public class MessageResponse {

    @Id
    @Column(name = "response_id", length = 15)
    private String responseId;

    @Column(name = "message_id", length = 15, nullable = false)
    private String messageId;

    @Column(name = "response_text", columnDefinition = "VARCHAR(MAX)", nullable = false)
    private String responseText;

    @Column(name = "responded_by", length = 50, nullable = false)
    private String respondedBy;

    @Column(name = "response_date", nullable = false)
    private LocalDateTime responseDate;

    // Constructors
    public MessageResponse() {
        this.responseDate = LocalDateTime.now();
    }

    public MessageResponse(String responseId, String messageId, String responseText, String respondedBy) {
        this.responseId = responseId;
        this.messageId = messageId;
        this.responseText = responseText;
        this.respondedBy = respondedBy;
        this.responseDate = LocalDateTime.now();
    }

    // Getters and Setters
    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public String getRespondedBy() {
        return respondedBy;
    }

    public void setRespondedBy(String respondedBy) {
        this.respondedBy = respondedBy;
    }

    public LocalDateTime getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(LocalDateTime responseDate) {
        this.responseDate = responseDate;
    }

    @Override
    public String toString() {
        return "MessageResponse{" +
                "responseId='" + responseId + '\'' +
                ", messageId='" + messageId + '\'' +
                ", respondedBy='" + respondedBy + '\'' +
                ", responseDate=" + responseDate +
                '}';
    }
}
