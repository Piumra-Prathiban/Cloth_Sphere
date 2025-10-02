package com.clothsphere.controller.HR;

import com.clothsphere.model.HR.Department;
import com.clothsphere.model.HR.Employee;
import com.clothsphere.model.SystemUser;
import com.clothsphere.repository.HR.EmployeeRepository;
import com.clothsphere.repository.SystemUserRepository;
import com.clothsphere.service.HR.DepartmentService;
import com.clothsphere.service.SystemUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/managers")
public class ManagerController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SystemUserService systemUserService;

    @Autowired
    private SystemUserRepository systemUserRepository;

    // ========================= DEPARTMENT MANAGER MANAGEMENT =========================

    /**
     * Get all departments with their manager information
     */
    @GetMapping("/department-managers")
    public ResponseEntity<List<Map<String, Object>>> getDepartmentManagers(HttpSession session) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            System.out.println("=== FETCHING DEPARTMENT MANAGERS ===");
            List<Department> departments = departmentService.getAllDepartments();
            List<Map<String, Object>> departmentManagers = new ArrayList<>();

            for (Department dept : departments) {
                Map<String, Object> deptManagerInfo = new HashMap<>();
                deptManagerInfo.put("departmentId", dept.getId());
                deptManagerInfo.put("departmentName", dept.getDepartmentName());
                deptManagerInfo.put("managerId", dept.getManagerId());
                deptManagerInfo.put("employeeCount", dept.getEmployeeCount());
                deptManagerInfo.put("description", dept.getDescription());

                // Get manager details if exists
                if (dept.getManagerId() != null && !dept.getManagerId().isEmpty()) {
                    Employee manager = employeeRepository.findById(dept.getManagerId()).orElse(null);
                    if (manager != null) {
                        deptManagerInfo.put("managerName", manager.getFullName());
                        deptManagerInfo.put("managerEmail", manager.getEmail());
                        deptManagerInfo.put("managerPhone", manager.getPhoneNumber());
                    } else {
                        deptManagerInfo.put("managerName", "Employee Not Found");
                        deptManagerInfo.put("managerEmail", "N/A");
                        deptManagerInfo.put("managerPhone", "N/A");
                    }
                } else {
                    deptManagerInfo.put("managerName", "Not Assigned");
                    deptManagerInfo.put("managerEmail", "N/A");
                    deptManagerInfo.put("managerPhone", "N/A");
                }

                departmentManagers.add(deptManagerInfo);
            }

            System.out.println("=== DEPARTMENT MANAGERS FETCHED SUCCESSFULLY ===");
            return new ResponseEntity<>(departmentManagers, HttpStatus.OK);

        } catch (Exception e) {
            System.out.println("Error fetching department managers: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Assign or update department manager
     */
    @PutMapping("/department/{departmentId}/manager")
    public ResponseEntity<Map<String, Object>> assignDepartmentManager(
            @PathVariable String departmentId,
            @RequestBody Map<String, String> requestData,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> response = new HashMap<>();
        System.out.println("=== ASSIGNING MANAGER TO DEPARTMENT ===");
        System.out.println("Department ID: " + departmentId);
        System.out.println("Request Data: " + requestData);

        try {
            Optional<Department> departmentOpt = departmentService.getDepartmentById(departmentId);
            if (!departmentOpt.isPresent()) {
                System.out.println("Department not found: " + departmentId);
                response.put("success", false);
                response.put("message", "Department not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            Department department = departmentOpt.get();
            String managerId = requestData.get("managerId");

            System.out.println("Current department: " + department.getDepartmentName());
            System.out.println("Manager ID to assign: " + managerId);

            // If managerId is null or empty, remove the manager
            if (managerId == null || managerId.isEmpty()) {
                department.setManagerId(null);
                Department updatedDepartment = departmentService.updateDepartment(departmentId, department);

                if (updatedDepartment != null) {
                    response.put("success", true);
                    response.put("message", "Manager removed successfully");
                    response.put("department", updatedDepartment);
                    System.out.println("Manager removed successfully from department: " + departmentId);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }
            }

            // Validate employee exists
            Employee employee = employeeRepository.findById(managerId).orElse(null);
            if (employee == null) {
                System.out.println("Employee not found: " + managerId);
                response.put("success", false);
                response.put("message", "Employee not found with ID: " + managerId);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            System.out.println("Found employee: " + employee.getFullName());

            // Check if employee belongs to the department
            if (employee.getDepartment() == null ||
                    !employee.getDepartment().getId().equals(departmentId)) {
                System.out.println("Employee does not belong to department");
                response.put("success", false);
                response.put("message", "Employee does not belong to this department. " +
                        "Please assign the employee to the department first.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Check if employee is already a manager of another department
            List<Department> allDepartments = departmentService.getAllDepartments();
            Optional<Department> existingManagerDept = allDepartments.stream()
                    .filter(dept -> managerId.equals(dept.getManagerId()) && !dept.getId().equals(departmentId))
                    .findFirst();

            if (existingManagerDept.isPresent()) {
                System.out.println("Employee is already manager of another department");
                response.put("success", false);
                response.put("message", "This employee is already manager of department: " +
                        existingManagerDept.get().getDepartmentName());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Update manager
            department.setManagerId(managerId);
            Department updatedDepartment = departmentService.updateDepartment(departmentId, department);

            if (updatedDepartment != null) {
                response.put("success", true);
                response.put("message", "Manager assigned successfully");
                response.put("department", updatedDepartment);

                // Include manager details in response
                response.put("managerName", employee.getFullName());
                response.put("managerEmail", employee.getEmail());

                System.out.println("Manager assigned successfully to department: " + departmentId);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                System.out.println("Failed to update department");
                response.put("success", false);
                response.put("message", "Failed to update department manager");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            System.out.println("Error updating department manager: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error updating department manager: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Remove department manager
     */
    @DeleteMapping("/department/{departmentId}/manager")
    public ResponseEntity<Map<String, Object>> removeDepartmentManager(
            @PathVariable String departmentId,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> response = new HashMap<>();
        System.out.println("=== REMOVING MANAGER FROM DEPARTMENT ===");
        System.out.println("Department ID: " + departmentId);

        try {
            Optional<Department> departmentOpt = departmentService.getDepartmentById(departmentId);
            if (!departmentOpt.isPresent()) {
                System.out.println("Department not found: " + departmentId);
                response.put("success", false);
                response.put("message", "Department not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            Department department = departmentOpt.get();

            // Store manager info for response
            String previousManagerId = department.getManagerId();
            String previousManagerName = "Unknown";
            if (previousManagerId != null) {
                Employee previousManager = employeeRepository.findById(previousManagerId).orElse(null);
                if (previousManager != null) {
                    previousManagerName = previousManager.getFullName();
                }
            }

            System.out.println("Removing manager: " + previousManagerName + " from department: " + department.getDepartmentName());

            department.setManagerId(null);
            Department updatedDepartment = departmentService.updateDepartment(departmentId, department);

            if (updatedDepartment != null) {
                response.put("success", true);
                response.put("message", "Manager removed successfully");
                response.put("previousManager", previousManagerName);
                response.put("department", updatedDepartment);
                System.out.println("Manager removed successfully");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                System.out.println("Failed to remove manager");
                response.put("success", false);
                response.put("message", "Failed to remove manager");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            System.out.println("Error removing department manager: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error removing department manager: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get available employees for manager assignment in a department
     */
    @GetMapping("/department/{departmentId}/available-employees")
    public ResponseEntity<List<Employee>> getAvailableEmployeesForManagement(
            @PathVariable String departmentId,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            System.out.println("=== FETCHING AVAILABLE EMPLOYEES FOR DEPARTMENT ===");
            System.out.println("Department ID: " + departmentId);

            // Get all employees in the department
            Optional<Department> departmentOpt = departmentService.getDepartmentById(departmentId);
            if (!departmentOpt.isPresent()) {
                System.out.println("Department not found: " + departmentId);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            Department department = departmentOpt.get();
            List<Employee> departmentEmployees = employeeRepository.findByDepartment(department);

            System.out.println("Found " + departmentEmployees.size() + " employees in department");

            // Filter out employees who are already managers of other departments
            List<Department> allDepartments = departmentService.getAllDepartments();
            List<Employee> availableEmployees = new ArrayList<>();

            for (Employee employee : departmentEmployees) {
                boolean isManagerElsewhere = allDepartments.stream()
                        .anyMatch(dept -> employee.getId().equals(dept.getManagerId()) &&
                                !dept.getId().equals(departmentId));

                if (!isManagerElsewhere) {
                    availableEmployees.add(employee);
                }
            }

            System.out.println("Available employees for management: " + availableEmployees.size());
            return new ResponseEntity<>(availableEmployees, HttpStatus.OK);

        } catch (Exception e) {
            System.out.println("Error fetching available employees: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========================= SYSTEM USER MANAGEMENT =========================

    /**
     * Get all system users with their roles from system_user_login_details table
     */
    @GetMapping("/system-users")
    public ResponseEntity<List<SystemUser>> getAllSystemUsers(HttpSession session) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            System.out.println("=== FETCHING ALL SYSTEM USERS ===");

            // Get all system users from the repository
            List<SystemUser> systemUsers = systemUserRepository.findAllOrderedByRole();

            System.out.println("Found " + systemUsers.size() + " system users in database");

            // Log each user for debugging
            for (SystemUser user : systemUsers) {
                System.out.println("User: " + user.getUserName() + ", Role: " + user.getRole() +
                        ", Email: " + user.getEmail() + ", Phone: " + user.getPhoneNumber());
            }

            System.out.println("=== SYSTEM USERS FETCHED SUCCESSFULLY ===");
            return new ResponseEntity<>(systemUsers, HttpStatus.OK);

        } catch (Exception e) {
            System.out.println("Error fetching system users: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get system users by role
     */
    @GetMapping("/system-users/role/{role}")
    public ResponseEntity<List<SystemUser>> getSystemUsersByRole(
            @PathVariable String role,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            System.out.println("=== FETCHING SYSTEM USERS BY ROLE: " + role + " ===");

            List<SystemUser> allUsers = systemUserRepository.findAllOrderedByRole();
            List<SystemUser> filteredUsers = new ArrayList<>();

            for (SystemUser user : allUsers) {
                if (user.getRole().equals(role)) {
                    filteredUsers.add(user);
                }
            }

            System.out.println("Found " + filteredUsers.size() + " users with role: " + role);
            return new ResponseEntity<>(filteredUsers, HttpStatus.OK);

        } catch (Exception e) {
            System.out.println("Error fetching system users by role: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get system user details
     */
    @GetMapping("/system-users/{username}/{role}")
    public ResponseEntity<SystemUser> getSystemUserDetails(
            @PathVariable String username,
            @PathVariable String role,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            System.out.println("=== FETCHING SYSTEM USER DETAILS ===");
            System.out.println("Username: " + username + ", Role: " + role);

            SystemUser user = systemUserRepository.findByUserName(username);

            if (user != null) {
                System.out.println("User found: " + user.toString());
                return new ResponseEntity<>(user, HttpStatus.OK);
            } else {
                System.out.println("User not found");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            System.out.println("Error fetching system user details: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========================= MANAGER DASHBOARD STATS =========================

    /**
     * Get manager management statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getManagerStats(HttpSession session) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            System.out.println("=== FETCHING MANAGER STATS ===");
            List<Department> departments = departmentService.getAllDepartments();
            List<SystemUser> systemUsers = systemUserRepository.findAllOrderedByRole();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalDepartments", departments.size());
            stats.put("totalSystemUsers", systemUsers.size());

            long departmentsWithManagers = departments.stream()
                    .filter(dept -> dept.getManagerId() != null && !dept.getManagerId().isEmpty())
                    .count();
            stats.put("departmentsWithManagers", departmentsWithManagers);
            stats.put("departmentsWithoutManagers", departments.size() - departmentsWithManagers);

            // Calculate manager assignment percentage
            double assignmentRate = departments.size() > 0 ?
                    (double) departmentsWithManagers / departments.size() * 100 : 0;
            stats.put("managerAssignmentRate", Math.round(assignmentRate));

            // Count users by role
            Map<String, Long> roleCount = new HashMap<>();
            for (SystemUser user : systemUsers) {
                roleCount.put(user.getRole(), roleCount.getOrDefault(user.getRole(), 0L) + 1);
            }
            stats.put("usersByRole", roleCount);

            System.out.println("Manager stats: " + stats);
            return new ResponseEntity<>(stats, HttpStatus.OK);

        } catch (Exception e) {
            System.out.println("Error fetching manager stats: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}