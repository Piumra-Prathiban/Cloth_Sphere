package com.clothsphere.strategy.IM;

import com.clothsphere.model.IM.Fabric;

public class StockOutStrategy implements StockStrategy {

    @Override
    public void updateStock(Fabric fabric, double amount) {
        if (fabric.getCurrentStock() < amount) {
            throw new IllegalArgumentException("Not enough stock! Current stock: " + fabric.getCurrentStock());
        }
        double newStock = fabric.getCurrentStock() - amount;
        fabric.setCurrentStock(newStock);
        System.out.println("Fabric : ");
        System.out.println("Stock OUT removed: " + amount + ". Current Stock: " + newStock);
    }
        // If amount is 0 or negative, do nothing (no message)
    }
