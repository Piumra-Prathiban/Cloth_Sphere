package com.clothsphere.controller.SOM;

import com.clothsphere.model.SOM.SummaryReport;
import com.clothsphere.service.SOM.SummaryReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/summary-reports")
@CrossOrigin(origins = "http://localhost:8080")
public class SummaryReportController {

    @Autowired
    private SummaryReportService summaryReportService;

    // Generate summary report
    @PostMapping("/generate")
    public ResponseEntity<?> generateReport(
            @RequestParam String reportType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam String generatedBy) {

        try {
            SummaryReport report = summaryReportService.generateSummaryReport(
                    reportType, startDate, endDate, generatedBy);

            return new ResponseEntity<>(report, HttpStatus.CREATED);

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to generate report: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    // Get all reports
    @GetMapping
    public ResponseEntity<List<SummaryReport>> getAllReports() {
        List<SummaryReport> reports = summaryReportService.getAllReports();
        return new ResponseEntity<>(reports, HttpStatus.OK);
    }

    // Get report by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getReport(@PathVariable Long id) {
        Optional<SummaryReport> report = summaryReportService.getReportById(id);
        if (report.isPresent()) {
            return new ResponseEntity<>(report.get(), HttpStatus.OK);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Report not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    // Get quick stats
    @GetMapping("/quick-stats")
    public ResponseEntity<Map<String, Object>> getQuickStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Map<String, Object> stats = summaryReportService.getQuickStats(startDate, endDate);
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    // Download CSV report
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long id) {
        try {
            Optional<SummaryReport> report = summaryReportService.getReportById(id);
            if (report.isPresent() && report.get().getFilePath() != null) {
                Path filePath = Paths.get(report.get().getFilePath());

                if (Files.exists(filePath)) {
                    byte[] csvData = Files.readAllBytes(filePath);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    headers.setContentDispositionFormData("attachment",
                            filePath.getFileName().toString());

                    return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
                }
            }

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete report
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable Long id) {
        try {
            summaryReportService.deleteReport(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Report deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to delete report: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    // Get reports by type
    @GetMapping("/type/{reportType}")
    public ResponseEntity<List<SummaryReport>> getReportsByType(@PathVariable String reportType) {
        List<SummaryReport> reports = summaryReportService.getReportsByType(reportType);
        return new ResponseEntity<>(reports, HttpStatus.OK);
    }
}