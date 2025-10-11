package com.clothsphere.model.IM;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "garment_movements")
public class GarmentMovement {

    @Id
    @Column(name = "movement_id", length = 6)
    private String movementId; // format GMI001, GMI002, etc.

    @NotBlank(message = "Garment ID is required")
    @Column(name = "garment_id", nullable = false, length = 6)
    private String garmentId;

    @NotBlank(message = "Fabric ID is required")
    @Column(name = "fabric_id", nullable = false, length = 6)
    private String fabricId;

    @NotBlank(message = "Status is required")
    @Column(nullable = false, length = 20)
    private String status; // "In" or "Shipped"

    @Column(name = "movement_date", nullable = false)
    private LocalDate movementDate;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "approved_quantity", nullable = false)
    private Integer approvedQuantity = 0;

    @Column(name = "rejected_quantity", nullable = false)
    private Integer rejectedQuantity = 0;

    @Column(name = "total_stock", nullable = false)
    private Integer totalStock = 0;

    @Column(name = "approval_status", length = 20, nullable = false)
    private String approvalStatus = "PENDING";

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }



    // ----------------- Constructors -----------------
    public GarmentMovement() {}

    public GarmentMovement(String movementId, String garmentId, String fabricId, String status, LocalDate movementDate, Integer quantity) {
        this.movementId = movementId;
        this.garmentId = garmentId;
        this.fabricId = fabricId;
        this.status = status;
        this.movementDate = movementDate;
        this.quantity = quantity;
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

    public LocalDate getMovementDate() { return movementDate; }
    public void setMovementDate(LocalDate movementDate) { this.movementDate = movementDate; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) {
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        this.quantity = quantity;
    }

    public Integer getApprovedQuantity() { return approvedQuantity; }
    public void setApprovedQuantity(Integer approvedQuantity) {
        this.approvedQuantity = approvedQuantity;
        updateTotalStock();
    }

    public Integer getRejectedQuantity() { return rejectedQuantity; }
    public void setRejectedQuantity(Integer rejectedQuantity) {
        this.rejectedQuantity = rejectedQuantity;
        updateTotalStock();
    }

    public Integer getTotalStock() { return totalStock; }
    public void setTotalStock(Integer totalStock) {
        this.totalStock = totalStock;
    }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ----------------- Lifecycle Hooks -----------------
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now(); // Set updated_at on creation
        if (movementDate == null) movementDate = LocalDate.now();
        updateTotalStock();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(); // Update timestamp on update
        updateTotalStock();
    }

    // ----------------- Utility Methods -----------------
    private void updateTotalStock() {
        this.totalStock = (approvedQuantity != null ? approvedQuantity : 0)
                + (rejectedQuantity != null ? rejectedQuantity : 0);
    }

    @Override
    public String toString() {
        return "GarmentMovement{" +
                "movementId='" + movementId + '\'' +
                ", garmentId='" + garmentId + '\'' +
                ", fabricId='" + fabricId + '\'' +
                ", status='" + status + '\'' +
                ", approvedQuantity=" + approvedQuantity +
                ", rejectedQuantity=" + rejectedQuantity +
                ", totalStock=" + totalStock +
                ", approvalStatus='" + approvalStatus + '\'' +
                '}';
    }
}