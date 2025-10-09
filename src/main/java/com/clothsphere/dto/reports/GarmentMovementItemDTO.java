package com.clothsphere.dto.reports;
import java.util.List;

public class GarmentMovementItemDTO {
    private String garmentId;
    private String type;
    private String size;
    private int totalProduced;
    private int totalShipped;
    private int netMovement;

    // Getters & Setters
    public String getGarmentId() { return garmentId; }
    public void setGarmentId(String garmentId) { this.garmentId = garmentId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public int getTotalProduced() { return totalProduced; }
    public void setTotalProduced(int totalProduced) { this.totalProduced = totalProduced; }

    public int getTotalShipped() { return totalShipped; }
    public void setTotalShipped(int totalShipped) { this.totalShipped = totalShipped; }

    public int getNetMovement() { return netMovement; }
    public void setNetMovement(int netMovement) { this.netMovement = netMovement; }
}



