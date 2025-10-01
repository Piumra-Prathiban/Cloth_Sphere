package com.clothsphere.model.Inventory;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "FabricMovement")
public class FabricMovement {

    @Id
    @Pattern(regexp = "^FMI\\d{3}$", message = "Movement ID must start with FMI followed by 3 digits")
    @Column(nullable = false, unique = true, length = 6)
    private String movementId;

    @NotBlank(message = "Fabric ID is required")
    @Column(nullable = false, length = 6)
    private String fabricId;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(In|Out)$", message = "Status must be 'In' or 'Out'")
    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDate movementDate;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be positive")
    @Column(nullable = false)
    private Double quantity;

    @Min(value = 0, message = "Total quantity cannot be negative")
    @Column(nullable = false)
    private Double totalQuantity;

    // Reference to Fabric (for joins)
    @ManyToOne
    @JoinColumn(name = "fabricId", insertable = false, updatable = false)
    private Fabric fabric;

    // Constructors
    public FabricMovement() {}

    public FabricMovement(String movementId, String fabricId, String status,
                          LocalDate movementDate, Double quantity, Double totalQuantity) {
        this.movementId = movementId;
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

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Double totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Fabric getFabric() {
        return fabric;
    }

    public void setFabric(Fabric fabric) {
        this.fabric = fabric;
    }
}