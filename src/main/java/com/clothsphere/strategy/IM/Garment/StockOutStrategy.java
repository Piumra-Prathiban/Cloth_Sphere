package com.clothsphere.strategy.IM.Garment;

public class StockOutStrategy implements StockStrategy {
    @Override
    public int calculateNewStock(int currentStock, int quantity) {
        int newStock = currentStock - quantity;
        return Math.max(newStock, 0); // Prevent negative stock
    }
}