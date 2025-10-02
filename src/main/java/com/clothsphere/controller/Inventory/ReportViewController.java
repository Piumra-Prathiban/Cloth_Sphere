package com.clothsphere.controller.Inventory;

import com.clothsphere.service.Inventory.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/reports")
public class ReportViewController {

    @Autowired
    private ReportService reportService;

    // Fabric Availability Report View
    @GetMapping("/fabric-availability")
    public String showFabricAvailabilityReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        Map<String, Object> report = reportService.getFabricAvailabilityReport();
        model.addAttribute("report", report);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "fabric-availability-report";
    }

    // Garment Availability Report View
    @GetMapping("/garment-availability")
    public String showGarmentAvailabilityReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        Map<String, Object> report = reportService.getGarmentAvailabilityReport();
        model.addAttribute("report", report);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "garment-availability-report";
    }

    // Fabric Usage Report View
    @GetMapping("/fabric-usage")
    public String showFabricUsageReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        Map<String, Object> report = reportService.getFabricUsageReport(startDate, endDate);
        model.addAttribute("report", report);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "fabric-usage-report";
    }

    // Garment Shipped Report View
    @GetMapping("/garment-shipped")
    public String showGarmentShippedReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        Map<String, Object> report = reportService.getGarmentMovementReport(startDate, endDate);
        model.addAttribute("report", report);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "garment-shipped-report";
    }
}