package com.clothsphere.repository.Inventory;

import com.clothsphere.model.Inventory.Garment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class GarmentRepository {

    private final JdbcTemplate jdbcTemplate;

    public GarmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Garment> garmentMapper = (rs, rowNum) -> new Garment(
            rs.getString("id"),
            rs.getString("type"),
            rs.getString("size"),
            rs.getString("fabric_id"),
            rs.getInt("stock")
    );

    public List<Garment> findAll() {
        return jdbcTemplate.query("SELECT * FROM garments", garmentMapper);
    }

    public Garment findById(String id) {
        List<Garment> list = jdbcTemplate.query(
                "SELECT * FROM garments WHERE id = ?",
                garmentMapper,
                id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public void save(Garment garment) {
        jdbcTemplate.update(
                "INSERT INTO garments (id, type, size, fabric_id, stock) VALUES (?, ?, ?, ?, ?)",
                garment.getId(),
                garment.getType(),
                garment.getSize(),
                garment.getFabricId(),
                garment.getStock()
        );
    }

    public void update(Garment garment) {
        jdbcTemplate.update(
                "UPDATE garments SET type=?, size=?, fabric_id=?, stock=? WHERE id=?",
                garment.getType(),
                garment.getSize(),
                garment.getFabricId(),
                garment.getStock(),
                garment.getId()
        );
    }

    public void delete(String id) {
        jdbcTemplate.update("DELETE FROM garments WHERE id=?", id);
    }

    // Count total garments
    public long count() {
        String sql = "SELECT COUNT(*) FROM garments";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
}


