package com.clothsphere.service;

import com.clothsphere.model.Employee;
import com.clothsphere.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
}