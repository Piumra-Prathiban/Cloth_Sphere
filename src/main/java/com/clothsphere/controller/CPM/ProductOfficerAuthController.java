package com.clothsphere.controller.CPM;

import com.clothsphere.model.SystemUser;
import com.clothsphere.service.CPM.BuyerMessageService;
import com.clothsphere.service.CPM.ProductManagementService;
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

    /**
     * Officer Dashboard - Main landing page after login
     */
    @GetMapping("/dashboard")
    public String showOfficerDashboard(HttpSession session, Model model) {
        // Check if user is logged in and has correct role
        SystemUser user = (SystemUser) session.getAttribute("loggedInUser");

        if (user == null) {
            System.out.println("No user in session, redirecting to login");
            return "redirect:/login";
        }

        // Verify role
        if (!"Customer & Product Management Officer".equals(user.getRole())) {
            System.out.println("User does not have officer role, redirecting to login");
            return "redirect:/login";
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

        return "officerDashboard";
    }
}
