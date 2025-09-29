package com.clothsphere.service;

import com.clothsphere.model.HR.Employee;
import com.clothsphere.repository.HR.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;


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
     * Create a new employee with auto-generated ID
     */
    public Employee createEmployeeWithId(Employee employee) {
        String newId = generateNextEmployeeId();
        employee.setId(newId);
        return employee;
    }

    /**
     * Get employee by ID
     */
    public Employee getEmployeeById(String employeeId) {
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        return employee.orElse(null);
    }

    /**
     * Get all employees
     */
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    /**
     * Get employees by department
     */
    public List<Employee> getEmployeesByDepartmentId(String departmentId) {
        // This would need a custom repository method
        // For now, filter from all employees
        List<Employee> allEmployees = employeeRepository.findAll();
        return allEmployees.stream()
                .filter(emp -> emp.getDepartment() != null &&
                        emp.getDepartment().getId().equals(departmentId))
                .toList();
    }

    /**
     * Check if employee exists
     */
    public boolean employeeExists(String employeeId) {
        return employeeRepository.existsById(employeeId);
    }
}