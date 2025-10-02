package com.clothsphere.service.HR;

import com.clothsphere.model.HR.Department;
import com.clothsphere.model.HR.Employee;
import com.clothsphere.repository.HR.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    /**
     * Generate the next employee ID in format emp01, emp02, etc.
     */
    public String generateNextEmployeeId() {
        List<String> existingIds = employeeRepository.findAllEmployeeIds();

        if (existingIds.isEmpty()) {
            return "emp01";
        }

        // Find the highest number
        int maxNumber = 0;
        for (String id : existingIds) {
            if (id != null && id.startsWith("emp") && id.length() == 5) {
                try {
                    String numberPart = id.substring(3); // Get part after "emp"
                    int number = Integer.parseInt(numberPart);
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid format IDs
                    continue;
                }
            }
        }

        // Generate next ID
        int nextNumber = maxNumber + 1;
        return String.format("emp%02d", nextNumber);
    }

    /**
     * Create a new employee with auto-generated ID using manual INSERT query
     */
    @Transactional
    public boolean createEmployeeWithId(Employee employee) {
        try {
            String newId = generateNextEmployeeId();

            // Get department ID (can be null)
            String departmentId = employee.getDepartment() != null ? employee.getDepartment().getId() : null;

            // Use manual INSERT query
            int result = employeeRepository.insertEmployee(
                    newId,
                    employee.getFullName(),
                    employee.getAddress(),
                    employee.getPhoneNumber(),
                    employee.getEmail(),
                    employee.getDateOfBirth(),
                    employee.getQualification1(),
                    employee.getQualification2(),
                    employee.getQualification3(),
                    employee.getUsername(),
                    employee.getPassword(),
                    departmentId
            );

            if (result > 0) {
                employee.setId(newId);
                logger.info("Employee created successfully with ID: {}", newId);
                return true;
            } else {
                logger.error("Failed to create employee with ID: {}", newId);
                return false;
            }

        } catch (Exception e) {
            logger.error("Error creating employee: {}", e.getMessage());
            throw new RuntimeException("Failed to create employee", e); // Re-throw to trigger rollback
        }
    }

    /**
     * Get employee by ID using manual query
     */
    @Transactional(readOnly = true)
    public Employee getEmployeeById(String employeeId) {
        try {
            Optional<Employee> employee = employeeRepository.findEmployeeById(employeeId);
            return employee.orElse(null);
        } catch (Exception e) {
            logger.error("Error finding employee by ID {}: {}", employeeId, e.getMessage());
            return null;
        }
    }

    /**
     * Get employee by username using manual query
     */
    @Transactional(readOnly = true)
    public Employee getEmployeeByUsername(String username) {
        try {
            return employeeRepository.findByUsername(username);
        } catch (Exception e) {
            logger.error("Error finding employee by username {}: {}", username, e.getMessage());
            return null;
        }
    }

    /**
     * Get all employees using manual query
     */
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        try {
            return employeeRepository.findAllEmployees();
        } catch (Exception e) {
            logger.error("Error fetching all employees: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Update employee using manual UPDATE query
     */
    @Transactional
    public boolean updateEmployee(Employee employee) {
        try {
            // Use manual UPDATE query
            int result = employeeRepository.updateEmployee(
                    employee.getId(),
                    employee.getFullName(),
                    employee.getAddress(),
                    employee.getPhoneNumber(),
                    employee.getEmail(),
                    employee.getDateOfBirth(),
                    employee.getQualification1(),
                    employee.getQualification2(),
                    employee.getQualification3(),
                    employee.getDepartment()
            );

            if (result > 0) {
                logger.info("Employee updated successfully: {}", employee.getId());
                return true;
            } else {
                logger.error("Failed to update employee: {}", employee.getId());
                return false;
            }

        } catch (Exception e) {
            logger.error("Error updating employee {}: {}", employee.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * Delete employee using manual DELETE query
     */
    @Transactional
    public boolean deleteEmployee(String employeeId) {
        try {
            // Use manual DELETE query
            int result = employeeRepository.deleteEmployeeById(employeeId);

            if (result > 0) {
                logger.info("Employee deleted successfully: {}", employeeId);
                return true;
            } else {
                logger.error("Failed to delete employee: {}", employeeId);
                return false;
            }

        } catch (Exception e) {
            logger.error("Error deleting employee {}: {}", employeeId, e.getMessage());
            return false;
        }
    }

    /**
     * Check if employee exists using manual query
     */
    @Transactional(readOnly = true)
    public boolean employeeExists(String employeeId) {
        try {
            return employeeRepository.existsEmployeeById(employeeId);
        } catch (Exception e) {
            logger.error("Error checking if employee exists {}: {}", employeeId, e.getMessage());
            return false;
        }
    }

    /**
     * Check if username exists using manual query
     */
    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        try {
            return employeeRepository.existsByUsername(username);
        } catch (Exception e) {
            logger.error("Error checking if username exists {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Check if email exists using manual query
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        try {
            return employeeRepository.existsByEmail(email);
        } catch (Exception e) {
            logger.error("Error checking if email exists {}: {}", email, e.getMessage());
            return false;
        }
    }

    /**
     * Check if username exists excluding specific employee
     */
    @Transactional(readOnly = true)
    public boolean usernameExistsExcluding(String username, String excludeId) {
        try {
            return employeeRepository.existsByUsernameExcludingId(username, excludeId);
        } catch (Exception e) {
            logger.error("Error checking if username exists excluding {}: {}", excludeId, e.getMessage());
            return false;
        }
    }

    /**
     * Check if email exists excluding specific employee
     */
    @Transactional(readOnly = true)
    public boolean emailExistsExcluding(String email, String excludeId) {
        try {
            return employeeRepository.existsByEmailExcludingId(email, excludeId);
        } catch (Exception e) {
            logger.error("Error checking if email exists excluding {}: {}", excludeId, e.getMessage());
            return false;
        }
    }

    /**
     * Get employees by department ID using manual query
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDepartmentId(String departmentId) {
        try {
            return employeeRepository.findByDepartmentId(departmentId);
        } catch (Exception e) {
            logger.error("Error fetching employees by department ID {}: {}", departmentId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Count employees by department ID using manual query
     */
    @Transactional(readOnly = true)
    public Long countEmployeesByDepartmentId(String departmentId) {
        try {
            return employeeRepository.countByDepartmentId(departmentId);
        } catch (Exception e) {
            logger.error("Error counting employees by department ID {}: {}", departmentId, e.getMessage());
            return 0L;
        }
    }
}