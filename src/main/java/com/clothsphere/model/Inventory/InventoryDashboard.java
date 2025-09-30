package com.clothsphere.model.Inventory;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table(name = "Outbound")
public class InventoryDashboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_type")
    private String itemType; // e.g., Fabric or Garment

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "unit")
    private String unit; // e.g., meters for fabric, units for garments

    @Column(name = "low_stock_threshold")
    private int lowStockThreshold;

    // Constructors
    public InventoryDashboard() {}

    public InventoryDashboard(String itemType, String itemName, int quantity, String unit, int lowStockThreshold) {
        this.itemType = itemType;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unit = unit;
        this.lowStockThreshold = lowStockThreshold;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }
}


