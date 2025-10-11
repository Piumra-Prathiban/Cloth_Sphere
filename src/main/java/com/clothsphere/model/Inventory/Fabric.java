package com.clothsphere.model.Inventory;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "fabric",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"fabric_type", "color"}) // prevent duplicates of same type+color
        }
)
public class Fabric {

    @Id
    @Column(name = "fabric_id", length = 6)
    private String fabricId;

    @NotBlank(message = "Fabric type is required")
    @Column(name = "fabric_type", nullable = false, length = 100)
    private String fabricType;

    @NotBlank(message = "Color is required")
    @Column(name = "color", nullable = false, length = 50)
    private String color;

    @Column(name = "current_stock", nullable = false)
    private Double currentStock = 0.0;

    @Column(name = "min_stock_level", nullable = false)
    private Double minStockLevel = 10.0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // ----------------- Constructors -----------------
    public Fabric() {}

    public Fabric(String fabricId, String fabricType, String color, Double currentStock) {
        this.fabricId = fabricId;
        this.fabricType = fabricType;
        this.color = color;
        this.currentStock = currentStock;
    }


    // ----------------- Getters & Setters -----------------
    public String getFabricId() { return fabricId; }
    public void setFabricId(String fabricId) { this.fabricId = fabricId; }

    public String getFabricType() { return fabricType; }
    public void setFabricType(String fabricType) { this.fabricType = fabricType; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Double getCurrentStock() { return currentStock; }
    public void setCurrentStock(Double currentStock) {
        if (currentStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        this.currentStock = currentStock;
    }

    public Double getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(Double minStockLevel) { this.minStockLevel = minStockLevel; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isLowStock() {
        return this.currentStock <= this.minStockLevel;
    }

    @Override
    public String toString() {
        return "Fabric{" +
                "fabricId='" + fabricId + '\'' +
                ", fabricType='" + fabricType + '\'' +
                ", color='" + color + '\'' +
                ", currentStock=" + currentStock +
                ", minStockLevel=" + minStockLevel +
                '}';
    }
}
