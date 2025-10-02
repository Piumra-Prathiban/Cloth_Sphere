package com.clothsphere.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_user_login_details")
public class SystemUser {

    @Id
    @Column(name = "user_name", length = 15, nullable = false)
    private String userName;

    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "role", length = 40, nullable = false)
    private String role;

    @Column(name = "log_count", nullable = false)
    private Integer logCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Default constructor
    public SystemUser() {}

    // Parameterized constructor
    public SystemUser(String userName, String password, String email, String phoneNumber, String role) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.logCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getLogCount() { return logCount; }
    public void setLogCount(Integer logCount) { this.logCount = logCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "SystemUser{" +
                "userName='" + userName + '\'' +
                ", role='" + role + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}