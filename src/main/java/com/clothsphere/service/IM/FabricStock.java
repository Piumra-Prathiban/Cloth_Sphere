package com.clothsphere.service.IM;

import com.clothsphere.observer.Fabric.StockObserver;
import com.clothsphere.observer.Fabric.StockObservable;
import java.util.ArrayList;
import java.util.List;

public class FabricStock implements StockObservable {

    private final List<StockObserver> observers = new ArrayList<>();

    @Override
    public void addObserver(StockObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(StockObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String fabricId, double stock) {
        for(StockObserver observer : observers) {
            observer.update(fabricId, stock);
        }
    }

    // Call this method whenever fabric stock changes
    public void checkStock(String fabricId, double stock) {
        notifyObservers(fabricId, stock);
    }
}