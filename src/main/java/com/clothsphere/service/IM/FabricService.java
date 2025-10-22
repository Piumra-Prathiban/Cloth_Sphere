package com.clothsphere.service.IM;

import com.clothsphere.model.IM.Fabric;
import com.clothsphere.model.IM.FabricMovement;
import com.clothsphere.repository.IM.FabricRepository;
import com.clothsphere.repository.IM.FabricMovementRepository;
import com.clothsphere.strategy.IM.StockContext;
import com.clothsphere.strategy.IM.StockInStrategy;
import com.clothsphere.strategy.IM.StockOutStrategy;
import com.clothsphere.service.IM.FabricStock;
import com.clothsphere.observer.Fabric.LowStockAlert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class FabricService {

    @Autowired
    private FabricRepository fabricRepository;

    @Autowired
    private FabricMovementRepository fabricMovementRepository;

    private final StockContext stockContext = new StockContext();
    private final FabricStock fabricStock = new FabricStock();

    // -------------------- FABRIC METHODS --------------------

    public FabricService() {
        // Set low stock threshold to 50 meters (you can change this)
        fabricStock.addObserver(new LowStockAlert(10.0));
    }
    public List<Fabric> getAllFabrics() {
        return fabricRepository.findAll();
    }

    public Map<String, Object> getFabricStats() {
        return fabricRepository.getStats();
    }

    public boolean isDuplicateFabric(String type, String color) {
        return fabricRepository.existsByTypeAndColor(type, color);
    }

    public Fabric createFabric(Fabric fabric) {
        // Validate input
        if (fabric.getFabricType() == null || fabric.getFabricType().trim().isEmpty() ||
                fabric.getColor() == null || fabric.getColor().trim().isEmpty()) {
            throw new IllegalArgumentException("Fabric type and color are required");
        }

        // Check for duplicates
        if (fabricRepository.existsByTypeAndColor(fabric.getFabricType(), fabric.getColor())) {
            throw new IllegalArgumentException("Fabric with this type and color already exists");
        }

        // Validate initial stock
        if (fabric.getCurrentStock() == null || fabric.getCurrentStock() < 10) {
            throw new IllegalArgumentException("Initial stock must be at least 10 meters");
        }

        // Generate new ID if not provided
        if (fabric.getFabricId() == null || fabric.getFabricId().trim().isEmpty()) {
            String lastFabricId = fabricRepository.findLastFabricId();
            String newFabricId = generateId("FAB", lastFabricId);
            fabric.setFabricId(newFabricId);
        }

        // Insert fabric directly without creating duplicate movement
        fabricRepository.insertFabric(fabric);

        return fabric;
    }

    @Transactional
    public boolean deleteFabric(String fabricId) {
        try {
            // First check if fabric exists
            Fabric fabric = getFabricById(fabricId);
            if (fabric == null) {
                return false;
            }

            // Delete associated movements first (to avoid foreign key constraint)
            List<FabricMovement> movements = fabricMovementRepository.findMovementsByFabricId(fabricId);
            for (FabricMovement movement : movements) {
                fabricMovementRepository.deleteMovement(movement.getMovementId());
            }

            // Then delete the fabric
            int result = fabricRepository.deleteFabric(fabricId);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("Error deleting fabric: " + e.getMessage());
        }
    }

    public Fabric getFabricById(String fabricId) {
        return fabricRepository.findById(fabricId).orElse(null);
    }

    public List<Fabric> getLowStockFabrics() {
        return fabricRepository.findLowStock();
    }

    public List<Fabric> getFabricsByType(String type) {
        return fabricRepository.findByType(type);
    }

    public List<Fabric> getFabricsByColor(String color) {
        return fabricRepository.findByColor(color);
    }

    // -------------------- FABRIC MOVEMENT METHODS --------------------
    public List<FabricMovement> getAllMovements() {
        return fabricMovementRepository.findAll();
    }

    public FabricMovement addMovement(FabricMovement movement) {
        Fabric fabric = getFabricById(movement.getFabricId());
        if (fabric == null) throw new IllegalArgumentException("Fabric not found");
        return createMovement(fabric, movement.getStatus(), movement.getQuantity(), movement.getApprovedQuantity(), movement.getRejectedQuantity());
    }

    @Transactional
    public FabricMovement approveMovement(String movementId, String fabricId, double approved, double rejected, String rejectionReason) {
        FabricMovement movement = fabricMovementRepository.findById(movementId)
                .orElseThrow(() -> new IllegalArgumentException("Movement not found"));
        Fabric fabric = getFabricById(fabricId);

        if (fabric == null) {
            throw new IllegalArgumentException("Fabric not found");
        }

        // Validate quantities
        double totalQuantity = movement.getQuantity();
        if (Math.abs((approved + rejected) - totalQuantity) > 0.01) { // Allow small floating point differences
            throw new IllegalArgumentException("Approved + rejected quantity must equal total quantity");
        }

        // Store previous approved quantity for stock adjustment
        double previousApproved = movement.getApprovedQuantity();

        // Update movement with approval data
        movement.setApprovedQuantity(approved);
        movement.setRejectedQuantity(rejected);
        movement.setRejectionReason(rejectionReason);
        movement.setApprovalStatus("APPROVED");

        // Calculate net change for stock update
        double netChange = approved - previousApproved;

        // Update stock based on movement type
        if (netChange != 0) {
            if ("IN".equalsIgnoreCase(movement.getStatus())) {
                // For stock in: add the net change
                fabric.setCurrentStock(fabric.getCurrentStock() + netChange);
            } else if ("OUT".equalsIgnoreCase(movement.getStatus())) {
                // For stock out: subtract the net change
                fabric.setCurrentStock(fabric.getCurrentStock() - netChange);

                // Ensure stock doesn't go negative
                if (fabric.getCurrentStock() < 0) {
                    throw new IllegalArgumentException("Stock cannot be negative after movement");
                }
            }
        }

        // Save both
        fabricMovementRepository.updateMovement(movement);
        fabricRepository.updateFabric(fabric);

        return movement;
    }

    @Transactional
    public boolean deleteMovement(String movementId) {
        try {
            // First check if movement exists
            FabricMovement movement = fabricMovementRepository.findById(movementId).orElse(null);
            if (movement == null) {
                return false;
            }

            // If movement is approved, reverse the stock change
            if ("APPROVED".equals(movement.getApprovalStatus())) {
                Fabric fabric = getFabricById(movement.getFabricId());
                if (fabric != null) {
                    if ("IN".equalsIgnoreCase(movement.getStatus())) {
                        // Reverse stock in: subtract approved quantity
                        fabric.setCurrentStock(fabric.getCurrentStock() - movement.getApprovedQuantity());
                    } else if ("OUT".equalsIgnoreCase(movement.getStatus())) {
                        // Reverse stock out: add back approved quantity
                        fabric.setCurrentStock(fabric.getCurrentStock() + movement.getApprovedQuantity());
                    }
                    fabricRepository.updateFabric(fabric);
                }
            }

            // Delete the movement
            int result = fabricMovementRepository.deleteMovement(movementId);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("Error deleting movement: " + e.getMessage());
        }
    }

    public List<FabricMovement> getMovementsByDateRange(LocalDateTime start, LocalDateTime end) {
        return fabricMovementRepository.findByDateRange(start, end);
    }

    // -------------------- INTERNAL HELPER METHODS --------------------
    private FabricMovement createMovement(Fabric fabric, String status, double quantity, double approved, double rejected) {
        String lastMovementId = fabricMovementRepository.findLastMovementId();
        String newMovementId = generateId("FMI", lastMovementId);

        FabricMovement movement = new FabricMovement();
        movement.setMovementId(newMovementId);
        movement.setFabricId(fabric.getFabricId());
        movement.setStatus(status);
        movement.setQuantity(quantity);
        movement.setApprovedQuantity(approved);
        movement.setRejectedQuantity(rejected);
        movement.setMovementDate(LocalDate.now());
        movement.setApprovalStatus("PENDING");

        // Update stock using strategy only for approved quantities
        if (true) {  // Change this from (approved > 0) to just true
            if ("IN".equalsIgnoreCase(status)) {
                stockContext.setStrategy(new StockInStrategy());
                stockContext.executeStrategy(fabric, quantity);  // Use quantity instead of approved
            } else if ("OUT".equalsIgnoreCase(status)) {
                stockContext.setStrategy(new StockOutStrategy());
                stockContext.executeStrategy(fabric, quantity);  // Use quantity instead of approved
            }
            fabricRepository.updateFabric(fabric);
            fabricStock.checkStock(fabric.getFabricId(), fabric.getCurrentStock());
        }

        fabricMovementRepository.addMovement(movement);
        return movement;
    }

    public void checkAllFabricsStock() {
        List<Fabric> allFabrics = getAllFabrics();
        for (Fabric fabric : allFabrics) {
            fabricStock.checkStock(fabric.getFabricId(), fabric.getCurrentStock());
        }
    }

    private String generateId(String prefix, String lastId) {
        int number = 1;
        if (lastId != null && !lastId.isEmpty()) {
            number = Integer.parseInt(lastId.substring(3)) + 1;
        }
        return String.format("%s%03d", prefix, number);
    }
}