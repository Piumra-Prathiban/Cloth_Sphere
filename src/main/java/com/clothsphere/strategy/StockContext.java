package com.clothsphere.strategy;

import com.clothsphere.model.Inventory.Fabric;

public class StockContext {
    private StockStrategy strategy;

    public void setStrategy(StockStrategy strategy) {
        this.strategy = strategy;
    }

    public void executeStrategy(Fabric fabric, double amount) {
        if (strategy == null) throw new IllegalStateException("Strategy not set!");
        strategy.updateStock(fabric, amount);
    }
}
