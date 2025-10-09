package com.clothsphere.dto;

import java.time.LocalDate;

public class GarmentMovementDTO {
    private int id;
    private String garmentId;
    private String fabricId;
    private String status;
    private LocalDate date;
    private int quantity;
    private int totalStock;

    public GarmentMovementDTO() {}

    public GarmentMovementDTO(int id, String garmentId, String fabricId, String status, LocalDate date, int quantity, int totalStock) {
        this.id = id;
        this.garmentId = garmentId;
        this.fabricId = fabricId;
        this.status = status;
        this.date = date;
        this.quantity = quantity;
        this.totalStock = totalStock;
    }

    // Getters
    public int getId() { return id; }
    public String getGarmentId() { return garmentId; }
    public String getFabricId() { return fabricId; }
    public String getStatus() { return status; }
    public LocalDate getDate() { return date; }
    public int getQuantity() { return quantity; }
    public int getTotalStock() { return totalStock; }
}
