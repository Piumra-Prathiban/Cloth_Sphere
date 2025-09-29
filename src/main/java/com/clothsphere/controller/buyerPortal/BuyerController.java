package com.clothsphere.controller.buyerPortal;

import com.clothsphere.model.buyerPortal.Buyer;
import com.clothsphere.service.buyerPortal.BuyerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class BuyerController {

    @Autowired
    private BuyerService buyerService;

    // Login page
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // Register page
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // Handle registration form
    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           Model model) {
        try {
            Buyer buyer = new Buyer();
            buyer.setName(name);
            buyer.setEmail(email);
            buyer.setPassword(password);
            buyerService.register(buyer);
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    // Handle login form
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model) {
        Buyer buyer = buyerService.login(email, password);
        if (buyer != null) {
            model.addAttribute("buyerName", buyer.getName()); // ← Add this
            return "dashboard"; // ← Show dashboard.html
        } else {
            model.addAttribute("error", true);
            return "login";
        }
    }
}
