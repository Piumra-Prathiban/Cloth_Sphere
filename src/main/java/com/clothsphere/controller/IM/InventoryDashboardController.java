package com.clothsphere.controller.IM;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


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

    // Profile page
    @GetMapping("/profile")
    public String showProfile() {
        return "profile";  // returns profile.html
    }



}
