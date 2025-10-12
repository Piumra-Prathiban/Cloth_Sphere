package com.clothsphere.observer.Garment;

public interface StockObservable {
    void addObserver(StockObserver observer);
    void removeObserver(StockObserver observer);
    void notifyObservers(String garmentId, int stock);
}
