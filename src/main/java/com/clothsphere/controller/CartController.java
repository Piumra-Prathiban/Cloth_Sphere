package com.clothsphere.controller;

import com.clothsphere.model.SOM.Cart;
import com.clothsphere.model.SOM.OnlineBuyerLogin;
import com.clothsphere.service.SOM.CartService;
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
@RequestMapping("/buyer/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * Display shopping cart page
     */
    @GetMapping
    public String showCart(HttpSession session, Model model) {
        OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("buyerUser");

        if (buyer == null) {
            return "redirect:/buyer/login";
        }

        // Get cart items with product details
        List<Cart> cartItems = cartService.getCartItems(buyer.getBuyerId());
        BigDecimal subtotal = cartService.calculateCartSubtotal(buyer.getBuyerId());

        model.addAttribute("buyer", buyer);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("cartCount", cartService.getCartItemCount(buyer.getBuyerId()));

        return "buyerCart";
    }

    /**
     * Add product to cart (AJAX endpoint)
     */
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("buyerUser");

            if (buyer == null) {
                response.put("success", false);
                response.put("message", "Please login to add items to cart");
                return ResponseEntity.ok(response);
            }

            Cart cart = cartService.addToCart(buyer.getBuyerId(), productId, quantity);

            response.put("success", true);
            response.put("message", "Product added to cart successfully");
            response.put("cartCount", cartService.getCartItemCount(buyer.getBuyerId()));
            response.put("cart", cart);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Update cart item quantity (AJAX endpoint)
     */
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCartItem(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("buyerUser");

            if (buyer == null) {
                response.put("success", false);
                response.put("message", "Please login");
                return ResponseEntity.ok(response);
            }

            Cart cart = cartService.updateCartItemQuantity(buyer.getBuyerId(), productId, quantity);
            BigDecimal subtotal = cartService.calculateCartSubtotal(buyer.getBuyerId());

            response.put("success", true);
            response.put("message", "Cart updated successfully");
            response.put("cartCount", cartService.getCartItemCount(buyer.getBuyerId()));
            response.put("subtotal", subtotal);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Remove item from cart (AJAX endpoint)
     */
    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromCart(
            @RequestParam Long productId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("buyerUser");

            if (buyer == null) {
                response.put("success", false);
                response.put("message", "Please login");
                return ResponseEntity.ok(response);
            }

            cartService.removeFromCart(buyer.getBuyerId(), productId);
            BigDecimal subtotal = cartService.calculateCartSubtotal(buyer.getBuyerId());

            response.put("success", true);
            response.put("message", "Item removed from cart");
            response.put("cartCount", cartService.getCartItemCount(buyer.getBuyerId()));
            response.put("subtotal", subtotal);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Clear entire cart
     */
    @PostMapping("/clear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearCart(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("buyerUser");

            if (buyer == null) {
                response.put("success", false);
                response.put("message", "Please login");
                return ResponseEntity.ok(response);
            }

            cartService.clearCart(buyer.getBuyerId());

            response.put("success", true);
            response.put("message", "Cart cleared successfully");
            response.put("cartCount", 0);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Get cart count (AJAX endpoint for header icon)
     */
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCartCount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("buyerUser");

        if (buyer == null) {
            response.put("cartCount", 0);
        } else {
            response.put("cartCount", cartService.getCartItemCount(buyer.getBuyerId()));
        }

        return ResponseEntity.ok(response);
    }
}
