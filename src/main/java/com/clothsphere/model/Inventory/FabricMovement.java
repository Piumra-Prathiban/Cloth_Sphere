package com.clothsphere.model.Inventory;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fabric_movements")
public class FabricMovement {

    @Id
    @Column(name = "movement_id", length = 6)
    private String movementId;

    @Column(name = "fabric_id", length = 6, nullable = false)
    private String fabricId; // simple String, no @ManyToOne

    @Column(nullable = false, length = 50)
    private String status; // IN / OUT / SPECIAL RELEASE

    @Column(name = "movement_date", nullable = false)
    private LocalDate movementDate;

    @Column(nullable = false)
    private Double quantity;

    @Column(name = "total_stock", nullable = false)
    private Double totalStock;

    @Column(name = "approved_quantity")
    private Double approvedQuantity;

    @Column(name = "rejected_quantity")
    private Double rejectedQuantity;

    @Column(name = "approval_status", length = 20)
    private String approvalStatus = "PENDING"; // PENDING/APPROVED/REJECTED

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ----------------- Constructors -----------------
    public FabricMovement() {}

    public FabricMovement(String movementId, String fabricId, String status, LocalDate movementDate, Double quantity, Double totalStock) {
        this.movementId = movementId;
        this.fabricId = fabricId;
        this.status = status;
        this.movementDate = movementDate;
        this.quantity = quantity;
        this.totalStock = totalStock;
    }

    // ----------------- Getters & Setters -----------------
    public String getMovementId() { return movementId; }
    public void setMovementId(String movementId) { this.movementId = movementId; }

    public String getFabricId() { return fabricId; }
    public void setFabricId(String fabricId) { this.fabricId = fabricId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getMovementDate() { return movementDate; }
    public void setMovementDate(LocalDate movementDate) { this.movementDate = movementDate; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public Double getTotalStock() { return totalStock; }
    public void setTotalStock(Double totalStock) { this.totalStock = totalStock; }

    public Double getApprovedQuantity() { return approvedQuantity; }
    public void setApprovedQuantity(Double approvedQuantity) { this.approvedQuantity = approvedQuantity; }

    public Double getRejectedQuantity() { return rejectedQuantity; }
    public void setRejectedQuantity(Double rejectedQuantity) { this.rejectedQuantity = rejectedQuantity; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


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
