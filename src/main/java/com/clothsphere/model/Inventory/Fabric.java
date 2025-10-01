package com.clothsphere.model.Inventory;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "Fabric")
public class Fabric {

    @Id
    @Pattern(regexp = "^FAB\\d{3}$", message = "Fabric ID must start with FAB followed by 3 digits")
    @Column(nullable = false, unique = true, length = 6)
    private String fabricId;

    @NotBlank(message = "Fabric type is required")
    @Column(nullable = false)
    private String fabricType;

    @NotBlank(message = "Color is required")
    @Column(nullable = false)
    private String color;

    @Column(name = "low_stock_threshold")
    private Double lowStockThreshold = 50.0; // Default threshold: 50 meters

    @Column(name = "reorder_level")
    private Double reorderLevel = 100.0; // Default reorder level: 100 meters

    // Constructors
    public Fabric() {}

    public Fabric(String fabricId, String fabricType, String color) {
        this.fabricId = fabricId;
        this.fabricType = fabricType;
        this.color = color;
        this.lowStockThreshold = 50.0;
        this.reorderLevel = 100.0;
    }

    // Getters and Setters
    public String getFabricId() {
        return fabricId;
    }

    public void setFabricId(String fabricId) {
        this.fabricId = fabricId;
    }

    public String getFabricType() {
        return fabricType;
    }

    public void setFabricType(String fabricType) {
        this.fabricType = fabricType;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Double getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Double lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public Double getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(Double reorderLevel) {
        this.reorderLevel = reorderLevel;
    }
}