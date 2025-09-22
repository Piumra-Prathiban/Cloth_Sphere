package com.clothsphere.service;

import com.clothsphere.model.Department;
import com.clothsphere.model.Employee;
import com.clothsphere.model.SystemUser;
import com.clothsphere.repository.DepartmentRepository;
import com.clothsphere.repository.EmployeeRepository;
import com.clothsphere.repository.SystemUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemUserService {

    @Autowired
    private SystemUserRepository systemUserRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeService employeeService;

    /**
     * Validate user by username, password, and role.
     */
    public SystemUser validateUser(String username, String password, String role) {
        System.out.println("=== USER VALIDATION ===");
        System.out.println("Looking for user: " + username + " with role: " + role);

        // Find user by username and role (composite key)
        SystemUser user = systemUserRepository.findByUserNameAndRole(username, role);

        if (user != null) {
            System.out.println("User found in database: " + user.getUserName());
            System.out.println("Stored password: " + user.getPassword());
            System.out.println("Provided password: " + password);

            if (user.getPassword().equals(password)) {
                System.out.println("Password matches - validation successful");
                return user; // Valid credentials
            } else {
                System.out.println("Password mismatch - validation failed");
            }
        } else {
            System.out.println("User not found in database");
        }

        return null; // Invalid credentials
    }

    /**
     * Find user by username and role.
     */
    public SystemUser findByUserNameAndRole(String username, String role) {
        return systemUserRepository.findByUserNameAndRole(username, role);
    }

    /**
     * Update user password.
     */
    @Transactional
    public boolean updatePassword(String username, String role, String newPassword) {
        try {
            SystemUser user = systemUserRepository.findByUserNameAndRole(username, role);
            if (user != null) {
                user.setPassword(newPassword);
                systemUserRepository.save(user);
                System.out.println("Password updated successfully for user: " + username);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("Error updating password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Create a new employee and corresponding system user.
     */
    @Transactional
    public boolean createEmployee(Employee employee) {
        try {
            System.out.println("=== CREATING EMPLOYEE ===");
            System.out.println("Employee department: " + (employee.getDepartment() != null ?
                    employee.getDepartment().getDepartmentName() : "NULL"));

            // Generate employee ID
            employee = employeeService.createEmployeeWithId(employee);

            // Check if username already exists
            SystemUser existingUser = systemUserRepository.findByUserName(employee.getUsername());
            if (existingUser != null) {
                System.out.println("Username already exists: " + employee.getUsername());
                return false;
            }

            // Check if email already exists
            Employee existingEmployee = employeeRepository.findByEmail(employee.getEmail());
            if (existingEmployee != null) {
                System.out.println("Email already exists: " + employee.getEmail());
                return false;
            }

            // Save employee to Employee table
            employeeRepository.save(employee);
            System.out.println("Employee saved with ID: " + employee.getId());

            // Create system user credentials
            SystemUser systemUser = new SystemUser(
                    employee.getUsername(),
                    "employee", // default role for employees
                    employee.getPassword(),
                    employee.getEmail(),
                    employee.getPhoneNumber()
            );

            systemUserRepository.save(systemUser);

            System.out.println("Employee created successfully: " + employee.getUsername() +
                    " with ID: " + employee.getId() +
                    " in department: " + (employee.getDepartment() != null ?
                    employee.getDepartment().getDepartmentName() : "None"));
            return true;

        } catch (Exception e) {
            System.out.println("Error creating employee: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update an existing employee.
     */
    @Transactional
    public boolean updateEmployee(Employee employee) {
        try {
            // Update employee in Employee table
            employeeRepository.save(employee);

            // Update corresponding system user if email or phone changed
            SystemUser systemUser = systemUserRepository.findByUserNameAndRole(employee.getUsername(), "employee");
            if (systemUser != null) {
                systemUser.setEmail(employee.getEmail());
                systemUser.setPhoneNumber(employee.getPhoneNumber());
                systemUserRepository.save(systemUser);
            }

            System.out.println("Employee updated successfully: " + employee.getUsername());
            return true;

        } catch (Exception e) {
            System.out.println("Error updating employee: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete an employee and corresponding system user.
     */
    @Transactional
    public boolean deleteEmployee(String employeeId) {
        try {
            Employee employee = employeeRepository.findById(employeeId).orElse(null);
            if (employee != null) {
                String username = employee.getUsername();

                // Delete employee from Employee table
                employeeRepository.deleteById(employeeId);

                // Delete corresponding system user
                SystemUser systemUser = systemUserRepository.findByUserNameAndRole(username, "employee");
                if (systemUser != null) {
                    systemUserRepository.delete(systemUser);
                }

                System.out.println("Employee deleted successfully: " + username);
                return true;
            }
            return false;

        } catch (Exception e) {
            System.out.println("Error deleting employee: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a system user.
     */
    @Transactional
    public boolean deleteSystemUser(SystemUser systemUser) {
        try {
            systemUserRepository.delete(systemUser);
            System.out.println("System user deleted successfully: " + systemUser.getUserName());
            return true;
        } catch (Exception e) {
            System.out.println("Error deleting system user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if username exists.
     */
    public boolean usernameExists(String username) {
        return systemUserRepository.findByUserName(username) != null;
    }

    /**
     * Check if email exists in employee table.
     */
    public boolean emailExists(String email) {
        return employeeRepository.findByEmail(email) != null;
    }
}