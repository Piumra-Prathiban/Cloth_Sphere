package com.clothsphere.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for SystemUser entity
 * Combines userName and role as the primary key
 */
public class SystemUserId implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userName;
    private String role;

    // Default constructor required by JPA
    public SystemUserId() {}

    public SystemUserId(String userName, String role) {
        this.userName = userName;
        this.role = role;
    }

    // Getters and Setters
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // equals() and hashCode() are REQUIRED for composite keys
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemUserId that = (SystemUserId) o;
        return Objects.equals(userName, that.userName) &&
                Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, role);
    }

    @Override
    public String toString() {
        return "SystemUserId{" +
                "userName='" + userName + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}