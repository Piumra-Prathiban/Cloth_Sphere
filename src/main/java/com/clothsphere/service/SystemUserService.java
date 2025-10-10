package com.clothsphere.service;

import com.clothsphere.model.HR.Employee;
import com.clothsphere.model.SystemUser;
import com.clothsphere.repository.HR.CommunicationRepository;
import com.clothsphere.repository.HR.DepartmentRepository;
import com.clothsphere.repository.HR.EmployeeRepository;
import com.clothsphere.repository.SystemUserRepository;
import com.clothsphere.service.HR.CommunicationService;
import com.clothsphere.service.HR.EmployeeService;
import com.clothsphere.util.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Autowired
    private CommunicationService communicationService;

    /**
     * Validate user by username and password using BCrypt.
     */
    public SystemUser validateUser(String username, String password) {
        System.out.println("=== USER VALIDATION ===");
        System.out.println("Looking for user: " + username);

        SystemUser user = systemUserRepository.findByUserName(username);

        if (user != null) {
            System.out.println("User found in database: " + user.getUserName());
            System.out.println("User role: " + user.getRole());

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

    public SystemUser findByUserName(String username) {
        return systemUserRepository.findByUserName(username);
    }

    /**
     * Update user password with encryption using custom query.
     */
    @Transactional
    public boolean updatePassword(String username, String newPassword) {
        try {
            // Encrypt the new password before saving
            String encryptedPassword = PasswordEncoder.encryptPassword(newPassword);

            // Use custom query to update password
            int updatedRows = systemUserRepository.updatePassword(username, encryptedPassword);

            if (updatedRows > 0) {
                System.out.println("Password updated successfully for user: " + username);
                return true;
            } else {
                System.out.println("No user found with username: " + username);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error updating password: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean updateLogCount(String username, Integer logCount) {
        try {
            // Use custom query to update log count
            int updatedRows = systemUserRepository.updateLogCount(username, logCount);

            if (updatedRows > 0) {
                System.out.println("Log count updated to " + logCount + " for user: " + username);
                return true;
            } else {
                System.out.println("No user found with username: " + username);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error updating log count: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update user password and log count for first-time employees using custom query.
     */
    @Transactional
    public boolean updatePasswordAndLogCount(String username, String newPassword) {
        try {
            SystemUser user = systemUserRepository.findByUserName(username);
            if (user == null) {
                System.out.println("User not found: " + username);
                return false;
            }

            // Encrypt the new password
            String encryptedPassword = PasswordEncoder.encryptPassword(newPassword);

            int newLogCount = user.getLogCount();
            if ("employee".equals(user.getRole()) && user.getLogCount() == 0) {
                newLogCount = 1;
                System.out.println("Setting log count to 1 for first-time employee: " + username);
            }

            // Use custom query to update both password and log count
            int updatedRows = systemUserRepository.updatePasswordAndLogCount(username, encryptedPassword, newLogCount);

            if (updatedRows > 0) {
                System.out.println("Password and log count updated successfully for user: " + username);
                return true;
            } else {
                System.out.println("Failed to update password and log count for user: " + username);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error updating password and log count: " + e.getMessage());
            return false;
        }
    }

    /**
     * Create a new employee and corresponding system user with proper transaction management.
     */
    @Transactional
    public boolean createEmployee(Employee employee) {
        try {
            System.out.println("=== CREATING EMPLOYEE ===");

            // Use the new manual query method
            boolean employeeCreated = employeeService.createEmployeeWithId(employee);

            if (!employeeCreated) {
                System.out.println("Failed to create employee record");
                return false;
            }

            SystemUser existingUser = systemUserRepository.findByUserName(employee.getUsername());
            if (existingUser != null) {
                System.out.println("Username already exists: " + employee.getUsername());
                // Manually rollback since we're in the same transaction
                throw new RuntimeException("Username already exists: " + employee.getUsername());
            }

            // Encrypt the default password
            String defaultPassword = "changeme123";
            String encryptedPassword = PasswordEncoder.encryptPassword(defaultPassword);

            // Use manual query for system user creation
            int result = systemUserRepository.insertSystemUser(
                    employee.getUsername(),
                    encryptedPassword,
                    employee.getEmail(),
                    employee.getPhoneNumber(),
                    "employee",
                    0,
                    LocalDateTime.now()
            );

            if (result <= 0) {
                System.out.println("Failed to create system user for: " + employee.getUsername());
                // This will trigger rollback of the entire transaction
                throw new RuntimeException("Failed to create system user");
            }

            System.out.println("Employee created successfully: " + employee.getUsername() +
                    " with ID: " + employee.getId() +
                    " with default password. First login will require password change.");
            return true;

        } catch (Exception e) {
            System.out.println("Error creating employee: " + e.getMessage());
            e.printStackTrace();
            // Mark transaction for rollback
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    @Transactional
    public boolean updateEmployee(Employee employee) {
        try {
            // Use the new manual query method
            boolean employeeUpdated = employeeService.updateEmployee(employee);

            if (!employeeUpdated) {
                System.out.println("Failed to update employee record");
                return false;
            }

            SystemUser systemUser = systemUserRepository.findByUserName(employee.getUsername());
            if (systemUser != null) {
                // Use manual query to update system user details
                int updateResult = systemUserRepository.updateSystemUserDetails(
                        employee.getUsername(),
                        employee.getEmail(),
                        employee.getPhoneNumber()
                );

                if (updateResult > 0) {
                    System.out.println("System user details updated successfully for: " + employee.getUsername());
                } else {
                    System.out.println("Failed to update system user details for: " + employee.getUsername());
                    return false;
                }
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
            Employee employee = employeeService.getEmployeeById(employeeId);
            if (employee != null) {
                String username = employee.getUsername();

                // Delete system user first
                SystemUser systemUser = systemUserRepository.findByUserName(username);
                if (systemUser != null) {
                    systemUserRepository.delete(systemUser);
                }

                // Then delete employee using manual query
                boolean employeeDeleted = employeeService.deleteEmployee(employeeId);

                if (employeeDeleted) {
                    System.out.println("Employee deleted successfully: " + username);
                    return true;
                }
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

    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public Optional<SystemUser> getUserByEmail(String email) {
        return systemUserRepository.findByEmail(email);
    }

    /**
     * Get all users by role
     */
    @Transactional(readOnly = true)
    public List<SystemUser> getUsersByRole(String role) {
        return systemUserRepository.findByRole(role);
    }

    /**
     * Get all users except specific role
     */
    @Transactional(readOnly = true)
    public List<SystemUser> getUsersByRoleNot(String role) {
        return systemUserRepository.findByRoleNot(role);
    }

    /**
     * Check if user can communicate with another user
     */
    @Transactional(readOnly = true)
    public boolean canCommunicate(String senderEmail, String receiverEmail) {
        Optional<SystemUser> senderOpt = systemUserRepository.findByEmail(senderEmail);
        Optional<SystemUser> receiverOpt = systemUserRepository.findByEmail(receiverEmail);

        if (senderOpt.isEmpty() || receiverOpt.isEmpty()) {
            return false;
        }

        SystemUser sender = senderOpt.get();
        SystemUser receiver = receiverOpt.get();

        // Employees cannot message Factory Manager directly
        if (sender.getRole().equals("Employee") && receiver.getRole().equals("Factory Manager")) {
            return false;
        }

        return true;
    }

    /**
     * Get user communication statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserCommunicationStats(String userEmail) {
        Map<String, Object> stats = new HashMap<>();

        Optional<SystemUser> userOpt = systemUserRepository.findByEmail(userEmail);
        if (userOpt.isPresent()) {
            SystemUser user = userOpt.get();

            // Get sent messages count
            Map<String, Object> sentResult = communicationService.getSentMessages(userEmail);
            long sentCount = sentResult.get("success").equals(true) ?
                    ((List<?>) sentResult.get("messages")).size() : 0;

            // Get received messages count
            Map<String, Object> inboxResult = communicationService.getInboxMessages(userEmail);
            long receivedCount = inboxResult.get("success").equals(true) ?
                    ((List<?>) inboxResult.get("messages")).size() : 0;

            long unreadCount = (Long) inboxResult.get("unreadCount");

            stats.put("userName", user.getUserName());
            stats.put("role", user.getRole());
            stats.put("sentCount", sentCount);
            stats.put("receivedCount", receivedCount);
            stats.put("unreadCount", unreadCount);
            stats.put("totalMessages", sentCount + receivedCount);
        }

        return stats;
    }

    /**
     * Search users by name
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchUsersByName(String name) {
        List<SystemUser> users = systemUserRepository.findByUserNameContainingIgnoreCase(name);

        return users.stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("email", user.getEmail());
                    userMap.put("userName", user.getUserName());
                    userMap.put("role", user.getRole());
                    userMap.put("phoneNumber", user.getPhoneNumber());
                    return userMap;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all roles in the system
     */
    @Transactional(readOnly = true)
    public List<String> getAllRoles() {
        return List.of("Factory Manager", "HR Manager", "Inventory Manager",
                "Sales and Order Manager", "Product Manager", "Employee");
    }

    /**
     * Validate user credentials
     */
    @Transactional(readOnly = true)
    public boolean validateUserCredentials(String email, String password) {
        Optional<SystemUser> userOpt = systemUserRepository.findByEmail(email);
        return userOpt.isPresent() && userOpt.get().getPassword().equals(password);
    }

    /**
     * Update user login count
     */
    public void updateLoginCount(String email) {
        Optional<SystemUser> userOpt = systemUserRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            SystemUser user = userOpt.get();
            user.setLogCount(user.getLogCount() + 1);
            systemUserRepository.save(user);
        }
    }
}