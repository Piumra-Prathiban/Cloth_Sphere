package com.clothsphere.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "department")
public class Department {

    @Id
    @Column(name = "id", length = 10, nullable = false)
    private String id;

    @Column(name = "department_name", nullable = false, length = 100)
    private String departmentName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "salary_budget")
    private Double salaryBudget;

    @Column(name = "manager_id", length = 10)
    private String managerId;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Prevent infinite recursion during JSON serialization
    private List<Employee> employees = new ArrayList<>();

    // Transient field for employee count (not persisted in database)
    @Transient
    private Integer employeeCount;

    // Default constructor
    public Department() {}

    // Parameterized constructor
    public Department(String departmentName, String description, Double salaryBudget, String managerId) {
        this.departmentName = departmentName;
        this.description = description;
        this.salaryBudget = salaryBudget;
        this.managerId = managerId;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getSalaryBudget() { return salaryBudget; }
    public void setSalaryBudget(Double salaryBudget) { this.salaryBudget = salaryBudget; }

    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }

    public List<Employee> getEmployees() { return employees; }
    public void setEmployees(List<Employee> employees) { this.employees = employees; }

    // Get employee count - calculate dynamically
    public Integer getEmployeeCount() {
        if (employeeCount != null) {
            return employeeCount;
        }
        return employees != null ? employees.size() : 0;
    }

    // Set employee count (for JSON serialization)
    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }

    @Override
    public String toString() {
        return "Department{" +
                "id='" + id + '\'' +
                ", departmentName='" + departmentName + '\'' +
                ", description='" + description + '\'' +
                ", salaryBudget=" + salaryBudget +
                ", managerId='" + managerId + '\'' +
                ", employeeCount=" + getEmployeeCount() +
                '}';
    }
}