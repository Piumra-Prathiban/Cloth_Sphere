package com.clothsphere.repository.IM;

import com.clothsphere.model.IM.FabricMovement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class FabricMovementRepository {

    @Autowired
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    // ---------------- RowMapper ----------------
    private final RowMapper<FabricMovement> movementRowMapper = (ResultSet rs, int rowNum) -> {
        FabricMovement movement = new FabricMovement();
        movement.setMovementId(rs.getString("movement_id"));
        movement.setFabricId(rs.getString("fabric_id"));
        movement.setStatus(rs.getString("status"));
        movement.setQuantity(rs.getDouble("quantity"));
        movement.setApprovedQuantity(rs.getDouble("approved_quantity"));
        movement.setRejectedQuantity(rs.getDouble("rejected_quantity"));
        movement.setApprovalStatus(rs.getString("approval_status")); // Make sure this is set
        movement.setRejectionReason(rs.getString("rejection_reason"));
        movement.setMovementDate(rs.getDate("movement_date").toLocalDate());
        movement.setTotalStock(rs.getDouble("total_stock"));
        return movement;
    };

    // ---------------- CRUD / Queries ----------------
    public List<FabricMovement> findAll() {
        String sql = "SELECT * FROM fabric_movements ORDER BY movement_id";
        return namedJdbcTemplate.query(sql, movementRowMapper);
    }

    public Optional<FabricMovement> findById(String movementId) {
        String sql = "SELECT * FROM fabric_movements WHERE movement_id = :movementId";
        try {
            MapSqlParameterSource params = new MapSqlParameterSource().addValue("movementId", movementId);
            FabricMovement movement = namedJdbcTemplate.queryForObject(sql, params, movementRowMapper);
            return Optional.ofNullable(movement);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public int addMovement(FabricMovement movement) {
        String sql = "INSERT INTO fabric_movements " +
                "(movement_id, fabric_id, status, movement_date, quantity, approved_quantity, rejected_quantity, " +
                "total_stock, approval_status, rejection_reason, created_at, updated_at) " +
                "VALUES (:movementId, :fabricId, :status, :movementDate, :quantity, :approvedQuantity, :rejectedQuantity, " +
                ":totalStock, :approvalStatus, :rejectionReason, :createdAt, :updatedAt)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("movementId", movement.getMovementId())
                .addValue("fabricId", movement.getFabricId())
                .addValue("status", movement.getStatus())
                .addValue("movementDate", movement.getMovementDate())
                .addValue("quantity", movement.getQuantity())
                .addValue("approvedQuantity", movement.getApprovedQuantity())
                .addValue("rejectedQuantity", movement.getRejectedQuantity())
                .addValue("totalStock", movement.getTotalStock())
                .addValue("approvalStatus", movement.getApprovalStatus())
                .addValue("rejectionReason", movement.getRejectionReason())
                .addValue("createdAt", LocalDateTime.now()) // Manually set since @PrePersist won't work
                .addValue("updatedAt", LocalDateTime.now()); // Manually set since @PrePersist won't work

        return namedJdbcTemplate.update(sql, params);
    }

    public int updateMovement(FabricMovement movement) {
        String sql = "UPDATE fabric_movements SET approved_quantity = :approvedQuantity, " +
                "rejected_quantity = :rejectedQuantity, approval_status = :approvalStatus, " +
                "rejection_reason = :rejectionReason, updated_at = GETDATE() " +
                "WHERE movement_id = :movementId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("approvedQuantity", movement.getApprovedQuantity())
                .addValue("rejectedQuantity", movement.getRejectedQuantity())
                .addValue("approvalStatus", movement.getApprovalStatus())
                .addValue("rejectionReason", movement.getRejectionReason())
                .addValue("movementId", movement.getMovementId());
        return namedJdbcTemplate.update(sql, params);
    }

    public int deleteMovement(String movementId) {
        String sql = "DELETE FROM fabric_movements WHERE movement_id = :movementId";
        return namedJdbcTemplate.update(sql, new MapSqlParameterSource("movementId", movementId));
    }

    public List<FabricMovement> findMovementsByFabricId(String fabricId) {
        String sql = "SELECT * FROM fabric_movements WHERE fabric_id = :fabricId ORDER BY movement_id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("fabricId", fabricId);
        return namedJdbcTemplate.query(sql, params, movementRowMapper);
    }

    public List<FabricMovement> findByDateRange(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT * FROM fabric_movements WHERE movement_date BETWEEN :start AND :end ORDER BY movement_date";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("start", start)
                .addValue("end", end);
        return namedJdbcTemplate.query(sql, params, movementRowMapper);
    }

    public String findLastMovementId() {
        String sql = "SELECT TOP 1 movement_id FROM fabric_movements ORDER BY movement_id DESC";
        try {
            return namedJdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), String.class);
        } catch (Exception e) {
            return null; // no movements yet
        }
    }
}
