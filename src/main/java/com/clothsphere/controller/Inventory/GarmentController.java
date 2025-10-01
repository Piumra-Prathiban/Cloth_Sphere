package com.clothsphere.controller.Inventory;

import com.clothsphere.model.Inventory.Garment;
import com.clothsphere.service.Inventory.GarmentService;

import com.clothsphere.model.Inventory.GarmentMovement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GarmentController {

    @Autowired
    private GarmentService garmentService;

    // ========== GARMENT ENDPOINTS ==========

    @PostMapping("/garments/new")
    public ResponseEntity<Map<String, Object>> addGarment(@Valid @RequestBody Garment garment) {
        Map<String, Object> response = new HashMap<>();
        try {
            Garment savedGarment = garmentService.addGarment(garment);
            response.put("success", true);
            response.put("message", "Garment type added successfully");
            response.put("data", savedGarment);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/garments/all")
    public ResponseEntity<List<Map<String, Object>>> getAllGarmentsWithStock() {
        try {
            List<Garment> garments = garmentService.getAllGarments();
            List<Map<String, Object>> garmentsWithStock = new ArrayList<>();

            for (Garment garment : garments) {
                Map<String, Object> garmentData = new HashMap<>();
                garmentData.put("garmentId", garment.getGarmentId());
                garmentData.put("garmentType", garment.getGarmentType());
                garmentData.put("size", garment.getSize());
                garmentData.put("currentStock", garmentService.getCurrentTotalQuantity(garment.getGarmentId()));
                garmentsWithStock.add(garmentData);
            }

            return ResponseEntity.ok(garmentsWithStock);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/garments/{garmentId}")
    public ResponseEntity<Garment> getGarmentById(@PathVariable String garmentId) {
        try {
            Garment garment = garmentService.getGarmentById(garmentId);
            return ResponseEntity.ok(garment);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/garments/{garmentId}")
    public ResponseEntity<Map<String, Object>> updateGarment(
            @PathVariable String garmentId,
            @Valid @RequestBody Garment garment) {
        Map<String, Object> response = new HashMap<>();
        try {
            Garment updated = garmentService.updateGarment(garmentId, garment);
            response.put("success", true);
            response.put("message", "Garment updated successfully");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/garments/{garmentId}/thresholds")
    public ResponseEntity<Map<String, Object>> updateGarmentThresholds(
            @PathVariable String garmentId,
            @RequestBody Map<String, Integer> thresholds) {
        Map<String, Object> response = new HashMap<>();
        try {
            Garment garment = garmentService.getGarmentById(garmentId);
            if (thresholds.containsKey("lowStockThreshold")) {
                garment.setLowStockThreshold(thresholds.get("lowStockThreshold"));
            }
            if (thresholds.containsKey("reorderLevel")) {
                garment.setReorderLevel(thresholds.get("reorderLevel"));
            }
            Garment updated = garmentService.updateGarment(garmentId, garment);
            response.put("success", true);
            response.put("message", "Thresholds updated successfully");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/garments/{garmentId}")
    public ResponseEntity<Map<String, Object>> deleteGarment(@PathVariable String garmentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            garmentService.deleteGarment(garmentId);
            response.put("success", true);
            response.put("message", "Garment deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ========== MOVEMENT ENDPOINTS ==========

    @PostMapping("/garment-movements")
    public ResponseEntity<Map<String, Object>> addMovement(@Valid @RequestBody GarmentMovement movement) {
        Map<String, Object> response = new HashMap<>();
        try {
            GarmentMovement savedMovement = garmentService.addMovement(movement);
            response.put("success", true);
            response.put("message", "Movement recorded successfully");
            response.put("data", savedMovement);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/garment-movements")
    public ResponseEntity<List<Map<String, Object>>> getAllMovements() {
        try {
            List<GarmentMovement> movements = garmentService.getAllMovements();
            List<Map<String, Object>> movementsWithDetails = new ArrayList<>();

            for (GarmentMovement movement : movements) {
                Map<String, Object> data = new HashMap<>();
                data.put("movementId", movement.getMovementId());
                data.put("garmentId", movement.getGarmentId());
                data.put("fabricId", movement.getFabricId());
                data.put("status", movement.getStatus());
                data.put("movementDate", movement.getMovementDate().toString());
                data.put("quantity", movement.getQuantity());
                data.put("totalQuantity", movement.getTotalQuantity());

                // Get garment details
                try {
                    Garment garment = garmentService.getGarmentById(movement.getGarmentId());
                    data.put("garmentType", garment.getGarmentType());
                    data.put("size", garment.getSize());
                } catch (Exception e) {
                    data.put("garmentType", "Unknown");
                    data.put("size", "Unknown");
                }

                movementsWithDetails.add(data);
            }

            return ResponseEntity.ok(movementsWithDetails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/garment-movements/garment/{garmentId}")
    public ResponseEntity<List<GarmentMovement>> getMovementsByGarmentId(@PathVariable String garmentId) {
        try {
            List<GarmentMovement> movements = garmentService.getMovementsByGarmentId(garmentId);
            return ResponseEntity.ok(movements);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/garment-movements/{movementId}")
    public ResponseEntity<GarmentMovement> getMovementById(@PathVariable String movementId) {
        try {
            GarmentMovement movement = garmentService.getMovementById(movementId);
            return ResponseEntity.ok(movement);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/garment-movements/{movementId}")
    public ResponseEntity<Map<String, Object>> deleteMovement(@PathVariable String movementId) {
        Map<String, Object> response = new HashMap<>();
        try {
            garmentService.deleteMovement(movementId);
            response.put("success", true);
            response.put("message", "Movement deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ========== DASHBOARD ENDPOINTS ==========

    @GetMapping("/garments/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = garmentService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/garments/dashboard/low-stock")
    public ResponseEntity<List<Map<String, Object>>> getLowStockAlerts() {
        try {
            List<Map<String, Object>> alerts = garmentService.getLowStockAlerts();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/garments/dashboard/chart-data")
    public ResponseEntity<Map<String, Object>> getChartData() {
        try {
            Map<String, Object> chartData = garmentService.getGarmentChartData();
            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/garments/dashboard/type-distribution")
    public ResponseEntity<Map<String, Object>> getTypeDistribution() {
        try {
            Map<String, Object> chartData = garmentService.getGarmentTypeDistribution();
            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/garments/dashboard/stock-summary")
    public ResponseEntity<Map<String, Object>> getStockSummary() {
        try {
            Map<String, Object> summary = garmentService.getGarmentStockSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // ========== CATEGORY MANAGEMENT ==========

    @GetMapping("/garments/categories/types")
    public ResponseEntity<List<String>> getAllGarmentTypes() {
        try {
            List<String> types = garmentService.getAllGarmentTypes();
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/garments/categories/sizes")
    public ResponseEntity<List<String>> getAllSizes() {
        try {
            List<String> sizes = garmentService.getAllSizes();
            return ResponseEntity.ok(sizes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/garments/categories/by-type/{garmentType}")
    public ResponseEntity<List<Map<String, Object>>> getGarmentsByType(@PathVariable String garmentType) {
        try {
            List<Map<String, Object>> garments = garmentService.getGarmentsByType(garmentType);
            return ResponseEntity.ok(garments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/garments/categories/by-size/{size}")
    public ResponseEntity<List<Map<String, Object>>> getGarmentsBySize(@PathVariable String size) {
        try {
            List<Map<String, Object>> garments = garmentService.getGarmentsBySize(size);
            return ResponseEntity.ok(garments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/garments/categories/summary")
    public ResponseEntity<Map<String, Object>> getCategorySummary() {
        try {
            Map<String, Object> summary = garmentService.getGarmentCategorySummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}