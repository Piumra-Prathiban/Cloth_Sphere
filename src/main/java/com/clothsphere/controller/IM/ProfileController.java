package com.clothsphere.controller.IM;

import com.clothsphere.model.SystemUser;
import com.clothsphere.model.SystemUserId;
import com.clothsphere.service.IM.ProfileIMService;
import com.clothsphere.Singleton.LoginLogger;
import com.clothsphere.util.PasswordEncoder;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class ProfileController {

    @Autowired
    private ProfileIMService profileService;

    // Get the singleton instance of LoginLogger
    private final LoginLogger logger = LoginLogger.getInstance();

    // Show profile page - FETCH FROM DATABASE
    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/systemUserLogin";
        }

        try {
            // Fetch fresh data from database
            SystemUser dbUser = profileService.getUserProfile(
                    currentUser.getUserName(),
                    currentUser.getRole()
            );

            // Add to model for display
            model.addAttribute("currentUser", dbUser);

            // Debug logging
            System.out.println("Fetched User - Username: " + dbUser.getUserName());
            System.out.println("Fetched User - Role: " + dbUser.getRole());
            System.out.println("Fetched User - Email: " + dbUser.getEmail());
            System.out.println("Fetched User - Phone: " + dbUser.getPhoneNumber());

        } catch (Exception e) {
            System.err.println("Error fetching user profile: " + e.getMessage());
            session.setAttribute("updateMessage", "error:Could not load profile data");
        }

        return "profile";
    }

    // Update email & phone
    @PostMapping("/update")
    public String updateProfile(@RequestParam String email,
                                @RequestParam String phoneNumber,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");

        if (currentUser == null) {
            session.setAttribute("updateMessage", "error:User not logged in");
            return "redirect:/systemUserLogin";
        }

        try {
            // Get current values
            String oldEmail = currentUser.getEmail();
            String oldPhone = currentUser.getPhoneNumber();

            // Validate email format
            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                session.setAttribute("updateMessage", "error:Invalid email format");
                return "redirect:/user/profile";
            }

            // Validate phone number
            if (!phoneNumber.matches("^[+]?[0-9]{10,15}$")) {
                session.setAttribute("updateMessage", "error:Invalid phone number format");
                return "redirect:/user/profile";
            }

            SystemUserId id = new SystemUserId(currentUser.getUserName(), currentUser.getRole());
            SystemUser updatedUser = profileService.updateProfile(id, email, phoneNumber);

            // Update session with new data
            session.setAttribute("currentUser", updatedUser);

            // Create specific success message based on what changed
            boolean emailChanged = !email.equals(oldEmail);
            boolean phoneChanged = !phoneNumber.equals(oldPhone);

            String message;
            if (emailChanged && phoneChanged) {
                message = "success:Email and phone number updated successfully!";
            } else if (emailChanged) {
                message = "success:Email updated successfully!";
            } else if (phoneChanged) {
                message = "success:Phone number updated successfully!";
            } else {
                message = "success:No changes detected";
            }

            session.setAttribute("updateMessage", message);

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("updateMessage", "error:Failed to update profile: " + e.getMessage());
        }

        return "redirect:/user/profile";
    }

    // Password update method with Singleton logging
    @PostMapping("/updatePassword")
    public String updatePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/systemUserLogin";
        }

        try {
            // Fetch latest user data from database
            SystemUser dbUser = profileService.getUserProfile(
                    currentUser.getUserName(),
                    currentUser.getRole()
            );

            // Use PasswordEncoder to verify current password
            if (!PasswordEncoder.matches(currentPassword, dbUser.getPassword())) {
                session.setAttribute("updateMessage", "error:Current password is incorrect");

                // Log failed password change attempt
                System.out.println("Failed password change attempt for user: " +
                        currentUser.getUserName() + " (Inventory Manager)");

                return "redirect:/user/profile";
            }

            // Check if passwords match
            if (!newPassword.equals(confirmPassword)) {
                session.setAttribute("updateMessage", "error:New passwords do not match");
                return "redirect:/user/profile";
            }

            // Validate password length
            if (newPassword.length() < 4) {
                session.setAttribute("updateMessage", "error:Password must be at least 4 characters long");
                return "redirect:/user/profile";
            }

            // Check if new password is different from current
            if (PasswordEncoder.matches(newPassword, dbUser.getPassword())) {
                session.setAttribute("updateMessage", "error:New password must be different from current password");
                return "redirect:/user/profile";
            }

            SystemUserId id = new SystemUserId(currentUser.getUserName(), currentUser.getRole());
            profileService.updatePassword(id, newPassword);

            session.setAttribute("updateMessage", "success:Password changed successfully!");

            // Log successful password change
            System.out.println("Password changed successfully for user: " +
                    currentUser.getUserName() + " (Inventory Manager) via ProfileController");

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("updateMessage", "error:Failed to change password: " + e.getMessage());
        }

        return "redirect:/user/profile";
    }

    // Clear message from session
    @PostMapping("/clearMessage")
    @ResponseBody
    public String clearMessage(HttpSession session) {
        session.removeAttribute("updateMessage");
        return "OK";
    }

    // ========================= View User's Own Login Logs =========================

    /**
     * Allow Inventory Manager to view their own login history
     */
    @GetMapping("/myLoginHistory")
    public String viewMyLoginHistory(HttpSession session, Model model) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/systemUserLogin";
        }

        // Get user's login statistics from singleton
        LoginLogger.LoginStatistics stats = logger.getUserStatistics(currentUser.getUserName());

        // Get user's specific logs
        var userLogs = logger.getLogsByUsername(currentUser.getUserName());

        model.addAttribute("loginLogs", userLogs);
        model.addAttribute("statistics", stats);
        model.addAttribute("currentUser", currentUser);

        return "userLoginHistory"; // Create this view if needed
    }
}