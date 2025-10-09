package com.clothsphere.controller.Inventory;

import com.clothsphere.model.Inventory.Garment;
import com.clothsphere.model.Inventory.GarmentMovement;
import com.clothsphere.service.Inventory.GarmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GarmentController {

    @Autowired
    private GarmentService garmentService;

    // ---------------- Garments ----------------
    @GetMapping("/garments/all")
    public ResponseEntity<?> getAllGarments() {
        try {
            List<Garment> garments = garmentService.getAllGarments();
            return ResponseEntity.ok(garments);
        } catch (SQLException e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/garments/new")
    public ResponseEntity<?> addGarment(@RequestBody Garment garment) {
        try {
            garmentService.addGarment(garment);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (SQLException e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/garments/{id}")
    public ResponseEntity<?> updateGarment(@PathVariable String id, @RequestBody Garment garment) {
        try {
            garment.setId(id);
            garmentService.updateGarment(garment);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (SQLException e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/garments/{id}")
    public ResponseEntity<?> deleteGarment(@PathVariable String id) {
        try {
            garmentService.deleteGarment(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (SQLException e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ---------------- Garment Movements ----------------

    // ✅ Load all movements (this fixes your issue)
    @GetMapping("/garment-movements")
    public ResponseEntity<?> getAllMovements() {
        try {
            // We’ll fetch all movements (for all garments)
            List<GarmentMovement> allMovements = new java.util.ArrayList<>();
            List<Garment> garments = garmentService.getAllGarments();
            for (Garment g : garments) {
                allMovements.addAll(garmentService.getMovementsByGarmentId(g.getId()));
            }
            return ResponseEntity.ok(allMovements);
        } catch (SQLException e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Get movements for a specific garment
    @GetMapping("/garment-movements/{garmentId}")
    public ResponseEntity<?> getMovements(@PathVariable String garmentId) {
        try {
            List<GarmentMovement> movements = garmentService.getMovementsByGarmentId(garmentId);
            return ResponseEntity.ok(movements);
        } catch (SQLException e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/garment-movements/new")
    public ResponseEntity<?> addMovement(@RequestBody GarmentMovement movement) {
        try {
            garmentService.recordMovement(movement);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (SQLException e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }


}
