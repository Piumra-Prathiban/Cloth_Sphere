package com.clothsphere.controller.IM;

import com.clothsphere.service.IM.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reports") // Changed from "/api/reports" to match your HTML
public class ReportController {

    @Autowired
    private ReportService reportService;

    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Show reports page
     */
    @GetMapping
    public String showReportsPage() {
        return "reports"; // Make sure this template exists
    }

    /**
     * Generate and display Fabric Availability Report
     */
    @GetMapping("/fabric-availability")
    public String getFabricAvailabilityReport(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            Model model) {

        try {
            List<Map<String, Object>> data = reportService.getFabricAvailabilityData(startDate, endDate);

            model.addAttribute("data", data);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("totalFabrics", data.size());
            model.addAttribute("generatedDate", LocalDateTime.now().format(DATETIME_FORMATTER));

            return "fabric-availability-report";

        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while generating the report: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Generate and display Fabric Usage Report
     */
    @GetMapping("/fabric-usage")
    public String getFabricUsageReport(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            Model model) {

        try {
            List<Map<String, Object>> data = reportService.getFabricUsageData(startDate, endDate);

            model.addAttribute("data", data);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("totalRecords", data.size());
            model.addAttribute("generatedDate", LocalDateTime.now().format(DATETIME_FORMATTER));

            return "fabric-usage-report";

        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while generating the report: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Generate and display Garment Availability Report
     */
    @GetMapping("/garment-availability")
    public String getGarmentAvailabilityReport(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            Model model) {

        try {
            // TODO: Implement garment availability service method
            // For now, return empty data
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("generatedDate", LocalDateTime.now().format(DATETIME_FORMATTER));

            // Add dummy data structure to match your HTML template
            model.addAttribute("report", Map.of(
                    "generatedDate", LocalDateTime.now().format(DATETIME_FORMATTER),
                    "totalGarments", 0,
                    "data", List.of()
            ));

            return "garment-availability-report";

        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while generating the report: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Generate and display Garment Shipped Report
     */
    @GetMapping("/garment-shipped")
    public String getGarmentShippedReport(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            Model model) {

        try {
            // TODO: Implement garment shipped service method
            // For now, return empty data
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("generatedDate", LocalDateTime.now().format(DATETIME_FORMATTER));

            // Add dummy data structure to match your HTML template
            model.addAttribute("report", Map.of(
                    "startDate", startDate,
                    "endDate", endDate,
                    "generatedDate", LocalDateTime.now().format(DATETIME_FORMATTER),
                    "totalRecords", 0,
                    "data", List.of()
            ));

            return "garment-shipped-report";

        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while generating the report: " + e.getMessage());
            return "error";
        }
    }
}