package com.clothsphere.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FabricMovementDTO {

    private String movementId;
    private String fabricId;
    private String status;             // IN / OUT / SPECIAL RELEASE
    private LocalDate movementDate;
    private double quantity;
    private double totalStock;
    private double approvedQuantity;
    private double rejectedQuantity;
    private String approvalStatus;
    private String rejectionReason;

    // Constructors
    public FabricMovementDTO() {}

    public FabricMovementDTO(String movementId, String fabricId, String status, LocalDate movementDate,
                             double quantity, double totalStock, double approvedQuantity,
                             double rejectedQuantity, String approvalStatus, String rejectionReason) {
        this.movementId = movementId;
        this.fabricId = fabricId;
        this.status = status;
        this.movementDate = movementDate;
        this.quantity = quantity;
        this.totalStock = totalStock;
        this.approvedQuantity = approvedQuantity;
        this.rejectedQuantity = rejectedQuantity;
        this.approvalStatus = approvalStatus;
        this.rejectionReason = rejectionReason;
    }

    // Getters & Setters
    public String getMovementId() { return movementId; }
    public void setMovementId(String movementId) { this.movementId = movementId; }

    public String getFabricId() { return fabricId; }
    public void setFabricId(String fabricId) { this.fabricId = fabricId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getMovementDate() { return movementDate; }
    public void setMovementDate(LocalDate movementDate) { this.movementDate = movementDate; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getTotalStock() { return totalStock; }
    public void setTotalStock(double totalStock) { this.totalStock = totalStock; }

    public double getApprovedQuantity() { return approvedQuantity; }
    public void setApprovedQuantity(double approvedQuantity) { this.approvedQuantity = approvedQuantity; }

    public double getRejectedQuantity() { return rejectedQuantity; }
    public void setRejectedQuantity(double rejectedQuantity) { this.rejectedQuantity = rejectedQuantity; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
