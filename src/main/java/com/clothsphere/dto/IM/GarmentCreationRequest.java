package com.clothsphere.dto.IM;

public class GarmentCreationRequest {
    private String garmentType;
    private String size;
    private String fabricId;
    private Integer initialStock;
    private String name;
    private String description;
    private Double price;
    private String category;

    // Constructors
    public GarmentCreationRequest() {}

    public GarmentCreationRequest(String garmentType, String size, String fabricId,
                                  Integer initialStock, String name, String description,
                                  Double price, String category) {
        this.garmentType = garmentType;
        this.size = size;
        this.fabricId = fabricId;
        this.initialStock = initialStock;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    // Getters and Setters
    public String getGarmentType() { return garmentType; }
    public void setGarmentType(String garmentType) { this.garmentType = garmentType; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getFabricId() { return fabricId; }
    public void setFabricId(String fabricId) { this.fabricId = fabricId; }

    public Integer getInitialStock() { return initialStock; }
    public void setInitialStock(Integer initialStock) { this.initialStock = initialStock; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}