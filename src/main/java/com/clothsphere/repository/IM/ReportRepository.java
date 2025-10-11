package com.clothsphere.repository.IM;

import org.springframework.beans.factory.annotation.Autowired;
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
}