package com.clothsphere.dto;

public class FabricDTO {

    private String fabricId;
    private String fabricType;
    private String color;
    private double currentStock;

    // Constructors
    public FabricDTO() {}

    public FabricDTO(String fabricId, String fabricType, String color, double currentStock) {
        this.fabricId = fabricId;
        this.fabricType = fabricType;
        this.color = color;
        this.currentStock = currentStock;
    }

    // Getters & Setters
    public String getFabricId() { return fabricId; }
    public void setFabricId(String fabricId) { this.fabricId = fabricId; }

    public String getFabricType() { return fabricType; }
    public void setFabricType(String fabricType) { this.fabricType = fabricType; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public double getCurrentStock() { return currentStock; }
    public void setCurrentStock(double currentStock) { this.currentStock = currentStock; }
}
