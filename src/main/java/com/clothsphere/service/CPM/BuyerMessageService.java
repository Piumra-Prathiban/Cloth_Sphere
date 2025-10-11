package com.clothsphere.service.CPM;

import com.clothsphere.model.CPM.BuyerMessage;
import com.clothsphere.model.CPM.MessageResponse;
import com.clothsphere.repository.CPM.BuyerMessageRepository;
import com.clothsphere.repository.CPM.MessageResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class BuyerMessageService {

    @Autowired
    private BuyerMessageRepository messageRepository;

    @Autowired
    private MessageResponseRepository responseRepository;

    /**
     * Get all messages ordered by creation date (newest first)
     */
    public List<BuyerMessage> getAllMessages() {
        return messageRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get messages by status
     */
    public List<BuyerMessage> getMessagesByStatus(String status) {
        return messageRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * Get messages by type
     */
    public List<BuyerMessage> getMessagesByType(String messageType) {
        return messageRepository.findByMessageType(messageType);
    }

    /**
     * Get message by ID
     */
    public Optional<BuyerMessage> getMessageById(String messageId) {
        return messageRepository.findById(messageId);
    }

    /**
     * Get messages for a specific buyer
     */
    public List<BuyerMessage> getMessagesByBuyerId(String buyerId) {
        return messageRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId);
    }

    /**
     * Create a new message (for buyer to contact support)
     */
    public BuyerMessage createMessage(BuyerMessage message) {
        // Generate message ID if not set
        if (message.getMessageId() == null || message.getMessageId().isEmpty()) {
            message.setMessageId(generateMessageId());
        }
        message.setCreatedAt(LocalDateTime.now());
        message.setStatus("open");
        return messageRepository.save(message);
    }

    /**
     * Update message status
     */
    public BuyerMessage updateMessageStatus(String messageId, String status, String resolvedBy) {
        Optional<BuyerMessage> messageOpt = messageRepository.findById(messageId);

        if (messageOpt.isEmpty()) {
            throw new RuntimeException("Message not found with ID: " + messageId);
        }

        BuyerMessage message = messageOpt.get();
        message.setStatus(status);

        if ("resolved".equals(status) || "closed".equals(status)) {
            message.setResolvedAt(LocalDateTime.now());
            message.setResolvedBy(resolvedBy);
        }

        return messageRepository.save(message);
    }

    /**
     * Update message priority
     */
    public BuyerMessage updateMessagePriority(String messageId, String priority) {
        Optional<BuyerMessage> messageOpt = messageRepository.findById(messageId);

        if (messageOpt.isEmpty()) {
            throw new RuntimeException("Message not found with ID: " + messageId);
        }

        BuyerMessage message = messageOpt.get();
        message.setPriority(priority);
        return messageRepository.save(message);
    }

    /**
     * Add a response to a message
     */
    public MessageResponse addResponse(String messageId, String responseText, String respondedBy) {
        // Check if message exists
        Optional<BuyerMessage> messageOpt = messageRepository.findById(messageId);
        if (messageOpt.isEmpty()) {
            throw new RuntimeException("Message not found with ID: " + messageId);
        }

        // Update message status to 'in_progress' if it's 'open'
        BuyerMessage message = messageOpt.get();
        if ("open".equals(message.getStatus())) {
            message.setStatus("in_progress");
            messageRepository.save(message);
        }

        // Create response
        MessageResponse response = new MessageResponse();
        response.setResponseId(generateResponseId());
        response.setMessageId(messageId);
        response.setResponseText(responseText);
        response.setRespondedBy(respondedBy);
        response.setResponseDate(LocalDateTime.now());

        return responseRepository.save(response);
    }

    /**
     * Get all responses for a message
     */
    public List<MessageResponse> getResponsesByMessageId(String messageId) {
        return responseRepository.findByMessageIdOrderByResponseDateAsc(messageId);
    }

    /**
     * Get message statistics
     */
    public Map<String, Long> getMessageStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", messageRepository.count());
        stats.put("open", messageRepository.countByStatus("open"));
        stats.put("in_progress", messageRepository.countByStatus("in_progress"));
        stats.put("resolved", messageRepository.countByStatus("resolved"));
        stats.put("closed", messageRepository.countByStatus("closed"));
        return stats;
    }

    /**
     * Delete a message
     */
    public void deleteMessage(String messageId) {
        messageRepository.deleteById(messageId);
    }

    /**
     * Generate unique message ID
     */
    private String generateMessageId() {
        long count = messageRepository.count();
        return String.format("MSG%012d", count + 1);
    }

    /**
     * Generate unique response ID
     */
    private String generateResponseId() {
        long count = responseRepository.count();
        return String.format("RES%012d", count + 1);
    }
}
