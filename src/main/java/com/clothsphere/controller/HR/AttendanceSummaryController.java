package com.clothsphere.controller.HR;

import com.clothsphere.service.HR.MonthlyAttendanceSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/attendance-summary")
public class AttendanceSummaryController {

    @Autowired
    private MonthlyAttendanceSummaryService summaryService;

    /**
     * Calculate monthly attendance summary for all employees
     */
    @PostMapping("/calculate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> calculateMonthlySummary(
            @RequestParam int year,
            @RequestParam int month) {

        Map<String, Object> result = summaryService.calculateAndGetMonthlySummary(year, month);
        return ResponseEntity.ok(result);
    }

    /**
     * Get monthly attendance report
     */
    @GetMapping("/report")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMonthlyReport(
            @RequestParam int year,
            @RequestParam int month) {

        Map<String, Object> result = summaryService.getMonthlyAttendanceReport(year, month);
        return ResponseEntity.ok(result);
    }

    /**
     * Get employee monthly summary
     */
    @GetMapping("/employee")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getEmployeeSummary(
            @RequestParam String employeeId,
            @RequestParam int year,
            @RequestParam int month) {

        Map<String, Object> result = summaryService.getEmployeeMonthlySummary(employeeId, year, month);
        return ResponseEntity.ok(result);
    }
}