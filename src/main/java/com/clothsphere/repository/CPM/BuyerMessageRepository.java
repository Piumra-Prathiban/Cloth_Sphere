package com.clothsphere.repository.CPM;

import com.clothsphere.model.CPM.BuyerMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuyerMessageRepository extends JpaRepository<BuyerMessage, String> {

    // Find messages by buyer ID
    List<BuyerMessage> findByBuyerId(String buyerId);

    // Find messages by status
    List<BuyerMessage> findByStatus(String status);

    // Find messages by message type
    List<BuyerMessage> findByMessageType(String messageType);

    // Find messages by priority
    List<BuyerMessage> findByPriority(String priority);

    // Find messages by status and buyer ID
    List<BuyerMessage> findByStatusAndBuyerId(String status, String buyerId);

    // Count messages by status
    long countByStatus(String status);

    // Find all messages ordered by creation date (newest first)
    List<BuyerMessage> findAllByOrderByCreatedAtDesc();

    // Find messages by status ordered by creation date
    List<BuyerMessage> findByStatusOrderByCreatedAtDesc(String status);

    // Find messages by buyer ID ordered by creation date
    List<BuyerMessage> findByBuyerIdOrderByCreatedAtDesc(String buyerId);
}
