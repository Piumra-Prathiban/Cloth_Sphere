package com.clothsphere.controller.Inventory;

import com.clothsphere.model.Inventory.Fabric;
import com.clothsphere.service.Inventory.FabricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.clothsphere.model.Inventory.FabricMovement;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FabricController {

    @Autowired
    private FabricService fabricService;

    // ========== FABRIC ENDPOINTS ==========

    @PostMapping("/fabrics/new")
    public ResponseEntity<Map<String, Object>> addFabric(@Valid @RequestBody Fabric fabric) {
        Map<String, Object> response = new HashMap<>();
        try {
            Fabric savedFabric = fabricService.addFabric(fabric);
            response.put("success", true);
            response.put("message", "Fabric added successfully");
            response.put("data", savedFabric);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/fabrics/all")
    public ResponseEntity<List<Map<String, Object>>> getAllFabricsWithStock() {
        try {
            List<Fabric> fabrics = fabricService.getAllFabrics();
            List<Map<String, Object>> fabricsWithStock = new ArrayList<>();

            for (Fabric fabric : fabrics) {
                Map<String, Object> fabricData = new HashMap<>();
                fabricData.put("fabricId", fabric.getFabricId());
                fabricData.put("fabricType", fabric.getFabricType());
                fabricData.put("color", fabric.getColor());
                fabricData.put("currentStock", fabricService.getCurrentTotalQuantity(fabric.getFabricId()));
                fabricsWithStock.add(fabricData);
            }

            return ResponseEntity.ok(fabricsWithStock);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/fabrics/{fabricId}")
    public ResponseEntity<Fabric> getFabricById(@PathVariable String fabricId) {
        try {
            Fabric fabric = fabricService.getFabricById(fabricId);
            return ResponseEntity.ok(fabric);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/fabrics/{fabricId}")
    public ResponseEntity<Map<String, Object>> updateFabric(
            @PathVariable String fabricId,
            @Valid @RequestBody Fabric fabric) {
        Map<String, Object> response = new HashMap<>();
        try {
            Fabric updated = fabricService.updateFabric(fabricId, fabric);
            response.put("success", true);
            response.put("message", "Fabric updated successfully");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/fabrics/{fabricId}/thresholds")
    public ResponseEntity<Map<String, Object>> updateFabricThresholds(
            @PathVariable String fabricId,
            @RequestBody Map<String, Double> thresholds) {
        Map<String, Object> response = new HashMap<>();
        try {
            Fabric fabric = fabricService.getFabricById(fabricId);
            if (thresholds.containsKey("lowStockThreshold")) {
                fabric.setLowStockThreshold(thresholds.get("lowStockThreshold"));
            }
            if (thresholds.containsKey("reorderLevel")) {
                fabric.setReorderLevel(thresholds.get("reorderLevel"));
            }
            Fabric updated = fabricService.updateFabric(fabricId, fabric);
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

    @DeleteMapping("/fabrics/{fabricId}")
    public ResponseEntity<Map<String, Object>> deleteFabric(@PathVariable String fabricId) {
        Map<String, Object> response = new HashMap<>();
        try {
            fabricService.deleteFabric(fabricId);
            response.put("success", true);
            response.put("message", "Fabric deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ========== MOVEMENT ENDPOINTS ==========

    @PostMapping("/movements")
    public ResponseEntity<Map<String, Object>> addMovement(@Valid @RequestBody FabricMovement movement) {
        Map<String, Object> response = new HashMap<>();
        try {
            FabricMovement savedMovement = fabricService.addMovement(movement);
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

    @GetMapping("/movements")
    public ResponseEntity<List<Map<String, Object>>> getAllMovements() {
        try {
            List<FabricMovement> movements = fabricService.getAllMovements();
            List<Map<String, Object>> movementsWithDetails = new ArrayList<>();

            for (FabricMovement movement : movements) {
                Map<String, Object> data = new HashMap<>();
                data.put("movementId", movement.getMovementId());
                data.put("fabricId", movement.getFabricId());
                data.put("status", movement.getStatus());
                data.put("movementDate", movement.getMovementDate().toString());
                data.put("quantity", movement.getQuantity());
                data.put("totalQuantity", movement.getTotalQuantity());

                // Get fabric details
                try {
                    Fabric fabric = fabricService.getFabricById(movement.getFabricId());
                    data.put("fabricType", fabric.getFabricType());
                    data.put("color", fabric.getColor());
                } catch (Exception e) {
                    data.put("fabricType", "Unknown");
                    data.put("color", "Unknown");
                }

                movementsWithDetails.add(data);
            }

            return ResponseEntity.ok(movementsWithDetails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/movements/fabric/{fabricId}")
    public ResponseEntity<List<FabricMovement>> getMovementsByFabricId(@PathVariable String fabricId) {
        try {
            List<FabricMovement> movements = fabricService.getMovementsByFabricId(fabricId);
            return ResponseEntity.ok(movements);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/movements/{movementId}")
    public ResponseEntity<FabricMovement> getMovementById(@PathVariable String movementId) {
        try {
            FabricMovement movement = fabricService.getMovementById(movementId);
            return ResponseEntity.ok(movement);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/movements/{movementId}")
    public ResponseEntity<Map<String, Object>> deleteMovement(@PathVariable String movementId) {
        Map<String, Object> response = new HashMap<>();
        try {
            fabricService.deleteMovement(movementId);
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

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = fabricService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/dashboard/low-stock")
    public ResponseEntity<List<Map<String, Object>>> getLowStockAlerts() {
        try {
            List<Map<String, Object>> alerts = fabricService.getLowStockAlerts();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/dashboard/chart-data")
    public ResponseEntity<Map<String, Object>> getChartData() {
        try {
            Map<String, Object> chartData = fabricService.getFabricChartData();
            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/dashboard/stock-summary")
    public ResponseEntity<Map<String, Object>> getStockSummary() {
        try {
            Map<String, Object> summary = fabricService.getFabricStockSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // ========== CATEGORY MANAGEMENT ==========

    @GetMapping("/categories/types")
    public ResponseEntity<List<String>> getAllFabricTypes() {
        try {
            List<String> types = fabricService.getAllFabricTypes();
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/categories/by-type/{fabricType}")
    public ResponseEntity<List<Map<String, Object>>> getFabricsByType(@PathVariable String fabricType) {
        try {
            List<Map<String, Object>> fabrics = fabricService.getFabricsByType(fabricType);
            return ResponseEntity.ok(fabrics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/categories/summary")
    public ResponseEntity<Map<String, Object>> getCategorySummary() {
        try {
            Map<String, Object> summary = fabricService.getFabricCategorySummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}