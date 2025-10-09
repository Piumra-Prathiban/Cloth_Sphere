package com.clothsphere.model.Inventory;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fabric")
public class Fabric {

    @Id
    @Column(name = "fabric_id", length = 6)
    private String fabricId;

    @Column(name = "fabric_type", nullable = false, length = 100)
    private String fabricType;

    @Column(name = "color", nullable = false, length = 50)
    private String color;

    @Column(name = "current_stock", nullable = false)
    private Double currentStock = 0.0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "low_stock_threshold")
    private Double lowStockThreshold;

    @Column(name = "reorder_level")
    private Double reorderLevel;

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
    public void setCurrentStock(Double currentStock) { this.currentStock = currentStock; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Double getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(Double lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    public Double getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Double reorderLevel) { this.reorderLevel = reorderLevel; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
