package com.clothsphere.controller;

import com.clothsphere.model.SystemUser;
import com.clothsphere.service.SystemUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @Autowired
    private SystemUserService systemUserService;

    // Landing page
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/getstart")
    public String getStart() {
        return "getstart";
    }

    // ========================= LOGIN =========================

    // Login page
    @GetMapping("/systemUserLogin")
    public String showSystemUserLoginPage() {
        return "systemUserLogin";
    }

    // Process login
    @PostMapping("/systemUserLogin")
    public String processSystemUserLogin(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String role,
            Model model,
            HttpSession session) {

        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Username: " + username);
        System.out.println("Role: " + role);

        SystemUser user = systemUserService.validateUser(username, password, role);

        if (user != null) {
            System.out.println("User validation successful for: " + username);

            // Store user in session
            session.setAttribute("currentUser", user);

            // Role-based redirection
            String lowerRole = role.toLowerCase().trim();
            System.out.println("Switching on role: '" + lowerRole + "'");

            switch (lowerRole) {
                case "hr-manager":
                    return "redirect:/hrDashboard";
                case "factory-manager":
                    return "redirect:/factoryDashboard";
                case "inventory-manager":
                    return "redirect:/inventoryDashboard";
                case "customer-officer":
                    return "redirect:/customerDashboard";
                case "sales-executive":
                    return "redirect:/salesDashboard";
                case "employee":
                    return "redirect:/employeeDashboard";
                default:
                    return "redirect:/dashboard";
            }
        } else {
            System.out.println("User validation failed for: " + username);
            return "redirect:/systemUserLogin?error=true";
        }
    }

    // Update password (only for logged-in users)
    @PostMapping("/updatePassword")
    public String updatePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/systemUserLogin";
        }

        // Validate current password
        if (!currentUser.getPassword().equals(currentPassword)) {
            session.setAttribute("updateMessage", "error:Current password is incorrect");
            return "redirect:/hrDashboard";
        }

        // Validate new password confirmation
        if (!newPassword.equals(confirmPassword)) {
            session.setAttribute("updateMessage", "error:New passwords do not match");
            return "redirect:/hrDashboard";
        }

        // Update password in database
        boolean success = systemUserService.updatePassword(
                currentUser.getUserName(),
                currentUser.getRole(),
                newPassword
        );

        if (success) {
            currentUser.setPassword(newPassword); // update session user
            session.setAttribute("currentUser", currentUser);
            session.setAttribute("updateMessage", "success:Password updated successfully");
        } else {
            session.setAttribute("updateMessage", "error:Failed to update password");
        }

        return "redirect:/hrDashboard";
    }
    private String loadDashboard(String viewName, HttpSession session, Model model) {
        System.out.println("Accessing " + viewName + " page");
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser != null) {
            model.addAttribute("user", currentUser);
            return viewName;
        }
        return "redirect:/systemUserLogin";
    }

    // Other role-based dashboards
    @GetMapping("/factoryDashboard")
    public String showFactoryDashboard(HttpSession session, Model model) {
        return loadDashboard("factoryDashboard", session, model);
    }

    @GetMapping("/inventoryDashboard")
    public String showInventoryDashboard(HttpSession session, Model model) {
        return loadDashboard("inventoryDashboard", session, model);
    }

    @GetMapping("/customerDashboard")
    public String showCustomerDashboard(HttpSession session, Model model) {
        return loadDashboard("customerDashboard", session, model);
    }

    @GetMapping("/salesDashboard")
    public String showSalesDashboard(HttpSession session, Model model) {
        return loadDashboard("salesDashboard", session, model);
    }
}