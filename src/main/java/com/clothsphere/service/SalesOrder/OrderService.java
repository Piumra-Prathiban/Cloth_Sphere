package com.clothsphere.service.SalesOrder;

import com.clothsphere.model.SalesOrder.*;
import com.clothsphere.repository.SalesOrder.OrderRepository;
import com.clothsphere.repository.SalesOrder.BuyerRepository;
import com.clothsphere.repository.SalesOrder.ProductRepository;
import com.clothsphere.repository.SalesOrder.OrderMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderMessageRepository orderMessageRepository;

    // Create simple order (corrected to match your classes)
    public Order createSimpleOrder(String buyerName, String buyerEmail, String buyerPhone,
                                   String buyerAddress, String productDetails, Integer quantity,
                                   BigDecimal unitPrice, LocalDate deliveryDeadline, String notes) {

        // Find or create buyer
        Buyer buyer = buyerRepository.findByEmail(buyerEmail)
                .orElse(new Buyer());

        // Set buyer details using your actual setters
        buyer.setName(buyerName);
        buyer.setEmail(buyerEmail);
        buyer.setPhone(buyerPhone);
        buyer.setAddress(buyerAddress);
        buyer.setIsActive(true);

        if (buyer.getId() == null) {
            buyer = buyerRepository.save(buyer);
        }

        // Create order
        Order order = new Order(buyer, deliveryDeadline);
        order.setNotes(notes);

        // Create simple product using your actual setters
        Product product = new Product();
        product.setName("Custom Product - " + System.currentTimeMillis());
        product.setDescription(productDetails);
        product.setCategory("GENERAL");
        // Convert BigDecimal to Double for your price field
        product.setPrice(unitPrice.doubleValue());
        product.setStockQuantity(quantity);

        Product savedProduct = productRepository.save(product);

        // Create order item
        OrderItem orderItem = new OrderItem(order, savedProduct, quantity, unitPrice);
        order.addOrderItem(orderItem);

        return orderRepository.save(order);
    }

    public Order createOrder(Order order) {
        if (order.getBuyer() != null && order.getBuyer().getId() == null) {
            Buyer savedBuyer = buyerRepository.save(order.getBuyer());
            order.setBuyer(savedBuyer);
        }

        if (order.getOrderStatus() == null) {
            order.setOrderStatus(OrderStatus.PENDING);
        }

        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Optional<Order> getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    public Order updateOrder(Order order) {
        if (order.getId() == null) {
            throw new IllegalArgumentException("Order ID cannot be null for update");
        }
        return orderRepository.save(order);
    }

    public boolean deleteOrder(Long id) {
        if (orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Order updateOrderStatus(Long id, OrderStatus newStatus) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setOrderStatus(newStatus);
            return orderRepository.save(order);
        }
        throw new IllegalArgumentException("Order not found with ID: " + id);
    }

    public List<Order> searchOrders(String buyerName, OrderStatus orderStatus,
                                    LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        return orderRepository.searchOrders(buyerName, orderStatus, startDateTime, endDateTime);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByOrderStatus(status);
    }

    public List<Order> getOrdersByBuyerName(String buyerName) {
        return orderRepository.findByBuyerNameContaining(buyerName);
    }

    public List<Order> getOverdueOrders() {
        List<OrderStatus> excludedStatuses = Arrays.asList(
                OrderStatus.DELIVERED, OrderStatus.CANCELLED
        );
        return orderRepository.findOverdueOrders(LocalDate.now(), excludedStatuses);
    }

    public List<Order> getRecentOrders() {
        return orderRepository.findTop10ByOrderByOrderDateDesc();
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalOrders = orderRepository.count();
        stats.put("totalOrders", totalOrders);

        for (OrderStatus status : OrderStatus.values()) {
            long count = orderRepository.countByOrderStatus(status);
            stats.put(status.name().toLowerCase() + "Count", count);
        }

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        List<Order> todayOrders = orderRepository.findByOrderDateBetween(todayStart, todayEnd);
        stats.put("todayOrdersCount", todayOrders.size());

        return stats;
    }

    public Map<String, Object> generateSalesReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Map<String, Object> report = new HashMap<>();

        List<Order> orders = orderRepository.findByOrderDateBetween(startDateTime, endDateTime);

        BigDecimal totalSales = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.put("totalSalesAmount", totalSales);

        report.put("totalOrdersCount", orders.size());

        Map<OrderStatus, Long> statusBreakdown = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            long count = orders.stream()
                    .filter(order -> order.getOrderStatus() == status)
                    .count();
            statusBreakdown.put(status, count);
        }
        report.put("statusBreakdown", statusBreakdown);

        BigDecimal averageOrderValue = orders.isEmpty() ? BigDecimal.ZERO :
                totalSales.divide(BigDecimal.valueOf(orders.size()), 2, BigDecimal.ROUND_HALF_UP);
        report.put("averageOrderValue", averageOrderValue);

        return report;
    }

    // ========== MESSAGING FUNCTIONALITY ==========

    /**
     * Send an order update message to the buyer
     */
    public OrderMessage sendOrderUpdateMessage(Long orderId, String message, String senderName, String senderType) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new IllegalArgumentException("Order not found with ID: " + orderId);
        }

        Order order = orderOpt.get();
        OrderMessage orderMessage = new OrderMessage(order, message, senderName, senderType);

        // Set recipient information
        if (order.getBuyer() != null) {
            orderMessage.setRecipientName(order.getBuyer().getName());
            orderMessage.setRecipientEmail(order.getBuyer().getEmail());
        }

        orderMessage.setMessageType(MessageType.STATUS_UPDATE);

        return orderMessageRepository.save(orderMessage);
    }

    /**
     * Send a status change notification
     */
    public OrderMessage sendStatusChangeNotification(Long orderId, OrderStatus oldStatus, OrderStatus newStatus, String senderName) {
        String message = String.format(
            "Order status updated from '%s' to '%s'. %s",
            oldStatus.getDisplayName(),
            newStatus.getDisplayName(),
            newStatus.getDescription()
        );

        return sendOrderUpdateMessage(orderId, message, senderName, "SYSTEM");
    }

    /**
     * Send a custom message for an order
     */
    public OrderMessage sendCustomMessage(Long orderId, String message, String senderName,
                                          String senderType, MessageType messageType) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new IllegalArgumentException("Order not found with ID: " + orderId);
        }

        Order order = orderOpt.get();
        OrderMessage orderMessage = new OrderMessage(order, message, senderName, senderType);

        // Set recipient information
        if (order.getBuyer() != null) {
            orderMessage.setRecipientName(order.getBuyer().getName());
            orderMessage.setRecipientEmail(order.getBuyer().getEmail());
        }

        orderMessage.setMessageType(messageType);

        return orderMessageRepository.save(orderMessage);
    }

    /**
     * Get all messages for an order
     */
    public List<OrderMessage> getOrderMessages(Long orderId) {
        return orderMessageRepository.findByOrder_IdOrderByCreatedAtDesc(orderId);
    }

    /**
     * Get unread messages for an order
     */
    public List<OrderMessage> getUnreadOrderMessages(Long orderId) {
        return orderMessageRepository.findUnreadMessagesByOrderId(orderId);
    }

    /**
     * Mark a message as read
     */
    public void markMessageAsRead(Long messageId) {
        Optional<OrderMessage> messageOpt = orderMessageRepository.findById(messageId);
        if (messageOpt.isPresent()) {
            OrderMessage message = messageOpt.get();
            message.markAsRead();
            orderMessageRepository.save(message);
        }
    }

    /**
     * Get count of unread messages for an order
     */
    public long getUnreadMessageCount(Long orderId) {
        return orderMessageRepository.countUnreadMessagesByOrderId(orderId);
    }

    /**
     * Get recent messages across all orders
     */
    public List<OrderMessage> getRecentMessages() {
        return orderMessageRepository.findTop20ByOrderByCreatedAtDesc();
    }

    /**
     * Send delivery update message
     */
    public OrderMessage sendDeliveryUpdate(Long orderId, String deliveryInfo, String senderName) {
        String message = "Delivery Update: " + deliveryInfo;
        return sendCustomMessage(orderId, message, senderName, "SALES_EXECUTIVE", MessageType.DELIVERY_UPDATE);
    }

    /**
     * Send urgent message
     */
    public OrderMessage sendUrgentMessage(Long orderId, String urgentMessage, String senderName) {
        String message = "URGENT: " + urgentMessage;
        return sendCustomMessage(orderId, message, senderName, "SALES_EXECUTIVE", MessageType.URGENT);
    }

    /**
     * Enhanced update order status with automatic messaging
     */
    public Order updateOrderStatusWithNotification(Long id, OrderStatus newStatus, String updatedBy) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (!orderOpt.isPresent()) {
            throw new IllegalArgumentException("Order not found with ID: " + id);
        }

        Order order = orderOpt.get();
        OrderStatus oldStatus = order.getOrderStatus();

        // Update the status
        order.setOrderStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        // Send automatic notification
        sendStatusChangeNotification(id, oldStatus, newStatus, updatedBy);

        return updatedOrder;
    }
}