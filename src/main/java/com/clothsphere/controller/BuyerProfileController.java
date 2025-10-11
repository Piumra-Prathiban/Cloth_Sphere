package com.clothsphere.controller;

import com.clothsphere.model.SOM.OnlineBuyerLogin;
import com.clothsphere.service.SOM.BuyerProfileService;
import com.clothsphere.service.SOM.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/buyer/profile")
public class BuyerProfileController {

    @Autowired
    private BuyerProfileService profileService;

    @Autowired
    private CartService cartService;

    /**
     * Display profile page
     */
    @GetMapping
    public String viewProfile(HttpSession session, Model model) {
        OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("loggedInBuyer");
        if (buyer == null) {
            return "redirect:/buyer/login";
        }

        // Refresh buyer data from database
        Optional<OnlineBuyerLogin> buyerOpt = profileService.getBuyerProfile(buyer.getBuyerId());
        if (buyerOpt.isPresent()) {
            buyer = buyerOpt.get();
            session.setAttribute("loggedInBuyer", buyer); // Update session
        }

        int cartCount = cartService.getCartItemCount(buyer.getBuyerId());

        model.addAttribute("buyer", buyer);
        model.addAttribute("cartCount", cartCount);

        return "buyerProfile";
    }

    /**
     * Update profile information (AJAX)
     */
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestParam String customerName,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("loggedInBuyer");
            if (buyer == null) {
                response.put("success", false);
                response.put("message", "Please login first");
                return ResponseEntity.ok(response);
            }

            // Update profile
            OnlineBuyerLogin updatedBuyer = profileService.updateProfile(
                buyer.getBuyerId(),
                customerName,
                email,
                phone,
                address
            );

            // Update session
            session.setAttribute("loggedInBuyer", updatedBuyer);

            response.put("success", true);
            response.put("message", "Profile updated successfully");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Change password (AJAX)
     */
    @PostMapping("/change-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("loggedInBuyer");
            if (buyer == null) {
                response.put("success", false);
                response.put("message", "Please login first");
                return ResponseEntity.ok(response);
            }

            // Validate new password
            if (!newPassword.equals(confirmPassword)) {
                response.put("success", false);
                response.put("message", "New passwords do not match");
                return ResponseEntity.ok(response);
            }

            // Check password strength
            if (!profileService.isPasswordStrong(newPassword)) {
                response.put("success", false);
                response.put("message", "Password must be at least 8 characters with uppercase, lowercase, and number");
                return ResponseEntity.ok(response);
            }

            // Change password
            boolean changed = profileService.changePassword(buyer.getBuyerId(), currentPassword, newPassword);

            if (changed) {
                response.put("success", true);
                response.put("message", "Password changed successfully");
            } else {
                response.put("success", false);
                response.put("message", "Current password is incorrect");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Update address only (AJAX)
     */
    @PostMapping("/update-address")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateAddress(
            @RequestParam String address,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("loggedInBuyer");
            if (buyer == null) {
                response.put("success", false);
                response.put("message", "Please login first");
                return ResponseEntity.ok(response);
            }

            OnlineBuyerLogin updatedBuyer = profileService.updateAddress(
                buyer.getBuyerId(),
                address
            );

            // Update session
            session.setAttribute("loggedInBuyer", updatedBuyer);

            response.put("success", true);
            response.put("message", "Address updated successfully");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}
