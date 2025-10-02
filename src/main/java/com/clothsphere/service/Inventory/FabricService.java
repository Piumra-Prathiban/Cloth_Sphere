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

@Service
public class FabricService {

    @Autowired
    private FabricRepository fabricRepository;

    @Autowired
    private FabricMovementRepository movementRepository;

    // ========== FABRIC OPERATIONS ==========

    // Add new fabric type - ONLY CHECK FABRIC ID, NOT TYPE/COLOR COMBINATION
    @Transactional
    public Fabric addFabric(Fabric fabric) {
        // Only check if Fabric ID already exists
        if (fabricRepository.existsByFabricId(fabric.getFabricId())) {
            throw new RuntimeException("Fabric ID already exists: " + fabric.getFabricId());
        }

        // REMOVED: duplicate type/color check
        // We allow FAB001 Blue Cotton and FAB002 Blue Cotton

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
        if (fabric.getLowStockThreshold() != null) {
            existing.setLowStockThreshold(fabric.getLowStockThreshold());
        }
        if (fabric.getReorderLevel() != null) {
            existing.setReorderLevel(fabric.getReorderLevel());
        }
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

        // Set movement date to today if not provided
        if (movement.getMovementDate() == null) {
            movement.setMovementDate(LocalDate.now());
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

    // Get low stock alerts - ONLY for fabrics with existing movements
    // NOT for newly added fabrics with 0 stock
    public List<Map<String, Object>> getLowStockAlerts() {
        List<Fabric> allFabrics = fabricRepository.findAll();
        List<Map<String, Object>> lowStockItems = new ArrayList<>();

        for (Fabric fabric : allFabrics) {
            double total = getCurrentTotalQuantity(fabric.getFabricId());
            double threshold = fabric.getLowStockThreshold() != null ? fabric.getLowStockThreshold() : 50.0;

            // ONLY alert if fabric has movements AND is below threshold
            // Skip newly added fabrics with 0 stock (no movements)
            boolean hasMovements = !movementRepository.findByFabricIdOrderByMovementDateDesc(fabric.getFabricId()).isEmpty();

            if (hasMovements && total > 0 && total < threshold) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("fabricId", fabric.getFabricId());
                alert.put("type", fabric.getFabricType());
                alert.put("color", fabric.getColor());
                alert.put("currentQuantity", total);
                alert.put("threshold", threshold);
                alert.put("shortage", threshold - total);
                alert.put("reorderLevel", fabric.getReorderLevel() != null ? fabric.getReorderLevel() : 100.0);
                alert.put("criticalLevel", total < (threshold * 0.5));
                lowStockItems.add(alert);
            }
        }

        // Sort by criticality and shortage amount
        lowStockItems.sort((a, b) -> {
            boolean aCritical = (boolean) a.get("criticalLevel");
            boolean bCritical = (boolean) b.get("criticalLevel");
            if (aCritical != bCritical) {
                return bCritical ? 1 : -1;
            }
            double aShortage = (double) a.get("shortage");
            double bShortage = (double) b.get("shortage");
            return Double.compare(bShortage, aShortage);
        });

        return lowStockItems;
    }

    // Get dashboard stats
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();

        // Total fabric types
        stats.put("totalFabricTypes", fabricRepository.count());

        // Total meters (sum all current totals)
        double totalMeters = fabricRepository.findAll().stream()
                .mapToDouble(f -> getCurrentTotalQuantity(f.getFabricId()))
                .sum();
        stats.put("totalMeters", Math.round(totalMeters * 100.0) / 100.0);

        // Stock in today
        double stockInToday = movementRepository.findStockInToday(today).stream()
                .mapToDouble(FabricMovement::getQuantity)
                .sum();
        stats.put("stockInToday", Math.round(stockInToday * 100.0) / 100.0);

        // Stock out today
        double stockOutToday = movementRepository.findStockOutToday(today).stream()
                .mapToDouble(FabricMovement::getQuantity)
                .sum();
        stats.put("stockOutToday", Math.round(stockOutToday * 100.0) / 100.0);

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
            labels.add(fabric.getFabricId() + " - " + fabric.getFabricType() + " (" + fabric.getColor() + ")");
            quantities.add(getCurrentTotalQuantity(fabric.getFabricId()));
        }

        chartData.put("labels", labels);
        chartData.put("quantities", quantities);

        return chartData;
    }

    // ========== CATEGORY OPERATIONS ==========

    // Get all distinct fabric types
    public List<String> getAllFabricTypes() {
        return fabricRepository.findDistinctFabricTypes();
    }

    // Get fabrics by type with stock information
    public List<Map<String, Object>> getFabricsByType(String fabricType) {
        List<Fabric> fabrics = fabricRepository.findByFabricType(fabricType);
        List<Map<String, Object>> fabricsWithStock = new ArrayList<>();

        for (Fabric fabric : fabrics) {
            Map<String, Object> fabricData = new HashMap<>();
            fabricData.put("fabricId", fabric.getFabricId());
            fabricData.put("fabricType", fabric.getFabricType());
            fabricData.put("color", fabric.getColor());
            double currentStock = getCurrentTotalQuantity(fabric.getFabricId());
            fabricData.put("currentStock", currentStock);
            fabricData.put("lowStockThreshold", fabric.getLowStockThreshold());
            fabricData.put("reorderLevel", fabric.getReorderLevel());
            fabricData.put("status", currentStock < fabric.getLowStockThreshold() ? "Low" : "Normal");
            fabricsWithStock.add(fabricData);
        }

        return fabricsWithStock;
    }

    // Get fabric category summary (grouped by type)
    public Map<String, Object> getFabricCategorySummary() {
        List<String> types = getAllFabricTypes();
        Map<String, Object> summary = new HashMap<>();
        List<Map<String, Object>> categoryData = new ArrayList<>();

        for (String type : types) {
            List<Fabric> fabricsOfType = fabricRepository.findByFabricType(type);

            Map<String, Object> typeData = new HashMap<>();
            typeData.put("fabricType", type);
            typeData.put("totalCount", fabricsOfType.size());

            double totalStock = 0;
            int lowStockCount = 0;

            for (Fabric fabric : fabricsOfType) {
                double stock = getCurrentTotalQuantity(fabric.getFabricId());
                totalStock += stock;
                double threshold = fabric.getLowStockThreshold() != null ? fabric.getLowStockThreshold() : 50.0;
                if (stock < threshold && stock > 0) {
                    lowStockCount++;
                }
            }

            typeData.put("totalStock", totalStock);
            typeData.put("lowStockCount", lowStockCount);
            typeData.put("averageStock", fabricsOfType.size() > 0 ? totalStock / fabricsOfType.size() : 0);

            categoryData.add(typeData);
        }

        summary.put("categories", categoryData);
        summary.put("totalCategories", types.size());

        return summary;
    }
}