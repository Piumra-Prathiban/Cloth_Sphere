package com.clothsphere.controller;

import com.clothsphere.model.SOM.OnlineBuyerLogin;
import com.clothsphere.service.SOM.OnlineBuyerLoginService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/buyer")
public class OnlineBuyerAuthController {

    @Autowired
    private OnlineBuyerLoginService buyerLoginService;

    // ========================= Buyer Login/Register Page =========================

    @GetMapping("/login")
    public String showBuyerLoginPage() {
        return "buyerLogin";
    }

    // ========================= Process Buyer Login =========================

    @PostMapping("/login")
    public String processBuyerLogin(
            @RequestParam String username,
            @RequestParam String password,
            Model model,
            HttpSession session) {

        System.out.println("=== BUYER LOGIN ATTEMPT ===");
        System.out.println("Username: " + username);

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "Username and password are required");
            return "buyerLogin";
        }

        OnlineBuyerLogin buyer = buyerLoginService.validateBuyer(username, password);

        if (buyer == null) {
            System.out.println("Login failed for buyer: " + username);
            model.addAttribute("error", "Invalid username or password");
            return "buyerLogin";
        }

        // Update log count
        buyer.setLogCount(buyer.getLogCount() + 1);
        buyerLoginService.updateLogCount(username, buyer.getLogCount());

        // Set session attributes
        session.setAttribute("buyerUser", buyer);
        session.setAttribute("buyerId", buyer.getBuyerId());
        session.setAttribute("buyerUsername", username);
        session.setAttribute("buyerEmail", buyer.getEmail());

        System.out.println("Buyer logged in successfully: " + username);

        // Redirect to buyer dashboard
        return "redirect:/buyer/dashboard";
    }

    // ========================= Process Buyer Registration =========================

    @PostMapping("/register")
    public String processBuyerRegistration(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String email,
            @RequestParam String customerName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String company,
            Model model,
            HttpSession session) {

        System.out.println("=== BUYER REGISTRATION ATTEMPT ===");
        System.out.println("Username: " + username);
        System.out.println("Email: " + email);

        // Validate required fields
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            customerName == null || customerName.trim().isEmpty()) {
            model.addAttribute("error", "All required fields must be filled");
            return "buyerLogin";
        }

        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "buyerLogin";
        }

        // Check if username exists
        if (buyerLoginService.usernameExists(username)) {
            model.addAttribute("error", "Username already exists");
            return "buyerLogin";
        }

        // Check if email exists
        if (buyerLoginService.emailExists(email)) {
            model.addAttribute("error", "Email already exists");
            return "buyerLogin";
        }

        // Create new buyer
        OnlineBuyerLogin newBuyer = new OnlineBuyerLogin();
        newBuyer.setUsername(username);
        newBuyer.setPassword(password); // Will be encrypted in service
        newBuyer.setEmail(email);
        newBuyer.setCustomerName(customerName);
        newBuyer.setPhone(phone);
        newBuyer.setAddress(address);
        newBuyer.setCompany(company);

        boolean registered = buyerLoginService.registerBuyer(newBuyer);

        if (registered) {
            System.out.println("Buyer registered successfully: " + username);
            model.addAttribute("success", "Registration successful! Please login.");
            return "buyerLogin";
        } else {
            System.out.println("Registration failed for buyer: " + username);
            model.addAttribute("error", "Registration failed. Please try again.");
            return "buyerLogin";
        }
    }

    // ========================= Buyer Dashboard =========================

    @GetMapping("/dashboard")
    public String showBuyerDashboard(HttpSession session, Model model) {
        OnlineBuyerLogin buyer = (OnlineBuyerLogin) session.getAttribute("buyerUser");

        if (buyer == null) {
            System.out.println("No buyer in session, redirecting to login");
            return "redirect:/buyer/login";
        }

        model.addAttribute("buyer", buyer);
        return "buyerDashboard";
    }

    // ========================= Buyer Logout =========================

    @GetMapping("/logout")
    public String buyerLogout(HttpSession session) {
        System.out.println("=== BUYER LOGOUT ===");

        if (session != null) {
            session.invalidate();
        }

        System.out.println("Buyer logged out successfully");
        return "redirect:/buyer/login?logout=true";
    }

    @PostMapping("/logout")
    public String buyerLogoutPost(HttpSession session) {
        return buyerLogout(session);
    }
}
