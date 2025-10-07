package com.clothsphere.service.SOM;

import com.clothsphere.model.SOM.Order;
import com.clothsphere.repository.SOM.OrderRepository;
import com.clothsphere.service.PM.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    // FIXED: Generate next order ID with proper prefix handling
    public String generateNextOrderId(String orderType) {
        // Get all orders of this type
        String maxId = orderRepository.findMaxOrderIdByOrderType(orderType);

        // Determine prefix based on order type
        String prefix;
        if (orderType.equals("PHYSICAL")) {
            prefix = "PHY";
        } else if (orderType.equals("ONLINE")) {
            prefix = "ONL";
        } else {
            prefix = orderType.substring(0, Math.min(3, orderType.length())).toUpperCase();
        }

        // If no orders exist yet, start from 01
        if (maxId == null || maxId.isEmpty()) {
            return prefix + "01";
        }

        try {
            // Extract the numeric part from the order ID
            // Handles both PHY01, PHY02 and ONL01, ONL02 formats
            String numericPart = maxId.replaceAll("[^0-9]", "");

            if (numericPart.isEmpty()) {
                return prefix + "01";
            }

            int number = Integer.parseInt(numericPart);
            number++;

            // Format with leading zeros (2 digits)
            return String.format("%s%02d", prefix, number);

        } catch (NumberFormatException e) {
            System.err.println("Error parsing order ID: " + maxId);
            return prefix + "01";
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

    // Create order with proper ID generation
    public Order createOrder(Order order, String createdBy) {
        try {
            // Extract product ID from order notes
            String productId = extractProductIdFromOrder(order);

            System.out.println("Creating order for product ID: " + productId);
            System.out.println("Order quantity: " + order.getQuantity());

            // Validate stock before creating order
            if (productId != null && !productId.equals("UNKNOWN")) {
                Integer availableStock = productService.getProductStock(productId);
                System.out.println("Available stock: " + availableStock);

                if (availableStock < order.getQuantity()) {
                    throw new RuntimeException("Insufficient stock. Available: " + availableStock + ", Requested: " + order.getQuantity());
                }
            }

            // Set order type to PHYSICAL for sales manager
            order.setOrderType("PHYSICAL");

            // CRITICAL FIX: Always generate a NEW order ID
            String nextOrderId = generateNextOrderId(order.getOrderType());
            order.setOrderId(nextOrderId);
            System.out.println("Generated NEW order ID: " + nextOrderId);

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

            // Save the order
            Order savedOrder = orderRepository.save(order);
            System.out.println("Order saved successfully: " + savedOrder.getOrderId());

            // Update stock after successful order creation
            if (productId != null && !productId.equals("UNKNOWN")) {
                System.out.println("Updating stock for product: " + productId + ", quantity: " + order.getQuantity());
                boolean stockUpdated = productService.updateProductStock(productId, order.getQuantity());
                if (stockUpdated) {
                    System.out.println("Stock updated successfully");
                } else {
                    System.out.println("Warning: Stock update failed for product: " + productId);
                }
            }

            return savedOrder;

        } catch (Exception e) {
            System.err.println("Error creating order: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create order: " + e.getMessage());
        }
    }

    // Extract product ID from order notes
    private String extractProductIdFromOrder(Order order) {
        if (order.getOrderNotes() == null) {
            return "UNKNOWN";
        }

        String notes = order.getOrderNotes();
        System.out.println("Extracting product ID from notes: " + notes);

        // Look for "ProductId: P01" pattern
        if (notes.contains("ProductId:")) {
            try {
                String[] parts = notes.split("ProductId:");
                if (parts.length > 1) {
                    String productIdPart = parts[1].split(",")[0].trim();
                    System.out.println("Found product ID in notes: " + productIdPart);
                    return productIdPart;
                }
            } catch (Exception e) {
                System.err.println("Error extracting product ID from notes: " + e.getMessage());
            }
        }

        // Look for product code as fallback
        if (notes.contains("Code:")) {
            try {
                String[] parts = notes.split("Code:");
                if (parts.length > 1) {
                    String code = parts[1].split(",")[0].trim();
                    System.out.println("Using code as product ID: " + code);
                    return code;
                }
            } catch (Exception e) {
                System.err.println("Error extracting code from notes: " + e.getMessage());
            }
        }

        System.out.println("Could not extract product ID from notes");
        return "UNKNOWN";
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

    // Update order status
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