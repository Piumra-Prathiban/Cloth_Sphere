package com.clothsphere.service.buyerPortal;

import com.clothsphere.model.buyerPortal.*;
import com.clothsphere.repository.buyerPortal.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order placeOrder(Order order) {
        if (order.getBuyer() == null) {
            throw new IllegalArgumentException("Order must have a buyer");
        }
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }

        // Generate order number
        order.setOrderNumber(generateOrderNumber());

        // Calculate total
        order.calculateTotalAmount();

        // Set default dates
        order.setOrderDate(LocalDateTime.now());
        order.setEstimatedDeliveryDate(LocalDateTime.now().plusDays(7)); // 7 days default

        return orderRepository.save(order);
    }

    public List<Order> getOrdersByBuyer(Buyer buyer) {
        return orderRepository.findByBuyer(buyer);
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);

        // Set actual delivery date if status is DELIVERED
        if ("DELIVERED".equals(status)) {
            order.setActualDeliveryDate(LocalDateTime.now());
        }

        return orderRepository.save(order);
    }

    public Order updateTrackingNumber(Long orderId, String trackingNumber) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setTrackingNumber(trackingNumber);
        return orderRepository.save(order);
    }

    private String generateOrderNumber() {
        // Format: ORD-YYYYMMDD-XXXX
        String date = LocalDateTime.now().toString().substring(0, 10).replace("-", "");
        long count = orderRepository.count() + 1;
        return String.format("ORD-%s-%04d", date, count);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}


