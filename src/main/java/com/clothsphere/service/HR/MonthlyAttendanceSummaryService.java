package com.clothsphere.service.HR;

import com.clothsphere.model.HR.MonthlyAttendanceSummary;
import com.clothsphere.repository.HR.MonthlyAttendanceSummaryRepository;
import com.clothsphere.repository.HR.AttendanceRepository;
import com.clothsphere.repository.HR.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MonthlyAttendanceSummaryService {

    @Autowired
    private MonthlyAttendanceSummaryRepository monthlySummaryRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Calculate monthly summary for all employees and identify absent employees
     */
    @Transactional
    public Map<String, Object> calculateAndGetMonthlySummary(int year, int month) {
        System.out.println("=== CALCULATING MONTHLY ATTENDANCE SUMMARY ===");
        System.out.println("Year: " + year + ", Month: " + month);

        // Calculate summaries first
        calculateMonthlySummary(year, month);

        // Get the calculated summaries
        return getMonthlyAttendanceReport(year, month);
    }

    /**
     * Calculate and update monthly summary for all employees
     */
    @Transactional
    public void calculateMonthlySummary(int year, int month) {
        String monthYear = String.format("%d-%02d", year, month);
        YearMonth yearMonth = YearMonth.of(year, month);
        int totalDaysInMonth = yearMonth.lengthOfMonth();

        System.out.println("Total days in month: " + totalDaysInMonth);
        System.out.println("Month-Year format: " + monthYear);

        // Get all employees
        List<String> employeeIds = employeeRepository.findAllEmployeeIds();
        System.out.println("Found " + employeeIds.size() + " employees to process");

        int successCount = 0;
        int failCount = 0;

        for (String employeeId : employeeIds) {
            try {
                calculateEmployeeMonthlySummary(employeeId, year, month, monthYear, totalDaysInMonth);
                successCount++;
                System.out.println("✓ Summary calculated for: " + employeeId);
            } catch (Exception e) {
                failCount++;
                System.err.println("✗ Failed for " + employeeId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("=== SUMMARY CALCULATION COMPLETE ===");
        System.out.println("Success: " + successCount + " / Failures: " + failCount);
    }

    /**
     * Calculate monthly summary for a specific employee
     */
    @Transactional
    public void calculateEmployeeMonthlySummary(String employeeId, int year, int month,
                                                String monthYear, int totalDaysInMonth) {
        System.out.println("\n--- Processing employee: " + employeeId + " ---");

        try {
            // Get all attendance records for the month
            List<com.clothsphere.model.HR.Attendance> monthlyRecords =
                    attendanceRepository.findMonthlyAttendance(employeeId, year, month);

            System.out.println("Found " + monthlyRecords.size() + " attendance records");

            // Calculate present days (PRESENT, LATE, HALF_DAY count as present)
            int presentDays = (int) monthlyRecords.stream()
                    .filter(a -> "PRESENT".equals(a.getStatus()) ||
                            "LATE".equals(a.getStatus()) ||
                            "HALF_DAY".equals(a.getStatus()))
                    .count();

            // Calculate total work hours
            double totalWorkHours = monthlyRecords.stream()
                    .mapToDouble(a -> a.getWorkHours() != null ? a.getWorkHours() : 0.0)
                    .sum();

            // Calculate absent days
            int absentDays = totalDaysInMonth - presentDays;

            // Calculate attendance rate
            double attendanceRate = totalDaysInMonth > 0 ?
                    Math.round((presentDays * 100.0 / totalDaysInMonth) * 100.0) / 100.0 : 0.0;

            System.out.println("Calculated values:");
            System.out.println("  - Present Days: " + presentDays);
            System.out.println("  - Absent Days: " + absentDays);
            System.out.println("  - Total Work Hours: " + totalWorkHours);
            System.out.println("  - Attendance Rate: " + attendanceRate + "%");

            // Check if record exists
            Optional<MonthlyAttendanceSummary> existingOpt =
                    monthlySummaryRepository.findByEmployeeAndMonth(employeeId, monthYear);

            if (existingOpt.isPresent()) {
                System.out.println("Updating existing record...");
                // Update existing record
                monthlySummaryRepository.updateMonthlySummary(
                        employeeId, monthYear, presentDays, absentDays,
                        totalWorkHours, attendanceRate);
                System.out.println("✓ Record updated successfully");
            } else {
                System.out.println("Inserting new record...");
                // Insert new record
                monthlySummaryRepository.insertMonthlySummary(
                        employeeId, monthYear, totalDaysInMonth, presentDays,
                        absentDays, totalWorkHours, attendanceRate);
                System.out.println("✓ Record inserted successfully");
            }

        } catch (Exception e) {
            System.err.println("ERROR processing employee " + employeeId);
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to be caught by caller
        }
    }

    /**
     * Get monthly attendance report with absent employee identification
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMonthlyAttendanceReport(int year, int month) {
        String monthYear = String.format("%d-%02d", year, month);

        System.out.println("=== GENERATING ATTENDANCE REPORT ===");
        System.out.println("Month-Year: " + monthYear);

        List<MonthlyAttendanceSummary> allSummaries =
                monthlySummaryRepository.findAllByMonth(monthYear);

        System.out.println("Found " + allSummaries.size() + " summary records");

        // Identify employees with low attendance
        List<MonthlyAttendanceSummary> lowAttendance =
                monthlySummaryRepository.findLowAttendanceEmployees(monthYear, 80.0);

        // Identify completely absent employees
        List<MonthlyAttendanceSummary> absentEmployees = allSummaries.stream()
                .filter(s -> s.getPresentDays() == 0)
                .collect(Collectors.toList());

        // Identify employees with partial attendance but high absence
        List<MonthlyAttendanceSummary> highAbsence = allSummaries.stream()
                .filter(s -> s.getAbsentDays() > 15 && s.getPresentDays() > 0)
                .collect(Collectors.toList());

        Map<String, Object> report = new HashMap<>();
        report.put("monthYear", monthYear);
        report.put("year", year);
        report.put("month", month);
        report.put("allEmployees", allSummaries);
        report.put("lowAttendanceEmployees", lowAttendance);
        report.put("completelyAbsentEmployees", absentEmployees);
        report.put("highAbsenceEmployees", highAbsence);
        report.put("totalEmployees", allSummaries.size());
        report.put("averageAttendanceRate", calculateAverageAttendanceRate(allSummaries));
        report.put("totalPresentDays", calculateTotalPresentDays(allSummaries));
        report.put("totalAbsentDays", calculateTotalAbsentDays(allSummaries));

        System.out.println("✓ Report generated successfully");
        return report;
    }

    /**
     * Get individual employee monthly summary
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getEmployeeMonthlySummary(String employeeId, int year, int month) {
        String monthYear = String.format("%d-%02d", year, month);

        Optional<MonthlyAttendanceSummary> summaryOpt =
                monthlySummaryRepository.findByEmployeeAndMonth(employeeId, monthYear);

        Map<String, Object> result = new HashMap<>();

        if (summaryOpt.isPresent()) {
            MonthlyAttendanceSummary summary = summaryOpt.get();

            // Get employee details
            String employeeName = getEmployeeName(employeeId);

            result.put("employeeId", employeeId);
            result.put("employeeName", employeeName);
            result.put("monthYear", monthYear);
            result.put("totalDaysInMonth", summary.getTotalDaysInMonth());
            result.put("presentDays", summary.getPresentDays());
            result.put("absentDays", summary.getAbsentDays());
            result.put("totalWorkHours", summary.getTotalWorkHours());
            result.put("attendanceRate", summary.getAttendanceRate());
            result.put("attendanceStatus", getAttendanceStatus(summary.getAttendanceRate()));
            result.put("isAbsentEmployee", summary.getPresentDays() == 0);

        } else {
            result.put("error", "No summary found for employee " + employeeId + " in " + monthYear);
        }

        return result;
    }

    /**
     * Search employees by attendance criteria
     */
    @Transactional(readOnly = true)
    public Map<String, Object> searchEmployeesByAttendance(int year, int month,
                                                           String criteria, Double threshold) {
        String monthYear = String.format("%d-%02d", year, month);
        List<MonthlyAttendanceSummary> results = new ArrayList<>();

        switch (criteria.toUpperCase()) {
            case "ABSENT":
                results = monthlySummaryRepository.findAllByMonth(monthYear).stream()
                        .filter(s -> s.getPresentDays() == 0)
                        .collect(Collectors.toList());
                break;
            case "LOW_ATTENDANCE":
                double rateThreshold = threshold != null ? threshold : 80.0;
                results = monthlySummaryRepository.findLowAttendanceEmployees(monthYear, rateThreshold);
                break;
            case "HIGH_ABSENCE":
                results = monthlySummaryRepository.findAllByMonth(monthYear).stream()
                        .filter(s -> s.getAbsentDays() > 15)
                        .collect(Collectors.toList());
                break;
            case "ALL":
            default:
                results = monthlySummaryRepository.findAllByMonth(monthYear);
                break;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("monthYear", monthYear);
        response.put("searchCriteria", criteria);
        response.put("threshold", threshold);
        response.put("employees", results);
        response.put("count", results.size());

        return response;
    }

    private double calculateAverageAttendanceRate(List<MonthlyAttendanceSummary> summaries) {
        if (summaries.isEmpty()) return 0.0;
        return Math.round(summaries.stream()
                .mapToDouble(MonthlyAttendanceSummary::getAttendanceRate)
                .average()
                .orElse(0.0) * 100.0) / 100.0;
    }

    private long calculateTotalPresentDays(List<MonthlyAttendanceSummary> summaries) {
        return summaries.stream()
                .mapToLong(MonthlyAttendanceSummary::getPresentDays)
                .sum();
    }

    private long calculateTotalAbsentDays(List<MonthlyAttendanceSummary> summaries) {
        return summaries.stream()
                .mapToLong(MonthlyAttendanceSummary::getAbsentDays)
                .sum();
    }

    private String getAttendanceStatus(double attendanceRate) {
        if (attendanceRate >= 90) return "EXCELLENT";
        else if (attendanceRate >= 80) return "GOOD";
        else if (attendanceRate >= 70) return "AVERAGE";
        else if (attendanceRate >= 60) return "POOR";
        else return "CRITICAL";
    }

    private String getEmployeeName(String employeeId) {
        try {
            return employeeRepository.findEmployeeById(employeeId)
                    .map(com.clothsphere.model.HR.Employee::getFullName)
                    .orElse("Unknown Employee");
        } catch (Exception e) {
            return "Unknown Employee";
        }
    }
}