package com.clothsphere.model.Inventory;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "GarmentMovement")
public class GarmentMovement {

    @Id
    @Pattern(regexp = "^GMI\\d{3}$", message = "Movement ID must start with GMI followed by 3 digits")
    @Column(nullable = false, unique = true, length = 6)
    private String movementId;

    @NotBlank(message = "Garment ID is required")
    @Column(nullable = false, length = 6)
    private String garmentId;

    @NotBlank(message = "Fabric ID is required")
    @Column(nullable = false, length = 6)
    private String fabricId;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(In|Shipped)$", message = "Status must be 'In' or 'Shipped'")
    @Column(nullable = false)
    private String status;

    @NotNull(message = "Movement date is required")
    @Column(nullable = false)
    private LocalDate movementDate;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be positive")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "Total quantity is required")
    @Min(value = 0, message = "Total quantity cannot be negative")
    @Column(nullable = false)
    private Integer totalQuantity;

    // References for joins
    @ManyToOne
    @JoinColumn(name = "garmentId", insertable = false, updatable = false)
    private Garment garment;

    @ManyToOne
    @JoinColumn(name = "fabricId", insertable = false, updatable = false)
    private Fabric fabric;

    // Constructors
    public GarmentMovement() {}

    public GarmentMovement(String movementId, String garmentId, String fabricId,
                           String status, LocalDate movementDate, Integer quantity, Integer totalQuantity) {
        this.movementId = movementId;
        this.garmentId = garmentId;
        this.fabricId = fabricId;
        this.status = status;
        this.movementDate = movementDate;
        this.quantity = quantity;
        this.totalQuantity = totalQuantity;
    }

    // Getters and Setters
    public String getMovementId() {
        return movementId;
    }

    public void setMovementId(String movementId) {
        this.movementId = movementId;
    }

    public String getGarmentId() {
        return garmentId;
    }

    public void setGarmentId(String garmentId) {
        this.garmentId = garmentId;
    }

    public String getFabricId() {
        return fabricId;
    }

    public void setFabricId(String fabricId) {
        this.fabricId = fabricId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(LocalDate movementDate) {
        this.movementDate = movementDate;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Garment getGarment() {
        return garment;
    }

    public void setGarment(Garment garment) {
        this.garment = garment;
    }

    public Fabric getFabric() {
        return fabric;
    }

    public void setFabric(Fabric fabric) {
        this.fabric = fabric;
    }
}