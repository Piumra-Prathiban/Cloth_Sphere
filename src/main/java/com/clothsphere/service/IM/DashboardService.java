package com.clothsphere.service.IM;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    // Get total fabrics in stock
    public int getTotalFabricsInStock() {
        String sql = "SELECT COALESCE(SUM(current_stock), 0) FROM fabric";
        try {
            return namedJdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
        } catch (Exception e) {
            System.out.println("Error getting fabric stock: " + e.getMessage());
            return 0;
        }
    }

    // Get total garments in stock
    public int getTotalGarmentsInStock() {
        String sql = "SELECT COALESCE(SUM(current_stock), 0) FROM garments";
        try {
            return namedJdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
        } catch (Exception e) {
            System.out.println("Error getting garment stock: " + e.getMessage());
            return 0;
        }
    }

    // Get fabric movement data for last 7 days
    public List<Map<String, Object>> getFabricMovementData() {
        try {
            String sql = "SELECT movement_date, status, COALESCE(SUM(quantity), 0) as total_quantity " +
                    "FROM fabric_movements " +
                    "WHERE movement_date >= DATEADD(day, -7, GETDATE()) " +
                    "GROUP BY movement_date, status " +
                    "ORDER BY movement_date";
            return namedJdbcTemplate.queryForList(sql, new MapSqlParameterSource());
        } catch (Exception e) {
            System.out.println("Error getting fabric movements: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Get garment movement data for last 7 days
    public List<Map<String, Object>> getGarmentMovementData() {
        try {
            String sql = "SELECT movement_date, status, COALESCE(SUM(quantity), 0) as total_quantity " +
                    "FROM garment_movements " +
                    "WHERE movement_date >= DATEADD(day, -7, GETDATE()) " +
                    "GROUP BY movement_date, status " +
                    "ORDER BY movement_date";
            return namedJdbcTemplate.queryForList(sql, new MapSqlParameterSource());
        } catch (Exception e) {
            System.out.println("Error getting garment movements: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Get low stock fabrics - SIMPLIFIED VERSION
    public List<Map<String, Object>> getLowStockFabrics() {
        try {
            // First, check if the fabrics table exists and has data
            String checkSql = "SELECT COUNT(*) as count FROM fabric";
            Integer fabricCount = namedJdbcTemplate.queryForObject(checkSql, new MapSqlParameterSource(), Integer.class);

            if (fabricCount == null || fabricCount == 0) {
                return new ArrayList<>(); // Return empty list if no fabrics
            }

            // Try simplified query first (without reorder_level check)
            String sql = "SELECT fabric_id, fabric_type, color, current_stock, 10 as reorder_level " +
                    "FROM fabric " +
                    "WHERE current_stock <= 10 " + // Default threshold
                    "ORDER BY current_stock ASC";

            return namedJdbcTemplate.queryForList(sql, new MapSqlParameterSource());
        } catch (Exception e) {
            System.out.println("Error getting low stock fabrics: " + e.getMessage());
            // Return empty list instead of throwing error
            return new ArrayList<>();
        }
    }

    // Get low stock garments - SIMPLIFIED VERSION
    public List<Map<String, Object>> getLowStockGarments() {
        try {
            // First, check if the garments table exists and has data
            String checkSql = "SELECT COUNT(*) as count FROM garments";
            Integer garmentCount = namedJdbcTemplate.queryForObject(checkSql, new MapSqlParameterSource(), Integer.class);

            if (garmentCount == null || garmentCount == 0) {
                return new ArrayList<>(); // Return empty list if no garments
            }

            // Try simplified query first (without reorder_level check)
            String sql = "SELECT garment_id, type, size, fabric_id, current_stock, 10 as reorder_level " +
                    "FROM garments " +
                    "WHERE current_stock <= 10 " + // Default threshold
                    "ORDER BY current_stock ASC";

            return namedJdbcTemplate.queryForList(sql, new MapSqlParameterSource());
        } catch (Exception e) {
            System.out.println("Error getting low stock garments: " + e.getMessage());
            // Return empty list instead of throwing error
            return new ArrayList<>();
        }
    }

    // Get rejected fabrics count - UPDATED
    public int getRejectedFabricsCount() {
        try {
            // Try multiple possible column names for rejection status
            String[] possibleQueries = {
                    "SELECT COUNT(*) FROM fabric_movements WHERE approval_status = 'REJECTED' AND movement_date >= DATEADD(day, -30, GETDATE())",
                    "SELECT COUNT(*) FROM fabric_movements WHERE status = 'REJECTED' AND movement_date >= DATEADD(day, -30, GETDATE())",
                    "SELECT COUNT(*) FROM fabric_movements WHERE rejected_quantity > 0 AND movement_date >= DATEADD(day, -30, GETDATE())",
                    "SELECT COUNT(*) FROM fabric_movements WHERE movement_date >= DATEADD(day, -30, GETDATE())" // Fallback
            };

            for (String sql : possibleQueries) {
                try {
                    Integer result = namedJdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
                    if (result != null && result > 0) {
                        return result;
                    }
                } catch (Exception e) {
                    // Try next query
                    continue;
                }
            }
            return 0;
        } catch (Exception e) {
            System.out.println("Error getting rejected fabrics count: " + e.getMessage());
            return 0;
        }
    }

    // Get rejected garments count - UPDATED
    public int getRejectedGarmentsCount() {
        try {
            // Try multiple possible column names for rejection status
            String[] possibleQueries = {
                    "SELECT COUNT(*) FROM garment_movements WHERE approval_status = 'REJECTED' AND movement_date >= DATEADD(day, -30, GETDATE())",
                    "SELECT COUNT(*) FROM garment_movements WHERE status = 'REJECTED' AND movement_date >= DATEADD(day, -30, GETDATE())",
                    "SELECT COUNT(*) FROM garment_movements WHERE rejected_quantity > 0 AND movement_date >= DATEADD(day, -30, GETDATE())",
                    "SELECT COUNT(*) FROM garment_movements WHERE movement_date >= DATEADD(day, -30, GETDATE())" // Fallback
            };

            for (String sql : possibleQueries) {
                try {
                    Integer result = namedJdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
                    if (result != null && result > 0) {
                        return result;
                    }
                } catch (Exception e) {
                    // Try next query
                    continue;
                }
            }
            return 0;
        } catch (Exception e) {
            System.out.println("Error getting rejected garments count: " + e.getMessage());
            return 0;
        }
    }
}