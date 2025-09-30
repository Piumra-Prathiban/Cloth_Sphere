package com.clothsphere.controller.Inventory;

import com.clothsphere.model.SystemUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InventoryDashboardController {

    // ✅ Helper method for session check
    private String loadPage(String pageName, HttpSession session, Model model) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/systemUserLogin"; // redirect if not logged in
        }
        model.addAttribute("user", currentUser);
        return pageName; // Thymeleaf/JSP page name
    }

    // ✅ Fabrics
    @GetMapping("/fabrics")
    public String showFabrics(HttpSession session, Model model) {
        return loadPage("fabrics", session, model);
    }

    // ✅ Garments
    @GetMapping("/garments")
    public String showGarments(HttpSession session, Model model) {
        return loadPage("garments", session, model);
    }

    // ✅ Stock Reports
    @GetMapping("/reports")
    public String showReports(HttpSession session, Model model) {
        return loadPage("reports", session, model);
    }

    // ✅ Edit Profile
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        return loadPage("profile", session, model);
    }

    // ✅ Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // clear session
        return "redirect:/systemUserLogin"; // back to login page
    }
}
