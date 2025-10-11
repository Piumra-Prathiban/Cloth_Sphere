package com.clothsphere.service.IM;

import com.clothsphere.repository.IM.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Get Fabric Availability Report Data
     * Returns raw data from repository - no model needed
     */
    public List<Map<String, Object>> getFabricAvailabilityData(String startDateStr, String endDateStr) {
        try {
            LocalDate startDate = LocalDate.parse(startDateStr, DATE_FORMATTER);
            LocalDate endDate = LocalDate.parse(endDateStr, DATE_FORMATTER);

            validateDates(startDate, endDate);

            return reportRepository.getFabricAvailabilityData(startDate, endDate);

        } catch (Exception e) {
            throw new RuntimeException("Error getting fabric availability data: " + e.getMessage(), e);
        }
    }

    /**
     * Get Fabric Usage Report Data
     * Returns raw data from repository - no model needed
     */
    public List<Map<String, Object>> getFabricUsageData(String startDateStr, String endDateStr) {
        try {
            LocalDate startDate = LocalDate.parse(startDateStr, DATE_FORMATTER);
            LocalDate endDate = LocalDate.parse(endDateStr, DATE_FORMATTER);

            validateDates(startDate, endDate);

            return reportRepository.getFabricUsageData(startDate, endDate);

        } catch (Exception e) {
            throw new RuntimeException("Error getting fabric usage data: " + e.getMessage(), e);
        }
    }

    /**
     * Get total fabric count
     */
    public int getTotalFabricCount() {
        return reportRepository.countTotalFabrics();
    }

    /**
     * Validate date range
     */
    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }

    // Add these methods to ReportService.java

    /**
     * Get Garment Availability Report Data
     */
    public List<Map<String, Object>> getGarmentAvailabilityData(String startDateStr, String endDateStr) {
        try {
            LocalDate startDate = LocalDate.parse(startDateStr, DATE_FORMATTER);
            LocalDate endDate = LocalDate.parse(endDateStr, DATE_FORMATTER);
            validateDates(startDate, endDate);

            // TODO: Implement repository method for garment availability
            return List.of(); // Return empty list for now
        } catch (Exception e) {
            throw new RuntimeException("Error getting garment availability data: " + e.getMessage(), e);
        }
    }

    /**
     * Get Garment Shipped Report Data
     */
    public List<Map<String, Object>> getGarmentShippedData(String startDateStr, String endDateStr) {
        try {
            LocalDate startDate = LocalDate.parse(startDateStr, DATE_FORMATTER);
            LocalDate endDate = LocalDate.parse(endDateStr, DATE_FORMATTER);
            validateDates(startDate, endDate);

            // TODO: Implement repository method for garment shipments
            return List.of(); // Return empty list for now
        } catch (Exception e) {
            throw new RuntimeException("Error getting garment shipped data: " + e.getMessage(), e);
        }
    }

}