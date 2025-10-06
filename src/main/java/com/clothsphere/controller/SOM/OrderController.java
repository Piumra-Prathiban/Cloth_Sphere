package com.clothsphere.controller.SOM;

import com.clothsphere.model.SOM.Order;
import com.clothsphere.service.SOM.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:8080")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Create new order (Sales Manager - only PHYSICAL orders)
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order,
                                         @RequestParam String createdBy) {
        try {
            Order createdOrder = orderService.createOrder(order, createdBy);
            return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    // Get all orders
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    // Get orders by type
    @GetMapping("/type/{orderType}")
    public ResponseEntity<List<Order>> getOrdersByType(@PathVariable String orderType) {
        List<Order> orders = orderService.getOrdersByType(orderType);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    // Get order by ID
    @GetMapping("/{orderType}/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable String orderType, @PathVariable String orderId) {
        Optional<Order> order = orderService.getOrderById(orderType, orderId);
        if (order.isPresent()) {
            return new ResponseEntity<>(order.get(), HttpStatus.OK);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Order not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    // Update order status
    @PutMapping("/{orderType}/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable String orderType,
                                               @PathVariable String orderId,
                                               @RequestParam String newStatus,
                                               @RequestParam String updatedBy) {
        try {
            Order updatedOrder = orderService.updateOrderStatus(orderType, orderId, newStatus, updatedBy);
            return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    // Update order discount
    @PutMapping("/{orderType}/{orderId}/discount")
    public ResponseEntity<?> updateOrderDiscount(@PathVariable String orderType,
                                                 @PathVariable String orderId,
                                                 @RequestParam Double discountPercentage,
                                                 @RequestParam String updatedBy) {
        try {
            Order updatedOrder = orderService.updateOrderDiscount(orderType, orderId, discountPercentage, updatedBy);
            return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    // Search orders by customer name
    @GetMapping("/search")
    public ResponseEntity<List<Order>> searchOrders(@RequestParam String customerName) {
        List<Order> orders = orderService.searchOrdersByCustomerName(customerName);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    // Get orders by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable String status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    // Get next order ID
    @GetMapping("/next-id/{orderType}")
    public ResponseEntity<Map<String, String>> getNextOrderId(@PathVariable String orderType) {
        String nextId = orderService.generateNextOrderId(orderType);
        Map<String, String> response = new HashMap<>();
        response.put("nextOrderId", nextId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}