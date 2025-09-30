package com.clothsphere.service.Inventory;

import com.clothsphere.model.Inventory.FabricMovement;
import com.clothsphere.model.Inventory.Fabric;
import com.clothsphere.repository.Inventory.FabricRepository;
import com.clothsphere.repository.Inventory.FabricMovementRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FabricService {

    @Autowired
    private FabricRepository fabricRepository;

    @Autowired
    private FabricMovementRepository movementRepository;

    // ========== FABRIC OPERATIONS ==========

    // Add new fabric type
    @Transactional
    public Fabric addFabric(Fabric fabric) {
        if (fabricRepository.existsByFabricId(fabric.getFabricId())) {
            throw new RuntimeException("Fabric ID already exists: " + fabric.getFabricId());
        }
        return fabricRepository.save(fabric);
    }

    // Get all fabrics
    public List<Fabric> getAllFabrics() {
        return fabricRepository.findAll();
    }

    // Get fabric by ID
    public Fabric getFabricById(String fabricId) {
        return fabricRepository.findById(fabricId)
                .orElseThrow(() -> new RuntimeException("Fabric not found: " + fabricId));
    }

    // Update fabric
    @Transactional
    public Fabric updateFabric(String fabricId, Fabric fabric) {
        Fabric existing = getFabricById(fabricId);
        existing.setFabricType(fabric.getFabricType());
        existing.setColor(fabric.getColor());
        return fabricRepository.save(existing);
    }

    // Delete fabric
    @Transactional
    public void deleteFabric(String fabricId) {
        fabricRepository.deleteById(fabricId);
    }

    // ========== MOVEMENT OPERATIONS ==========

    // Add fabric movement (Stock In/Out)
    @Transactional
    public FabricMovement addMovement(FabricMovement movement) {
        // Validate fabric exists
        if (!fabricRepository.existsByFabricId(movement.getFabricId())) {
            throw new RuntimeException("Fabric not found: " + movement.getFabricId());
        }

        // Get current total quantity
        double currentTotal = getCurrentTotalQuantity(movement.getFabricId());

        // Calculate new total based on status
        double newTotal;
        if ("In".equals(movement.getStatus())) {
            newTotal = currentTotal + movement.getQuantity();
        } else { // "Out"
            newTotal = currentTotal - movement.getQuantity();
            if (newTotal < 0) {
                throw new RuntimeException("Insufficient stock. Available: " + currentTotal + " meters");
            }
        }

        movement.setTotalQuantity(newTotal);
        return movementRepository.save(movement);
    }

    // Get all movements
    public List<FabricMovement> getAllMovements() {
        return movementRepository.findAll();
    }

    // Get movements by fabric ID
    public List<FabricMovement> getMovementsByFabricId(String fabricId) {
        return movementRepository.findByFabricIdOrderByMovementDateDesc(fabricId);
    }

    // Get movement by ID
    public FabricMovement getMovementById(String movementId) {
        return movementRepository.findById(movementId)
                .orElseThrow(() -> new RuntimeException("Movement not found: " + movementId));
    }

    // Delete movement
    @Transactional
    public void deleteMovement(String movementId) {
        movementRepository.deleteById(movementId);
    }

    // ========== DASHBOARD DATA ==========

    // Get current total quantity for a fabric
    public double getCurrentTotalQuantity(String fabricId) {
        List<FabricMovement> movements = movementRepository.findLatestMovementByFabricId(fabricId);
        if (movements.isEmpty()) {
            return 0.0;
        }
        return movements.get(0).getTotalQuantity();
    }

    // Get fabric stock summary (for dashboard)
    public Map<String, Object> getFabricStockSummary() {
        List<Fabric> allFabrics = fabricRepository.findAll();
        Map<String, Object> summary = new HashMap<>();

        List<Map<String, Object>> stockDetails = new ArrayList<>();

        for (Fabric fabric : allFabrics) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("fabricId", fabric.getFabricId());
            detail.put("type", fabric.getFabricType());
            detail.put("color", fabric.getColor());
            detail.put("totalQuantity", getCurrentTotalQuantity(fabric.getFabricId()));
            stockDetails.add(detail);
        }

        summary.put("fabrics", stockDetails);
        return summary;
    }

    // Get low stock alerts (< 50 meters)
    public List<Map<String, Object>> getLowStockAlerts() {
        List<Fabric> allFabrics = fabricRepository.findAll();
        List<Map<String, Object>> lowStockItems = new ArrayList<>();

        for (Fabric fabric : allFabrics) {
            double total = getCurrentTotalQuantity(fabric.getFabricId());
            if (total < 50.0 && total >= 0) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("fabricId", fabric.getFabricId());
                alert.put("type", fabric.getFabricType());
                alert.put("color", fabric.getColor());
                alert.put("currentQuantity", total);
                alert.put("threshold", 50.0);
                alert.put("shortage", 50.0 - total);
                lowStockItems.add(alert);
            }
        }

        return lowStockItems;
    }

    // Get dashboard stats
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();

        // Total fabric types
        stats.put("totalFabricTypes", fabricRepository.findDistinctFabricTypes().size());

        // Total meters (sum all current totals)
        double totalMeters = fabricRepository.findAll().stream()
                .mapToDouble(f -> getCurrentTotalQuantity(f.getFabricId()))
                .sum();
        stats.put("totalMeters", totalMeters);

        // Stock in today
        double stockInToday = movementRepository.findStockInToday(today).stream()
                .mapToDouble(FabricMovement::getQuantity)
                .sum();
        stats.put("stockInToday", stockInToday);

        // Stock out today
        double stockOutToday = movementRepository.findStockOutToday(today).stream()
                .mapToDouble(FabricMovement::getQuantity)
                .sum();
        stats.put("stockOutToday", stockOutToday);

        // Low stock count
        stats.put("lowStockCount", getLowStockAlerts().size());

        return stats;
    }

    // Get chart data for fabric quantities
    public Map<String, Object> getFabricChartData() {
        List<Fabric> allFabrics = fabricRepository.findAll();
        Map<String, Object> chartData = new HashMap<>();

        List<String> labels = new ArrayList<>();
        List<Double> quantities = new ArrayList<>();

        for (Fabric fabric : allFabrics) {
            labels.add(fabric.getFabricType() + " - " + fabric.getColor());
            quantities.add(getCurrentTotalQuantity(fabric.getFabricId()));
        }

        chartData.put("labels", labels);
        chartData.put("quantities", quantities);

        return chartData;
    }
}