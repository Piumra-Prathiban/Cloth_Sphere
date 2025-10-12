package com.clothsphere.model.IM;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fabric_movements")
public class FabricMovement {

    @Id
    @Column(name = "movement_id", length = 6)
    private String movementId; // format FMI001, FMI002, etc.

    @NotBlank(message = "Fabric ID is required")
    @Column(name = "fabric_id", length = 6, nullable = false)
    private String fabricId;

    @NotBlank(message = "Status is required")
    @Column(nullable = false, length = 50)
    private String status; // IN / OUT

    @Column(name = "movement_date", nullable = false)
    private LocalDate movementDate;

    @Column(nullable = false)
    private Double quantity;

    @Column(name = "approved_quantity", nullable = false)
    private Double approvedQuantity = 0.0;

    @Column(name = "rejected_quantity", nullable = false)
    private Double rejectedQuantity = 0.0;

    @Column(name = "total_stock", nullable = false)
    private Double totalStock = 0.0;

    @Column(name = "approval_status", length = 20, nullable = false)
    private String approvalStatus = "PENDING";

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // ----------------- Constructors -----------------
    public FabricMovement() {}

    public FabricMovement(String movementId, String fabricId, String status, LocalDate movementDate, Double quantity) {
        this.movementId = movementId;
        this.fabricId = fabricId;
        this.status = status;
        this.movementDate = movementDate;
        this.quantity = quantity;
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
    public void setQuantity(Double quantity) {
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        this.quantity = quantity;
    }

    public Double getApprovedQuantity() { return approvedQuantity; }
    public void setApprovedQuantity(Double approvedQuantity) {
        this.approvedQuantity = approvedQuantity;
        updateTotalStock();
    }

    public Double getRejectedQuantity() { return rejectedQuantity; }
    public void setRejectedQuantity(Double rejectedQuantity) {
        this.rejectedQuantity = rejectedQuantity;
        updateTotalStock();
    }

    public Double getTotalStock() { return totalStock; }

    public void setTotalStock(Double totalStock) {
        this.totalStock = totalStock;
    }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }



    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }


    // ----------------- Lifecycle Hooks -----------------
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (movementDate == null) movementDate = LocalDate.now();
        updateTotalStock();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        updateTotalStock();
    }


    // ----------------- Utility Methods -----------------
    private void updateTotalStock() {
        this.totalStock = (approvedQuantity != null ? approvedQuantity : 0.0)
                + (rejectedQuantity != null ? rejectedQuantity : 0.0);
    }

    @Override
    public String toString() {
        return "FabricMovement{" +
                "movementId='" + movementId + '\'' +
                ", fabricId='" + fabricId + '\'' +
                ", status='" + status + '\'' +
                ", approvedQuantity=" + approvedQuantity +
                ", rejectedQuantity=" + rejectedQuantity +
                ", totalStock=" + totalStock +
                '}';
    }
}
