package com.clothsphere.repository.Inventory;

import com.clothsphere.model.Inventory.GarmentMovement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class GarmentMovementRepository {

    private final JdbcTemplate jdbcTemplate;

    public GarmentMovementRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper with null checks
    private final RowMapper<GarmentMovement> movementMapper = (rs, rowNum) -> {
        GarmentMovement gm = new GarmentMovement(
                rs.getString("garment_id"),
                rs.getString("fabric_id"),
                rs.getString("status"),
                rs.getInt("quantity"),
                rs.getInt("total_stock")
        );

        gm.setMovementId(rs.getString("movement_id")); // GMI001 format

        Timestamp ts = rs.getTimestamp("movement_date");
        if (ts != null) {
            gm.setMovementDate(ts.toLocalDateTime());
        }

        return gm;
    };

    // Generate custom ID like GMI001
    public String generateMovementId() {
        String lastId = jdbcTemplate.queryForObject(
                "SELECT movement_id FROM garment_movements ORDER BY movement_id DESC LIMIT 1",
                String.class
        );

        if (lastId == null) return "GMI001";

        int num = Integer.parseInt(lastId.substring(3)); // extract number part
        num++;
        return String.format("GMI%03d", num); // GMI002, GMI003...
    }

    // Get all movements
    public List<GarmentMovement> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM garment_movements ORDER BY movement_date DESC",
                movementMapper
        );
    }

    // Get movements by garment ID
    public List<GarmentMovement> findByGarmentId(String garmentId) {
        return jdbcTemplate.query(
                "SELECT * FROM garment_movements WHERE garment_id=? ORDER BY movement_date DESC",
                movementMapper,
                garmentId
        );
    }

    // Save a new movement
    public void save(GarmentMovement movement) {
        movement.setMovementId(generateMovementId());
        movement.setMovementDate(LocalDateTime.now());

        jdbcTemplate.update(
                "INSERT INTO garment_movements (movement_id, garment_id, fabric_id, status, movement_date, quantity, total_stock) VALUES (?, ?, ?, ?, ?, ?, ?)",
                movement.getMovementId(),
                movement.getGarmentId(),
                movement.getFabricId(),
                movement.getStatus(),
                movement.getMovementDate(),
                movement.getQuantity(),
                movement.getTotalStock()
        );
    }
}
