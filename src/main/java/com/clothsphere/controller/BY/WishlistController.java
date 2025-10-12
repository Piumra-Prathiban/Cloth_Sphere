package com.clothsphere.controller.BY;

import com.clothsphere.model.BY.OnlineBuyerLogin;
import com.clothsphere.model.BY.Wishlist;
import com.clothsphere.service.BY.WishlistService;
import com.clothsphere.service.BY.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/buyer/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private CartService cartService;

    /**
     * Display wishlist page
     */
    @GetMapping
    public String viewWishlist(HttpSession session, Model model) {
        OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("loggedInBuyer");
        if (buyer == null) {
            return "redirect:/buyer/login";
        }

        List<Wishlist> wishlistItems = wishlistService.getWishlistWithProducts(buyer.getBuyerId());
        int cartCount = cartService.getCartItemCount(buyer.getBuyerId());

        model.addAttribute("buyer", buyer);
        model.addAttribute("wishlistItems", wishlistItems);
        model.addAttribute("cartCount", cartCount);

        return "buyerWishlist";
    }

    /**
     * Add product to wishlist (AJAX)
     */
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToWishlist(
            @RequestParam Long productId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("loggedInBuyer");
            if (buyer == null) {
                response.put("success", false);
                response.put("message", "Please login first");
                return ResponseEntity.ok(response);
            }

            boolean added = wishlistService.addToWishlist(buyer.getBuyerId(), productId);

            if (added) {
                response.put("success", true);
                response.put("message", "Added to wishlist");
                response.put("wishlistCount", wishlistService.getWishlistItemCount(buyer.getBuyerId()));
            } else {
                response.put("success", false);
                response.put("message", "Already in wishlist");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Remove product from wishlist (AJAX)
     */
    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromWishlist(
            @RequestParam Long productId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("loggedInBuyer");
            if (buyer == null) {
                response.put("success", false);
                response.put("message", "Please login first");
                return ResponseEntity.ok(response);
            }

            wishlistService.removeFromWishlist(buyer.getBuyerId(), productId);

            response.put("success", true);
            response.put("message", "Removed from wishlist");
            response.put("wishlistCount", wishlistService.getWishlistItemCount(buyer.getBuyerId()));

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Move product from wishlist to cart (AJAX)
     */
    @PostMapping("/move-to-cart")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> moveToCart(
            @RequestParam Long productId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("loggedInBuyer");
            if (buyer == null) {
                response.put("success", false);
                response.put("message", "Please login first");
                return ResponseEntity.ok(response);
            }

            // Add to cart
            cartService.addToCart(buyer.getBuyerId(), productId, 1);

            // Remove from wishlist
            wishlistService.removeFromWishlist(buyer.getBuyerId(), productId);

            response.put("success", true);
            response.put("message", "Moved to cart");
            response.put("wishlistCount", wishlistService.getWishlistItemCount(buyer.getBuyerId()));
            response.put("cartCount", cartService.getCartItemCount(buyer.getBuyerId()));

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get wishlist count (AJAX)
     */
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getWishlistCount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("loggedInBuyer");
        if (buyer == null) {
            response.put("wishlistCount", 0);
            return ResponseEntity.ok(response);
        }

        int count = wishlistService.getWishlistItemCount(buyer.getBuyerId());
        response.put("wishlistCount", count);

        return ResponseEntity.ok(response);
    }

    /**
     * Check if product is in wishlist (AJAX)
     */
    @GetMapping("/check")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkWishlist(
            @RequestParam Long productId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("loggedInBuyer");
        if (buyer == null) {
            response.put("inWishlist", false);
            return ResponseEntity.ok(response);
        }

        boolean inWishlist = wishlistService.isInWishlist(buyer.getBuyerId(), productId);
        response.put("inWishlist", inWishlist);

        return ResponseEntity.ok(response);
    }
}
