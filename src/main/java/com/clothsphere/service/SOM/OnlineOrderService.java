package com.clothsphere.service.SOM;

import com.clothsphere.model.CPM.Product;
import com.clothsphere.model.SOM.Cart;
import com.clothsphere.model.SOM.OnlineOrder;
import com.clothsphere.model.SOM.OnlineOrderItem;
import com.clothsphere.repository.CPM.ProductRepository;
import com.clothsphere.repository.SOM.OnlineOrderRepository;
import com.clothsphere.repository.SOM.OnlineOrderItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OnlineOrderService {

    @Autowired
    private OnlineOrderRepository orderRepository;

    @Autowired
    private OnlineOrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    /**
     * Create order from cart items
     */
    public OnlineOrder createOrderFromCart(String buyerId, OnlineOrder orderDetails) {
        // Get cart items
        List<Cart> cartItems = cartService.getCartItems(buyerId);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Validate stock for all items
        for (Cart cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product == null) {
                throw new RuntimeException("Product not found in cart");
            }
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for " + product.getName());
            }
        }

        // Generate order number
        String orderNumber = generateOrderNumber();
        orderDetails.setOrderNumber(orderNumber);
        orderDetails.setBuyerId(buyerId);
        orderDetails.setStatus("PENDING");
        orderDetails.setPaymentStatus("PENDING");

        // Calculate totals
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Cart cartItem : cartItems) {
            BigDecimal itemTotal = BigDecimal.valueOf(cartItem.getProduct().getPrice())
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(itemTotal);
        }

        orderDetails.setSubtotal(subtotal);

        // Calculate shipping (simple flat rate for now)
        BigDecimal shippingFee = subtotal.compareTo(new BigDecimal("100")) >= 0
                ? BigDecimal.ZERO
                : new BigDecimal("10.00");
        orderDetails.setShippingFee(shippingFee);

        // Calculate tax (10% for example)
        BigDecimal taxAmount = subtotal.multiply(new BigDecimal("0.10"));
        orderDetails.setTaxAmount(taxAmount);

        // Calculate total
        BigDecimal total = subtotal.add(shippingFee).add(taxAmount)
                .subtract(orderDetails.getDiscountAmount() != null ? orderDetails.getDiscountAmount() : BigDecimal.ZERO);
        orderDetails.setTotalAmount(total);

        // Save order
        OnlineOrder savedOrder = orderRepository.save(orderDetails);

        // Create order items and update product stock
        for (Cart cartItem : cartItems) {
            Product product = cartItem.getProduct();

            OnlineOrderItem orderItem = new OnlineOrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductCode(product.getCode());
            orderItem.setProductImagePath(product.getImagePath());
            orderItem.setUnitPrice(BigDecimal.valueOf(product.getPrice()));
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscountPercentage(BigDecimal.ZERO);
            orderItem.calculateItemTotal();

            orderItemRepository.save(orderItem);

            // Update product stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // Clear cart after successful order
        cartService.clearCart(buyerId);

        return savedOrder;
    }

    /**
     * Get all orders for a buyer
     */
    public List<OnlineOrder> getBuyerOrders(String buyerId) {
        return orderRepository.findByBuyerIdOrderByOrderDateDesc(buyerId);
    }

    /**
     * Get order by ID (with authorization check)
     */
    public OnlineOrder getOrderById(Long orderId, String buyerId) {
        Optional<OnlineOrder> order = orderRepository.findById(orderId);

        if (order.isEmpty()) {
            throw new RuntimeException("Order not found");
        }

        if (!order.get().getBuyerId().equals(buyerId)) {
            throw new RuntimeException("Unauthorized access to order");
        }

        return order.get();
    }

    /**
     * Get order with items
     */
    public OnlineOrder getOrderWithItems(Long orderId, String buyerId) {
        OnlineOrder order = getOrderById(orderId, buyerId);
        List<OnlineOrderItem> items = orderItemRepository.findByOrder_OrderId(orderId);
        order.setOrderItems(items);
        return order;
    }

    /**
     * Get order by order number
     */
    public OnlineOrder getOrderByOrderNumber(String orderNumber, String buyerId) {
        Optional<OnlineOrder> order = orderRepository.findByOrderNumber(orderNumber);

        if (order.isEmpty()) {
            throw new RuntimeException("Order not found");
        }

        if (!order.get().getBuyerId().equals(buyerId)) {
            throw new RuntimeException("Unauthorized access to order");
        }

        return order.get();
    }

    /**
     * Get orders by status
     */
    public List<OnlineOrder> getBuyerOrdersByStatus(String buyerId, String status) {
        return orderRepository.findByBuyerIdAndStatusOrderByOrderDateDesc(buyerId, status);
    }

    /**
     * Cancel order (only if pending)
     */
    public OnlineOrder cancelOrder(Long orderId, String buyerId) {
        OnlineOrder order = getOrderById(orderId, buyerId);

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("Only pending orders can be cancelled");
        }

        order.setStatus("CANCELLED");
        order.setCancelledDate(LocalDateTime.now());

        // Restore product stock
        List<OnlineOrderItem> items = orderItemRepository.findByOrder_OrderId(orderId);
        for (OnlineOrderItem item : items) {
            Optional<Product> productOpt = productRepository.findById(item.getProductId());
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        return orderRepository.save(order);
    }

    /**
     * Update order status (for admin/system use)
     */
    public OnlineOrder updateOrderStatus(Long orderId, String newStatus) {
        Optional<OnlineOrder> orderOpt = orderRepository.findById(orderId);

        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Order not found");
        }

        OnlineOrder order = orderOpt.get();
        order.setStatus(newStatus);

        // Update status-specific dates
        switch (newStatus) {
            case "CONFIRMED":
                order.setConfirmedDate(LocalDateTime.now());
                break;
            case "SHIPPED":
                order.setShippedDate(LocalDateTime.now());
                break;
            case "DELIVERED":
                order.setDeliveredDate(LocalDateTime.now());
                break;
            case "CANCELLED":
                order.setCancelledDate(LocalDateTime.now());
                break;
        }

        return orderRepository.save(order);
    }

    /**
     * Get order statistics for buyer
     */
    public long getTotalOrderCount(String buyerId) {
        return orderRepository.countByBuyerId(buyerId);
    }

    public long getPendingOrderCount(String buyerId) {
        return orderRepository.countByBuyerIdAndStatus(buyerId, "PENDING");
    }

    public Double getTotalOrderAmount(String buyerId) {
        Double total = orderRepository.getTotalOrderAmountByBuyer(buyerId);
        return total != null ? total : 0.0;
    }

    /**
     * Generate unique order number
     */
    private String generateOrderNumber() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Find the highest order number for today
        String pattern = "ORD-" + datePrefix + "%";
        List<OnlineOrder> todaysOrders = orderRepository.findAll().stream()
                .filter(o -> o.getOrderNumber().startsWith("ORD-" + datePrefix))
                .toList();

        int sequence = todaysOrders.size() + 1;

        return String.format("ORD-%s-%04d", datePrefix, sequence);
    }

    /**
     * Get recent orders (last 30 days)
     */
    public List<OnlineOrder> getRecentOrders(String buyerId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return orderRepository.findRecentOrdersByBuyer(buyerId, thirtyDaysAgo);
    }
}
