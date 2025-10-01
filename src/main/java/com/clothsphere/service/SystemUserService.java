package com.clothsphere.service;

import com.clothsphere.model.HR.Employee;
import com.clothsphere.model.SystemUser;
import com.clothsphere.repository.HR.DepartmentRepository;
import com.clothsphere.repository.HR.EmployeeRepository;
import com.clothsphere.repository.SystemUserRepository;
import com.clothsphere.service.HR.EmployeeService;
import com.clothsphere.util.PasswordEncoder;
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
     * Validate user by username, password, and role using BCrypt.
     */
    public SystemUser validateUser(String username, String password, String role) {
        System.out.println("=== USER VALIDATION ===");
        System.out.println("Looking for user: " + username + " with role: " + role);

        SystemUser user = systemUserRepository.findByUserNameAndRole(username, role);

        if (user != null) {
            System.out.println("User found in database: " + user.getUserName());

            // Use BCrypt to compare passwords
            if (PasswordEncoder.matches(password, user.getPassword())) {
                System.out.println("Password matches - validation successful");
                return user;
            } else {
                System.out.println("Password mismatch - validation failed");
            }
        } else {
            System.out.println("User not found in database");
        }

        return null;
    }

    public SystemUser findByUserNameAndRole(String username, String role) {
        return systemUserRepository.findByUserNameAndRole(username, role);
    }

    /**
     * Update user password with encryption.
     */
    @Transactional
    public boolean updatePassword(String username, String role, String newPassword) {
        try {
            SystemUser user = systemUserRepository.findByUserNameAndRole(username, role);
            if (user != null) {
                // Encrypt the new password before saving
                String encryptedPassword = PasswordEncoder.encryptPassword(newPassword);
                user.setPassword(encryptedPassword);
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

    @Transactional
    public boolean updateLogCount(String username, String role, Integer logCount) {
        try {
            SystemUser user = systemUserRepository.findByUserNameAndRole(username, role);
            if (user != null) {
                user.setLogCount(logCount);
                systemUserRepository.save(user);
                System.out.println("Log count updated to " + logCount + " for user: " + username);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("Error updating log count: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update user password and log count for first-time employees.
     */
    @Transactional
    public boolean updatePasswordAndLogCount(String username, String role, String newPassword) {
        try {
            SystemUser user = systemUserRepository.findByUserNameAndRole(username, role);
            if (user != null) {
                // Encrypt the new password
                String encryptedPassword = PasswordEncoder.encryptPassword(newPassword);
                user.setPassword(encryptedPassword);

                if ("employee".equals(role) && user.getLogCount() == 0) {
                    user.setLogCount(1);
                    System.out.println("Updated log count to 1 for first-time employee: " + username);
                }

                systemUserRepository.save(user);
                System.out.println("Password and log count updated successfully for user: " + username);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("Error updating password and log count: " + e.getMessage());
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

            employee = employeeService.createEmployeeWithId(employee);

            SystemUser existingUser = systemUserRepository.findByUserName(employee.getUsername());
            if (existingUser != null) {
                System.out.println("Username already exists: " + employee.getUsername());
                return false;
            }

            Employee existingEmployee = employeeRepository.findByEmail(employee.getEmail());
            if (existingEmployee != null) {
                System.out.println("Email already exists: " + employee.getEmail());
                return false;
            }

            employeeRepository.save(employee);
            System.out.println("Employee saved with ID: " + employee.getId());

            // Encrypt the default password
            String defaultPassword = "changeme123";
            String encryptedPassword = PasswordEncoder.encryptPassword(defaultPassword);

            SystemUser systemUser = new SystemUser(
                    employee.getUsername(),
                    "employee",
                    encryptedPassword,  // Store encrypted password
                    employee.getEmail(),
                    employee.getPhoneNumber()
            );
            systemUser.setLogCount(0);

            systemUserRepository.save(systemUser);

            System.out.println("Employee created successfully: " + employee.getUsername() +
                    " with ID: " + employee.getId() +
                    " with default password. First login will require password change.");
            return true;

        } catch (Exception e) {
            System.out.println("Error creating employee: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public boolean updateEmployee(Employee employee) {
        try {
            employeeRepository.save(employee);

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

    @Transactional
    public boolean deleteEmployee(String employeeId) {
        try {
            Employee employee = employeeRepository.findById(employeeId).orElse(null);
            if (employee != null) {
                String username = employee.getUsername();

                employeeRepository.deleteById(employeeId);

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

    public boolean usernameExists(String username) {
        return systemUserRepository.findByUserName(username) != null;
    }

    public boolean emailExists(String email) {
        return employeeRepository.findByEmail(email) != null;
    }
}