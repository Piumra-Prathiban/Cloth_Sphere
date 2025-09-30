package com.clothsphere.controller.Inventory;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InventoryPageController {

    // Fabrics management page
    @GetMapping("/fabrics")
    public String showFabrics() {
        return "fabrics";  // returns fabrics.html
    }

    // Garments management page
    @GetMapping("/garments")
    public String showGarments() {
        return "garments";  // returns garments.html
    }

    // Reports page
    @GetMapping("/reports")
    public String showReports() {
        return "reports";  // returns reports.html
    }

    // Profile page
    @GetMapping("/profile")
    public String showProfile() {
        return "profile";  // returns profile.html
    }

    // Logout - redirect to login page
    @GetMapping("/logout")
    public String logout() {
        // In real application, you'd invalidate session here
        return "redirect:/systemUserlogin";
    }

    // System login page (if you have one)
    @GetMapping("/systemUserlogin")
    public String showLogin() {
        return "systemUserlogin";  // returns systemlogin.html
    }
}