package com.clothsphere.repository.Inventory;

import com.clothsphere.model.Inventory.Fabric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class FabricRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<Fabric> fabricRowMapper = new RowMapper<Fabric>() {
        @Override
        public Fabric mapRow(ResultSet rs, int rowNum) throws SQLException {
            Fabric fabric = new Fabric();
            fabric.setFabricId(rs.getString("fabric_id"));
            fabric.setFabricType(rs.getString("fabric_type"));
            fabric.setColor(rs.getString("color"));
            fabric.setCurrentStock(rs.getDouble("current_stock"));
            if (rs.getTimestamp("created_at") != null)
                fabric.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            if (rs.getTimestamp("updated_at") != null)
                fabric.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return fabric;
        }
    };

    // Get all fabrics
    public List<Fabric> getAllFabrics() {
        String sql = "SELECT * FROM fabric ORDER BY fabric_id";
        return jdbcTemplate.query(sql, fabricRowMapper);
    }

    // Get fabric by ID
    public Fabric getFabricById(String fabricId) {
        String sql = "SELECT * FROM fabric WHERE fabric_id = ?";
        List<Fabric> fabrics = jdbcTemplate.query(sql, new Object[]{fabricId}, fabricRowMapper);
        if (fabrics.isEmpty()) {
            return null;
        }
        return fabrics.get(0);
    }

    // Add new fabric
    public int addFabric(Fabric fabric) {
        String sql = "INSERT INTO fabric (fabric_id, fabric_type, color, current_stock, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, GETDATE(), GETDATE())";
        return jdbcTemplate.update(sql,
                fabric.getFabricId(),
                fabric.getFabricType(),
                fabric.getColor(),
                fabric.getCurrentStock());
    }

    // Update fabric
    public int updateFabric(Fabric fabric) {
        String sql = "UPDATE fabric SET fabric_type = ?, color = ?, current_stock = ?, updated_at = GETDATE() " +
                "WHERE fabric_id = ?";
        return jdbcTemplate.update(sql,
                fabric.getFabricType(),
                fabric.getColor(),
                fabric.getCurrentStock(),
                fabric.getFabricId());
    }

    // Delete fabric
    public int deleteFabric(String fabricId) {
        String sql = "DELETE FROM fabric WHERE fabric_id = ?";
        return jdbcTemplate.update(sql, fabricId);
    }

    // Find fabric by ID (returns Optional)
    public Optional<Fabric> findById(String fabricId) {
        try {
            Fabric fabric = getFabricById(fabricId);
            return Optional.ofNullable(fabric);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // Find all fabrics (returns List)
    public List<Fabric> findAll() {
        return getAllFabrics();
    }

    // Count total fabrics
    public long count() {
        String sql = "SELECT COUNT(*) FROM fabric";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
}
