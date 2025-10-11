package com.clothsphere.strategy.IM.Garment;

public class StockContext {
    private StockStrategy strategy;

    public void setStrategy(StockStrategy strategy) {
        this.strategy = strategy;
    }

    public int executeStrategy(int currentStock, int quantity) {
        if (strategy != null) {
            return strategy.calculateNewStock(currentStock, quantity);
        } else {
            throw new IllegalStateException("Strategy not set!");
        }
    }
}