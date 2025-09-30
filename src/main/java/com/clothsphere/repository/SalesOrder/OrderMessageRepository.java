package com.clothsphere.repository.SalesOrder;

import com.clothsphere.model.SalesOrder.Order;
import com.clothsphere.model.SalesOrder.OrderMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderMessageRepository extends JpaRepository<OrderMessage, Long> {

    // Find all messages for a specific order
    List<OrderMessage> findByOrderOrderByCreatedAtDesc(Order order);

    // Find all messages for a specific order by ID
    List<OrderMessage> findByOrder_IdOrderByCreatedAtDesc(Long orderId);

    // Find unread messages for an order
    @Query("SELECT om FROM OrderMessage om WHERE om.order.id = :orderId AND om.isRead = false ORDER BY om.createdAt DESC")
    List<OrderMessage> findUnreadMessagesByOrderId(@Param("orderId") Long orderId);

    // Find messages by sender type
    List<OrderMessage> findBySenderTypeOrderByCreatedAtDesc(String senderType);

    // Find messages by recipient email
    List<OrderMessage> findByRecipientEmailOrderByCreatedAtDesc(String recipientEmail);

    // Count unread messages for an order
    @Query("SELECT COUNT(om) FROM OrderMessage om WHERE om.order.id = :orderId AND om.isRead = false")
    long countUnreadMessagesByOrderId(@Param("orderId") Long orderId);

    // Get recent messages across all orders
    List<OrderMessage> findTop20ByOrderByCreatedAtDesc();
}