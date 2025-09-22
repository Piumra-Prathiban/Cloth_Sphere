package com.clothsphere.controller;

import com.clothsphere.model.Department;
import com.clothsphere.model.Employee;
import com.clothsphere.model.SystemUser;
import com.clothsphere.repository.EmployeeRepository;
import com.clothsphere.service.DepartmentService;
import com.clothsphere.service.EmployeeService;
import com.clothsphere.service.SystemUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private SystemUserService systemUserService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private EmployeeService employeeService;

    // Landing page
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/getstart")
    public String getStart() {
        return "getstart";
    }

    // ========================= LOGIN =========================

    // Login page
    @GetMapping("/systemUserLogin")
    public String showSystemUserLoginPage() {
        return "systemUserLogin";
    }

    // Process login
    @PostMapping("/systemUserLogin")
    public String processSystemUserLogin(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String role,
            Model model,
            HttpSession session) {

        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Username: " + username);
        System.out.println("Role: " + role);

        SystemUser user = systemUserService.validateUser(username, password, role);

        if (user != null) {
            System.out.println("User validation successful for: " + username);

            // Store user in session
            session.setAttribute("currentUser", user);

            // Role-based redirection
            String lowerRole = role.toLowerCase().trim();
            System.out.println("Switching on role: '" + lowerRole + "'");

            switch (lowerRole) {
                case "hr-manager":
                    return "redirect:/hrDashboard";
                case "factory-manager":
                    return "redirect:/factoryDashboard";
                case "inventory-manager":
                    return "redirect:/inventoryDashboard";
                case "customer-officer":
                    return "redirect:/customerDashboard";
                case "sales-executive":
                    return "redirect:/salesDashboard";
                case "employee":
                    return "redirect:/employeeDashboard";
                default:
                    return "redirect:/dashboard";
            }
        } else {
            System.out.println("User validation failed for: " + username);
            return "redirect:/systemUserLogin?error=true";
        }
    }

    // ========================= EMPLOYEE MANAGEMENT API =========================

    // Get all employees (REST API)
    @GetMapping("/api/employees")
    @ResponseBody
    public ResponseEntity<List<Employee>> getAllEmployees(HttpSession session) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            List<Employee> employees = employeeRepository.findAll();
            return new ResponseEntity<>(employees, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error fetching employees: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get single employee (REST API)
    @GetMapping("/api/employees/{id}")
    @ResponseBody
    public ResponseEntity<Employee> getEmployee(@PathVariable String id, HttpSession session) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Employee employee = employeeRepository.findById(id).orElse(null);
            if (employee != null) {
                return new ResponseEntity<>(employee, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.out.println("Error fetching employee: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Add new employee (REST API)
    @PostMapping("/api/employees")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addEmployeeAPI(
            @RequestBody Map<String, String> employeeData,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> response = new HashMap<>();

        try {
            // Generate username from email
            String email = employeeData.get("email");
            String username = email.split("@")[0];

            // Generate random password
            String password = generateRandomPassword();

            // Parse date of birth
            LocalDate dob = LocalDate.parse(employeeData.get("dateOfBirth"), DateTimeFormatter.ISO_DATE);

            // Update the department handling section in addEmployeeAPI method
            Department department = null;
            String departmentName = employeeData.get("department");
            if (departmentName != null && !departmentName.isEmpty()) {
                department = departmentService.getDepartmentByName(departmentName);
                if (department == null) {
                    System.out.println("Department not found: " + departmentName);
                }
            }

// Create employee object (ID will be generated in service)
            Employee employee = new Employee();
            employee.setFullName(employeeData.get("fullName"));
            employee.setAddress(employeeData.get("address"));
            employee.setPhoneNumber(employeeData.get("phoneNumber"));
            employee.setEmail(email);
            employee.setDateOfBirth(dob);
            employee.setQualification1(employeeData.get("qualification1"));
            employee.setQualification2(employeeData.get("qualification2"));
            employee.setQualification3(employeeData.get("qualification3"));
            employee.setUsername(username);
            employee.setPassword(password);
            employee.setDepartment(department); // This should now work properly

            // Save employee
            boolean success = systemUserService.createEmployee(employee);

            if (success) {
                response.put("success", true);
                response.put("message", "Employee added successfully!");
                response.put("employeeId", employee.getId());
                response.put("username", username);
                response.put("password", password);
                response.put("employee", employee);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "Failed to add employee. Username or email may already exist.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error adding employee: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update employee (REST API)
    @PutMapping("/api/employees/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateEmployeeAPI(
            @PathVariable String id,
            @RequestBody Map<String, String> employeeData,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> response = new HashMap<>();

        try {
            Employee existingEmployee = employeeRepository.findById(id).orElse(null);
            if (existingEmployee == null) {
                response.put("success", false);
                response.put("message", "Employee not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // Get department by name
            Department department = null;
            String departmentName = employeeData.get("department");
            if (departmentName != null && !departmentName.isEmpty()) {
                department = departmentService.getDepartmentByName(departmentName);
            }

            // Update employee fields
            existingEmployee.setFullName(employeeData.get("fullName"));
            existingEmployee.setAddress(employeeData.get("address"));
            existingEmployee.setPhoneNumber(employeeData.get("phoneNumber"));
            existingEmployee.setEmail(employeeData.get("email"));
            existingEmployee.setDateOfBirth(LocalDate.parse(employeeData.get("dateOfBirth"), DateTimeFormatter.ISO_DATE));
            existingEmployee.setDepartment(department);
            existingEmployee.setQualification1(employeeData.get("qualification1"));
            existingEmployee.setQualification2(employeeData.get("qualification2"));
            existingEmployee.setQualification3(employeeData.get("qualification3"));

            // Save updated employee
            employeeRepository.save(existingEmployee);

            response.put("success", true);
            response.put("message", "Employee updated successfully!");
            response.put("employee", existingEmployee);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating employee: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete employee (REST API)
    @DeleteMapping("/api/employees/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteEmployeeAPI(
            @PathVariable String id,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> response = new HashMap<>();

        try {
            Employee employee = employeeRepository.findById(id).orElse(null);
            if (employee == null) {
                response.put("success", false);
                response.put("message", "Employee not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // Delete from both tables
            String username = employee.getUsername();
            boolean success = systemUserService.deleteEmployee(id);

            if (success) {
                response.put("success", true);
                response.put("message", "Employee deleted successfully!");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "Failed to delete employee");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting employee: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========================= LEGACY EMPLOYEE CREATION =========================

    @PostMapping("/addEmployee")
    public String addEmployee(
            @RequestParam String fullName,
            @RequestParam String address,
            @RequestParam String phoneNumber,
            @RequestParam String email,
            @RequestParam String dateOfBirth,
            @RequestParam String qualification1,
            @RequestParam String qualification2,
            @RequestParam String qualification3,
            @RequestParam String department,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/systemUserLogin";
        }

        try {
            // Generate username from email
            String username = email.split("@")[0];

            // Generate random password
            String password = generateRandomPassword();

            // Parse date of birth
            LocalDate dob = LocalDate.parse(dateOfBirth, DateTimeFormatter.ISO_DATE);

            // Get department by name
            Department departmentObj = null;
            if (department != null && !department.isEmpty()) {
                departmentObj = departmentService.getDepartmentByName(department);
            }

            // Create employee object
            Employee employee = new Employee();
            employee.setFullName(fullName);
            employee.setAddress(address);
            employee.setPhoneNumber(phoneNumber);
            employee.setEmail(email);
            employee.setDateOfBirth(dob);
            employee.setQualification1(qualification1);
            employee.setQualification2(qualification2);
            employee.setQualification3(qualification3);
            employee.setUsername(username);
            employee.setPassword(password);
            employee.setDepartment(departmentObj);

            // Save employee
            boolean success = systemUserService.createEmployee(employee);

            if (success) {
                session.setAttribute("updateMessage",
                        "success:Employee added successfully! ID: " + employee.getId() +
                                ", Username: " + username + ", Password: " + password);
            } else {
                session.setAttribute("updateMessage",
                        "error:Failed to add employee. Username or email may already exist.");
            }

        } catch (Exception e) {
            session.setAttribute("updateMessage", "error:Error adding employee: " + e.getMessage());
        }

        return "redirect:/hrDashboard";
    }

    private String generateRandomPassword() {
        // Generate an 8-character random password
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return password.toString();
    }

    // ========================= DASHBOARDS =========================

    // Default dashboard
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser != null) {
            model.addAttribute("user", currentUser);
            return "dashboard";
        }
        return "redirect:/systemUserLogin";
    }

    // HR Dashboard with password update / employee creation messages
    @GetMapping("/hrDashboard")
    public String showHrDashboard(HttpSession session, Model model) {
        System.out.println("Accessing HR Dashboard page");

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser != null) {
            model.addAttribute("user", currentUser);

            // Load all employees for the dashboard
            try {
                List<Employee> employees = employeeRepository.findAll();
                model.addAttribute("employees", employees);
                System.out.println("Loaded " + employees.size() + " employees");
            } catch (Exception e) {
                System.out.println("Error loading employees: " + e.getMessage());
            }

            // Load all departments for the dashboard (with proper error handling)
            try {
                List<Department> departments = departmentService.getAllDepartments();
                model.addAttribute("departments", departments);
                System.out.println("Loaded " + departments.size() + " departments");
            } catch (Exception e) {
                System.out.println("Error loading departments: " + e.getMessage());
                e.printStackTrace();
                // Add empty list to prevent template errors
                model.addAttribute("departments", new ArrayList<Department>());
            }

            // Pass update message (if any) to the view
            if (session.getAttribute("updateMessage") != null) {
                model.addAttribute("updateMessage", session.getAttribute("updateMessage"));
                session.removeAttribute("updateMessage");
            }
            return "hrDashboard";
        } else {
            return "redirect:/systemUserLogin";
        }
    }

    // Update password (only for logged-in users)
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

        // Validate current password
        if (!currentUser.getPassword().equals(currentPassword)) {
            session.setAttribute("updateMessage", "error:Current password is incorrect");
            return "redirect:/hrDashboard";
        }

        // Validate new password confirmation
        if (!newPassword.equals(confirmPassword)) {
            session.setAttribute("updateMessage", "error:New passwords do not match");
            return "redirect:/hrDashboard";
        }

        // Update password in database
        boolean success = systemUserService.updatePassword(
                currentUser.getUserName(),
                currentUser.getRole(),
                newPassword
        );

        if (success) {
            currentUser.setPassword(newPassword); // update session user
            session.setAttribute("currentUser", currentUser);
            session.setAttribute("updateMessage", "success:Password updated successfully");
        } else {
            session.setAttribute("updateMessage", "error:Failed to update password");
        }

        return "redirect:/hrDashboard";
    }

    // Other role-based dashboards
    @GetMapping("/factoryDashboard")
    public String showFactoryDashboard(HttpSession session, Model model) {
        return loadDashboard("factoryDashboard", session, model);
    }

    @GetMapping("/inventoryDashboard")
    public String showInventoryDashboard(HttpSession session, Model model) {
        return loadDashboard("inventoryDashboard", session, model);
    }

    @GetMapping("/customerDashboard")
    public String showCustomerDashboard(HttpSession session, Model model) {
        return loadDashboard("customerDashboard", session, model);
    }

    @GetMapping("/salesDashboard")
    public String showSalesDashboard(HttpSession session, Model model) {
        return loadDashboard("salesDashboard", session, model);
    }

    @GetMapping("/employeeDashboard")
    public String showEmployeeDashboard(HttpSession session, Model model) {
        System.out.println("Accessing Employee Dashboard page");
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");

        if (currentUser != null) {
            model.addAttribute("user", currentUser);

            // Load employee details from database
            Employee employee = employeeRepository.findByUsername(currentUser.getUserName());
            if (employee != null) {
                model.addAttribute("employee", employee);
                System.out.println("Employee details loaded: " + employee.getFullName());
            } else {
                System.out.println("No employee found for username: " + currentUser.getUserName());
                // Create a default employee object to prevent template errors
                Employee defaultEmployee = new Employee();
                defaultEmployee.setFullName("Employee Name Not Found");
                defaultEmployee.setEmail("email@example.com");
                defaultEmployee.setPhoneNumber("N/A");
                defaultEmployee.setDepartment(null);
                model.addAttribute("employee", defaultEmployee);
            }

            return "employeeDashboard";
        }
        return "redirect:/systemUserLogin";
    }

    // Helper method for session check
    private String loadDashboard(String viewName, HttpSession session, Model model) {
        System.out.println("Accessing " + viewName + " page");
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser != null) {
            model.addAttribute("user", currentUser);
            return viewName;
        }
        return "redirect:/systemUserLogin";
    }
}