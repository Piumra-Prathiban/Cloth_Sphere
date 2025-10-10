package com.clothsphere.controller.HR;

import com.clothsphere.service.HR.MonthlyAttendanceSummaryService;
import com.clothsphere.service.HR.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/hr/attendance")
public class HRAttendanceController {

    @Autowired
    private MonthlyAttendanceSummaryService monthlySummaryService;

    @Autowired
    private AttendanceService attendanceService;

    /**
     * Get monthly attendance report with absent employees
     */
    @GetMapping("/monthly-report")
    public ResponseEntity<Map<String, Object>> getMonthlyReport(
            @RequestParam int year,
            @RequestParam int month) {

        Map<String, Object> report = monthlySummaryService.getMonthlyAttendanceReport(year, month);
        return ResponseEntity.ok(report);
    }

    /**
     * Get individual employee monthly summary
     */
    @GetMapping("/employee-monthly")
    public ResponseEntity<Map<String, Object>> getEmployeeMonthlySummary(
            @RequestParam String employeeId,
            @RequestParam int year,
            @RequestParam int month) {

        Map<String, Object> summary = monthlySummaryService.getEmployeeMonthlySummary(employeeId, year, month);
        return ResponseEntity.ok(summary);
    }

    /**
     * Search employees by attendance criteria
     */
    @GetMapping("/search-by-attendance")
    public ResponseEntity<Map<String, Object>> searchByAttendance(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam String criteria,
            @RequestParam(required = false) Double threshold) {

        Map<String, Object> result = monthlySummaryService.searchEmployeesByAttendance(year, month, criteria, threshold);
        return ResponseEntity.ok(result);
    }

    /**
     * Calculate and update monthly summaries
     */
    @PostMapping("/calculate-monthly")
    public ResponseEntity<Map<String, Object>> calculateMonthlySummary(
            @RequestParam int year,
            @RequestParam int month) {

        monthlySummaryService.calculateMonthlySummary(year, month);

        Map<String, Object> response = Map.of(
                "success", true,
                "message", "Monthly summaries calculated for " + year + "-" + month,
                "year", year,
                "month", month
        );

        return ResponseEntity.ok(response);
    }

    /**
     * HR Attendance Overview
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getHRAttendanceOverview(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        Map<String, Object> overview = attendanceService.getHRAttendanceOverview(fromDate, toDate);
        return ResponseEntity.ok(overview);
    }
}