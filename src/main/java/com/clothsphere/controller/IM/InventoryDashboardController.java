package com.clothsphere.controller.IM;

import com.clothsphere.service.IM.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InventoryDashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/inventoryDashboard")
    public String showDashboard() {
        return "inventorydashboard";  // returns inventorydashboard.html
    }

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
}