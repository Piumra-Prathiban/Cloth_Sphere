package com.clothsphere.model;

import java.io.Serializable;
import java.util.Objects;

public class SystemUserId implements Serializable {
    private String userName;
    private String role;

    public SystemUserId() {}

    public SystemUserId(String userName, String role) {
        this.userName = userName;
        this.role = role;
    }

    // Getters and setters
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
}