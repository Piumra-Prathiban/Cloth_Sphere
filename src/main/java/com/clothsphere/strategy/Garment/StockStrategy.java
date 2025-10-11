package com.clothsphere.strategy.Garment;

public interface StockStrategy {
    int calculateNewStock(int currentStock, int quantity);
}