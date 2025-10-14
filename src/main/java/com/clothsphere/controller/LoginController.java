package com.clothsphere.controller;

import com.clothsphere.model.HR.Employee;
import com.clothsphere.model.SystemUser;
import com.clothsphere.repository.HR.EmployeeRepository;
import com.clothsphere.service.SystemUserService;
import com.clothsphere.Singleton.LoginLogger;
import com.clothsphere.util.PasswordEncoder;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @Autowired
    private SystemUserService systemUserService;

    @Autowired
    private EmployeeRepository employeeRepository;

    // Get the singleton instance of LoginLogger
    private final LoginLogger logger = LoginLogger.getInstance();

    // ========================= Landing page =========================

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/getstart")
    public String getStart() {
        return "getstart";
    }

    @GetMapping("/buyerLogin")
    public String showBuyerLoginPage() {
        return "redirect:/buyer/login";
    }

    @GetMapping("/systemUserLogin")
    public String showSystemUserLoginPage() {
        return "systemUserLogin";
    }

    @PostMapping("/systemUserLogin")
    public String processSystemUserLogin(
            @RequestParam String username,
            @RequestParam String password,
            Model model,
            HttpSession session) {

        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Username: " + username);

        SystemUser user = systemUserService.findByUserName(username);

        if (user == null) {
            System.out.println("User not found: " + username);
            // Log failed login attempt
            logger.logFailedLogin(username, "LoginController", "User not found");
            return "redirect:/systemUserLogin?error=true";
        }

        String userRole = user.getRole();
        System.out.println("User role: " + userRole);

        String actualEmployeeId = null;
        if ("employee".equalsIgnoreCase(userRole.trim())) {
            Employee employee = employeeRepository.findByUsername(username);
            if (employee != null) {
                actualEmployeeId = employee.getId();
                System.out.println("Found employee ID: " + actualEmployeeId);
            }
        }

        // Special handling for first-time employee login
        if ("employee".equalsIgnoreCase(userRole.trim())) {
            if (user.getLogCount() == 0) {
                System.out.println("First-time employee login detected");

                if (password == null || password.trim().isEmpty()) {
                    // Set ALL session attributes including userEmail
                    session.setAttribute("currentUser", user);
                    session.setAttribute("employeeId", actualEmployeeId);
                    session.setAttribute("username", username);
                    session.setAttribute("userEmail", user.getEmail());
                    session.setAttribute("firstLogin", true);
                    session.setAttribute("requirePasswordChange", true);

                    // Log first-time login
                    logger.logFirstTimeLogin(username, "LoginController");

                    return "redirect:/employeeDashboard?firstLogin=true";
                } else {
                    if (PasswordEncoder.matches(password, user.getPassword())) {
                        // Set ALL session attributes including userEmail
                        session.setAttribute("currentUser", user);
                        session.setAttribute("employeeId", actualEmployeeId);
                        session.setAttribute("username", username);
                        session.setAttribute("userEmail", user.getEmail());
                        session.setAttribute("firstLogin", true);
                        session.setAttribute("requirePasswordChange", true);

                        // Log first-time login
                        logger.logFirstTimeLogin(username, "LoginController");

                        return "redirect:/employeeDashboard?firstLogin=true";
                    } else {
                        // Log failed login
                        logger.logFailedLogin(username, "LoginController", "Incorrect password");
                        return "redirect:/systemUserLogin?error=true";
                    }
                }
            } else {
                // Returning employee
                if (password == null || password.trim().isEmpty()) {
                    logger.logFailedLogin(username, "LoginController", "Password required");
                    return "redirect:/systemUserLogin?error=true";
                }

                if (PasswordEncoder.matches(password, user.getPassword())) {
                    user.setLogCount(user.getLogCount() + 1);
                    systemUserService.updateLogCount(username, user.getLogCount());

                    // Set ALL session attributes including userEmail
                    session.setAttribute("currentUser", user);
                    session.setAttribute("employeeId", actualEmployeeId);
                    session.setAttribute("username", username);
                    session.setAttribute("userEmail", user.getEmail());

                    // Log successful login
                    logger.logSuccessfulLogin(username, userRole, "LoginController");

                    return "redirect:/employeeDashboard";
                } else {
                    logger.logFailedLogin(username, "LoginController", "Incorrect password");
                    return "redirect:/systemUserLogin?error=true";
                }
            }
        }

        // Normal login for other roles (HR Manager, Factory Manager, etc.)
        if (password == null || password.trim().isEmpty()) {
            logger.logFailedLogin(username, "LoginController", "Password required");
            return "redirect:/systemUserLogin?error=true";
        }

        if (PasswordEncoder.matches(password, user.getPassword())) {
            // Update log count
            user.setLogCount(user.getLogCount() + 1);
            systemUserService.updateLogCount(username, user.getLogCount());

            // Set ALL session attributes including userEmail
            session.setAttribute("loggedInUser", user);
            session.setAttribute("currentUser", user);
            session.setAttribute("username", username);
            session.setAttribute("userEmail", user.getEmail());

            String lowerRole = userRole.toLowerCase().trim();

            // Log successful login before redirecting
            logger.logSuccessfulLogin(username, userRole, "LoginController");

            switch (lowerRole) {
                case "hr-manager":
                    return "redirect:/hrDashboard";
                case "factory-manager":
                    return "redirect:/factory/dashboard";
                case "inventory-manager":
                    // Inventory Manager uses ProfileController, but logged here
                    return "redirect:/inventoryDashboard";
                case "customer-officer":
                    return "redirect:/customerDashboard";
                case "sales-executive":
                    return "redirect:/salesDashboard";
                case "employee":
                    session.setAttribute("employeeId", actualEmployeeId);
                    return "redirect:/employeeDashboard";
                case "customer & product management officer":
                    // Product Officer uses ProductOfficerAuthController, but logged here
                    return "redirect:/officer/dashboard";
                default:
                    return "redirect:/dashboard";
            }
        } else {
            logger.logFailedLogin(username, "LoginController", "Incorrect password");
            return "redirect:/systemUserLogin?error=true";
        }
    }

    @PostMapping("/updatePassword")
    public String updatePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/systemUserLogin";
        }

        // Use BCrypt to validate current password
        if (!PasswordEncoder.matches(currentPassword, currentUser.getPassword())) {
            session.setAttribute("updateMessage", "error:Current password is incorrect");
            return "redirect:/hrDashboard";
        }

        if (!newPassword.equals(confirmPassword)) {
            session.setAttribute("updateMessage", "error:New passwords do not match");
            return "redirect:/hrDashboard";
        }

        // Update password (will be encrypted in service)
        boolean success = systemUserService.updatePassword(
                currentUser.getUserName(),
                newPassword
        );

        if (success) {
            // Update session with new encrypted password
            SystemUser updatedUser = systemUserService.findByUserName(currentUser.getUserName());
            session.setAttribute("currentUser", updatedUser);
            session.setAttribute("updateMessage", "success:Password updated successfully");

            // Log password change event
            System.out.println("Password changed for user: " + currentUser.getUserName());
        } else {
            session.setAttribute("updateMessage", "error:Failed to update password");
        }

        return "redirect:/hrDashboard";
    }

    private String loadDashboard(String viewName, HttpSession session, Model model) {
        System.out.println("Accessing " + viewName + " page");
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser != null) {
            model.addAttribute("user", currentUser);
            return viewName;
        }
        return "redirect:/systemUserLogin";
    }

    @GetMapping("/factoryDashboard")
    public String showFactoryDashboard(HttpSession session, Model model) {
        return "redirect:/factory/dashboard";
    }

    @GetMapping("/customerDashboard")
    public String showCustomerDashboard(HttpSession session, Model model) {
        return loadDashboard("customerDashboard", session, model);
    }

    // ========================= Logout =========================

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        System.out.println("=== USER LOGOUT ===");

        // Get user info before invalidating session
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");

        if (currentUser != null) {
            // Log logout event
            logger.logLogout(currentUser.getUserName(), currentUser.getRole());
        }

        // Invalidate the session
        if (session != null) {
            session.invalidate();
        }

        System.out.println("User logged out successfully");
        return "redirect:/systemUserLogin?logout=true";
    }

    @PostMapping("/logout")
    public String logoutPost(HttpSession session) {
        return logout(session);
    }

    // ========================= View Login Logs (Optional) =========================

    /**
     * Optional endpoint to view all login logs
     * Only accessible by Factory Manager or HR Manager
     */
    @GetMapping("/admin/loginLogs")
    public String viewLoginLogs(HttpSession session, Model model) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/systemUserLogin";
        }

        // Only allow Factory Manager and HR Manager to view logs
        String role = currentUser.getRole().toLowerCase().trim();
        if (!role.equals("factory-manager") && !role.equals("hr-manager")) {
            return "redirect:/dashboard";
        }

        // Get all logs from singleton
        model.addAttribute("allLogs", logger.getAllLogs());
        model.addAttribute("totalLogs", logger.getTotalLogCount());
        model.addAttribute("successLogs", logger.getLogsByStatus("SUCCESS").size());
        model.addAttribute("failedLogs", logger.getLogsByStatus("FAILED").size());

        return "loginLogsView"; // Create this view if needed
    }
}