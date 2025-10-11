package com.clothsphere.controller.IM;

import com.clothsphere.model.IM.Garment;
import com.clothsphere.model.IM.GarmentMovement;
import com.clothsphere.service.IM.GarmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/garments")
@CrossOrigin(origins = "*")
public class GarmentController {

    @Autowired
    private GarmentService garmentService;

    // ------------------ GARMENT ENDPOINTS ------------------

    @GetMapping
    public ResponseEntity<List<Garment>> getAllGarments() {
        try {
            List<Garment> garments = garmentService.getAllGarments();
            return ResponseEntity.ok(garments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{garmentId}")
    public ResponseEntity<Garment> getGarmentById(@PathVariable String garmentId) {
        try {
            Garment garment = garmentService.getGarmentById(garmentId);
            if (garment != null) {
                return ResponseEntity.ok(garment);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createGarment(@RequestBody Garment garment) {
        try {
            Garment createdGarment = garmentService.createGarment(garment);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGarment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create garment: " + e.getMessage()));
        }
    }

    @PutMapping("/{garmentId}")
    public ResponseEntity<?> updateGarment(@PathVariable String garmentId, @RequestBody Garment garmentDetails) {
        try {
            Garment updatedGarment = garmentService.updateGarment(garmentId, garmentDetails);
            return ResponseEntity.ok(updatedGarment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update garment: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{garmentId}")
    public ResponseEntity<?> deleteGarment(@PathVariable String garmentId) {
        try {
            boolean isDeleted = garmentService.deleteGarment(garmentId);
            if (isDeleted) {
                return ResponseEntity.ok(Map.of("message", "Garment deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Garment not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete garment: " + e.getMessage()));
        }
    }

    // ------------------ MOVEMENT ENDPOINTS ------------------

    @GetMapping("/movements")
    public ResponseEntity<List<GarmentMovement>> getAllMovements() {
        try {
            List<GarmentMovement> movements = garmentService.getAllMovements();
            return ResponseEntity.ok(movements);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/movements/{movementId}")
    public ResponseEntity<GarmentMovement> getMovementById(@PathVariable String movementId) {
        try {
            GarmentMovement movement = garmentService.getMovementById(movementId);
            if (movement != null) {
                return ResponseEntity.ok(movement);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{garmentId}/movements")
    public ResponseEntity<List<GarmentMovement>> getMovementsByGarmentId(@PathVariable String garmentId) {
        try {
            List<GarmentMovement> movements = garmentService.getMovementsByGarmentId(garmentId);
            return ResponseEntity.ok(movements);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/movements")
    public ResponseEntity<?> addMovement(@RequestBody GarmentMovement movement) {
        try {
            GarmentMovement createdMovement = garmentService.addMovement(movement);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMovement);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create movement: " + e.getMessage()));
        }
    }

    @DeleteMapping("/movements/{movementId}")
    public ResponseEntity<?> deleteMovement(@PathVariable String movementId) {
        try {
            boolean isDeleted = garmentService.deleteMovement(movementId);
            if (isDeleted) {
                return ResponseEntity.ok(Map.of("message", "Movement deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Movement not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete movement: " + e.getMessage()));
        }
    }

    // ------------------ APPROVAL ENDPOINT ------------------

    @PostMapping("/movements/{movementId}/approve")
    public ResponseEntity<?> approveMovement(
            @PathVariable String movementId,
            @RequestParam String garmentId,
            @RequestParam Integer approvedQty,
            @RequestParam(required = false, defaultValue = "0") Integer rejectedQty,
            @RequestParam(required = false, defaultValue = "") String rejectionReason) {

        try {
            GarmentMovement approvedMovement = garmentService.approveMovement(
                    movementId, garmentId, approvedQty, rejectedQty, rejectionReason
            );
            return ResponseEntity.ok(Map.of(
                    "message", "Movement approved successfully",
                    "movement", approvedMovement
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to approve movement: " + e.getMessage()));
        }
    }

    // ------------------ FILTER ENDPOINTS ------------------

    @GetMapping("/filter/type/{type}")
    public ResponseEntity<List<Garment>> getGarmentsByType(@PathVariable String type) {
        try {
            List<Garment> garments = garmentService.getGarmentsByType(type);
            return ResponseEntity.ok(garments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/filter/size/{size}")
    public ResponseEntity<List<Garment>> getGarmentsBySize(@PathVariable String size) {
        try {
            List<Garment> garments = garmentService.getGarmentsBySize(size);
            return ResponseEntity.ok(garments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/filter/type/{type}/size/{size}")
    public ResponseEntity<List<Garment>> getGarmentsByTypeAndSize(
            @PathVariable String type, @PathVariable String size) {
        try {
            List<Garment> garments = garmentService.getGarmentsByTypeAndSize(type, size);
            return ResponseEntity.ok(garments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/movements/filter/date-range")
    public ResponseEntity<List<GarmentMovement>> getMovementsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<GarmentMovement> movements = garmentService.getMovementsByDateRange(startDate, endDate);
            return ResponseEntity.ok(movements);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/movements/filter/status/{status}")
    public ResponseEntity<List<GarmentMovement>> getMovementsByStatus(@PathVariable String status) {
        try {
            List<GarmentMovement> movements = garmentService.getMovementsByStatus(status);
            return ResponseEntity.ok(movements);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/movements/filter/approval-status/{approvalStatus}")
    public ResponseEntity<List<GarmentMovement>> getMovementsByApprovalStatus(@PathVariable String approvalStatus) {
        try {
            List<GarmentMovement> movements = garmentService.getMovementsByApprovalStatus(approvalStatus);
            return ResponseEntity.ok(movements);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ------------------ STOCK MANAGEMENT ENDPOINTS ------------------

    @GetMapping("/low-stock")
    public ResponseEntity<List<Garment>> getLowStockGarments() {
        try {
            List<Garment> garments = garmentService.getLowStockGarments();
            return ResponseEntity.ok(garments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/low-stock/{threshold}")
    public ResponseEntity<List<Garment>> getLowStockGarments(@PathVariable int threshold) {
        try {
            List<Garment> garments = garmentService.getLowStockGarments(threshold);
            return ResponseEntity.ok(garments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{garmentId}/stock")
    public ResponseEntity<?> getCurrentStock(@PathVariable String garmentId) {
        try {
            int stock = garmentService.getCurrentStock(garmentId);
            return ResponseEntity.ok(Map.of("garmentId", garmentId, "currentStock", stock));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get stock: " + e.getMessage()));
        }
    }

    // ------------------ UTILITY ENDPOINTS ------------------

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getGarmentStats() {
        try {
            Map<String, Object> stats = garmentService.getGarmentStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/check-duplicate")
    public ResponseEntity<?> checkDuplicate(
            @RequestParam String type,
            @RequestParam String size,
            @RequestParam String fabricId) {
        try {
            boolean isDuplicate = garmentService.isDuplicateGarment(type, size, fabricId);
            return ResponseEntity.ok(Map.of("isDuplicate", isDuplicate));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check duplicate"));
        }
    }

    // ------------------ PENDING APPROVALS ENDPOINT ------------------

    @GetMapping("/movements/pending-approvals")
    public ResponseEntity<List<GarmentMovement>> getPendingApprovals() {
        try {
            List<GarmentMovement> movements = garmentService.getMovementsByApprovalStatus("PENDING");
            return ResponseEntity.ok(movements);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ------------------ RECENT MOVEMENTS ENDPOINT ------------------

    @GetMapping("/movements/recent")
    public ResponseEntity<List<GarmentMovement>> getRecentMovements() {
        try {
            // Get movements from the last 30 days
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);
            List<GarmentMovement> movements = garmentService.getMovementsByDateRange(startDate, endDate);
            return ResponseEntity.ok(movements);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}