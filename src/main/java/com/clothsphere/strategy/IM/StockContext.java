package com.clothsphere.strategy.IM;

import com.clothsphere.model.IM.Fabric;

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
