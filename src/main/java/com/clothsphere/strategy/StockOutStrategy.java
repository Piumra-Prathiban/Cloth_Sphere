package com.clothsphere.strategy;

import com.clothsphere.model.Inventory.Fabric;

public class StockOutStrategy implements StockStrategy {

    @Override
    public void updateStock(Fabric fabric, double amount) {
        if (fabric.getCurrentStock() < amount) {
            throw new IllegalArgumentException("Not enough stock! Current stock: " + fabric.getCurrentStock());
        }
        double newStock = fabric.getCurrentStock() - amount;
        fabric.setCurrentStock(newStock);
        System.out.println("Stock OUT removed: " + amount + ". Current Stock: " + newStock);
    }
}

