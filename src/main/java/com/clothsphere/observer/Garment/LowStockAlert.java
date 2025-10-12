package com.clothsphere.observer.Garment;

public class LowStockAlert implements StockObserver {
    private final int threshold;

    public LowStockAlert(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public void update(String garmentId, int stock) {
        if(stock < threshold) {
            System.out.println("⚠️ ALERT: Garment " + garmentId + " stock is low (" + stock + " pcs)");
        }
    }
}
