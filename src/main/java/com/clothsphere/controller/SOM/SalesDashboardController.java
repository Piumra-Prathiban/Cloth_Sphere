package com.clothsphere.controller.SOM;

import com.clothsphere.model.SystemUser;
import com.clothsphere.service.SystemUserService;
import com.clothsphere.util.PasswordEncoder;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SalesDashboardController {

    private final SystemUserService systemUserService;

    // Use constructor injection instead of field injection
    @Autowired
    public SalesDashboardController(SystemUserService systemUserService) {
        this.systemUserService = systemUserService;
    }

    @GetMapping("/salesDashboard")
    public String showSalesDashboard(HttpSession session, Model model) {
        System.out.println("=== ACCESSING SALES DASHBOARD ===");

        // Check if user is logged in
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            System.out.println("No user in session - redirecting to login");
            return "redirect:/systemUserLogin";
        }

        String userRole = currentUser.getRole();
        System.out.println("User role: " + userRole);

        // Check if user has sales role
        if (!"sales-executive".equalsIgnoreCase(userRole.trim())) {
            System.out.println("User does not have sales role - access denied");
            return "redirect:/systemUserLogin?error=access_denied";
        }

        System.out.println("Sales dashboard access granted for: " + currentUser.getUserName());
        model.addAttribute("user", currentUser);
        return "SOM/salesDashboard"; // Updated path to match your structure
    }

    @PostMapping("/sales/updatePassword")
    public String updatePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        System.out.println("=== SALES PASSWORD UPDATE ===");

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/systemUserLogin";
        }

        // Use BCrypt to validate current password
        if (!PasswordEncoder.matches(currentPassword, currentUser.getPassword())) {
            redirectAttributes.addFlashAttribute("updateMessage", "error:Current password is incorrect");
            return "redirect:/salesDashboard";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("updateMessage", "error:New passwords do not match");
            return "redirect:/salesDashboard";
        }

        // Update password (will be encrypted in service)
        boolean success = systemUserService.updatePassword(
                currentUser.getUserName(),
                newPassword
        );

        if (success) {
            // Update session with new encrypted password
            SystemUser updatedUser = systemUserService.findByUserName(currentUser.getUserName());
            session.setAttribute("currentUser", updatedUser);
            redirectAttributes.addFlashAttribute("updateMessage", "success:Password updated successfully");
        } else {
            redirectAttributes.addFlashAttribute("updateMessage", "error:Failed to update password");
        }

        return "redirect:/salesDashboard";
    }

}