package com.clothsphere.model.Inventory;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "Garment")
public class Garment {

    @Id
    @Pattern(regexp = "^GAR\\d{3}$", message = "Garment ID must start with GAR followed by 3 digits")
    @Column(nullable = false, unique = true, length = 6)
    private String garmentId;

    @NotBlank(message = "Garment type is required")
    @Column(nullable = false)
    private String garmentType;

    @NotBlank(message = "Size is required")
    @Column(nullable = false)
    private String size;

    // Constructors
    public Garment() {}

    public Garment(String garmentId, String garmentType, String size) {
        this.garmentId = garmentId;
        this.garmentType = garmentType;
        this.size = size;
    }

    // Getters and Setters
    public String getGarmentId() {
        return garmentId;
    }

    public void setGarmentId(String garmentId) {
        this.garmentId = garmentId;
    }

    public String getGarmentType() {
        return garmentType;
    }

    public void setGarmentType(String garmentType) {
        this.garmentType = garmentType;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}