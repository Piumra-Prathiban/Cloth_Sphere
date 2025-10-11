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
@RequestMapping("/api/reports")
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
        return "reports";
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
            // Get data directly from service
            List<Map<String, Object>> data = reportService.getFabricAvailabilityData(startDate, endDate);

            // Add attributes to model
            model.addAttribute("data", data);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("totalFabrics", data.size());
            model.addAttribute("generatedDate", LocalDateTime.now().format(DATETIME_FORMATTER));

            return "fabric-availability-report";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while generating the report");
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
            // Get data directly from service
            List<Map<String, Object>> data = reportService.getFabricUsageData(startDate, endDate);

            // Add attributes to model
            model.addAttribute("data", data);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("totalRecords", data.size());
            model.addAttribute("generatedDate", LocalDateTime.now().format(DATETIME_FORMATTER));

            return "fabric-usage-report";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while generating the report");
            return "error";
        }
    }
}