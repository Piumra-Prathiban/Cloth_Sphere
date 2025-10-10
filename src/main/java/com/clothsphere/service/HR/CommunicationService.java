package com.clothsphere.service.HR;

import com.clothsphere.model.SystemUser;
import com.clothsphere.repository.HR.CommunicationRepository;
import com.clothsphere.repository.SystemUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class CommunicationService {

    @Autowired
    private CommunicationRepository communicationRepository;

    @Autowired
    private SystemUserRepository systemUserRepository;

    /**
     * Send a new internal message with validation
     */
    public Map<String, Object> sendMessage(String senderEmail, String receiverEmail,
                                           String subject, String messageText) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Validate sender exists
            Optional<SystemUser> senderOpt = systemUserRepository.findByEmail(senderEmail);
            if (senderOpt.isEmpty()) {
                result.put("success", false);
                result.put("message", "Sender not found!");
                return result;
            }

            // Validate receiver exists
            Optional<SystemUser> receiverOpt = systemUserRepository.findByEmail(receiverEmail);
            if (receiverOpt.isEmpty()) {
                result.put("success", false);
                result.put("message", "Receiver not found!");
                return result;
            }

            SystemUser sender = senderOpt.get();
            SystemUser receiver = receiverOpt.get();

            // Check if employee is trying to message factory manager
            if ("Employee".equals(sender.getRole()) && "Factory Manager".equals(receiver.getRole())) {
                result.put("success", false);
                result.put("message", "Employees cannot message Factory Manager directly!");
                return result;
            }

            // Prevent self-messaging
            if (senderEmail.equals(receiverEmail)) {
                result.put("success", false);
                result.put("message", "Cannot send message to yourself!");
                return result;
            }

            // Try using stored procedure
            try {
                Integer procedureResult = communicationRepository.sendMessage(
                        senderEmail, receiverEmail, subject, messageText);

                if (procedureResult != null && procedureResult == 1) {
                    result.put("success", true);
                    result.put("message", "Message sent successfully!");
                } else if (procedureResult != null && procedureResult == -2) {
                    result.put("success", false);
                    result.put("message", "Employees cannot message Factory Manager directly!");
                } else {
                    // If procedure fails, fallback to manual insert
                    communicationRepository.insertMessage(senderEmail, receiverEmail, subject, messageText);
                    result.put("success", true);
                    result.put("message", "Message sent successfully!");
                }
            } catch (Exception procEx) {
                // Fallback to manual insert if procedure fails
                System.out.println("Procedure failed, using manual insert: " + procEx.getMessage());
                communicationRepository.insertMessage(senderEmail, receiverEmail, subject, messageText);
                result.put("success", true);
                result.put("message", "Message sent successfully!");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error sending message: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Get inbox messages with fallback handling
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getInboxMessages(String userEmail) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Map<String, Object>> messages = communicationRepository.getInboxMessages(userEmail);

            // Get unread count with fallback logic
            int unreadCount = 0;
            try {
                Integer count = communicationRepository.getUnreadMessageCount(userEmail);
                unreadCount = count != null ? count : 0;
            } catch (Exception e) {
                // Fallback to direct query if function doesn't exist
                System.out.println("Function failed, using direct query: " + e.getMessage());
                unreadCount = communicationRepository.getUnreadMessageCountDirect(userEmail);
            }

            result.put("success", true);
            result.put("messages", messages);
            result.put("unreadCount", unreadCount);
            result.put("totalCount", messages.size());

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error fetching inbox: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Get sent messages
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSentMessages(String userEmail) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Map<String, Object>> messages = communicationRepository.getSentMessages(userEmail);

            result.put("success", true);
            result.put("messages", messages);
            result.put("totalCount", messages.size());

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error fetching sent messages: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Get message statistics using GROUPING WITH HAVING
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMessageStatistics() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Map<String, Object>> statistics = communicationRepository.getMessageStatistics();

            result.put("success", true);
            result.put("statistics", statistics);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error fetching statistics: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Get active communicators using SUBQUERY
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getActiveCommunicators() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Map<String, Object>> activeUsers = communicationRepository.getActiveCommunicators();

            result.put("success", true);
            result.put("activeUsers", activeUsers);
            result.put("count", activeUsers.size());

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error fetching active communicators: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Get available users for messaging
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAvailableUsers(String currentUserEmail) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<SystemUser> currentUserOpt = systemUserRepository.findByEmail(currentUserEmail);
            if (currentUserOpt.isEmpty()) {
                result.put("success", false);
                result.put("message", "Current user not found!");
                return result;
            }

            SystemUser currentUser = currentUserOpt.get();
            List<Map<String, Object>> users = communicationRepository.getAvailableUsers(
                    currentUserEmail, currentUser.getRole());

            result.put("success", true);
            result.put("users", users);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error fetching available users: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Get message details by ID
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMessageDetails(Integer messageId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Object> message = communicationRepository.getMessageById(messageId);

            if (message != null && !message.isEmpty()) {
                result.put("success", true);
                result.put("message", message);
            } else {
                result.put("success", false);
                result.put("message", "Message not found!");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error fetching message details: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Mark message as read
     */
    public Map<String, Object> markMessageAsRead(Integer messageId) {
        Map<String, Object> result = new HashMap<>();

        try {
            communicationRepository.markAsRead(messageId);
            result.put("success", true);
            result.put("message", "Message marked as read");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error marking message as read: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Delete message (soft delete)
     */
    public Map<String, Object> deleteMessage(Integer messageId) {
        Map<String, Object> result = new HashMap<>();

        try {
            communicationRepository.deleteMessage(messageId);
            result.put("success", true);
            result.put("message", "Message deleted successfully");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error deleting message: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Get conversation between two users
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getConversation(String user1, String user2) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Map<String, Object>> conversation = communicationRepository.getConversation(user1, user2);

            result.put("success", true);
            result.put("conversation", conversation);
            result.put("messageCount", conversation.size());

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error fetching conversation: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Get user communication summary
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserCommunicationSummary(String userEmail) {
        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Object> summary = communicationRepository.getUserCommunicationSummary(userEmail);
            result.put("success", true);
            result.put("summary", summary);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error fetching communication summary: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }
}