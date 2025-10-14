package com.clothsphere.controller.CPM;

import com.clothsphere.model.SystemUser;
import com.clothsphere.service.CPM.BuyerMessageService;
import com.clothsphere.service.CPM.ProductManagementService;
import com.clothsphere.Singleton.LoginLogger;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/officer")
public class ProductOfficerAuthController {

    @Autowired
    private BuyerMessageService messageService;

    @Autowired
    private ProductManagementService productService;

    // Get the singleton instance of LoginLogger
    private final LoginLogger logger = LoginLogger.getInstance();

    /**
     * Officer Dashboard - Main landing page after login
     * This is accessed after successful login from LoginController
     */
    @GetMapping("/dashboard")
    public String showOfficerDashboard(HttpSession session, Model model) {
        // Check if user is logged in and has correct role
        SystemUser user = (SystemUser) session.getAttribute("loggedInUser");

        if (user == null) {
            System.out.println("No user in session, redirecting to login");
            logger.logFailedLogin("unknown", "ProductOfficerAuthController",
                    "Session expired or no user in session");
            return "redirect:/systemUserLogin";
        }

        // Verify role
        if (!"Customer & Product Management Officer".equals(user.getRole())) {
            System.out.println("User does not have officer role, redirecting to login");
            logger.logFailedLogin(user.getUserName(), "ProductOfficerAuthController",
                    "Invalid role for officer dashboard access");
            return "redirect:/systemUserLogin";
        }

        // Get statistics for dashboard
        Map<String, Long> messageStats = messageService.getMessageStatistics();
        long totalProducts = productService.getAllProducts().size();
        long activeProducts = productService.getActiveProducts().size();

        // Add data to model
        model.addAttribute("officer", user);
        model.addAttribute("messageStats", messageStats);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("activeProducts", activeProducts);

        // Log dashboard access
        System.out.println("Product Officer " + user.getUserName() +
                " accessed dashboard successfully");

        return "officerDashboard";
    }

    /**
     * View Product Officer's login history
     */
    @GetMapping("/myLoginHistory")
    public String viewMyLoginHistory(HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/systemUserLogin";
        }

        // Verify role
        if (!"Customer & Product Management Officer".equals(user.getRole())) {
            return "redirect:/systemUserLogin";
        }

        // Get user's login statistics from singleton
        LoginLogger.LoginStatistics stats = logger.getUserStatistics(user.getUserName());

        // Get user's specific logs
        var userLogs = logger.getLogsByUsername(user.getUserName());

        model.addAttribute("loginLogs", userLogs);
        model.addAttribute("statistics", stats);
        model.addAttribute("currentUser", user);

        return "officerLoginHistory"; // Create this view if needed
    }
}