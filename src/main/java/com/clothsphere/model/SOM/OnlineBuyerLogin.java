package com.clothsphere.model.SOM;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "online_buyer_login")
public class OnlineBuyerLogin {

    @Id
    @Column(name = "buyer_id", length = 10, nullable = false)
    private String buyerId;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "company", length = 100)
    private String company;

    @Column(name = "role", length = 20, nullable = false)
    private String role = "buyer";

    @Column(name = "log_count", nullable = false)
    private Integer logCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Default constructor
    public OnlineBuyerLogin() {
        this.createdAt = LocalDateTime.now();
    }

    // Parameterized constructor
    public OnlineBuyerLogin(String buyerId, String username, String password, String email,
                           String customerName, String phone, String address, String company) {
        this.buyerId = buyerId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.customerName = customerName;
        this.phone = phone;
        this.address = address;
        this.company = company;
        this.role = "buyer";
        this.logCount = 0;
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }

    // Getters and Setters
    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getLogCount() { return logCount; }
    public void setLogCount(Integer logCount) { this.logCount = logCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    @Override
    public String toString() {
        return "OnlineBuyerLogin{" +
                "buyerId='" + buyerId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", customerName='" + customerName + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
