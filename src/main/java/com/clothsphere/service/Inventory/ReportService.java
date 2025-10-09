package com.clothsphere.service.Inventory;

import com.clothsphere.model.Inventory.*;
import com.clothsphere.repository.Inventory.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private FabricRepository fabricRepository;

    @Autowired
    private FabricMovementRepository fabricMovementRepository;

    @Autowired
    private GarmentRepository garmentRepository;

    @Autowired
    private GarmentMovementRepository garmentMovementRepository;

    @Autowired
    private FabricService fabricService;

    @Autowired
    private GarmentService garmentService;

    // ========== FABRIC USAGE REPORT ==========
    public Map<String, Object> getFabricUsageReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();

        // Get all fabric movements in the date range
        List<FabricMovement> movements = fabricMovementRepository
                .findByMovementDateBetween(startDate, endDate);

        // Group by fabric and calculate usage
        Map<String, Map<String, Object>> fabricUsageMap = new HashMap<>();

        for (FabricMovement movement : movements) {
            try {
                Fabric fabric = fabricRepository.findById(movement.getFabricId()).orElse(null);
                if (fabric == null) continue;

                String key = movement.getFabricId();

                if (!fabricUsageMap.containsKey(key)) {
                    Map<String, Object> fabricData = new HashMap<>();
                    fabricData.put("fabricId", fabric.getFabricId());
                    fabricData.put("type", fabric.getFabricType());
                    fabricData.put("color", fabric.getColor());
                    fabricData.put("totalIn", 0.0);
                    fabricData.put("totalOut", 0.0);
                    fabricData.put("netUsage", 0.0);
                    fabricUsageMap.put(key, fabricData);
                }

                Map<String, Object> fabricData = fabricUsageMap.get(key);
                if ("In".equals(movement.getStatus())) {
                    fabricData.put("totalIn", (Double)fabricData.get("totalIn") + movement.getQuantity());
                } else {
                    fabricData.put("totalOut", (Double)fabricData.get("totalOut") + movement.getQuantity());
                }

                double netUsage = (Double)fabricData.get("totalIn") - (Double)fabricData.get("totalOut");
                fabricData.put("netUsage", netUsage);

            } catch (Exception e) {
                continue;
            }
        }

        report.put("reportType", "Fabric Usage Report");
        report.put("startDate", startDate.toString());
        report.put("endDate", endDate.toString());
        report.put("generatedDate", LocalDate.now().toString());
        report.put("data", new ArrayList<>(fabricUsageMap.values()));
        report.put("totalRecords", fabricUsageMap.size());

        return report;
    }

    // ========== FABRIC AVAILABILITY REPORT ==========
    public Map<String, Object> getFabricAvailabilityReport() {
        Map<String, Object> report = new HashMap<>();
        List<Fabric> allFabrics = fabricRepository.findAll();
        List<Map<String, Object>> availabilityData = new ArrayList<>();

        for (Fabric fabric : allFabrics) {
            double currentStock = fabricService.getCurrentTotalQuantity(fabric.getFabricId());
            double threshold = fabric.getLowStockThreshold() != null ? fabric.getLowStockThreshold() : 50.0;
            double reorderLevel = fabric.getReorderLevel() != null ? fabric.getReorderLevel() : 100.0;

            Map<String, Object> data = new HashMap<>();
            data.put("fabricId", fabric.getFabricId());
            data.put("type", fabric.getFabricType());
            data.put("color", fabric.getColor());
            data.put("currentStock", currentStock);
            data.put("threshold", threshold);
            data.put("reorderLevel", reorderLevel);
            data.put("status", currentStock < threshold ? "Low Stock" : currentStock > (reorderLevel * 2) ? "Overstocked" : "Normal");
            data.put("lastUpdated", getLastMovementDate(fabric.getFabricId(), "fabric"));

            availabilityData.add(data);
        }

        report.put("reportType", "Fabric Availability Report");
        report.put("generatedDate", LocalDate.now().toString());
        report.put("data", availabilityData);
        report.put("totalFabrics", availabilityData.size());
        report.put("lowStockCount", availabilityData.stream()
                .filter(d -> "Low Stock".equals(d.get("status"))).count());

        return report;
    }

    // ========== GARMENT MOVEMENT REPORT ==========
    public Map<String, Object> getGarmentMovementReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();

        // Get all garment movements in the date range
        List<GarmentMovement> movements = garmentMovementRepository
                .findByMovementDateBetween(startDate, endDate);

        // Group by garment and calculate movements
        Map<String, Map<String, Object>> garmentMovementMap = new HashMap<>();

        for (GarmentMovement movement : movements) {
            try {
                Garment garment = garmentRepository.findById(movement.getGarmentId());
                if (garment == null) continue;

                String key = movement.getGarmentId();

                if (!garmentMovementMap.containsKey(key)) {
                    Map<String, Object> garmentData = new HashMap<>();
                    garmentData.put("garmentId", garment.getGarmentId());
                    garmentData.put("type", garment.getGarmentType());
                    garmentData.put("size", garment.getSize());
                    garmentData.put("totalProduced", 0);
                    garmentData.put("totalShipped", 0);
                    garmentData.put("netMovement", 0);
                    garmentMovementMap.put(key, garmentData);
                }

                Map<String, Object> garmentData = garmentMovementMap.get(key);
                if ("In".equals(movement.getStatus())) {
                    garmentData.put("totalProduced", (Integer)garmentData.get("totalProduced") + movement.getQuantity());
                } else {
                    garmentData.put("totalShipped", (Integer)garmentData.get("totalShipped") + movement.getQuantity());
                }

                int netMovement = (Integer)garmentData.get("totalProduced") - (Integer)garmentData.get("totalShipped");
                garmentData.put("netMovement", netMovement);

            } catch (Exception e) {
                continue;
            }
        }

        report.put("reportType", "Garment Movement Report");
        report.put("startDate", startDate.toString());
        report.put("endDate", endDate.toString());
        report.put("generatedDate", LocalDate.now().toString());
        report.put("data", new ArrayList<>(garmentMovementMap.values()));
        report.put("totalRecords", garmentMovementMap.size());

        return report;
    }

    // ========== GARMENT AVAILABILITY REPORT ==========
    public Map<String, Object> getGarmentAvailabilityReport() {
        Map<String, Object> report = new HashMap<>();
        List<Garment> allGarments = garmentRepository.findAll();
        List<Map<String, Object>> availabilityData = new ArrayList<>();

        for (Garment garment : allGarments) {
            int currentStock = garmentService.getCurrentTotalQuantity(garment.getGarmentId());
            int threshold = garment.getLowStockThreshold() != null ? garment.getLowStockThreshold() : 50;
            int reorderLevel = garment.getReorderLevel() != null ? garment.getReorderLevel() : 100;

            // Get fabric details from latest movement
            List<GarmentMovement> movements = garmentMovementRepository
                    .findLatestMovementByGarmentId(garment.getGarmentId());

            String fabricId = "N/A";
            String fabricType = "N/A";
            String color = "N/A";

            if (!movements.isEmpty()) {
                fabricId = movements.get(0).getFabricId();
                try {
                    Fabric fabric = fabricRepository.findById(fabricId).orElse(null);
                    if (fabric != null) {
                        fabricType = fabric.getFabricType();
                        color = fabric.getColor();
                    }
                } catch (Exception e) {
                    // Use defaults
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("garmentId", garment.getGarmentId());
            data.put("type", garment.getGarmentType());
            data.put("size", garment.getSize());
            data.put("fabricType", fabricType);
            data.put("color", color);
            data.put("currentStock", currentStock);
            data.put("threshold", threshold);
            data.put("reorderLevel", reorderLevel);
            data.put("status", currentStock < threshold ? "Low Stock" : currentStock > (reorderLevel * 2) ? "Overstocked" : "Normal");
            data.put("lastUpdated", getLastMovementDate(garment.getGarmentId(), "garment"));

            availabilityData.add(data);
        }

        report.put("reportType", "Garment Availability Report");
        report.put("generatedDate", LocalDate.now().toString());
        report.put("data", availabilityData);
        report.put("totalGarments", availabilityData.size());
        report.put("lowStockCount", availabilityData.stream()
                .filter(d -> "Low Stock".equals(d.get("status"))).count());

        return report;
    }

    // Helper method to get last movement date
    private String getLastMovementDate(String id, String type) {
        try {
            if ("fabric".equals(type)) {
                List<FabricMovement> movements = fabricMovementRepository
                        .findLatestMovementByFabricId(id);
                if (!movements.isEmpty()) {
                    return movements.get(0).getMovementDate().toString();
                }
            } else {
                List<GarmentMovement> movements = garmentMovementRepository
                        .findLatestMovementByGarmentId(id);
                if (!movements.isEmpty()) {
                    return movements.get(0).getMovementDate().toString();
                }
            }
        } catch (Exception e) {
            // Return N/A if error
        }
        return "N/A";
    }

    // ========== FABRIC USAGE REPORT (with renamed method) ==========
    public Map<String, Object> generateFabricUsageReport(LocalDate startDate, LocalDate endDate) {
        return getFabricUsageReport(startDate, endDate);
    }

    // ========== FABRIC AVAILABILITY REPORT (with renamed method) ==========
    public Map<String, Object> generateFabricAvailabilityReport(LocalDate startDate, LocalDate endDate) {
        return getFabricAvailabilityReport();
    }

    // ========== GARMENT MOVEMENT REPORT (with renamed method) ==========
    public Map<String, Object> generateGarmentMovementReport(LocalDate startDate, LocalDate endDate) {
        return getGarmentMovementReport(startDate, endDate);
    }

    // ========== GARMENT AVAILABILITY REPORT (with renamed method) ==========
    public Map<String, Object> generateGarmentAvailabilityReport(LocalDate startDate, LocalDate endDate) {
        return getGarmentAvailabilityReport();
    }

    // ========== SUMMARY STATISTICS ==========
    public Map<String, Object> getReportSummary() {
        Map<String, Object> summary = new HashMap<>();

        // Fabric summary
        long totalFabrics = fabricRepository.count();
        double totalFabricStock = fabricRepository.findAll().stream()
                .mapToDouble(f -> fabricService.getCurrentTotalQuantity(f.getFabricId()))
                .sum();

        // Garment summary
        long totalGarments = garmentRepository.count();
        int totalGarmentStock = garmentRepository.findAll().stream()
                .mapToInt(g -> garmentService.getCurrentTotalQuantity(g.getGarmentId()))
                .sum();

        // Today's activity
        LocalDate today = LocalDate.now();
        long fabricMovementsToday = fabricMovementRepository.findByMovementDate(today).size();
        long garmentMovementsToday = garmentMovementRepository.findByMovementDate(today).size();

        summary.put("totalFabrics", totalFabrics);
        summary.put("totalFabricStock", totalFabricStock);
        summary.put("totalGarments", totalGarments);
        summary.put("totalGarmentStock", totalGarmentStock);
        summary.put("fabricMovementsToday", fabricMovementsToday);
        summary.put("garmentMovementsToday", garmentMovementsToday);

        return summary;
    }
}