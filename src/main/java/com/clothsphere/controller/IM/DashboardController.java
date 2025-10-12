package com.clothsphere.controller.IM;

import com.clothsphere.service.IM.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            stats.put("totalFabrics", dashboardService.getTotalFabricsInStock());
            stats.put("totalGarments", dashboardService.getTotalGarmentsInStock());
            stats.put("rejectedFabrics", dashboardService.getRejectedFabricsCount());
            stats.put("rejectedGarments", dashboardService.getRejectedGarmentsCount());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            // Return default values if there's an error
            stats.put("totalFabrics", 0);
            stats.put("totalGarments", 0);
            stats.put("rejectedFabrics", 0);
            stats.put("rejectedGarments", 0);
            return ResponseEntity.ok(stats);
        }
    }

    @GetMapping("/fabric-movements")
    public ResponseEntity<?> getFabricMovements() {
        try {
            return ResponseEntity.ok(dashboardService.getFabricMovementData());
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>()); // Return empty array instead of error
        }
    }

    @GetMapping("/garment-movements")
    public ResponseEntity<?> getGarmentMovements() {
        try {
            return ResponseEntity.ok(dashboardService.getGarmentMovementData());
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>()); // Return empty array instead of error
        }
    }

    @GetMapping("/low-stock/fabrics")
    public ResponseEntity<?> getLowStockFabrics() {
        try {
            return ResponseEntity.ok(dashboardService.getLowStockFabrics());
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>()); // Return empty array instead of error
        }
    }

    @GetMapping("/low-stock/garments")
    public ResponseEntity<?> getLowStockGarments() {
        try {
            return ResponseEntity.ok(dashboardService.getLowStockGarments());
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>()); // Return empty array instead of error
        }
    }
}