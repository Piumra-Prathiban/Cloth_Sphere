package com.clothsphere.controller.CPM;

import com.clothsphere.model.IM.Garment;
import com.clothsphere.model.SystemUser;
import com.clothsphere.service.IM.GarmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/officer")
public class OfficerInventoryController {

    @Autowired
    private GarmentService garmentService;

    /**
     * Inventory Management Page
     */
    @GetMapping("/inventory")
    public String showInventoryManagement(HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("loggedInUser");

        if (user == null || !"Customer & Product Management Officer".equals(user.getRole())) {
            return "redirect:/login";
        }

        // Get all garments for display
        List<Garment> garments = garmentService.getAllGarments();

        model.addAttribute("officer", user);
        model.addAttribute("garments", garments);
        return "officerInventory";
    }
}