package com.clothsphere.controller.buyerPortal;

import com.clothsphere.model.buyerPortal.Buyer;
import com.clothsphere.model.buyerPortal.Order;
import com.clothsphere.model.buyerPortal.Product;
import com.clothsphere.repository.buyerPortal.ProductRepository;
import com.clothsphere.service.buyerPortal.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderService orderService;

    // Show product detail page
    @GetMapping("/product/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id).orElse(null);
        model.addAttribute("product", product);
        return "product-details";
    }

    // Place order
    @PostMapping("/place")
    public String placeOrder(@RequestParam Long productId,
                             @RequestParam int quantity,
                             HttpSession session,
                             Model model) {
        Buyer buyer = (Buyer) session.getAttribute("buyer"); // assume login sets buyer in session
        if (buyer == null) {
            return "redirect:/login";
        }

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return "redirect:/catalog";
        }

        Order order = new Order();
        order.setBuyer(buyer);
        order.setProduct(product);
        order.setQuantity(quantity);

        orderService.placeOrder(order);

        model.addAttribute("message", "Order placed successfully!");
        return "redirect:/orders/my";
    }

    // Show buyer’s orders
    @GetMapping("/my")
    public String myOrders(HttpSession session, Model model) {
        Buyer buyer = (Buyer) session.getAttribute("buyer");
        if (buyer == null) {
            return "redirect:/login";
        }
        model.addAttribute("orders", orderService.getOrdersByBuyer(buyer));
        return "my-orders";
    }
}

