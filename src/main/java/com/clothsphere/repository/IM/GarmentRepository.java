package com.clothsphere.repository.IM;

import com.clothsphere.model.IM.Garment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class GarmentRepository {

    @Autowired
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    // ---------------- RowMapper ----------------
    private final RowMapper<Garment> garmentRowMapper = (ResultSet rs, int rowNum) -> {
        Garment garment = new Garment();
        garment.setGarmentId(rs.getString("garment_id"));
        garment.setType(rs.getString("type"));
        garment.setSize(rs.getString("size"));
        garment.setFabricId(rs.getString("fabric_id"));

        // Handle timestamp conversion
        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            garment.setCreatedAt(createdAt.toLocalDateTime());
        }

        java.sql.Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            garment.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return garment;
    };

    // ---------------- CRUD Operations ----------------
    public List<Garment> findAll() {
        String sql = "SELECT * FROM garments ORDER BY garment_id";
        return namedJdbcTemplate.query(sql, garmentRowMapper);
    }

    public Optional<Garment> findById(String garmentId) {
        String sql = "SELECT * FROM garments WHERE garment_id = :garmentId";
        try {
            MapSqlParameterSource params = new MapSqlParameterSource().addValue("garmentId", garmentId);
            Garment garment = namedJdbcTemplate.queryForObject(sql, params, garmentRowMapper);
            return Optional.ofNullable(garment);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public int insertGarment(Garment garment) {
        String sql = "INSERT INTO garments (garment_id, type, size, fabric_id, created_at, updated_at) " +
                "VALUES (:garmentId, :type, :size, :fabricId, :createdAt, :updatedAt)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("garmentId", garment.getGarmentId())
                .addValue("type", garment.getType())
                .addValue("size", garment.getSize())
                .addValue("fabricId", garment.getFabricId())
                .addValue("createdAt", LocalDateTime.now())
                .addValue("updatedAt", LocalDateTime.now());

        return namedJdbcTemplate.update(sql, params);
    }

    public int updateGarment(Garment garment) {
        String sql = "UPDATE garments SET type = :type, size = :size, fabric_id = :fabricId, " +
                "updated_at = :updatedAt WHERE garment_id = :garmentId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("type", garment.getType())
                .addValue("size", garment.getSize())
                .addValue("fabricId", garment.getFabricId())
                .addValue("updatedAt", LocalDateTime.now())
                .addValue("garmentId", garment.getGarmentId());

        return namedJdbcTemplate.update(sql, params);
    }

    public int deleteGarment(String garmentId) {
        String sql = "DELETE FROM garments WHERE garment_id = :garmentId";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("garmentId", garmentId);
        return namedJdbcTemplate.update(sql, params);
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM garments";
        return namedJdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Long.class);
    }

    // ---------------- Additional Utility Methods ----------------
    public boolean existsByTypeAndSizeAndFabric(String type, String size, String fabricId) {
        String sql = "SELECT COUNT(*) FROM garments WHERE type = :type AND size = :size AND fabric_id = :fabricId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("type", type)
                .addValue("size", size)
                .addValue("fabricId", fabricId);
        Integer count = namedJdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    public List<Garment> findByType(String type) {
        String sql = "SELECT * FROM garments WHERE type = :type ORDER BY garment_id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("type", type);
        return namedJdbcTemplate.query(sql, params, garmentRowMapper);
    }

    public List<Garment> findBySize(String size) {
        String sql = "SELECT * FROM garments WHERE size = :size ORDER BY garment_id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("size", size);
        return namedJdbcTemplate.query(sql, params, garmentRowMapper);
    }

    public List<Garment> findByFabricId(String fabricId) {
        String sql = "SELECT * FROM garments WHERE fabric_id = :fabricId ORDER BY garment_id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("fabricId", fabricId);
        return namedJdbcTemplate.query(sql, params, garmentRowMapper);
    }

    public List<Garment> findByTypeAndSize(String type, String size) {
        String sql = "SELECT * FROM garments WHERE type = :type AND size = :size ORDER BY garment_id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("type", type)
                .addValue("size", size);
        return namedJdbcTemplate.query(sql, params, garmentRowMapper);
    }

    // ---------------- Stock Calculation Methods ----------------
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

    // FIXED: This method now returns List<Garment> instead of List<Map>
    public List<Garment> findLowStock(int threshold) {
        String sql = "SELECT g.* FROM garments g " +
                "WHERE (SELECT " +
                "COALESCE(SUM(CASE WHEN gm.status = 'In' THEN gm.approved_quantity ELSE 0 END), 0) - " +
                "COALESCE(SUM(CASE WHEN gm.status = 'Shipped' THEN gm.approved_quantity ELSE 0 END), 0) " +
                "FROM garment_movements gm " +
                "WHERE gm.garment_id = g.garment_id AND gm.approval_status IN ('APPROVED', 'PARTIALLY_APPROVED')) < :threshold " +
                "ORDER BY g.garment_id";

        MapSqlParameterSource params = new MapSqlParameterSource("threshold", threshold);
        return namedJdbcTemplate.query(sql, params, garmentRowMapper);
    }

    public Map<String, Object> getStats() {
        String sql = "SELECT " +
                "COUNT(*) as total_garments, " +
                "(SELECT COUNT(*) FROM (" +
                "   SELECT g.garment_id, " +
                "          (COALESCE(SUM(CASE WHEN gm.status = 'In' THEN gm.approved_quantity ELSE 0 END), 0) - " +
                "           COALESCE(SUM(CASE WHEN gm.status = 'Shipped' THEN gm.approved_quantity ELSE 0 END), 0)) as calculated_stock " +
                "   FROM garments g " +
                "   LEFT JOIN garment_movements gm ON g.garment_id = gm.garment_id AND gm.approval_status IN ('APPROVED', 'PARTIALLY_APPROVED') " +
                "   GROUP BY g.garment_id" +
                ") AS garment_stocks WHERE calculated_stock < 10) as low_stock_count " +
                "FROM garments";

        return namedJdbcTemplate.queryForMap(sql, new MapSqlParameterSource());
    }

    // ---------------- Auto-increment Garment ID ----------------
    public String findLastGarmentId() {
        String sql = "SELECT TOP 1 garment_id FROM garments ORDER BY garment_id DESC";
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

    // ---------------- Methods that return Map (if needed for specific use cases) ----------------
    public List<Map<String, Object>> findAllWithCurrentStock() {
        String sql = "SELECT g.*, " +
                "(COALESCE(SUM(CASE WHEN gm.status = 'In' THEN gm.approved_quantity ELSE 0 END), 0) - " +
                "COALESCE(SUM(CASE WHEN gm.status = 'Shipped' THEN gm.approved_quantity ELSE 0 END), 0)) as current_stock " +
                "FROM garments g " +
                "LEFT JOIN garment_movements gm ON g.garment_id = gm.garment_id AND gm.approval_status IN ('APPROVED', 'PARTIALLY_APPROVED') " +
                "GROUP BY g.garment_id, g.type, g.size, g.fabric_id, g.created_at, g.updated_at " +
                "ORDER BY g.created_at DESC";

        return namedJdbcTemplate.queryForList(sql, new MapSqlParameterSource());
    }

    public Optional<Map<String, Object>> findByIdWithCurrentStock(String garmentId) {
        String sql = "SELECT g.*, " +
                "(COALESCE(SUM(CASE WHEN gm.status = 'In' THEN gm.approved_quantity ELSE 0 END), 0) - " +
                "COALESCE(SUM(CASE WHEN gm.status = 'Shipped' THEN gm.approved_quantity ELSE 0 END), 0)) as current_stock " +
                "FROM garments g " +
                "LEFT JOIN garment_movements gm ON g.garment_id = gm.garment_id AND gm.approval_status IN ('APPROVED', 'PARTIALLY_APPROVED') " +
                "WHERE g.garment_id = :garmentId " +
                "GROUP BY g.garment_id, g.type, g.size, g.fabric_id, g.created_at, g.updated_at";

        MapSqlParameterSource params = new MapSqlParameterSource("garmentId", garmentId);
        List<Map<String, Object>> results = namedJdbcTemplate.queryForList(sql, params);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}