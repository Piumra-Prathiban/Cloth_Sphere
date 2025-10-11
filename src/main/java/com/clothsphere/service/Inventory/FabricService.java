package com.clothsphere.service.Inventory;

import com.clothsphere.model.Inventory.Fabric;
import com.clothsphere.model.Inventory.FabricMovement;
import com.clothsphere.repository.Inventory.FabricRepository;
import com.clothsphere.repository.Inventory.FabricMovementRepository;
import com.clothsphere.strategy.StockContext;
import com.clothsphere.strategy.StockInStrategy;
import com.clothsphere.strategy.StockOutStrategy;
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

    // -------------------- FABRIC METHODS --------------------
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

        // Use the existing addNewFabric method
        return addNewFabric(fabric.getFabricType(), fabric.getColor(),
                fabric.getCurrentStock(), fabric.getCurrentStock(), 0.0);
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

        // Update movement with approval data
        movement.setApprovedQuantity(approved);
        movement.setRejectedQuantity(rejected);
        movement.setRejectionReason(rejectionReason);
        movement.setApprovalStatus("APPROVED");

        // Update stock using strategy
        if ("IN".equalsIgnoreCase(movement.getStatus())) {
            stockContext.setStrategy(new StockInStrategy());
            stockContext.executeStrategy(fabric, approved);
        } else if ("OUT".equalsIgnoreCase(movement.getStatus())) {
            stockContext.setStrategy(new StockOutStrategy());
            stockContext.executeStrategy(fabric, approved);
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
    private Fabric addNewFabric(String type, String color, double quantity, double approved, double rejected) {
        if (quantity < 10) throw new IllegalArgumentException("New fabric stock must be at least 10");

        // Generate new ID
        String lastFabricId = fabricRepository.findLastFabricId();
        String newFabricId = generateId("FAB", lastFabricId);

        Fabric fabric = new Fabric(newFabricId, type, color, quantity); // Set initial stock directly
        fabricRepository.insertFabric(fabric);

        // Create initial movement to track the stock
        createMovement(fabric, "IN", quantity, approved, rejected);

        return fabric;
    }

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
        if (approved > 0) {
            if ("IN".equalsIgnoreCase(status)) {
                stockContext.setStrategy(new StockInStrategy());
                stockContext.executeStrategy(fabric, approved);
            } else if ("OUT".equalsIgnoreCase(status)) {
                stockContext.setStrategy(new StockOutStrategy());
                stockContext.executeStrategy(fabric, approved);
            }
            fabricRepository.updateFabric(fabric);
        }

        fabricMovementRepository.addMovement(movement);
        return movement;
    }

    private String generateId(String prefix, String lastId) {
        int number = 1;
        if (lastId != null && !lastId.isEmpty()) {
            number = Integer.parseInt(lastId.substring(3)) + 1;
        }
        return String.format("%s%03d", prefix, number);
    }
}