package com.clothsphere.model.Inventory;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "garment_movements")
public class GarmentMovement {

    @Id
    @Column(name = "movement_id")
    private String movementId;

    @Column(name = "garment_id", nullable = false, length = 6)
    private String garmentId;

    @Column(name = "fabric_id", nullable = false, length = 6)
    private String fabricId;

    @Column(name = "status", nullable = false, length = 20) // "In" or "Shipped"
    private String status;

    @Column(name = "movement_date")
    private LocalDateTime movementDate;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "total_stock", nullable = false)
    private int totalStock;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ----------------- Constructors -----------------
    public GarmentMovement() {}

    public GarmentMovement(String garmentId, String fabricId, String status, int quantity, int totalStock) {
        this.garmentId = garmentId;
        this.fabricId = fabricId;
        this.status = status;
        this.quantity = quantity;
        this.totalStock = totalStock;
    }

    // ----------------- Getters & Setters -----------------
    public String getMovementId() { return movementId; }
    public void setMovementId(String movementId) { this.movementId = movementId; }

    public String getGarmentId() { return garmentId; }
    public void setGarmentId(String garmentId) { this.garmentId = garmentId; }

    public String getFabricId() { return fabricId; }
    public void setFabricId(String fabricId) { this.fabricId = fabricId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getMovementDate() { return movementDate; }
    public void setMovementDate(LocalDateTime movementDate) { this.movementDate = movementDate; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getTotalStock() { return totalStock; }
    public void setTotalStock(int totalStock) { this.totalStock = totalStock; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        movementDate = LocalDateTime.now();
    }
}
