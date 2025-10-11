package com.clothsphere.model.Inventory;

import com.fasterxml.jackson.annotation.JsonGetter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "garments")
public class Garment {

    @Id
    @Column(name = "garment_id", length = 6)
    private String garmentId;

    @NotBlank(message = "Type is required")
    @Column(nullable = false, length = 50)
    private String type;

    @NotBlank(message = "Size is required")
    @Column(nullable = false, length = 10)
    private String size;

    @NotBlank(message = "Fabric ID is required")
    @Column(name = "fabric_id", nullable = false, length = 6)
    private String fabricId;

    @Column(name = "current_stock" ,nullable = false)
    private Integer stock = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ----------------- Constructors -----------------
    public Garment() {}

    public Garment(String garmentId, String type, String size, String fabricId, Integer stock) {
        this.garmentId = garmentId;
        this.type = type;
        this.size = size;
        this.fabricId = fabricId;
        this.stock = stock;
    }

    // ----------------- Getters & Setters -----------------
    public String getGarmentId() { return garmentId; }
    public void setGarmentId(String garmentId) { this.garmentId = garmentId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getFabricId() { return fabricId; }
    public void setFabricId(String fabricId) { this.fabricId = fabricId; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) {
        if (stock < 0) throw new IllegalArgumentException("Stock cannot be negative");
        this.stock = stock;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ----------------- Lifecycle Hooks -----------------
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ----------------- Utility Methods -----------------
    @Override
    public String toString() {
        return "Garment{" +
                "garmentId='" + garmentId + '\'' +
                ", type='" + type + '\'' +
                ", size='" + size + '\'' +
                ", fabricId='" + fabricId + '\'' +
                ", stock=" + stock +
                '}';
    }

    // ----------------- Business Methods -----------------
    public void increaseStock(Integer quantity) {
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        this.stock += quantity;
    }

    public void decreaseStock(Integer quantity) {
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        if (this.stock < quantity) throw new IllegalArgumentException("Insufficient stock");
        this.stock -= quantity;
    }
}