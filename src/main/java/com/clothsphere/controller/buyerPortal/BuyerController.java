package com.clothsphere.controller.buyerPortal;


import com.clothsphere.model.buyerPortal.Buyer;
import com.clothsphere.service.buyerPortal.BuyerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;


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
                        HttpSession session,
                        Model model) {
        Buyer buyer = buyerService.login(email, password);
        if (buyer != null) {
            // Store buyer info in session
            session.setAttribute("buyerId", buyer.getId());
            session.setAttribute("buyerName", buyer.getName());
            return "redirect:/buyer/dashboard";
        } else {
            model.addAttribute("error", true);
            return "login";
        }
    }

    // Buyer Dashboard
    @GetMapping("/buyer/dashboard")
    public String buyerDashboard(HttpSession session, Model model) {
        Long buyerId = (Long) session.getAttribute("buyerId");
        if (buyerId == null) {
            return "redirect:/login";
        }
        String buyerName = (String) session.getAttribute("buyerName");
        model.addAttribute("buyerName", buyerName);
        return "buyerDashboard";
    }




    // Handle logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Clear the session
        return "redirect:/login"; // Redirect to login page
    }

    // View Profile
    @GetMapping("/profile")
    public String viewProfile(HttpSession session, Model model) {
        Long buyerId = (Long) session.getAttribute("buyerId");
        if (buyerId == null) {
            return "redirect:/profile"; // If not logged in, redirect
        }

        Buyer buyer = buyerService.getBuyerById(buyerId);
        model.addAttribute("buyer", buyer);
        return "profile"; // Return profile.html
    }

    // Show edit profile page
    @GetMapping("/edit_profile")
    public String editProfile(Model model, HttpSession session) {
        Long buyerId = (Long) session.getAttribute("buyerId");
        Buyer buyer = buyerService.getBuyerById(buyerId);
        model.addAttribute("buyer", buyer);
        return "edit_profile"; // Thymeleaf will load edit_profile.html
    }

    // --- Save edited profile ---
    @PostMapping("/profile/edit")
    public String saveProfile(@ModelAttribute Buyer buyer, HttpSession session) {
        // Get currently logged-in buyer ID
        Long buyerId = (Long) session.getAttribute("buyerId");
        if (buyerId == null) {
            return "redirect:/login"; // If not logged in, redirect
        }

        // Ensure we update the correct buyer
        buyer.setId(buyerId);

        // Save changes to DB
        buyerService.updateBuyer(buyer);

        // Redirect back to profile page
        return "redirect:/profile";
    }



}







