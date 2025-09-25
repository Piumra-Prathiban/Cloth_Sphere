package com.clothsphere.model.HR;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @Column(name = "id", length = 10, nullable = false)
    private String id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "qualification1", length = 100)
    private String qualification1;

    @Column(name = "qualification2", length = 100)
    private String qualification2;

    @Column(name = "qualification3", length = 100)
    private String qualification3;

    @Column(name = "username", nullable = false, length = 15)
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", referencedColumnName = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "employees"}) // Prevent serialization issues
    private Department department;

    // Default constructor
    public Employee() {}

    // Parameterized constructor
    public Employee(String id, String fullName, String address, String phoneNumber, String email,
                    LocalDate dateOfBirth, String qualification1, String qualification2,
                    String qualification3, String username, String password, Department department) {
        this.id = id;
        this.fullName = fullName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.qualification1 = qualification1;
        this.qualification2 = qualification2;
        this.qualification3 = qualification3;
        this.username = username;
        this.password = password;
        this.department = department;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getQualification1() { return qualification1; }
    public void setQualification1(String qualification1) { this.qualification1 = qualification1; }

    public String getQualification2() { return qualification2; }
    public void setQualification2(String qualification2) { this.qualification2 = qualification2; }

    public String getQualification3() { return qualification3; }
    public void setQualification3(String qualification3) { this.qualification3 = qualification3; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    @Override
    public String toString() {
        return "Employee{" +
                "id='" + id + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", department='" + (department != null ? department.getDepartmentName() : "None") + '\'' +
                '}';
    }
}