package com.clothsphere.service;

import com.clothsphere.model.HR.Department;
import com.clothsphere.model.HR.TaskAssignment;
import com.clothsphere.model.SystemUser;
import com.clothsphere.repository.HR.DepartmentRepository;
import com.clothsphere.repository.HR.EmployeeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        department.setId(generateNextDepartmentId());
        Department savedDepartment = departmentRepository.save(department);
        // Set initial employee count to 0 for new departments
        savedDepartment.setEmployeeCount(0);
        return savedDepartment;
    }

    /**
     * Update an existing department
     */
    @Transactional
    public Department updateDepartment(String id, Department departmentData) {
        Optional<Department> optionalDepartment = departmentRepository.findById(id);
        if (optionalDepartment.isPresent()) {
            Department department = optionalDepartment.get();
            department.setDepartmentName(departmentData.getDepartmentName());
            department.setDescription(departmentData.getDescription());
            department.setSalaryBudget(departmentData.getSalaryBudget());
            department.setManagerId(departmentData.getManagerId());
            Department savedDepartment = departmentRepository.save(department);

            // Calculate and set employee count
            Long count = employeeRepository.countByDepartment(department);
            savedDepartment.setEmployeeCount(count != null ? count.intValue() : 0);

            return savedDepartment;
        }
        return null;
    }

    /**
     * Get all departments with employee counts (optimized for JSON serialization)
     */
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        System.out.println("=== LOADING ALL DEPARTMENTS ===");
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
        Optional<Department> departmentOpt = departmentRepository.findById(id);
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
            if (departmentRepository.existsById(id)) {
                departmentRepository.deleteById(id);
                return true;
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
        return departmentRepository.findByDepartmentName(departmentName) != null;
    }

    /**
     * Check if department name exists excluding current department
     */
    public boolean departmentNameExistsExcluding(String departmentName, String excludeId) {
        Department existing = departmentRepository.findByDepartmentName(departmentName);
        return existing != null && !existing.getId().equals(excludeId);
    }

    /**
     * Refresh department employee counts (useful after employee operations)
     */
    @Transactional
    public void refreshDepartmentEmployeeCounts() {
        List<Department> departments = departmentRepository.findAll();
        for (Department dept : departments) {
            // Clear employees to prevent serialization issues
            dept.setEmployees(null);
            Long count = employeeRepository.countByDepartment(dept);
            dept.setEmployeeCount(count != null ? count.intValue() : 0);
        }
    }

}