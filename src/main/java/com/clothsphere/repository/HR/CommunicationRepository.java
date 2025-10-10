package com.clothsphere.repository.HR;

import com.clothsphere.model.HR.Communication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public interface CommunicationRepository extends JpaRepository<Communication, Integer> {

    // ============================================
    // MANUAL NATIVE QUERIES
    // ============================================

    /**
     * GROUPING WITH HAVING: Get message statistics by role
     * Shows total messages sent and average message length per role
     * Only includes roles that have sent at least 1 message
     */
    @Query(value = """
        SELECT 
            su.role as role,
            COUNT(ic.message_id) as total_messages_sent,
            AVG(LEN(ic.message_text)) as avg_message_length
        FROM system_user_login_details su
        LEFT JOIN internal_communications ic ON su.email = ic.sender_email
        WHERE ic.is_deleted = 0 OR ic.message_id IS NULL
        GROUP BY su.role
        HAVING COUNT(ic.message_id) > 0
        ORDER BY total_messages_sent DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getMessageStatistics();

    /**
     * SUBQUERY: Get active communicators
     * Users who have sent more than 5 messages
     */
    @Query(value = """
        SELECT 
            su.user_name,
            su.role,
            su.email,
            (SELECT COUNT(*) 
             FROM internal_communications 
             WHERE sender_email = su.email AND is_deleted = 0) as message_count
        FROM system_user_login_details su
        WHERE su.email IN (
            SELECT sender_email
            FROM internal_communications
            WHERE is_deleted = 0
            GROUP BY sender_email
            HAVING COUNT(message_id) > 5
        )
        ORDER BY message_count DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getActiveCommunicators();

    /**
     * Get available users for dropdown (excluding current user and applying restrictions)
     */
    @Query(value = """
    SELECT 
        su.email,
        su.user_name as user_name,
        su.role,
        su.phone_number
    FROM system_user_login_details su
    WHERE su.email != :currentUserEmail
    AND (
        :currentUserRole != 'Employee' 
        OR su.role != 'Factory Manager'
    )
    AND su.email IS NOT NULL
    AND su.user_name IS NOT NULL
    ORDER BY su.role, su.user_name
    """, nativeQuery = true)
    List<Map<String, Object>> getAvailableUsers(@Param("currentUserEmail") String currentUserEmail,
                                                @Param("currentUserRole") String currentUserRole);

    /**
     * Get inbox messages - UPDATED to handle NULL values
     */
    @Query(value = """
    SELECT 
        ic.message_id as message_id,
        ic.sender_email as sender_email,
        su_sender.user_name as sender_name,
        su_sender.role as sender_role,
        ic.receiver_email as receiver_email,
        ic.subject as subject,
        CASE 
            WHEN LEN(ic.message_text) > 100 
            THEN SUBSTRING(ic.message_text, 1, 100) + '...'
            ELSE ic.message_text
        END as message_preview,
        ic.sent_date as sent_date,
        COALESCE(ic.is_read, 0) as is_read  -- Handle NULL values
    FROM internal_communications ic
    INNER JOIN system_user_login_details su_sender ON ic.sender_email = su_sender.email
    WHERE ic.receiver_email = :userEmail
       AND COALESCE(ic.is_deleted, 0) = 0  -- Handle NULL values
    ORDER BY ic.sent_date DESC
    """, nativeQuery = true)
    List<Map<String, Object>> getInboxMessages(@Param("userEmail") String userEmail);

    /**
     * Get sent messages - UPDATED to handle NULL values
     */
    @Query(value = """
    SELECT 
        ic.message_id as message_id,
        ic.sender_email as sender_email,
        ic.receiver_email as receiver_email,
        su_receiver.user_name as receiver_name,
        su_receiver.role as receiver_role,
        ic.subject as subject,
        CASE 
            WHEN LEN(ic.message_text) > 100 
            THEN SUBSTRING(ic.message_text, 1, 100) + '...'
            ELSE ic.message_text
        END as message_preview,
        ic.sent_date as sent_date,
        COALESCE(ic.is_read, 0) as is_read  -- Handle NULL values
    FROM internal_communications ic
    INNER JOIN system_user_login_details su_receiver ON ic.receiver_email = su_receiver.email
    WHERE ic.sender_email = :userEmail
      AND COALESCE(ic.is_deleted, 0) = 0  -- Handle NULL values
    ORDER BY ic.sent_date DESC
    """, nativeQuery = true)
    List<Map<String, Object>> getSentMessages(@Param("userEmail") String userEmail);

    /**
     * Get message details by ID - UPDATED to handle NULL values
     */
    @Query(value = """
    SELECT 
        ic.message_id,
        ic.sender_email,
        su_sender.user_name as sender_name,
        su_sender.role as sender_role,
        ic.receiver_email,
        su_receiver.user_name as receiver_name,
        su_receiver.role as receiver_role,
        ic.subject,
        ic.message_text,
        ic.sent_date,
        COALESCE(ic.is_read, 0) as is_read  -- Handle NULL values
    FROM internal_communications ic
    INNER JOIN system_user_login_details su_sender ON ic.sender_email = su_sender.email
    INNER JOIN system_user_login_details su_receiver ON ic.receiver_email = su_receiver.email
    WHERE ic.message_id = :messageId
      AND COALESCE(ic.is_deleted, 0) = 0  -- Handle NULL values
    """, nativeQuery = true)
    Map<String, Object> getMessageById(@Param("messageId") int messageId);

    /**
     * FUNCTION CALL: Get unread message count - WITH FALLBACK LOGIC
     */
    @Query(value = "SELECT dbo.GetUnreadMessageCount(:userEmail)", nativeQuery = true)
    Integer getUnreadMessageCount(@Param("userEmail") String userEmail);

    /**
     * DIRECT QUERY: Get unread message count - UPDATED to handle NULL values
     */
    @Query(value = """
        SELECT COUNT(*) 
        FROM internal_communications 
        WHERE receiver_email = :userEmail 
        AND COALESCE(is_read, 0) = 0  -- Handle NULL values
        AND COALESCE(is_deleted, 0) = 0  -- Handle NULL values
        """, nativeQuery = true)
    int getUnreadMessageCountDirect(@Param("userEmail") String userEmail);

    /**
     * STORED PROCEDURE: Send message - UPDATED TO USE EXEC
     */
    @Query(value = "EXEC SendInternalMessage :senderEmail, :receiverEmail, :subject, :messageText", nativeQuery = true)
    Integer sendMessage(@Param("senderEmail") String senderEmail,
                        @Param("receiverEmail") String receiverEmail,
                        @Param("subject") String subject,
                        @Param("messageText") String messageText);

    /**
     * Alternative manual insert if procedure doesn't work
     */
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO internal_communications 
        (sender_email, receiver_email, subject, message_text, sent_date, is_read, is_deleted)
        VALUES (:senderEmail, :receiverEmail, :subject, :messageText, GETDATE(), 0, 0)
        """, nativeQuery = true)
    void insertMessage(@Param("senderEmail") String senderEmail,
                       @Param("receiverEmail") String receiverEmail,
                       @Param("subject") String subject,
                       @Param("messageText") String messageText);

    /**
     * Mark message as read
     */
    @Modifying
    @Transactional
    @Query(value = """
        UPDATE internal_communications 
        SET is_read = 1 
        WHERE message_id = :messageId
        """, nativeQuery = true)
    void markAsRead(@Param("messageId") int messageId);

    /**
     * Soft delete message
     */
    @Modifying
    @Transactional
    @Query(value = """
        UPDATE internal_communications 
        SET is_deleted = 1 
        WHERE message_id = :messageId
        """, nativeQuery = true)
    void deleteMessage(@Param("messageId") int messageId);

    /**
     * Get conversation between two users - UPDATED to handle NULL values
     */
    @Query(value = """
        SELECT 
            ic.message_id,
            ic.sender_email,
            su_sender.user_name as sender_name,
            ic.receiver_email,
            su_receiver.user_name as receiver_name,
            ic.subject,
            ic.message_text,
            ic.sent_date,
            COALESCE(ic.is_read, 0) as is_read  -- Handle NULL values
        FROM internal_communications ic
        INNER JOIN system_user_login_details su_sender ON ic.sender_email = su_sender.email
        INNER JOIN system_user_login_details su_receiver ON ic.receiver_email = su_receiver.email
        WHERE ((ic.sender_email = :user1 AND ic.receiver_email = :user2)
            OR (ic.sender_email = :user2 AND ic.receiver_email = :user1))
          AND COALESCE(ic.is_deleted, 0) = 0  -- Handle NULL values
        ORDER BY ic.sent_date ASC
        """, nativeQuery = true)
    List<Map<String, Object>> getConversation(@Param("user1") String user1,
                                              @Param("user2") String user2);

    /**
     * Check if communication is allowed
     */
    @Query(value = """
        SELECT CASE 
            WHEN su_sender.role = 'Employee' 
                 AND su_receiver.role = 'Factory Manager' 
            THEN 0
            ELSE 1
        END
        FROM system_user_login_details su_sender, system_user_login_details su_receiver
        WHERE su_sender.email = :senderEmail
          AND su_receiver.email = :receiverEmail
        """, nativeQuery = true)
    Integer canCommunicate(@Param("senderEmail") String senderEmail,
                           @Param("receiverEmail") String receiverEmail);

    /**
     * Get user communication summary
     */
    @Query(value = """
        SELECT 
            :userEmail as user_email,
            (SELECT COUNT(*) FROM internal_communications 
             WHERE sender_email = :userEmail AND is_deleted = 0) as sent_count,
            (SELECT COUNT(*) FROM internal_communications 
             WHERE receiver_email = :userEmail AND is_deleted = 0) as received_count,
            (SELECT COUNT(*) FROM internal_communications 
             WHERE receiver_email = :userEmail AND is_read = 0 AND is_deleted = 0) as unread_count
        """, nativeQuery = true)
    Map<String, Object> getUserCommunicationSummary(@Param("userEmail") String userEmail);
}