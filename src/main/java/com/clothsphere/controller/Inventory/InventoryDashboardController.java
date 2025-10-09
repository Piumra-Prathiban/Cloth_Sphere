package com.clothsphere.controller.Inventory;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller

public class InventoryDashboardController {



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

    @GetMapping("/logout")
    public String showLogout() {
        return "systemUserLogin";  // returns profile.html
    }
}
