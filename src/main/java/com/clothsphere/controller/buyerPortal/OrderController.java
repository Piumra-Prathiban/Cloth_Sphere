package com.clothsphere.controller.buyerPortal;

import com.clothsphere.model.buyerPortal.*;
import com.clothsphere.repository.buyerPortal.ProductRepository;
import com.clothsphere.repository.buyerPortal.BuyerRepository;
import com.clothsphere.service.buyerPortal.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private OrderService orderService;

    // Show product detail page
    @GetMapping("/product/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id).orElse(null);
        model.addAttribute("product", product);
        return "product-details";
    }

    // Place order (simplified - single product)
    @PostMapping("/place")
    public String placeOrder(@RequestParam Long productId,
                             @RequestParam int quantity,
                             @RequestParam(required = false) String deliveryAddress,
                             HttpSession session,
                             Model model) {
        Long buyerId = (Long) session.getAttribute("buyerId");
        if (buyerId == null) {
            return "redirect:/login";
        }

        Optional<Buyer> buyerOpt = buyerRepository.findById(buyerId);
        if (!buyerOpt.isPresent()) {
            return "redirect:/login";
        }
        Buyer buyer = buyerOpt.get();

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return "redirect:/catalog";
        }

        // Create order with single item
        Order order = new Order();
        order.setBuyer(buyer);

        // Create order item
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setPrice(product.getPrice());
        item.setProductName(product.getName());
        item.setProductSize(product.getSize());
        item.setProductMaterial(product.getMaterial());

        order.addOrderItem(item);

        // Set delivery address
        if (deliveryAddress != null && !deliveryAddress.isEmpty()) {
            order.setDeliveryAddress(deliveryAddress);
        } else {
            order.setDeliveryAddress(buyer.getAddress());
            order.setDeliveryCity(buyer.getCity());
            order.setDeliveryPostalCode(buyer.getPostalCode());
            order.setDeliveryCountry(buyer.getCountry());
        }
        order.setDeliveryPhone(buyer.getPhone());

        orderService.placeOrder(order);

        model.addAttribute("message", "Order placed successfully!");
        return "redirect:/orders/my";
    }

    // Show buyer's orders
    @GetMapping("/my")
    public String myOrders(HttpSession session, Model model) {
        Long buyerId = (Long) session.getAttribute("buyerId");
        if (buyerId == null) {
            return "redirect:/login";
        }

        Optional<Buyer> buyerOpt = buyerRepository.findById(buyerId);
        if (!buyerOpt.isPresent()) {
            return "redirect:/login";
        }

        model.addAttribute("orders", orderService.getOrdersByBuyer(buyerOpt.get()));
        return "my-orders";
    }

    // Track order
    @GetMapping("/{id}/track")
    public String trackOrder(@PathVariable Long id, HttpSession session, Model model) {
        Long buyerId = (Long) session.getAttribute("buyerId");
        if (buyerId == null) {
            return "redirect:/login";
        }

        Optional<Order> orderOpt = orderService.getOrderById(id);
        if (!orderOpt.isPresent() || !orderOpt.get().getBuyer().getId().equals(buyerId)) {
            return "redirect:/orders/my";
        }

        model.addAttribute("order", orderOpt.get());
        return "order-tracking";
    }
}

