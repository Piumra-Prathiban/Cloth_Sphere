package com.clothsphere.repository.Inventory;

import com.clothsphere.model.Inventory.FabricMovement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class FabricMovementRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<FabricMovement> movementRowMapper = new RowMapper<FabricMovement>() {
        @Override
        public FabricMovement mapRow(ResultSet rs, int rowNum) throws SQLException {
            FabricMovement movement = new FabricMovement();
            movement.setMovementId(rs.getString("movement_id"));
            movement.setFabricId(rs.getString("fabric_id"));
            movement.setStatus(rs.getString("status"));
            movement.setMovementDate(rs.getDate("movement_date").toLocalDate());
            movement.setQuantity(rs.getDouble("quantity"));
            movement.setTotalStock(rs.getDouble("total_stock"));
            movement.setApprovedQuantity(rs.getObject("approved_quantity") != null ? rs.getDouble("approved_quantity") : null);
            movement.setRejectedQuantity(rs.getObject("rejected_quantity") != null ? rs.getDouble("rejected_quantity") : null);
            movement.setApprovalStatus(rs.getString("approval_status"));
            movement.setRejectionReason(rs.getString("rejection_reason"));
            if (rs.getTimestamp("created_at") != null)
                movement.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            if (rs.getTimestamp("updated_at") != null)
                movement.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return movement;
        }
    };

    // Get all movements
    public List<FabricMovement> getAllMovements() {
        String sql = "SELECT * FROM fabric_movements ORDER BY movement_date DESC";
        return jdbcTemplate.query(sql, movementRowMapper);
    }

    // Get movements by fabric ID
    public List<FabricMovement> getMovementsByFabricId(String fabricId) {
        String sql = "SELECT * FROM fabric_movements WHERE fabric_id = ? ORDER BY movement_date DESC";
        return jdbcTemplate.query(sql, new Object[]{fabricId}, movementRowMapper);
    }

    // Add a new movement
    public int addMovement(FabricMovement movement) {
        String sql = "INSERT INTO fabric_movements " +
                "(movement_id, fabric_id, status, movement_date, quantity, total_stock, " +
                "approved_quantity, rejected_quantity, approval_status, rejection_reason, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";
        return jdbcTemplate.update(sql,
                movement.getMovementId(),
                movement.getFabricId(),
                movement.getStatus(),
                movement.getMovementDate(),
                movement.getQuantity(),
                movement.getTotalStock(),
                movement.getApprovedQuantity(),
                movement.getRejectedQuantity(),
                movement.getApprovalStatus(),
                movement.getRejectionReason());
    }

    // Update movement (approval)
    public int updateMovement(FabricMovement movement) {
        String sql = "UPDATE fabric_movements SET approved_quantity = ?, rejected_quantity = ?, " +
                "approval_status = ?, rejection_reason = ? WHERE movement_id = ?";
        return jdbcTemplate.update(sql,
                movement.getApprovedQuantity(),
                movement.getRejectedQuantity(),
                movement.getApprovalStatus(),
                movement.getRejectionReason(),
                movement.getMovementId());
    }

    // Delete movement
    public int deleteMovement(String movementId) {
        String sql = "DELETE FROM fabric_movements WHERE movement_id = ?";
        return jdbcTemplate.update(sql, movementId);
    }
}
