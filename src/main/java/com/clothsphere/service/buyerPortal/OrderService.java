package com.clothsphere.service.buyerPortal;


import com.clothsphere.model.buyerPortal.Order;
import com.clothsphere.model.buyerPortal.Buyer;
import com.clothsphere.repository.buyerPortal.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order placeOrder(Order order) {
        if (order.getBuyer() == null || order.getProduct() == null) {
            throw new IllegalArgumentException("Order must have a buyer and product");
        }
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING"); // later replace with Enum
        return orderRepository.save(order);
    }

    public List<Order> getOrdersByBuyer(Buyer buyer) {
        return orderRepository.findByBuyer(buyer);
    }
}


