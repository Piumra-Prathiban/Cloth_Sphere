package com.clothsphere.service.HR;

import com.clothsphere.model.HR.MonthlyAttendanceSummary;
import com.clothsphere.repository.HR.MonthlyAttendanceSummaryRepository;
import com.clothsphere.repository.HR.AttendanceRepository;
import com.clothsphere.repository.HR.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.LocalDate;
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
        Map<String, Object> result = new HashMap<>();

        // Calculate summaries first
        calculateMonthlySummary(year, month);

        // Get the calculated summaries
        String monthYear = String.format("%d-%02d", year, month);
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

        // Get all employees
        List<String> employeeIds = employeeRepository.findAllEmployeeIds();

        for (String employeeId : employeeIds) {
            calculateEmployeeMonthlySummary(employeeId, year, month, monthYear, totalDaysInMonth);
        }
    }

    /**
     * Calculate monthly summary for a specific employee
     */
    @Transactional
    public void calculateEmployeeMonthlySummary(String employeeId, int year, int month,
                                                String monthYear, int totalDaysInMonth) {
        // Get all attendance records for the month
        List<com.clothsphere.model.HR.Attendance> monthlyRecords =
                attendanceRepository.findMonthlyAttendance(employeeId, year, month);

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

        // Check if record exists
        Optional<MonthlyAttendanceSummary> existingOpt =
                monthlySummaryRepository.findByEmployeeAndMonth(employeeId, monthYear);

        if (existingOpt.isPresent()) {
            // Update existing record
            monthlySummaryRepository.updateMonthlySummary(
                    employeeId, monthYear, presentDays, absentDays,
                    totalWorkHours, attendanceRate);
        } else {
            // Insert new record
            monthlySummaryRepository.insertMonthlySummary(
                    employeeId, monthYear, totalDaysInMonth, presentDays,
                    absentDays, totalWorkHours, attendanceRate);
        }
    }

    /**
     * Get monthly attendance report with absent employee identification
     */
    public Map<String, Object> getMonthlyAttendanceReport(int year, int month) {
        String monthYear = String.format("%d-%02d", year, month);

        List<MonthlyAttendanceSummary> allSummaries =
                monthlySummaryRepository.findAllByMonth(monthYear);

        // Identify employees with low attendance
        List<MonthlyAttendanceSummary> lowAttendance =
                monthlySummaryRepository.findLowAttendanceEmployees(monthYear, 80.0);

        // Identify completely absent employees
        List<MonthlyAttendanceSummary> absentEmployees = allSummaries.stream()
                .filter(s -> s.getPresentDays() == 0)
                .collect(Collectors.toList());

        // Identify employees with partial attendance but high absence
        List<MonthlyAttendanceSummary> highAbsence = allSummaries.stream()
                .filter(s -> s.getAbsentDays() > 15 && s.getPresentDays() > 0) // More than 15 days absent
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

        return report;
    }

    /**
     * Get individual employee monthly summary
     */
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
                        .filter(s -> s.getAbsentDays() > 15) // More than 15 days absent
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