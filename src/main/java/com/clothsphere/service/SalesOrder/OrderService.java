package com.clothsphere.service.SalesOrder;

import com.clothsphere.model.SalesOrder.*;
import com.clothsphere.repository.SalesOrder.OrderRepository;
import com.clothsphere.repository.SalesOrder.BuyerRepository;
import com.clothsphere.repository.SalesOrder.ProductRepository;
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
}