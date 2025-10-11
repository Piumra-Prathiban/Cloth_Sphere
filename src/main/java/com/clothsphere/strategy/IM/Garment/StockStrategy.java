package com.clothsphere.strategy.IM.Garment;

public interface StockStrategy {
    int calculateNewStock(int currentStock, int quantity);
}