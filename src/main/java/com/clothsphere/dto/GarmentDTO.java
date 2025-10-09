package com.clothsphere.dto;

public class GarmentDTO {
    private String id;
    private String type;
    private String size;
    private String fabricId;
    private int stock;

    public GarmentDTO() {}

    public GarmentDTO(String id, String type, String size, String fabricId, int stock) {
        this.id = id;
        this.type = type;
        this.size = size;
        this.fabricId = fabricId;
        this.stock = stock;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getType() { return type; }
    public String getSize() { return size; }
    public String getFabricId() { return fabricId; }
    public int getStock() { return stock; }
}
