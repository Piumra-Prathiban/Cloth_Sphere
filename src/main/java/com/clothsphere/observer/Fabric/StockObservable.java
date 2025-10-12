package com.clothsphere.observer.Fabric;

public interface StockObservable {
    void addObserver(StockObserver observer);
    void removeObserver(StockObserver observer);
    void notifyObservers(String fabricId, double stock);
}
