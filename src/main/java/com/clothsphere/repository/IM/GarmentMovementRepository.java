package com.clothsphere.repository.IM;

import com.clothsphere.model.IM.GarmentMovement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class GarmentMovementRepository {

    @Autowired
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    // ---------------- RowMapper ----------------
    private final RowMapper<GarmentMovement> movementRowMapper = (ResultSet rs, int rowNum) -> {
        GarmentMovement movement = new GarmentMovement();
        movement.setMovementId(rs.getString("movement_id"));
        movement.setGarmentId(rs.getString("garment_id"));
        movement.setFabricId(rs.getString("fabric_id"));
        movement.setStatus(rs.getString("status"));

        // Handle date conversion safely
        java.sql.Date movementDate = rs.getDate("movement_date");
        if (movementDate != null) {
            movement.setMovementDate(movementDate.toLocalDate());
        }

        movement.setQuantity(rs.getInt("quantity"));
        movement.setApprovedQuantity(rs.getInt("approved_quantity"));
        movement.setRejectedQuantity(rs.getInt("rejected_quantity"));
        movement.setTotalStock(rs.getInt("total_stock"));
        movement.setApprovalStatus(rs.getString("approval_status"));
        movement.setRejectionReason(rs.getString("rejection_reason"));

        return movement;
    };

    // ---------------- CRUD Operations ----------------
    public List<GarmentMovement> findAll() {
        String sql = "SELECT * FROM garment_movements ORDER BY movement_id";
        return namedJdbcTemplate.query(sql, movementRowMapper);
    }

    public Optional<GarmentMovement> findById(String movementId) {
        String sql = "SELECT * FROM garment_movements WHERE movement_id = :movementId";
        try {
            MapSqlParameterSource params = new MapSqlParameterSource().addValue("movementId", movementId);
            GarmentMovement movement = namedJdbcTemplate.queryForObject(sql, params, movementRowMapper);
            return Optional.ofNullable(movement);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public int addMovement(GarmentMovement movement) {
        String sql = "INSERT INTO garment_movements (movement_id, garment_id, fabric_id, status, movement_date, quantity, " +
                "approved_quantity, rejected_quantity, total_stock, approval_status, rejection_reason, created_at, updated_at) " +
                "VALUES (:movementId, :garmentId, :fabricId, :status, :movementDate, :quantity, " +
                ":approvedQuantity, :rejectedQuantity, :totalStock, :approvalStatus, :rejectionReason, :createdAt, :updatedAt)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("movementId", movement.getMovementId())
                .addValue("garmentId", movement.getGarmentId())
                .addValue("fabricId", movement.getFabricId())
                .addValue("status", movement.getStatus())
                .addValue("movementDate", movement.getMovementDate())
                .addValue("quantity", movement.getQuantity())
                .addValue("approvedQuantity", movement.getApprovedQuantity())
                .addValue("rejectedQuantity", movement.getRejectedQuantity())
                .addValue("totalStock", movement.getTotalStock())
                .addValue("approvalStatus", movement.getApprovalStatus())
                .addValue("rejectionReason", movement.getRejectionReason())
                .addValue("createdAt", movement.getCreatedAt()) // Add this
                .addValue("updatedAt", movement.getUpdatedAt()); // Add this

        return namedJdbcTemplate.update(sql, params);
    }

    public int updateMovement(GarmentMovement movement) {
        String sql = "UPDATE garment_movements SET garment_id = :garmentId, fabric_id = :fabricId, status = :status, " +
                "movement_date = :movementDate, quantity = :quantity, approved_quantity = :approvedQuantity, " +
                "rejected_quantity = :rejectedQuantity, total_stock = :totalStock, approval_status = :approvalStatus, " +
                "rejection_reason = :rejectionReason, updated_at = :updatedAt WHERE movement_id = :movementId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("movementId", movement.getMovementId())
                .addValue("garmentId", movement.getGarmentId())
                .addValue("fabricId", movement.getFabricId())
                .addValue("status", movement.getStatus())
                .addValue("movementDate", movement.getMovementDate())
                .addValue("quantity", movement.getQuantity())
                .addValue("approvedQuantity", movement.getApprovedQuantity())
                .addValue("rejectedQuantity", movement.getRejectedQuantity())
                .addValue("totalStock", movement.getTotalStock())
                .addValue("approvalStatus", movement.getApprovalStatus())
                .addValue("rejectionReason", movement.getRejectionReason())
                .addValue("updatedAt", movement.getUpdatedAt()); // Add this

        return namedJdbcTemplate.update(sql, params);
    }

    public int deleteMovement(String movementId) {
        String sql = "DELETE FROM garment_movements WHERE movement_id = :movementId";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("movementId", movementId);
        return namedJdbcTemplate.update(sql, params);
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM garment_movements";
        return namedJdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Long.class);
    }

    // ---------------- Additional Utility Methods ----------------
    public List<GarmentMovement> findByGarmentId(String garmentId) {
        String sql = "SELECT * FROM garment_movements WHERE garment_id = :garmentId ORDER BY movement_date DESC";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("garmentId", garmentId);
        return namedJdbcTemplate.query(sql, params, movementRowMapper);
    }

    public List<GarmentMovement> findByFabricId(String fabricId) {
        String sql = "SELECT * FROM garment_movements WHERE fabric_id = :fabricId ORDER BY movement_date DESC";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("fabricId", fabricId);
        return namedJdbcTemplate.query(sql, params, movementRowMapper);
    }

    public List<GarmentMovement> findByStatus(String status) {
        String sql = "SELECT * FROM garment_movements WHERE status = :status ORDER BY movement_date DESC";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("status", status);
        return namedJdbcTemplate.query(sql, params, movementRowMapper);
    }

    public List<GarmentMovement> findByApprovalStatus(String approvalStatus) {
        String sql = "SELECT * FROM garment_movements WHERE approval_status = :approvalStatus ORDER BY movement_date DESC";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("approvalStatus", approvalStatus);
        return namedJdbcTemplate.query(sql, params, movementRowMapper);
    }

    public List<GarmentMovement> findByDateRange(LocalDate start, LocalDate end) {
        String sql = "SELECT * FROM garment_movements WHERE movement_date BETWEEN :start AND :end ORDER BY movement_date DESC";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("start", start)
                .addValue("end", end);
        return namedJdbcTemplate.query(sql, params, movementRowMapper);
    }

    public List<GarmentMovement> findByGarmentIdAndDateRange(String garmentId, LocalDate start, LocalDate end) {
        String sql = "SELECT * FROM garment_movements WHERE garment_id = :garmentId AND movement_date BETWEEN :start AND :end ORDER BY movement_date DESC";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("garmentId", garmentId)
                .addValue("start", start)
                .addValue("end", end);
        return namedJdbcTemplate.query(sql, params, movementRowMapper);
    }

    public List<GarmentMovement> findPendingApprovals() {
        String sql = "SELECT * FROM garment_movements WHERE approval_status = 'PENDING' ORDER BY movement_date ASC";
        return namedJdbcTemplate.query(sql, movementRowMapper);
    }

    public List<GarmentMovement> findApprovedMovements() {
        String sql = "SELECT * FROM garment_movements WHERE approval_status IN ('APPROVED', 'PARTIALLY_APPROVED') ORDER BY movement_date DESC";
        return namedJdbcTemplate.query(sql, movementRowMapper);
    }

    // ---------------- Stock Calculation Methods ----------------
    public int calculateTotalInQuantity(String garmentId) {
        String sql = "SELECT COALESCE(SUM(approved_quantity), 0) FROM garment_movements " +
                "WHERE garment_id = :garmentId AND status = 'In' AND approval_status IN ('APPROVED', 'PARTIALLY_APPROVED')";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("garmentId", garmentId);

        try {
            return namedJdbcTemplate.queryForObject(sql, params, Integer.class);
        } catch (Exception e) {
            return 0;
        }
    }

    public int calculateTotalOutQuantity(String garmentId) {
        String sql = "SELECT COALESCE(SUM(approved_quantity), 0) FROM garment_movements " +
                "WHERE garment_id = :garmentId AND status = 'Shipped' AND approval_status IN ('APPROVED', 'PARTIALLY_APPROVED')";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("garmentId", garmentId);

        try {
            return namedJdbcTemplate.queryForObject(sql, params, Integer.class);
        } catch (Exception e) {
            return 0;
        }
    }

    public int calculateCurrentStock(String garmentId) {
        String sql = "SELECT " +
                "COALESCE(SUM(CASE WHEN status = 'In' THEN approved_quantity ELSE 0 END), 0) - " +
                "COALESCE(SUM(CASE WHEN status = 'Shipped' THEN approved_quantity ELSE 0 END), 0) " +
                "FROM garment_movements " +
                "WHERE garment_id = :garmentId AND approval_status IN ('APPROVED', 'PARTIALLY_APPROVED')";

        MapSqlParameterSource params = new MapSqlParameterSource().addValue("garmentId", garmentId);

        try {
            Integer stock = namedJdbcTemplate.queryForObject(sql, params, Integer.class);
            return stock != null ? stock : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // ---------------- Update Approval Method ----------------
    public int updateApproval(String movementId, Integer approvedQty, Integer rejectedQty,
                              String rejectionReason, Integer totalStock) {
        String sql = "UPDATE garment_movements SET " +
                "approved_quantity = :approvedQuantity, " +
                "rejected_quantity = :rejectedQuantity, " +
                "total_stock = :totalStock, " +
                "approval_status = :approvalStatus, " +
                "rejection_reason = :rejectionReason, " +
                "updated_at = :updatedAt " + // Add this line
                "WHERE movement_id = :movementId";

        String approvalStatus = (rejectedQty != null && rejectedQty > 0) ? "PARTIALLY_APPROVED" : "APPROVED";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("movementId", movementId)
                .addValue("approvedQuantity", approvedQty)
                .addValue("rejectedQuantity", rejectedQty)
                .addValue("totalStock", totalStock)
                .addValue("approvalStatus", approvalStatus)
                .addValue("rejectionReason", rejectionReason)
                .addValue("updatedAt", LocalDateTime.now()); // Add this

        return namedJdbcTemplate.update(sql, params);
    }

    // ---------------- Auto-increment Movement ID ----------------
    public String findLastMovementId() {
        String sql = "SELECT TOP 1 movement_id FROM garment_movements ORDER BY movement_id DESC";
        try {
            return namedJdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), String.class);
        } catch (Exception e) {
            return null; // table empty
        }
    }

    public String generateId(String prefix, String lastId) {
        int number = 1;
        if (lastId != null && !lastId.isEmpty()) {
            number = Integer.parseInt(lastId.substring(prefix.length())) + 1;
        }
        return String.format("%s%03d", prefix, number);
    }

    // ---------------- Statistics Methods ----------------
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM garment_movements WHERE status = :status";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("status", status);

        try {
            return namedJdbcTemplate.queryForObject(sql, params, Integer.class);
        } catch (Exception e) {
            return 0;
        }
    }

    public int countByApprovalStatus(String approvalStatus) {
        String sql = "SELECT COUNT(*) FROM garment_movements WHERE approval_status = :approvalStatus";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("approvalStatus", approvalStatus);

        try {
            return namedJdbcTemplate.queryForObject(sql, params, Integer.class);
        } catch (Exception e) {
            return 0;
        }
    }

    public List<GarmentMovement> findRecentMovements(int limit) {
        String sql = "SELECT TOP (:limit) * FROM garment_movements ORDER BY movement_date DESC";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("limit", limit);
        return namedJdbcTemplate.query(sql, params, movementRowMapper);
    }
}