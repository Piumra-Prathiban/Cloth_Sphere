package com.clothsphere.observer.Fabric;

public class LowStockAlert implements StockObserver {
    private final double threshold;

    public LowStockAlert(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public void update(String fabricId, double stock) {
        if(stock < threshold) {
            System.out.println("⚠️ ALERT: Fabric " + fabricId + " stock is low (" + stock + " meters)");
        }
    }
}
