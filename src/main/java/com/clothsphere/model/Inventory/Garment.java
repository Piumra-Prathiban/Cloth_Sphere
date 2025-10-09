package com.clothsphere.model.Inventory;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "garments")
public class Garment {

    @Id
    @Column(name = "garment_id", length = 6)
    private String garmentId;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "size", nullable = false, length = 10)
    private String size;

    @Column(name = "fabric_id", nullable = false, length = 6)
    private String fabricId;

    @Column(name = "stock", nullable = false)
    private int stock = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold;

    @Column(name = "reorder_level")
    private Integer reorderLevel;

    // ----------------- Constructors -----------------
    public Garment() {}

    public Garment(String garmentId, String type, String size, String fabricId, int stock) {
        this.garmentId = garmentId;
        this.type = type;
        this.size = size;
        this.fabricId = fabricId;
        this.stock = stock;
    }

    // ----------------- Getters & Setters -----------------
    public String getGarmentId() { return garmentId; }
    public void setGarmentId(String garmentId) { this.garmentId = garmentId; }

    public String getId() { return garmentId; }
    public void setId(String id) { this.garmentId = garmentId; }

    public String getGarmentType() { return type; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getFabricId() { return fabricId; }
    public void setFabricId(String fabricId) { this.fabricId = fabricId; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public Integer getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(Integer lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }

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
