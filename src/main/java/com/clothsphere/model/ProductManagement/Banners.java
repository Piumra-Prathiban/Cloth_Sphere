package com.clothsphere.model.ProductManagement;

import jakarta.persistence.*;

@Entity
@Table(name = "banners")

public class Banners {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255) // store image path
    private String image;

    @Column(nullable = false)
    private Integer priority = 1;

    @Column(nullable = false, length = 10)
    private String status = "Inactive"; // Active or Inactive

    // ---- Constructors ----
    public Banners() {}

    public Banners(String name, String image, Integer priority, String status) {
        this.name = name;
        this.image = image;
        this.priority = priority;
        this.status = status;
    }

    // ---- Getters & Setters ----
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
