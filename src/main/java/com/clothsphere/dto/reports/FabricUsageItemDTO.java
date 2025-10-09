package com.clothsphere.dto.reports;

public class FabricUsageItemDTO {
    private String fabricId;
    private String type;
    private String color;
    private double totalIn;
    private double totalOut;
    private double netUsage;

    // Getters & Setters
    public String getFabricId() { return fabricId; }
    public void setFabricId(String fabricId) { this.fabricId = fabricId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public double getTotalIn() { return totalIn; }
    public void setTotalIn(double totalIn) { this.totalIn = totalIn; }

    public double getTotalOut() { return totalOut; }
    public void setTotalOut(double totalOut) { this.totalOut = totalOut; }

    public double getNetUsage() { return netUsage; }
    public void setNetUsage(double netUsage) { this.netUsage = netUsage; }
}