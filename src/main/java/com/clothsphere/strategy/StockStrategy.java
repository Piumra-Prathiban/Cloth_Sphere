package com.clothsphere.strategy;

import com.clothsphere.model.Inventory.Fabric;

public interface StockStrategy {
    void updateStock(Fabric fabric, double amount);
}
