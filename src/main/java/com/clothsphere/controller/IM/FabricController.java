package com.clothsphere.controller.IM;

import com.clothsphere.model.IM.Fabric;
import com.clothsphere.model.IM.FabricMovement;
import com.clothsphere.service.IM.FabricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fabrics")
@CrossOrigin(origins = "*")
public class FabricController {

    @Autowired
    private FabricService fabricService;

    // ----------------------- FABRIC ENDPOINTS -----------------------

    @GetMapping
    public ResponseEntity<List<Fabric>> getAllFabrics() {
        return ResponseEntity.ok(fabricService.getAllFabrics());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getFabricStats() {
        return ResponseEntity.ok(fabricService.getFabricStats());
    }

    @PostMapping("/new")
    public ResponseEntity<?> createFabric(@RequestBody Fabric fabric) {
        try {
            if (fabric.getFabricType() == null || fabric.getFabricType().trim().isEmpty() ||
                    fabric.getColor() == null || fabric.getColor().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Fabric type and color are required");
            }

            if (fabricService.isDuplicateFabric(fabric.getFabricType(), fabric.getColor())) {
                return ResponseEntity.badRequest().body("Fabric with this type and color already exists");
            }

            Fabric createdFabric = fabricService.createFabric(fabric);
            return ResponseEntity.ok(createdFabric);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating fabric: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{fabricId}")
    public ResponseEntity<?> deleteFabric(@PathVariable String fabricId) {
        try {
            boolean deleted = fabricService.deleteFabric(fabricId);
            if (deleted) return ResponseEntity.ok("Fabric deleted successfully");
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error deleting fabric: " + e.getMessage());
        }
    }

    // ----------------------- FABRIC MOVEMENTS -----------------------

    @GetMapping("/movements")
    public ResponseEntity<List<FabricMovement>> getAllMovements() {
        return ResponseEntity.ok(fabricService.getAllMovements());
    }

    @PostMapping("/movements/add")
    public ResponseEntity<?> addMovement(@RequestBody FabricMovement movement) {
        try {
            if (movement.getFabricId() == null || movement.getFabricId().trim().isEmpty() ||
                    movement.getStatus() == null || movement.getQuantity() <= 0) {
                return ResponseEntity.badRequest().body("Fabric ID, status, and positive quantity are required");
            }

            Fabric fabric = fabricService.getFabricById(movement.getFabricId());
            if (fabric == null) return ResponseEntity.badRequest().body("Fabric not found");

            if ("OUT".equalsIgnoreCase(movement.getStatus())) {
                if (movement.getQuantity() > fabric.getCurrentStock()) {
                    return ResponseEntity.badRequest().body("Quantity exceeds available stock");
                }
            }

            FabricMovement createdMovement = fabricService.addMovement(movement);
            return ResponseEntity.ok(createdMovement);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error recording movement: " + e.getMessage());
        }
    }

    @PostMapping("/movements/approve")
    public ResponseEntity<?> approveMovement(@RequestBody Map<String, Object> approvalData) {
        try {
            String movementId = (String) approvalData.get("movementId");
            String fabricId = (String) approvalData.get("fabricId");
            Double approvedQuantity = approvalData.get("approvedQuantity") != null ?
                    Double.valueOf(approvalData.get("approvedQuantity").toString()) : 0.0;
            Double rejectedQuantity = approvalData.get("rejectedQuantity") != null ?
                    Double.valueOf(approvalData.get("rejectedQuantity").toString()) : 0.0;
            String rejectionReason = (String) approvalData.get("rejectionReason");

            // Remove this line as it's causing the type cast issue
            // String approvalStatus = (String) approvalData.get("approvalStatus");

            if (movementId == null || fabricId == null || approvedQuantity == null) {
                return ResponseEntity.badRequest().body("Movement ID, Fabric ID, and approved quantity are required");
            }

            FabricMovement updatedMovement = fabricService.approveMovement(
                    movementId, fabricId, approvedQuantity, rejectedQuantity, rejectionReason);

            return ResponseEntity.ok(updatedMovement);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error approving movement: " + e.getMessage());
        }
    }

    @DeleteMapping("/movements/delete/{movementId}")
    public ResponseEntity<?> deleteMovement(@PathVariable String movementId) {
        try {
            boolean deleted = fabricService.deleteMovement(movementId);
            if (deleted) return ResponseEntity.ok("Movement deleted successfully");
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error deleting movement: " + e.getMessage());
        }
    }

    // ----------------------- FILTERS -----------------------

    @GetMapping("/type/{fabricType}")
    public ResponseEntity<List<Fabric>> getFabricsByType(@PathVariable String fabricType) {
        return ResponseEntity.ok(fabricService.getFabricsByType(fabricType));
    }

    @GetMapping("/color/{color}")
    public ResponseEntity<List<Fabric>> getFabricsByColor(@PathVariable String color) {
        return ResponseEntity.ok(fabricService.getFabricsByColor(color));
    }

    @GetMapping("/movements/date-range")
    public ResponseEntity<List<FabricMovement>> getMovementsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate) {
        return ResponseEntity.ok(fabricService.getMovementsByDateRange(startDate, endDate));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Fabric>> getLowStockFabrics() {
        return ResponseEntity.ok(fabricService.getLowStockFabrics());
    }

    // ----------------------- HEALTH CHECK -----------------------

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}
