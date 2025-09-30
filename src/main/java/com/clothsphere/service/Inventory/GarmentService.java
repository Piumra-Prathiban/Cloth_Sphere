package com.clothsphere.service.Inventory;

import com.clothsphere.model.Inventory.Garment;
import com.clothsphere.model.Inventory.GarmentMovement;
import com.clothsphere.model.Inventory.Fabric;
import com.clothsphere.repository.Inventory.GarmentRepository;
import com.clothsphere.repository.Inventory.GarmentMovementRepository;
import com.clothsphere.repository.Inventory.FabricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service
public class GarmentService {

    @Autowired
    private GarmentRepository garmentRepository;

    @Autowired
    private GarmentMovementRepository movementRepository;

    @Autowired
    private FabricRepository fabricRepository;

    @Autowired
    private FabricService fabricService;

    // ========== GARMENT OPERATIONS ==========

    // Add new garment type
    @Transactional
    public Garment addGarment(Garment garment) {
        if (garmentRepository.existsByGarmentId(garment.getGarmentId())) {
            throw new RuntimeException("Garment ID already exists: " + garment.getGarmentId());
        }
        return garmentRepository.save(garment);
    }

    // Get all garments
    public List<Garment> getAllGarments() {
        return garmentRepository.findAll();
    }

    // Get garment by ID
    public Garment getGarmentById(String garmentId) {
        return garmentRepository.findById(garmentId)
                .orElseThrow(() -> new RuntimeException("Garment not found: " + garmentId));
    }

    // Update garment
    @Transactional
    public Garment updateGarment(String garmentId, Garment garment) {
        Garment existing = getGarmentById(garmentId);
        existing.setGarmentType(garment.getGarmentType());
        existing.setSize(garment.getSize());
        return garmentRepository.save(existing);
    }

    // Delete garment
    @Transactional
    public void deleteGarment(String garmentId) {
        garmentRepository.deleteById(garmentId);
    }

    // ========== MOVEMENT OPERATIONS ==========

    // Add garment movement (Stock In/Shipped)
    @Transactional
    public GarmentMovement addMovement(GarmentMovement movement) {
        // Validate garment exists
        if (!garmentRepository.existsByGarmentId(movement.getGarmentId())) {
            throw new RuntimeException("Garment not found: " + movement.getGarmentId());
        }

        // Validate fabric exists (CRITICAL: fabric must exist to produce garments)
        if (!fabricRepository.existsByFabricId(movement.getFabricId())) {
            throw new RuntimeException("Fabric not found: " + movement.getFabricId() + ". Cannot produce garments without fabric.");
        }

        // Get current total quantity
        int currentTotal = getCurrentTotalQuantity(movement.getGarmentId());

        // Calculate new total based on status
        int newTotal;
        if ("In".equals(movement.getStatus())) {
            newTotal = currentTotal + movement.getQuantity();
        } else { // "Shipped"
            newTotal = currentTotal - movement.getQuantity();
            if (newTotal < 0) {
                throw new RuntimeException("Insufficient stock. Available: " + currentTotal + " pieces");
            }
        }

        // Auto-set movement date to today
        movement.setMovementDate(LocalDate.now());
        movement.setTotalQuantity(newTotal);

        return movementRepository.save(movement);
    }

    // Get all movements
    public List<GarmentMovement> getAllMovements() {
        return movementRepository.findAll();
    }

    // Get movements by garment ID
    public List<GarmentMovement> getMovementsByGarmentId(String garmentId) {
        return movementRepository.findByGarmentIdOrderByMovementDateDesc(garmentId);
    }

    // Get movement by ID
    public GarmentMovement getMovementById(String movementId) {
        return movementRepository.findById(movementId)
                .orElseThrow(() -> new RuntimeException("Movement not found: " + movementId));
    }

    // Delete movement
    @Transactional
    public void deleteMovement(String movementId) {
        movementRepository.deleteById(movementId);
    }

    // ========== DASHBOARD DATA ==========

    // Get current total quantity for a garment
    public int getCurrentTotalQuantity(String garmentId) {
        List<GarmentMovement> movements = movementRepository.findLatestMovementByGarmentId(garmentId);
        if (movements.isEmpty()) {
            return 0;
        }
        return movements.get(0).getTotalQuantity();
    }

