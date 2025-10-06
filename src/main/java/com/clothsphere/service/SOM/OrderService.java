package com.clothsphere.service.SOM;

import com.clothsphere.model.SOM.Order;
import com.clothsphere.repository.SOM.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    // Generate next order ID (PHY01, PHY02, etc. for PHYSICAL type)
    public String generateNextOrderId(String orderType) {
        String maxId = orderRepository.findMaxOrderIdByOrderType(orderType);
        if (maxId == null) {
            return orderType.substring(0, 3).toUpperCase() + "01";
        }

        try {
            String prefix = orderType.substring(0, 3).toUpperCase();
            int number = Integer.parseInt(maxId.substring(3));
            number++;
            return String.format("%s%02d", prefix, number);
        } catch (NumberFormatException e) {
            return orderType.substring(0, 3).toUpperCase() + "01";
        }
    }

    // Calculate total amount with discount
    public Double calculateTotalAmount(Integer quantity, Double unitPrice, Double discountPercentage) {
        Double subtotal = quantity * unitPrice;
        if (discountPercentage != null && discountPercentage > 0) {
            Double discountAmount = subtotal * (discountPercentage / 100);
            return subtotal - discountAmount;
        }
        return subtotal;
    }

    // Create new order (Sales Manager can only create PHYSICAL orders)
    public Order createOrder(Order order, String createdBy) {
        // Set order type to PHYSICAL for sales manager
        order.setOrderType("PHYSICAL");

        // Generate order ID if not provided
        if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
            order.setOrderId(generateNextOrderId(order.getOrderType()));
        }

        // Calculate total amount
        Double totalAmount = calculateTotalAmount(
                order.getQuantity(),
                order.getUnitPrice(),
                order.getDiscountPercentage()
        );
        order.setTotalAmount(totalAmount);

        // Set default values
        order.setStatus("PENDING");
        order.setPlaceDate(LocalDateTime.now());
        order.setCreatedBy(createdBy);

        return orderRepository.save(order);
    }

    // Get all orders
    public List<Order> getAllOrders() {
        return orderRepository.findAllOrderByPlaceDateDesc();
    }

    // Get orders by order type
    public List<Order> getOrdersByType(String orderType) {
        return orderRepository.findByOrderType(orderType);
    }

    // Get order by composite key
    public Optional<Order> getOrderById(String orderType, String orderId) {
        return orderRepository.findByOrderTypeAndOrderId(orderType, orderId);
    }

    // Update order status (Sales Manager can only update from PENDING to IN_PRODUCTION or READY_TO_SHIP)
    public Order updateOrderStatus(String orderType, String orderId, String newStatus, String updatedBy) {
        Optional<Order> existingOrder = orderRepository.findByOrderTypeAndOrderId(orderType, orderId);
        if (existingOrder.isPresent()) {
            Order order = existingOrder.get();

            // Validate status transition
            if (isValidStatusTransition(order.getStatus(), newStatus)) {
                order.setStatus(newStatus);
                return orderRepository.save(order);
            } else {
                throw new RuntimeException("Invalid status transition from " + order.getStatus() + " to " + newStatus);
            }
        }
        throw new RuntimeException("Order not found with Type: " + orderType + " and ID: " + orderId);
    }

    // Validate status transition rules
    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        // Sales Manager can only:
        // - PENDING → IN_PRODUCTION
        // - PENDING → READY_TO_SHIP
        // - IN_PRODUCTION → READY_TO_SHIP
        return (currentStatus.equals("PENDING") && (newStatus.equals("IN_PRODUCTION") || newStatus.equals("READY_TO_SHIP"))) ||
                (currentStatus.equals("IN_PRODUCTION") && newStatus.equals("READY_TO_SHIP"));
    }

    // Update order discount and recalculate total amount
    public Order updateOrderDiscount(String orderType, String orderId, Double discountPercentage, String updatedBy) {
        Optional<Order> existingOrder = orderRepository.findByOrderTypeAndOrderId(orderType, orderId);
        if (existingOrder.isPresent()) {
            Order order = existingOrder.get();

            // Update discount
            order.setDiscountPercentage(discountPercentage);

            // Recalculate total amount
            Double totalAmount = calculateTotalAmount(
                    order.getQuantity(),
                    order.getUnitPrice(),
                    discountPercentage
            );
            order.setTotalAmount(totalAmount);

            return orderRepository.save(order);
        }
        throw new RuntimeException("Order not found with Type: " + orderType + " and ID: " + orderId);
    }

    // Search orders by customer name
    public List<Order> searchOrdersByCustomerName(String customerName) {
        return orderRepository.findByCustomerNameContaining(customerName);
    }

    // Get orders by status
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    // Get orders by order type and status
    public List<Order> getOrdersByTypeAndStatus(String orderType, String status) {
        return orderRepository.findByOrderTypeAndStatus(orderType, status);
    }
}