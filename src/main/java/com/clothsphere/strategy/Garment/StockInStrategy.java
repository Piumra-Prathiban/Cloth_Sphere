package com.clothsphere.strategy.Garment;

public class StockInStrategy implements StockStrategy {
    @Override
    public int calculateNewStock(int currentStock, int quantity) {
        return currentStock + quantity;
    }
}