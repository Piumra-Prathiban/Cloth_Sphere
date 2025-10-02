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
        // REMOVED: Check for duplicate type+size combination - Allow different IDs with same type+size
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
        existing.setLowStockThreshold(garment.getLowStockThreshold());
        existing.setReorderLevel(garment.getReorderLevel());
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

    // Get low stock alerts (dynamic threshold per garment) - FIXED: Exclude garments with movements
    public List<Map<String, Object>> getLowStockAlerts() {
        List<Garment> allGarments = garmentRepository.findAll();
        List<Map<String, Object>> lowStockItems = new ArrayList<>();

        for (Garment garment : allGarments) {
            // Check if garment has any movements
            List<GarmentMovement> movements = movementRepository.findLatestMovementByGarmentId(garment.getGarmentId());

            // Skip garments with no movements (newly created)
            if (movements.isEmpty()) {
                continue;
            }

            int total = getCurrentTotalQuantity(garment.getGarmentId());
            int threshold = garment.getLowStockThreshold() != null ? garment.getLowStockThreshold() : 50;

            if (total < threshold && total >= 0) {
                String fabricId = movements.get(0).getFabricId();
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
                alert.put("threshold", threshold);
                alert.put("shortage", threshold - total);
                alert.put("reorderLevel", garment.getReorderLevel() != null ? garment.getReorderLevel() : 100);
                alert.put("criticalLevel", total < (threshold * 0.5));
                alert.put("description", garment.getSize() + " size " + color + " " + fabricType + " " + garment.getGarmentType());
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
            int aShortage = (int) a.get("shortage");
            int bShortage = (int) b.get("shortage");
            return Integer.compare(bShortage, aShortage);
        });

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

    // ========== CATEGORY OPERATIONS ==========

    // Get all distinct garment types
    public List<String> getAllGarmentTypes() {
        return garmentRepository.findDistinctGarmentTypes();
    }

    // Get all distinct sizes
    public List<String> getAllSizes() {
        return garmentRepository.findDistinctSizes();
    }

    // Get garments by type with stock information
    public List<Map<String, Object>> getGarmentsByType(String garmentType) {
        List<Garment> garments = garmentRepository.findByGarmentType(garmentType);
        List<Map<String, Object>> garmentsWithStock = new ArrayList<>();

        for (Garment garment : garments) {
            Map<String, Object> garmentData = new HashMap<>();
            garmentData.put("garmentId", garment.getGarmentId());
            garmentData.put("garmentType", garment.getGarmentType());
            garmentData.put("size", garment.getSize());
            int currentStock = getCurrentTotalQuantity(garment.getGarmentId());
            garmentData.put("currentStock", currentStock);
            garmentData.put("lowStockThreshold", garment.getLowStockThreshold());
            garmentData.put("reorderLevel", garment.getReorderLevel());
            garmentData.put("status", currentStock < garment.getLowStockThreshold() ? "Low" : "Normal");
            garmentsWithStock.add(garmentData);
        }

        return garmentsWithStock;
    }

    // Get garments by size with stock information
    public List<Map<String, Object>> getGarmentsBySize(String size) {
        List<Garment> garments = garmentRepository.findBySize(size);
        List<Map<String, Object>> garmentsWithStock = new ArrayList<>();

        for (Garment garment : garments) {
            Map<String, Object> garmentData = new HashMap<>();
            garmentData.put("garmentId", garment.getGarmentId());
            garmentData.put("garmentType", garment.getGarmentType());
            garmentData.put("size", garment.getSize());
            int currentStock = getCurrentTotalQuantity(garment.getGarmentId());
            garmentData.put("currentStock", currentStock);
            garmentData.put("lowStockThreshold", garment.getLowStockThreshold());
            garmentData.put("reorderLevel", garment.getReorderLevel());
            garmentData.put("status", currentStock < garment.getLowStockThreshold() ? "Low" : "Normal");
            garmentsWithStock.add(garmentData);
        }

        return garmentsWithStock;
    }

    // Get garment category summary (grouped by type and size)
    public Map<String, Object> getGarmentCategorySummary() {
        List<String> types = getAllGarmentTypes();
        List<String> sizes = getAllSizes();

        Map<String, Object> summary = new HashMap<>();
        List<Map<String, Object>> typeData = new ArrayList<>();
        List<Map<String, Object>> sizeData = new ArrayList<>();

        // Summary by type
        for (String type : types) {
            List<Garment> garmentsOfType = garmentRepository.findByGarmentType(type);

            Map<String, Object> data = new HashMap<>();
            data.put("garmentType", type);
            data.put("totalCount", garmentsOfType.size());

            int totalStock = 0;
            int lowStockCount = 0;

            for (Garment garment : garmentsOfType) {
                int stock = getCurrentTotalQuantity(garment.getGarmentId());
                totalStock += stock;
                int threshold = garment.getLowStockThreshold() != null ? garment.getLowStockThreshold() : 50;
                if (stock < threshold) {
                    lowStockCount++;
                }
            }

            data.put("totalStock", totalStock);
            data.put("lowStockCount", lowStockCount);
            data.put("averageStock", garmentsOfType.size() > 0 ? totalStock / garmentsOfType.size() : 0);

            typeData.add(data);
        }

        // Summary by size
        for (String size : sizes) {
            List<Garment> garmentsOfSize = garmentRepository.findBySize(size);

            Map<String, Object> data = new HashMap<>();
            data.put("size", size);
            data.put("totalCount", garmentsOfSize.size());

            int totalStock = 0;
            int lowStockCount = 0;

            for (Garment garment : garmentsOfSize) {
                int stock = getCurrentTotalQuantity(garment.getGarmentId());
                totalStock += stock;
                int threshold = garment.getLowStockThreshold() != null ? garment.getLowStockThreshold() : 50;
                if (stock < threshold) {
                    lowStockCount++;
                }
            }

            data.put("totalStock", totalStock);
            data.put("lowStockCount", lowStockCount);
            data.put("averageStock", garmentsOfSize.size() > 0 ? totalStock / garmentsOfSize.size() : 0);

            sizeData.add(data);
        }

        summary.put("byType", typeData);
        summary.put("bySize", sizeData);
        summary.put("totalTypes", types.size());
        summary.put("totalSizes", sizes.size());

        return summary;
    }
}