    // Get garment stock summary (for dashboard)
    public Map<String, Object> getGarmentStockSummary() {
        List<Garment> allGarments = garmentRepository.findAll();
        Map<String, Object> summary = new HashMap<>();

        List<Map<String, Object>> stockDetails = new ArrayList<>();

        for (Garment garment : allGarments) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("garmentId", garment.getGarmentId());
            detail.put("type", garment.getGarmentType());
            detail.put("size", garment.getSize());
            detail.put("totalQuantity", getCurrentTotalQuantity(garment.getGarmentId()));
            stockDetails.add(detail);
        }

        summary.put("garments", stockDetails);
        return summary;
    }

    // Get low stock alerts (< 50 pieces) - includes size, color, fabric type, garment type
    public List<Map<String, Object>> getLowStockAlerts() {
        List<Garment> allGarments = garmentRepository.findAll();
        List<Map<String, Object>> lowStockItems = new ArrayList<>();

        for (Garment garment : allGarments) {
            int total = getCurrentTotalQuantity(garment.getGarmentId());
            if (total < 50 && total >= 0) {
                // Get the latest movement to find fabric details
                List<GarmentMovement> movements = movementRepository.findLatestMovementByGarmentId(garment.getGarmentId());

                String fabricId = movements.isEmpty() ? "N/A" : movements.get(0).getFabricId();
                String fabricType = "N/A";
                String color = "N/A";

                if (!fabricId.equals("N/A")) {
                    try {
                        Fabric fabric = fabricRepository.findById(fabricId).orElse(null);
                        if (fabric != null) {
                            fabricType = fabric.getFabricType();
                            color = fabric.getColor();
                        }
                    } catch (Exception e) {
                        // Fabric not found, use defaults
                    }
                }

                Map<String, Object> alert = new HashMap<>();
                alert.put("garmentId", garment.getGarmentId());
                alert.put("garmentType", garment.getGarmentType());
                alert.put("size", garment.getSize());
                alert.put("fabricType", fabricType);
                alert.put("color", color);
                alert.put("currentQuantity", total);
                alert.put("threshold", 50);
                alert.put("shortage", 50 - total);
                alert.put("description", garment.getSize() + " size " + color + " " + fabricType + " " + garment.getGarmentType());
                lowStockItems.add(alert);
            }
        }

        return lowStockItems;
    }

    // Get dashboard stats
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();

        // Total garment types
        stats.put("totalGarmentTypes", garmentRepository.findDistinctGarmentTypes().size());

        // Total pieces (sum all current totals)
        int totalPieces = garmentRepository.findAll().stream()
                .mapToInt(g -> getCurrentTotalQuantity(g.getGarmentId()))
                .sum();
        stats.put("totalPieces", totalPieces);

        // Stock in today
        int stockInToday = movementRepository.findStockInToday(today).stream()
                .mapToInt(GarmentMovement::getQuantity)
                .sum();
        stats.put("stockInToday", stockInToday);

        // Shipped today
        int shippedToday = movementRepository.findShippedToday(today).stream()
                .mapToInt(GarmentMovement::getQuantity)
                .sum();
        stats.put("shippedToday", shippedToday);

        // Low stock count
        stats.put("lowStockCount", getLowStockAlerts().size());

        return stats;
    }

    // Get chart data for garment quantities
    public Map<String, Object> getGarmentChartData() {
        List<Garment> allGarments = garmentRepository.findAll();
        Map<String, Object> chartData = new HashMap<>();

        List<String> labels = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();

        for (Garment garment : allGarments) {
            labels.add(garment.getGarmentType() + " (" + garment.getSize() + ")");
            quantities.add(getCurrentTotalQuantity(garment.getGarmentId()));
        }

        chartData.put("labels", labels);
        chartData.put("quantities", quantities);

        return chartData;
    }

    // Get garment type distribution for pie chart
    public Map<String, Object> getGarmentTypeDistribution() {
        List<Garment> allGarments = garmentRepository.findAll();
        Map<String, Integer> typeMap = new HashMap<>();

        for (Garment garment : allGarments) {
            int total = getCurrentTotalQuantity(garment.getGarmentId());
            typeMap.put(garment.getGarmentType(),
                    typeMap.getOrDefault(garment.getGarmentType(), 0) + total);
        }

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", new ArrayList<>(typeMap.keySet()));
        chartData.put("quantities", new ArrayList<>(typeMap.values()));

        return chartData;
    }
}
