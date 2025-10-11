package com.clothsphere.repository.Inventory;

import com.clothsphere.model.Inventory.Fabric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public class FabricRepository {

    @Autowired
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    // ---------------- RowMapper ----------------
    private final RowMapper<Fabric> fabricRowMapper = (ResultSet rs, int rowNum) -> {
        Fabric fabric = new Fabric();
        fabric.setFabricId(rs.getString("fabric_id"));
        fabric.setFabricType(rs.getString("fabric_type"));
        fabric.setColor(rs.getString("color"));
        fabric.setCurrentStock(rs.getDouble("current_stock"));
        return fabric;
    };

    // ---------------- CRUD Operations ----------------
    public List<Fabric> findAll() {
        String sql = "SELECT * FROM fabric ORDER BY fabric_id";
        return namedJdbcTemplate.query(sql, fabricRowMapper);
    }

    public Optional<Fabric> findById(String fabricId) {
        String sql = "SELECT * FROM fabric WHERE fabric_id = :fabricId";
        try {
            MapSqlParameterSource params = new MapSqlParameterSource().addValue("fabricId", fabricId);
            Fabric fabric = namedJdbcTemplate.queryForObject(sql, params, fabricRowMapper);
            return Optional.ofNullable(fabric);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public int insertFabric(Fabric fabric) {
        String sql = "INSERT INTO fabric (fabric_id, fabric_type, color, current_stock, min_stock_level, created_at, updated_at) " +
                "VALUES (:fabricId, :fabricType, :color, :currentStock, :minStockLevel, :createdAt, :updatedAt)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("fabricId", fabric.getFabricId())
                .addValue("fabricType", fabric.getFabricType())
                .addValue("color", fabric.getColor())
                .addValue("currentStock", fabric.getCurrentStock())
                .addValue("minStockLevel", fabric.getMinStockLevel())
                .addValue("createdAt", LocalDateTime.now()) // Manually set
                .addValue("updatedAt", LocalDateTime.now()); // Manually set
        return namedJdbcTemplate.update(sql, params);
    }

    public int updateFabric(Fabric fabric) {
        String sql = "UPDATE fabric SET current_stock = :stock, updated_at = GETDATE() WHERE fabric_id = :fabricId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("stock", fabric.getCurrentStock())
                .addValue("fabricId", fabric.getFabricId());
        return namedJdbcTemplate.update(sql, params);
    }

    public int deleteFabric(String fabricId) {
        String sql = "DELETE FROM fabric WHERE fabric_id = :fabricId";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("fabricId", fabricId);
        return namedJdbcTemplate.update(sql, params);
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM fabric";
        return namedJdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Long.class);
    }

    // ---------------- Additional Utility Methods ----------------
    public boolean existsByTypeAndColor(String type, String color) {
        String sql = "SELECT COUNT(*) FROM fabric WHERE fabric_type = :type AND color = :color";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("type", type)
                .addValue("color", color);
        Integer count = namedJdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    public List<Fabric> findLowStock() {
        String sql = "SELECT * FROM fabric WHERE current_stock < 10 ORDER BY fabric_id";
        return namedJdbcTemplate.query(sql, fabricRowMapper);
    }

    public List<Fabric> findByType(String type) {
        String sql = "SELECT * FROM fabric WHERE fabric_type = :type ORDER BY fabric_id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("type", type);
        return namedJdbcTemplate.query(sql, params, fabricRowMapper);
    }

    public List<Fabric> findByColor(String color) {
        String sql = "SELECT * FROM fabric WHERE color = :color ORDER BY fabric_id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("color", color);
        return namedJdbcTemplate.query(sql, params, fabricRowMapper);
    }

    public Map<String, Object> getStats() {
        String sql = "SELECT COUNT(*) AS totalFabrics, SUM(current_stock) AS totalStock FROM fabric";
        return namedJdbcTemplate.queryForMap(sql, new MapSqlParameterSource());
    }

    // ---------------- Auto-increment Fabric ID ----------------
    public String findLastFabricId() {
        String sql = "SELECT TOP 1 fabric_id FROM fabric ORDER BY fabric_id DESC";
        try {
            return namedJdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), String.class);
        } catch (Exception e) {
            return null; // table empty
        }
    }
    public String generateId(String prefix, String lastId) {
        int number = 1;
        if (lastId != null && !lastId.isEmpty()) {
            number = Integer.parseInt(lastId.substring(3)) + 1;
        }
        return String.format("%s%03d", prefix, number);
    }

}
