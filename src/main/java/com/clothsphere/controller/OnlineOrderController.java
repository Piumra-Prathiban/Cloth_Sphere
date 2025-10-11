package com.clothsphere.controller;

import com.clothsphere.model.SOM.OnlineBuyerLogin;
import com.clothsphere.model.SOM.OnlineOrder;
import com.clothsphere.service.SOM.CartService;
import com.clothsphere.service.SOM.OnlineOrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/buyer/orders")
public class OnlineOrderController {

    @Autowired
    private OnlineOrderService orderService;

    @Autowired
    private CartService cartService;

    /**
     * Display checkout page
     */
    @GetMapping("/checkout")
    public String showCheckoutPage(HttpSession session, Model model) {
        OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("loggedInBuyer");

        if (buyer == null) {
            return "redirect:/buyer/login";
        }

        // Check if cart is empty
        int cartCount = cartService.getCartItemCount(buyer.getBuyerId());
        if (cartCount == 0) {
            return "redirect:/buyer/cart";
        }

        // Get cart items and calculate totals
        BigDecimal subtotal = cartService.calculateCartSubtotal(buyer.getBuyerId());
        BigDecimal shippingFee = subtotal.compareTo(new BigDecimal("100")) >= 0
                ? BigDecimal.ZERO
                : new BigDecimal("10.00");
        BigDecimal taxAmount = subtotal.multiply(new BigDecimal("0.10"));
        BigDecimal total = subtotal.add(shippingFee).add(taxAmount);

        model.addAttribute("buyer", buyer);
        model.addAttribute("cartItems", cartService.getCartItems(buyer.getBuyerId()));
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("taxAmount", taxAmount);
        model.addAttribute("total", total);
        model.addAttribute("cartCount", cartCount);

        return "buyerCheckout";
    }

    /**
     * Place order
     */
    @PostMapping("/place")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> placeOrder(
            @ModelAttribute OnlineOrder orderDetails,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("loggedInBuyer");

            if (buyer == null) {
                response.put("success", false);
                response.put("message", "Please login to place order");
                return ResponseEntity.ok(response);
            }

            // Create order
            OnlineOrder order = orderService.createOrderFromCart(buyer.getBuyerId(), orderDetails);

            response.put("success", true);
            response.put("message", "Order placed successfully!");
            response.put("orderNumber", order.getOrderNumber());
            response.put("orderId", order.getOrderId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Display My Orders page
     */
    @GetMapping
    public String showOrdersPage(HttpSession session, Model model) {
        OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("loggedInBuyer");

        if (buyer == null) {
            return "redirect:/buyer/login";
        }

        List<OnlineOrder> orders = orderService.getBuyerOrders(buyer.getBuyerId());

        model.addAttribute("buyer", buyer);
        model.addAttribute("orders", orders);
        model.addAttribute("cartCount", cartService.getCartItemCount(buyer.getBuyerId()));

        return "buyerOrders";
    }

    /**
     * Display single order details
     */
    @GetMapping("/{orderId}")
    public String showOrderDetails(@PathVariable Long orderId, HttpSession session, Model model) {
        OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("loggedInBuyer");

        if (buyer == null) {
            return "redirect:/buyer/login";
        }

        try {
            OnlineOrder order = orderService.getOrderWithItems(orderId, buyer.getBuyerId());

            model.addAttribute("buyer", buyer);
            model.addAttribute("order", order);
            model.addAttribute("cartCount", cartService.getCartItemCount(buyer.getBuyerId()));

            return "buyerOrderDetails";

        } catch (Exception e) {
            return "redirect:/buyer/orders";
        }
    }

    /**
     * Cancel order
     */
    @PostMapping("/{orderId}/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @PathVariable Long orderId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("loggedInBuyer");

            if (buyer == null) {
                response.put("success", false);
                response.put("message", "Please login");
                return ResponseEntity.ok(response);
            }

            OnlineOrder order = orderService.cancelOrder(orderId, buyer.getBuyerId());

            response.put("success", true);
            response.put("message", "Order cancelled successfully");
            response.put("status", order.getStatus());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Get order status (AJAX)
     */
    @GetMapping("/{orderId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOrderStatus(
            @PathVariable Long orderId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("loggedInBuyer");

            if (buyer == null) {
                response.put("success", false);
                response.put("message", "Please login");
                return ResponseEntity.ok(response);
            }

            OnlineOrder order = orderService.getOrderById(orderId, buyer.getBuyerId());

            response.put("success", true);
            response.put("status", order.getStatus());
            response.put("paymentStatus", order.getPaymentStatus());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
