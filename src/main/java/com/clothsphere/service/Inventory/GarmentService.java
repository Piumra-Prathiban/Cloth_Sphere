package com.clothsphere.service.Inventory;

import com.clothsphere.model.Inventory.Garment;
import com.clothsphere.model.Inventory.GarmentMovement;
import com.clothsphere.repository.Inventory.GarmentRepository;
import com.clothsphere.repository.Inventory.GarmentMovementRepository;
import com.clothsphere.strategy.Garment.StockContext;
import com.clothsphere.strategy.Garment.StockInStrategy;
import com.clothsphere.strategy.Garment.StockOutStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class GarmentService {

    @Autowired
    private GarmentRepository garmentRepository;

    @Autowired
    private GarmentMovementRepository garmentMovementRepository;

    private final StockContext stockContext = new StockContext();

    // -------------------- GARMENT METHODS --------------------
    public List<Garment> getAllGarments() {
        return garmentRepository.findAll();
    }

    public Map<String, Object> getGarmentStats() {
        return garmentRepository.getStats();
    }

    public boolean isDuplicateGarment(String type, String size, String fabricId) {
        return garmentRepository.existsByTypeAndSizeAndFabric(type, size, fabricId);
    }

    public Garment createGarment(Garment garment) {
        // Validate input
        if (garment.getType() == null || garment.getType().trim().isEmpty() ||
                garment.getSize() == null || garment.getSize().trim().isEmpty() ||
                garment.getFabricId() == null || garment.getFabricId().trim().isEmpty()) {
            throw new IllegalArgumentException("Garment type, size, and fabric ID are required");
        }

        // Check for duplicates
        if (garmentRepository.existsByTypeAndSizeAndFabric(garment.getType(), garment.getSize(), garment.getFabricId())) {
            throw new IllegalArgumentException("Garment with this type, size, and fabric already exists");
        }

        // Use the existing addNewGarment method
        return addNewGarment(garment.getType(), garment.getSize(), garment.getFabricId());
    }

    @Transactional
    public boolean deleteGarment(String garmentId) {
        try {
            // First check if garment exists
            Garment garment = getGarmentById(garmentId);
            if (garment == null) {
                return false;
            }

            // Delete associated movements first (to avoid foreign key constraint)
            List<GarmentMovement> movements = garmentMovementRepository.findByGarmentId(garmentId);
            for (GarmentMovement movement : movements) {
                garmentMovementRepository.deleteMovement(movement.getMovementId());
            }

            // Then delete the garment
            int result = garmentRepository.deleteGarment(garmentId);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("Error deleting garment: " + e.getMessage());
        }
    }

    public Garment getGarmentById(String garmentId) {
        return garmentRepository.findById(garmentId).orElse(null);
    }

    public List<Garment> getLowStockGarments() {
        return garmentRepository.findLowStock(10);
    }

    public List<Garment> getLowStockGarments(int threshold) {
        return garmentRepository.findLowStock(threshold);
    }

    public List<Garment> getGarmentsByType(String type) {
        return garmentRepository.findByType(type);
    }

    public List<Garment> getGarmentsBySize(String size) {
        return garmentRepository.findBySize(size);
    }

    public List<Garment> getGarmentsByTypeAndSize(String type, String size) {
        return garmentRepository.findByTypeAndSize(type, size);
    }

    // -------------------- GARMENT MOVEMENT METHODS --------------------
    public List<GarmentMovement> getAllMovements() {
        return garmentMovementRepository.findAll();
    }

    // ADDED: Missing method for getting movement by ID
    public GarmentMovement getMovementById(String movementId) {
        return garmentMovementRepository.findById(movementId).orElse(null);
    }

    public List<GarmentMovement> getMovementsByGarmentId(String garmentId) {
        return garmentMovementRepository.findByGarmentId(garmentId);
    }

    // ADDED: Missing method for getting movements by approval status
    public List<GarmentMovement> getMovementsByApprovalStatus(String approvalStatus) {
        return garmentMovementRepository.findByApprovalStatus(approvalStatus);
    }

    public List<GarmentMovement> getMovementsByStatus(String status) {
        return garmentMovementRepository.findByStatus(status);
    }

    public List<GarmentMovement> getMovementsByDateRange(LocalDate start, LocalDate end) {
        return garmentMovementRepository.findByDateRange(start, end);
    }

    public GarmentMovement addMovement(GarmentMovement movement) {
        Garment garment = getGarmentById(movement.getGarmentId());
        if (garment == null) throw new IllegalArgumentException("Garment not found");
        return createMovement(garment, movement.getStatus(), movement.getQuantity(),
                movement.getApprovedQuantity(), movement.getRejectedQuantity());
    }

    @Transactional
    public GarmentMovement approveMovement(String movementId, String garmentId, int approved, int rejected, String rejectionReason) {
        GarmentMovement movement = garmentMovementRepository.findById(movementId)
                .orElseThrow(() -> new IllegalArgumentException("Movement not found"));
        Garment garment = getGarmentById(garmentId);

        // Update movement with approval data
        movement.setApprovedQuantity(approved);
        movement.setRejectedQuantity(rejected);
        movement.setRejectionReason(rejectionReason);

        String approvalStatus = rejected > 0 ? "PARTIALLY_APPROVED" : "APPROVED";
        movement.setApprovalStatus(approvalStatus);

        // Calculate current stock for the garment
        int currentStock = garmentRepository.calculateCurrentStock(garmentId);

        // Update movement with total stock
        movement.setTotalStock(currentStock);

        // Save the updated movement
        garmentMovementRepository.updateApproval(movementId, approved, rejected, rejectionReason, currentStock);

        return movement;
    }

    @Transactional
    public boolean deleteMovement(String movementId) {
        try {
            // First check if movement exists
            GarmentMovement movement = garmentMovementRepository.findById(movementId).orElse(null);
            if (movement == null) {
                return false;
            }

            // Note: Since garment stock is calculated dynamically from movements,
            // we don't need to reverse stock changes when deleting movements
            // The stock will be automatically recalculated

            // Delete the movement
            int result = garmentMovementRepository.deleteMovement(movementId);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("Error deleting movement: " + e.getMessage());
        }
    }

    // -------------------- STOCK MANAGEMENT --------------------
    public int getCurrentStock(String garmentId) {
        return garmentRepository.calculateCurrentStock(garmentId);
    }

    // -------------------- INTERNAL HELPER METHODS --------------------
    private Garment addNewGarment(String type, String size, String fabricId) {
        // Generate new ID
        String lastGarmentId = garmentRepository.findLastGarmentId();
        String newGarmentId = generateId("GM", lastGarmentId);

        Garment garment = new Garment();
        garment.setGarmentId(newGarmentId);
        garment.setType(type);
        garment.setSize(size);
        garment.setFabricId(fabricId);
        garment.setCreatedAt(LocalDateTime.now());
        garment.setUpdatedAt(LocalDateTime.now());

        garmentRepository.insertGarment(garment);
        return garment;
    }

    private GarmentMovement createMovement(Garment garment, String status, int quantity, int approved, int rejected) {
        String lastMovementId = garmentMovementRepository.findLastMovementId();
        String newMovementId = generateId("GMI", lastMovementId);

        // Calculate current stock before this movement
        int currentStock = garmentRepository.calculateCurrentStock(garment.getGarmentId());

        GarmentMovement movement = new GarmentMovement();
        movement.setMovementId(newMovementId);
        movement.setGarmentId(garment.getGarmentId());
        movement.setFabricId(garment.getFabricId());
        movement.setStatus(status);
        movement.setQuantity(quantity);
        movement.setApprovedQuantity(approved);
        movement.setRejectedQuantity(rejected);
        movement.setMovementDate(LocalDate.now());
        movement.setTotalStock(currentStock);

        // Set approval status based on whether quantities are provided
        if (approved > 0 || rejected > 0) {
            String approvalStatus = rejected > 0 ? "PARTIALLY_APPROVED" : "APPROVED";
            movement.setApprovalStatus(approvalStatus);
        } else {
            movement.setApprovalStatus("PENDING");
        }

        garmentMovementRepository.addMovement(movement);
        return movement;
    }

    private String generateId(String prefix, String lastId) {
        int number = 1;
        if (lastId != null && !lastId.isEmpty()) {
            number = Integer.parseInt(lastId.substring(prefix.length())) + 1;
        }
        return String.format("%s%03d", prefix, number);
    }

    // -------------------- UPDATE GARMENT METHOD --------------------
    public Garment updateGarment(String garmentId, Garment garmentDetails) {
        Garment garment = getGarmentById(garmentId);
        if (garment == null) {
            throw new IllegalArgumentException("Garment not found with id: " + garmentId);
        }

        // Update fields if provided
        if (garmentDetails.getType() != null && !garmentDetails.getType().trim().isEmpty()) {
            garment.setType(garmentDetails.getType());
        }
        if (garmentDetails.getSize() != null && !garmentDetails.getSize().trim().isEmpty()) {
            garment.setSize(garmentDetails.getSize());
        }
        if (garmentDetails.getFabricId() != null && !garmentDetails.getFabricId().trim().isEmpty()) {
            garment.setFabricId(garmentDetails.getFabricId());
        }

        garment.setUpdatedAt(LocalDateTime.now());
        garmentRepository.updateGarment(garment);
        return garment;
    }

    // -------------------- ADDITIONAL METHODS FOR MAP RESULTS --------------------
    public List<Map<String, Object>> getAllGarmentsWithCurrentStock() {
        return garmentRepository.findAllWithCurrentStock();
    }

    public Optional<Map<String, Object>> getGarmentWithCurrentStock(String garmentId) {
        return garmentRepository.findByIdWithCurrentStock(garmentId);
    }
}