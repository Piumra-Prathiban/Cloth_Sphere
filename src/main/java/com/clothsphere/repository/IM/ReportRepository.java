package com.clothsphere.repository.IM;

import com.clothsphere.model.IM.Garment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public class ReportRepository {

    @Autowired
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    /**
     * Get fabric availability data within date range
     * Returns List of Maps with all fabric details
     */
    public List<Map<String, Object>> getFabricAvailabilityData(LocalDate startDate, LocalDate endDate) {

        String sql = """
            SELECT 
                f.fabric_id,
                f.fabric_type AS type,
                f.color,
                f.current_stock,
                f.min_stock_level AS threshold,
                (f.min_stock_level * 2) AS reorder_level,
                CASE 
                    WHEN f.current_stock <= f.min_stock_level THEN 'Low Stock'
                    WHEN f.current_stock <= (f.min_stock_level * 2) THEN 'Reorder'
                    ELSE 'Normal'
                END AS status,
                FORMAT(f.updated_at, 'yyyy-MM-dd HH:mm') AS last_updated
            FROM fabric f
            WHERE f.created_at <= :endDate
            ORDER BY f.fabric_id
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("endDate", endDate.plusDays(1));

        return namedJdbcTemplate.queryForList(sql, params);
    }

    /**
     * Get fabric usage data within date range
     * Returns List of Maps with movement summaries
     */
    public List<Map<String, Object>> getFabricUsageData(LocalDate startDate, LocalDate endDate) {

        String sql = """
            SELECT 
                f.fabric_id,
                f.fabric_type AS type,
                f.color,
                ISNULL(SUM(CASE WHEN fm.status = 'IN' AND fm.approval_status = 'APPROVED' 
                    THEN fm.approved_quantity ELSE 0 END), 0) AS total_in,
                ISNULL(SUM(CASE WHEN fm.status = 'OUT' AND fm.approval_status = 'APPROVED' 
                    THEN fm.approved_quantity ELSE 0 END), 0) AS total_out,
                ISNULL(SUM(CASE WHEN fm.status = 'IN' AND fm.approval_status = 'APPROVED' 
                    THEN fm.approved_quantity ELSE 0 END), 0) -
                ISNULL(SUM(CASE WHEN fm.status = 'OUT' AND fm.approval_status = 'APPROVED' 
                    THEN fm.approved_quantity ELSE 0 END), 0) AS net_usage
            FROM fabric f
            LEFT JOIN fabric_movements fm ON f.fabric_id = fm.fabric_id
                AND fm.movement_date BETWEEN :startDate AND :endDate
            GROUP BY f.fabric_id, f.fabric_type, f.color
            ORDER BY f.fabric_id
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);

        return namedJdbcTemplate.queryForList(sql, params);
    }

    /**
     * Count total fabrics
     */
    public int countTotalFabrics() {
        String sql = "SELECT COUNT(*) FROM fabric";
        Integer count = namedJdbcTemplate.queryForObject(sql,
                new MapSqlParameterSource(), Integer.class);
        return count != null ? count : 0;
    }

    // Add these methods to ReportRepository.java

    /**
     * Get garment availability data within date range
     */
    public List<Map<String, Object>> getGarmentAvailabilityData(LocalDate startDate, LocalDate endDate) {
        String sql = """
        SELECT 
            g.garment_id,
            g.type,
            g.size,
            g.fabric_id AS fabricType,
            f.color,
            g.current_stock,
            10 AS threshold, -- Default threshold
            20 AS reorder_level, -- Default reorder level
            CASE 
                WHEN g.current_stock <= 10 THEN 'Low Stock'
                WHEN g.current_stock <= 20 THEN 'Reorder'
                ELSE 'Normal'
            END AS status,
            FORMAT(g.updated_at, 'yyyy-MM-dd HH:mm') AS last_updated
        FROM garments g
        LEFT JOIN fabric f ON g.fabric_id = f.fabric_id
        WHERE g.created_at <= :endDate
        ORDER BY g.garment_id
    """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("endDate", endDate.plusDays(1));

        return namedJdbcTemplate.queryForList(sql, params);
    }

    /**
     * Get garment reject report data within date range
     */
    public List<Map<String, Object>> getGarmentRejectData(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT " +
                "gm.movement_id, " +
                "gm.garment_id, " +
                "g.type, " +
                "g.size, " +
                "gm.fabric_id, " +
                "gm.movement_date, " +
                "gm.quantity as requested_quantity, " +
                "gm.approved_quantity, " +
                "gm.rejected_quantity, " +
                "gm.rejection_reason, " +
                "gm.approval_status, " +
                "gm.updated_at as rejected_date " +
                "FROM garment_movements gm " +
                "JOIN garments g ON gm.garment_id = g.garment_id " +
                "WHERE gm.movement_date BETWEEN :startDate AND :endDate " +
                "AND gm.approval_status IN ('REJECTED', 'PARTIALLY_APPROVED') " +
                "AND gm.rejected_quantity > 0 " +
                "ORDER BY gm.updated_at DESC";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);

        return namedJdbcTemplate.queryForList(sql, params);
    }

    /**
     * Get garment movement/shipped data within date range
     */
    public List<Map<String, Object>> getGarmentMovementData(LocalDate startDate, LocalDate endDate) {
        String sql = """
        SELECT 
            g.garment_id,
            g.type,
            g.size,
            ISNULL(SUM(CASE WHEN gm.status = 'In' AND gm.approval_status IN ('APPROVED', 'PARTIALLY_APPROVED') 
                THEN gm.approved_quantity ELSE 0 END), 0) AS total_produced,
            ISNULL(SUM(CASE WHEN gm.status = 'Shipped' AND gm.approval_status IN ('APPROVED', 'PARTIALLY_APPROVED') 
                THEN gm.approved_quantity ELSE 0 END), 0) AS total_shipped,
            ISNULL(SUM(CASE WHEN gm.status = 'In' AND gm.approval_status IN ('APPROVED', 'PARTIALLY_APPROVED') 
                THEN gm.approved_quantity ELSE 0 END), 0) -
            ISNULL(SUM(CASE WHEN gm.status = 'Shipped' AND gm.approval_status IN ('APPROVED', 'PARTIALLY_APPROVED') 
                THEN gm.approved_quantity ELSE 0 END), 0) AS net_movement
        FROM garments g
        LEFT JOIN garment_movements gm ON g.garment_id = gm.garment_id
            AND gm.movement_date BETWEEN :startDate AND :endDate
        GROUP BY g.garment_id, g.type, g.size
        ORDER BY g.garment_id
    """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);

        return namedJdbcTemplate.queryForList(sql, params);
    }

    @Repository
    public interface GarmentRepository extends JpaRepository<Garment, String> {
        List<Garment> findByType(String type);
        List<Garment> findByFabricId(String fabricId);
        boolean existsByGarmentId(String garmentId);
    }


}