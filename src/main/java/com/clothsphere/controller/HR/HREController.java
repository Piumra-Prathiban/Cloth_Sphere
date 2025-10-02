package com.clothsphere.controller.HR;

import com.clothsphere.model.HR.Department;
import com.clothsphere.model.HR.Employee;
import com.clothsphere.model.SystemUser;
import com.clothsphere.repository.HR.EmployeeRepository;
import com.clothsphere.service.HR.DepartmentService;
import com.clothsphere.service.HR.EmployeeService;
import com.clothsphere.service.SystemUserService;
import com.clothsphere.util.PasswordEncoder;
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
public class HREController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private SystemUserService systemUserService;


    // ========================= EMPLOYEE MANAGEMENT API =========================

    // Get all employees
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

    // Get single employee
    @GetMapping("/api/employees/{id}")
    @ResponseBody
    public ResponseEntity<Employee> getEmployee(@PathVariable String id, HttpSession session) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Employee employee = employeeRepository.findEmployeeById(id).orElse(null);
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

    // Add new employee
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

            // Use default password for first-time login
            String defaultPassword = "changeme123";

            // Parse date of birth
            LocalDate dob = LocalDate.parse(employeeData.get("dateOfBirth"), DateTimeFormatter.ISO_DATE);

            // Update the department handling section
            Department department = null;
            String departmentName = employeeData.get("department");
            if (departmentName != null && !departmentName.isEmpty()) {
                department = departmentService.getDepartmentByName(departmentName);
                if (department == null) {
                    System.out.println("Department not found: " + departmentName);
                }
            }

            // Create employee object
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
            employee.setPassword(defaultPassword); // Set default password
            employee.setDepartment(department);

            // Save employee
            boolean success = systemUserService.createEmployee(employee);


            if (success) {
                response.put("success", true);
                response.put("message", "Employee added successfully!");
                response.put("employeeId", employee.getId());
                response.put("username", username);
                response.put("password", defaultPassword); // Return actual default password
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

    // Update employee
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
            Employee existingEmployee = employeeRepository.findEmployeeById(id).orElse(null);
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
            boolean success = employeeService.updateEmployee(existingEmployee);

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

    // Delete employee
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
            Employee employee = employeeRepository.findEmployeeById(id).orElse(null);
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

    // Legacy employee creation
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

            // Use default password for first-time login (not random password)
            String defaultPassword = "changeme123";

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
            employee.setPassword(defaultPassword); // Set default password
            employee.setDepartment(departmentObj);

            // Save employee
            boolean success = systemUserService.createEmployee(employee);

            if (success) {
                session.setAttribute("updateMessage",
                        "success:Employee added successfully! ID: " + employee.getId() +
                                ", Username: " + username +
                                ", Default Password: " + defaultPassword +
                                " (Employee must change this on first login)");
            } else {
                session.setAttribute("updateMessage",
                        "error:Failed to add employee. Username or email may already exist.");
            }

        } catch (Exception e) {
            session.setAttribute("updateMessage", "error:Error adding employee: " + e.getMessage());
        }

        return "redirect:/hrDashboard";
    }

    //Random password Genarator
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
            return "hrDashboard";
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

            // Load all departments for the dashboard
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


    @GetMapping("/employeeDashboard")
    public String showEmployeeDashboard(HttpSession session, Model model,
                                    @RequestParam(value = "firstLogin", required = false) String firstLogin,
                                    @RequestParam(value = "passwordChanged", required = false) String passwordChanged) {
    System.out.println("=== EMPLOYEE DASHBOARD DEBUG ===");
    SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");

    if (currentUser != null) {
        System.out.println("Current User: " + currentUser.getUserName());
        System.out.println("Log Count: " + currentUser.getLogCount());
        System.out.println("URL firstLogin param: " + firstLogin);

        // Check if this is first-time login
        boolean isFirstLogin = currentUser.getLogCount() == 0 || "true".equals(firstLogin);
        System.out.println("Calculated isFirstLogin: " + isFirstLogin);

        model.addAttribute("user", currentUser);
        model.addAttribute("firstLogin", isFirstLogin);
        model.addAttribute("passwordChanged", "true".equals(passwordChanged));

        // Load employee details from database
        Employee employee = employeeRepository.findByUsername(currentUser.getUserName());
        if (employee != null) {
            System.out.println("Employee found: " + employee.getFullName());

            // Set employee data for frontend - Fixed manager name access
            Map<String, String> employeeDataMap = new HashMap<>();
            employeeDataMap.put("username", employee.getUsername());
            employeeDataMap.put("fullName", employee.getFullName());
            employeeDataMap.put("email", employee.getEmail());
            employeeDataMap.put("phoneNumber", employee.getPhoneNumber() != null ? employee.getPhoneNumber() : "N/A");
            employeeDataMap.put("employeeId", employee.getId());
            employeeDataMap.put("departmentName", employee.getDepartment() != null ? employee.getDepartment().getDepartmentName() : "Not Assigned");
            employeeDataMap.put("departmentDescription", employee.getDepartment() != null ? employee.getDepartment().getDescription() : "No description available");
            // Fixed this line - was trying to access getManagerId() which might not exist
            employeeDataMap.put("managerName", "Manager Name TBD"); // Replace with actual manager lookup

            model.addAttribute("employeeData", employeeDataMap);
            System.out.println("Employee data added to model: " + employeeDataMap);
        } else {
            System.out.println("No employee found for username: " + currentUser.getUserName());
            // Create default employee data
            Map<String, String> defaultData = new HashMap<>();
            defaultData.put("username", currentUser.getUserName());
            defaultData.put("fullName", "Employee Name Not Found");
            defaultData.put("email", currentUser.getEmail() != null ? currentUser.getEmail() : "email@example.com");
            defaultData.put("phoneNumber", "N/A");
            defaultData.put("employeeId", "N/A");
            defaultData.put("departmentName", "Not Assigned");
            defaultData.put("departmentDescription", "No description available");
            defaultData.put("managerName", "Not Assigned");

            model.addAttribute("employeeData", defaultData);
        }

        System.out.println("Model attributes set - returning employeeDashboard view");
        return "employeeDashboard";
    }

    System.out.println("No current user - redirecting to login");
    return "redirect:/systemUserLogin";
}

    // Manager management page
    @GetMapping("/manageManagers")
    public String showManageManagersPage(HttpSession session, Model model) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser != null && "hr-manager".equals(currentUser.getRole())) {
            model.addAttribute("user", currentUser);
            return "managers"; // This should match the template name without .html
        }
        return "redirect:/systemUserLogin";
    }

    @GetMapping("/workload")
    public String workloadPage(HttpSession session, Model model) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser"); // Fix: should be "currentUser" not "workload"

        if (currentUser != null && "hr-manager".equals(currentUser.getRole())) {
            model.addAttribute("user", currentUser);
            return "WorkloadAssignment"; // This should match the template name without .html
        }
        return "redirect:/systemUserLogin";
    }

    /**
     * Load workload assignment data when switching to workload section
     */
    private void loadWorkloadData(Model model) {
        try {
            // This method will be called when the workload section is accessed
            // The actual data loading will be handled by the JavaScript on the frontend
            // via AJAX calls to the WorkloadController endpoints

            // We can add any initial server-side data preparation here if needed
            System.out.println("Workload section accessed - data will be loaded via AJAX");

        } catch (Exception e) {
            System.out.println("Error preparing workload data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @PostMapping("/updateEmployeePassword")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateEmployeePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        Map<String, Object> response = new HashMap<>();

        if (currentUser == null || !"employee".equals(currentUser.getRole())) {
            response.put("success", false);
            response.put("message", "Unauthorized access");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        try {
            // For first-time login (logCount = 0), use default password validation
            boolean isFirstLogin = currentUser.getLogCount() == 0;

            System.out.println("Password update attempt - First login: " + isFirstLogin);
            System.out.println("Provided current password: " + currentPassword);

            // Validate current password
            if (isFirstLogin) {
                // For first-time login, check against default password
                if (!"changeme123".equals(currentPassword)) {
                    response.put("success", false);
                    response.put("message", "Current password is incorrect");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            } else {
                // For returning employees, use BCrypt to validate current password
                if (!PasswordEncoder.matches(currentPassword, currentUser.getPassword())) {
                    response.put("success", false);
                    response.put("message", "Current password is incorrect");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }

            // Validate new password confirmation
            if (!newPassword.equals(confirmPassword)) {
                response.put("success", false);
                response.put("message", "New passwords do not match");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Validate new password strength
            if (newPassword.length() < 6) {
                response.put("success", false);
                response.put("message", "New password must be at least 6 characters long");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Update password and log count in database using the CORRECT method signature
            boolean success = systemUserService.updatePasswordAndLogCount(
                    currentUser.getUserName(),
                    newPassword
            );

            if (success) {
                // Update session with the latest user data
                SystemUser updatedUser = systemUserService.findByUserName(currentUser.getUserName());
                session.setAttribute("currentUser", updatedUser);

                response.put("success", true);
                response.put("message", "Password updated successfully");
                response.put("firstLoginCompleted", isFirstLogin);

                System.out.println("Password updated successfully for: " + currentUser.getUserName());
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "Failed to update password");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            System.out.println("Error updating password: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error updating password: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}