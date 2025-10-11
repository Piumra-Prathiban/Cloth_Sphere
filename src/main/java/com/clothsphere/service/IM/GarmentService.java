package com.clothsphere.service.IM;

import com.clothsphere.model.IM.Garment;
import com.clothsphere.model.IM.GarmentMovement;
import com.clothsphere.repository.IM.GarmentRepository;
import com.clothsphere.repository.IM.GarmentMovementRepository;
import com.clothsphere.strategy.IM.Garment.StockContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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

    @Autowired
    private NamedParameterJdbcTemplate namedJdbcTemplate;

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

        if (garment == null) {
            throw new IllegalArgumentException("Garment not found with id: " + garmentId);
        }

        // Validate that approved + rejected equals original quantity
        int originalQuantity = movement.getQuantity();
        if (approved + rejected != originalQuantity) {
            throw new IllegalArgumentException(
                    String.format("Approved (%d) + Rejected (%d) must equal original quantity (%d)",
                            approved, rejected, originalQuantity)
            );
        }

        // Update movement with approval data
        movement.setApprovedQuantity(approved);
        movement.setRejectedQuantity(rejected);
        movement.setRejectionReason(rejectionReason);

        // Set appropriate approval status
        String approvalStatus;
        if (approved == 0 && rejected > 0) {
            approvalStatus = "REJECTED";
        } else if (approved > 0 && rejected > 0) {
            approvalStatus = "PARTIALLY_APPROVED";
        } else if (approved > 0 && rejected == 0) {
            approvalStatus = "APPROVED";
        } else {
            approvalStatus = "PENDING";
        }
        movement.setApprovalStatus(approvalStatus);

        // Calculate current stock for the garment (only approved quantities affect stock)
        int currentStock = garmentRepository.calculateCurrentStock(garmentId);

        // Update movement with total stock
        movement.setTotalStock(currentStock);
        movement.setUpdatedAt(LocalDateTime.now());

        // Save the updated movement
        garmentMovementRepository.updateMovement(movement);

        // SYNC THE GARMENT STOCK
        syncGarmentStock(garmentId);

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
// In GarmentService.java - Ensure stock calculation is correct
    public int getCurrentStock(String garmentId) {
        try {
            // Calculate stock from APPROVED movements only
            String sql = "SELECT " +
                    "COALESCE(SUM(CASE WHEN status = 'In' AND approval_status IN ('APPROVED', 'PARTIALLY_APPROVED') THEN approved_quantity ELSE 0 END), 0) - " +
                    "COALESCE(SUM(CASE WHEN status = 'Shipped' AND approval_status IN ('APPROVED', 'PARTIALLY_APPROVED') THEN approved_quantity ELSE 0 END), 0) " +
                    "FROM garment_movements " +
                    "WHERE garment_id = :garmentId";

            MapSqlParameterSource params = new MapSqlParameterSource().addValue("garmentId", garmentId);

            Integer stock = namedJdbcTemplate.queryForObject(sql, params, Integer.class);
            return stock != null ? Math.max(stock, 0) : 0; // Ensure stock doesn't go negative
        } catch (Exception e) {
            System.err.println("Error calculating stock for garment " + garmentId + ": " + e.getMessage());
            return 0;
        }
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

        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        movement.setCreatedAt(now);
        movement.setUpdatedAt(now);

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


    @Transactional
    public GarmentMovement createInitialStockMovement(String garmentId, Integer initialStock) {
        Garment garment = getGarmentById(garmentId);
        if (garment == null) {
            throw new IllegalArgumentException("Garment not found");
        }

        String lastMovementId = garmentMovementRepository.findLastMovementId();
        String newMovementId = generateId("GMI", lastMovementId);

        GarmentMovement movement = new GarmentMovement();
        movement.setMovementId(newMovementId);
        movement.setGarmentId(garmentId);
        movement.setFabricId(garment.getFabricId());
        movement.setStatus("In");
        movement.setQuantity(initialStock);
        movement.setApprovedQuantity(initialStock); // Auto-approve initial stock
        movement.setRejectedQuantity(0);
        movement.setMovementDate(LocalDate.now());
        movement.setTotalStock(initialStock);
        movement.setApprovalStatus("APPROVED");

        // Set timestamps explicitly
        LocalDateTime now = LocalDateTime.now();
        movement.setCreatedAt(now);
        movement.setUpdatedAt(now);

        garmentMovementRepository.addMovement(movement);

        // SYNC THE GARMENT STOCK
        garmentRepository.updateGarmentStock(garmentId, initialStock);

        return movement;
    }

    @Transactional
    public void syncGarmentStock(String garmentId) {
        int currentStock = getCurrentStock(garmentId);

        // Update the garment's current_stock field
        String sql = "UPDATE garments SET current_stock = :currentStock, updated_at = :updatedAt WHERE garment_id = :garmentId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("currentStock", currentStock)
                .addValue("updatedAt", LocalDateTime.now())
                .addValue("garmentId", garmentId);

        // You'll need to inject NamedParameterJdbcTemplate or add this method to GarmentRepository
        namedJdbcTemplate.update(sql, params);
    }



}