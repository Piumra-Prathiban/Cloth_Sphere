package com.clothsphere.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/systemUserLogin")
    public String showLoginPage() {
        return "systemUserLogin";
    }

    @PostMapping("/systemUserLogin")
    public String login(@RequestParam String username,
                       @RequestParam String password,
                       HttpSession session,
                       Model model) {
        // Simple authentication - you should replace this with proper authentication
        if ("admin".equals(username) && "admin".equals(password)) {
            session.setAttribute("currentUser", username);
            return "redirect:/production/dashboard";
        }

        model.addAttribute("error", "Invalid username or password");
        return "systemUserLogin";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/systemUserLogin";
    }
}
