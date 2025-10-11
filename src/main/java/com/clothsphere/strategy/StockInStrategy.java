package com.clothsphere.strategy;

import com.clothsphere.model.Inventory.Fabric;

public class StockInStrategy implements StockStrategy {

    @Override
    public void updateStock(Fabric fabric, double amount) {
        double newStock = fabric.getCurrentStock() + amount;
        fabric.setCurrentStock(newStock);
        System.out.println("Stock IN added: " + amount + ". Current Stock: " + newStock);
    }
}
