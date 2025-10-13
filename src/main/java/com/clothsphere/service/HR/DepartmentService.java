package com.clothsphere.service.HR;

import com.clothsphere.model.HR.Department;
import com.clothsphere.repository.HR.DepartmentRepository;
import com.clothsphere.repository.HR.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Generate the next department ID in format dept01, dept02, etc.
     */
    public String generateNextDepartmentId() {
        List<String> existingIds = departmentRepository.findAllDepartmentIds();

        if (existingIds.isEmpty()) {
            return "dept01";
        }

        int maxNumber = 0;
        for (String id : existingIds) {
            if (id.startsWith("dept") && id.length() == 6) {
                try {
                    int number = Integer.parseInt(id.substring(4));
                    if (number > maxNumber) maxNumber = number;
                } catch (NumberFormatException e) {
                    continue; // skip invalid IDs
                }
            }
        }
        return String.format("dept%02d", maxNumber + 1);
    }

    /**
     * Create a new department with auto-generated ID
     */
    @Transactional
    public Department createDepartment(Department department) {
        String newId = generateNextDepartmentId();

        // Use manual INSERT query
        int result = departmentRepository.insertDepartment(
                newId,
                department.getDepartmentName(),
                department.getDescription(),
                department.getSalaryBudget(),
                department.getManagerId()
        );

        if (result > 0) {
            // Retrieve the saved department to return
            Optional<Department> savedDepartment = departmentRepository.findDepartmentById(newId);
            if (savedDepartment.isPresent()) {
                Department dept = savedDepartment.get();
                dept.setEmployeeCount(0); // Set initial employee count to 0
                return dept;
            }
        }
        throw new RuntimeException("Failed to create department");
    }

    /**
     * Update an existing department
     */
    @Transactional
    public Department updateDepartment(String id, Department departmentData) {
        // Check if department exists
        if (!departmentRepository.existsDepartmentById(id)) {
            return null;
        }

        // Use manual UPDATE query
        int result = departmentRepository.updateDepartment(
                id,
                departmentData.getDepartmentName(),
                departmentData.getDescription(),
                departmentData.getSalaryBudget(),
                departmentData.getManagerId()
        );

        if (result > 0) {
            // Retrieve the updated department
            Optional<Department> updatedDepartment = departmentRepository.findDepartmentById(id);
            if (updatedDepartment.isPresent()) {
                Department dept = updatedDepartment.get();

                // Calculate and set employee count
                Long count = employeeRepository.countByDepartment(dept);
                dept.setEmployeeCount(count != null ? count.intValue() : 0);

                return dept;
            }
        }
        return null;
    }

    /**
     * Get all departments with employee counts (optimized for JSON serialization)
     */
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        System.out.println("=== LOADING ALL DEPARTMENTS ===");

        // Use manual query to get all departments ordered by name
        List<Department> departments = departmentRepository.findAllOrderedByName();
        System.out.println("Found " + departments.size() + " departments in database");

        // Calculate employee count for each department
        for (Department dept : departments) {
            System.out.println("Processing department: " + dept.getId() + " - " + dept.getDepartmentName());

            // Clear the employees list to prevent JSON serialization issues
            dept.setEmployees(null);

            Long employeeCount = employeeRepository.countByDepartment(dept);
            System.out.println("  - Employee count: " + employeeCount);

            dept.setEmployeeCount(employeeCount != null ? employeeCount.intValue() : 0);
            System.out.println("  - Final count set to: " + dept.getEmployeeCount());
        }

        System.out.println("=== DEPARTMENT LOADING COMPLETE ===");
        return departments;
    }

    /**
     * Get department by ID with employee count
     */
    @Transactional(readOnly = true)
    public Optional<Department> getDepartmentById(String id) {
        // Use manual query to find department by ID
        Optional<Department> departmentOpt = departmentRepository.findDepartmentById(id);
        if (departmentOpt.isPresent()) {
            Department dept = departmentOpt.get();
            // Clear employees to prevent serialization issues
            dept.setEmployees(null);
            Long employeeCount = employeeRepository.countByDepartment(dept);
            dept.setEmployeeCount(employeeCount != null ? employeeCount.intValue() : 0);
        }
        return departmentOpt;
    }

    /**
     * Get department by name with employee count
     */
    @Transactional(readOnly = true)
    public Department getDepartmentByName(String departmentName) {
        // Use manual query to find department by name
        Department department = departmentRepository.findByDepartmentName(departmentName);
        if (department != null) {
            // Clear employees to prevent serialization issues
            department.setEmployees(null);
            Long employeeCount = employeeRepository.countByDepartment(department);
            department.setEmployeeCount(employeeCount != null ? employeeCount.intValue() : 0);
        }
        return department;
    }

    /**
     * Delete department by ID
     */
    @Transactional
    public boolean deleteDepartment(String id) {
        try {
            // Use manual query to check if department exists
            if (departmentRepository.existsDepartmentById(id)) {
                // Use manual DELETE query
                int result = departmentRepository.deleteDepartmentById(id);
                return result > 0;
            }
            return false;
        } catch (Exception e) {
            System.out.println("Error deleting department: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if department name already exists
     */
    public boolean departmentNameExists(String departmentName) {
        // Use manual query to check if department name exists
        return departmentRepository.existsByDepartmentName(departmentName);
    }

    /**
     * Check if department name exists excluding current department
     */
    public boolean departmentNameExistsExcluding(String departmentName, String excludeId) {
        // Use manual query to check if department name exists excluding specific ID
        return departmentRepository.existsByDepartmentNameExcludingId(departmentName, excludeId);
    }

    /**
     * Refresh department employee counts (useful after employee operations)
     */
    @Transactional
    public void refreshDepartmentEmployeeCounts() {
        // Use manual query to get all departments
        List<Department> departments = departmentRepository.findAllDepartments();
        for (Department dept : departments) {
            // Clear employees to prevent serialization issues
            dept.setEmployees(null);
            Long count = employeeRepository.countByDepartment(dept);
            dept.setEmployeeCount(count != null ? count.intValue() : 0);
        }
    }
}