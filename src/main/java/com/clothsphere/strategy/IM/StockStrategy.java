package com.clothsphere.strategy.IM;

import com.clothsphere.model.IM.Fabric;

public interface StockStrategy {
    void updateStock(Fabric fabric, double amount);
}
