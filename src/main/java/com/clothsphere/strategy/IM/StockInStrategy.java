package com.clothsphere.strategy.IM;

import com.clothsphere.model.IM.Fabric;

public class StockInStrategy implements StockStrategy {

    @Override
    public void updateStock(Fabric fabric, double amount) {
        double newStock = fabric.getCurrentStock() + amount;
        fabric.setCurrentStock(newStock);
        System.out.println("Fabric : ");
        System.out.println("Stock IN added: " + amount + ". Current Stock: " + newStock);
    }
        // If amount is 0 or negative, do nothing (no message)
    }
