package com.clothsphere.service.Inventory;

import com.clothsphere.model.Inventory.Garment;
import com.clothsphere.model.Inventory.GarmentMovement;
import com.clothsphere.observer.Garment.StockObservable;
import com.clothsphere.observer.Garment.StockObserver;
import com.clothsphere.repository.Inventory.GarmentRepository;
import com.clothsphere.repository.Inventory.GarmentMovementRepository;

import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GarmentService implements StockObservable {

    private final GarmentRepository garmentRepo;
    private final GarmentMovementRepository movementRepo;
    private final List<StockObserver> observers = new ArrayList<>();
    private final int LOW_STOCK_THRESHOLD = 10;

    // Constructor injection for Spring
    public GarmentService(GarmentRepository garmentRepo, GarmentMovementRepository movementRepo) {
        this.garmentRepo = garmentRepo;
        this.movementRepo = movementRepo;
    }

    // ---------------- Observer methods ----------------
    @Override
    public void addObserver(StockObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(StockObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String garmentId, int stock) {
        for (StockObserver observer : observers) {
            observer.update(garmentId, stock);
        }
    }

    // ---------------- Garment CRUD ----------------
    public List<Garment> getAllGarments() throws SQLException {
        return garmentRepo.findAll();
    }

    public Garment getGarmentById(String id) throws SQLException {
        return garmentRepo.findById(id);
    }

    public void addGarment(Garment garment) throws SQLException {
        garmentRepo.save(garment);
        checkLowStock(garment);
    }

    public void updateGarment(Garment garment) throws SQLException {
        garmentRepo.update(garment);
        checkLowStock(garment);
    }

    public void deleteGarment(String id) throws SQLException {
        garmentRepo.delete(id);
    }

    // ---------------- Garment Movements ----------------
    public void recordMovement(GarmentMovement movement) throws SQLException {
        Garment garment = garmentRepo.findById(movement.getGarmentId());
        if (garment == null) throw new SQLException("Garment not found");

        int newStock = garment.getStock();
        if ("In".equalsIgnoreCase(movement.getStatus())) {
            newStock += movement.getQuantity();
        } else if ("Shipped".equalsIgnoreCase(movement.getStatus())) {
            newStock -= movement.getQuantity();
            if (newStock < 0) newStock = 0;
        }

        garment.setStock(newStock);
        garmentRepo.update(garment);

        movement.setTotalStock(newStock);
        if (movement.getMovementDate() == null) {
            movement.setMovementDate(LocalDateTime.now());
        }
        movementRepo.save(movement);

        checkLowStock(garment);
    }

    // Fetch all movements for a garment
    public List<GarmentMovement> getMovementsByGarmentId(String garmentId) throws SQLException {
        return movementRepo.findByGarmentId(garmentId);
    }

    // ---------------- Private helper ----------------
    private void checkLowStock(Garment garment) {
        if (garment.getStock() < LOW_STOCK_THRESHOLD) {
            notifyObservers(garment.getId(), garment.getStock());
        }
    }
}
