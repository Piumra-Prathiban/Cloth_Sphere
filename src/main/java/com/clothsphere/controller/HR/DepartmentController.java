package com.clothsphere.controller.HR;

import com.clothsphere.model.HR.Department;
import com.clothsphere.model.HR.Employee;
import com.clothsphere.model.SystemUser;
import com.clothsphere.repository.HR.EmployeeRepository;
import com.clothsphere.service.DepartmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private EmployeeRepository employeeRepository;

    // Get all departments
    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments(HttpSession session) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            List<Department> departments = departmentService.getAllDepartments();
            return new ResponseEntity<>(departments, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error fetching departments: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get department by ID
    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartment(@PathVariable String id, HttpSession session) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Optional<Department> department = departmentService.getDepartmentById(id);
            if (department.isPresent()) {
                return new ResponseEntity<>(department.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.out.println("Error fetching department: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Create new department
    @PostMapping
    public ResponseEntity<Map<String, Object>> createDepartment(
            @RequestBody Department department,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> response = new HashMap<>();

        try {
            // Check if department name already exists
            if (departmentService.departmentNameExists(department.getDepartmentName())) {
                response.put("success", false);
                response.put("message", "Department name already exists");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Create department with auto-generated ID
            Department savedDepartment = departmentService.createDepartment(department);

            response.put("success", true);
            response.put("message", "Department created successfully with ID: " + savedDepartment.getId());
            response.put("department", savedDepartment);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating department: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update department
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateDepartment(
            @PathVariable String id,
            @RequestBody Department departmentData,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Department> optionalDepartment = departmentService.getDepartmentById(id);
            if (!optionalDepartment.isPresent()) {
                response.put("success", false);
                response.put("message", "Department not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // Check if department name is being changed and if it already exists
            if (departmentService.departmentNameExistsExcluding(departmentData.getDepartmentName(), id)) {
                response.put("success", false);
                response.put("message", "Department name already exists");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Update department
            Department updatedDepartment = departmentService.updateDepartment(id, departmentData);

            if (updatedDepartment != null) {
                response.put("success", true);
                response.put("message", "Department updated successfully");
                response.put("department", updatedDepartment);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "Failed to update department");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating department: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete department
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteDepartment(
            @PathVariable String id,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Department> optionalDepartment = departmentService.getDepartmentById(id);
            if (!optionalDepartment.isPresent()) {
                response.put("success", false);
                response.put("message", "Department not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            Department department = optionalDepartment.get();

            // Check if department has employees
            List<Employee> departmentEmployees = employeeRepository.findByDepartment(department);
            if (departmentEmployees != null && !departmentEmployees.isEmpty()) {
                response.put("success", false);
                response.put("message", "Cannot delete department with employees. Please reassign employees first.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Delete department
            boolean deleted = departmentService.deleteDepartment(id);

            if (deleted) {
                response.put("success", true);
                response.put("message", "Department deleted successfully");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "Failed to delete department");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting department: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}