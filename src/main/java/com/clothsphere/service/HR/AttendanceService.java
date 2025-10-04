package com.clothsphere.service.HR;

import com.clothsphere.model.HR.Attendance;
import com.clothsphere.repository.HR.AttendanceRepository;
import com.clothsphere.repository.HR.MonthlyAttendanceSummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.clothsphere.repository.HR.EmployeeRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private MonthlyAttendanceSummaryRepository monthlySummaryRepository;

    @Transactional
    public Map<String, Object> checkIn(String employeeId, String notes) {
        Map<String, Object> response = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalDateTime currentDateTime = LocalDateTime.now();

        Optional<Attendance> existingAttendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, today);

        if (existingAttendance.isPresent()) {
            Attendance attendance = existingAttendance.get();

            if (attendance.getCheckInTime() != null) {
                response.put("success", false);
                response.put("message", "You have already checked in today at " + attendance.getCheckInTime());
                return response;
            }

            attendanceRepository.updateCheckIn(employeeId, today, now, "PRESENT",
                    notes != null && !notes.trim().isEmpty() ? notes : null, currentDateTime);
        } else {
            attendanceRepository.insertAttendance(employeeId, today, now, null, "PRESENT",
                    null, notes != null && !notes.trim().isEmpty() ? notes : null, currentDateTime, currentDateTime);
        }

        response.put("success", true);
        response.put("message", "Check-in successful!");
        response.put("checkInTime", now.toString());
        return response;
    }

    @Transactional
    public Map<String, Object> checkOut(String employeeId, String notes) {
        Map<String, Object> response = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalDateTime currentDateTime = LocalDateTime.now();

        Optional<Attendance> existingAttendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, today);

        if (existingAttendance.isEmpty()) {
            response.put("success", false);
            response.put("message", "No check-in found for today. Please check in first.");
            return response;
        }

        Attendance attendance = existingAttendance.get();

        if (attendance.getCheckInTime() == null) {
            response.put("success", false);
            response.put("message", "You must check in before checking out.");
            return response;
        }

        if (attendance.getCheckOutTime() != null) {
            response.put("success", false);
            response.put("message", "You have already checked out today at " + attendance.getCheckOutTime());
            return response;
        }

        // Calculate work hours accurately
        Duration duration = Duration.between(attendance.getCheckInTime(), now);
        long totalMinutes = duration.toMinutes();
        Double workHours = Math.round(totalMinutes * 100.0 / 60.0) / 100.0; // Round to 2 decimal places

        attendanceRepository.updateCheckOut(employeeId, today, now, workHours,
                notes != null && !notes.trim().isEmpty() ? notes : null, currentDateTime);

        response.put("success", true);
        response.put("message", "Check-out successful!");
        response.put("checkOutTime", now.toString());
        response.put("workHours", workHours);
        return response;
    }

    @Transactional
    public Map<String, Object> markManualAttendance(String employeeId, LocalDate date,
                                                    LocalTime checkIn, LocalTime checkOut,
                                                    String status, String notes) {
        Map<String, Object> response = new HashMap<>();
        LocalDateTime currentDateTime = LocalDateTime.now();

        Optional<Attendance> existingAttendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, date);

        Double workHours = null;
        if (checkIn != null && checkOut != null) {
            Duration duration = Duration.between(checkIn, checkOut);
            long totalMinutes = duration.toMinutes();
            workHours = Math.round(totalMinutes * 100.0 / 60.0) / 100.0; // Round to 2 decimal places
        }

        if (existingAttendance.isPresent()) {
            // For composite key, we need to delete and re-insert since we can't update the key
            attendanceRepository.deleteByEmployeeIdAndAttendanceDate(employeeId, date);
        }

        attendanceRepository.insertAttendance(employeeId, date, checkIn, checkOut,
                status, workHours, notes, currentDateTime, currentDateTime);

        response.put("success", true);
        response.put("message", "Attendance marked successfully!");
        return response;
    }

    @Transactional
    public Map<String, Object> deleteAttendanceByEmployeeAndDate(String employeeId, LocalDate date) {
        Map<String, Object> response = new HashMap<>();

        Optional<Attendance> attendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, date);
        if (attendance.isEmpty()) {
            response.put("success", false);
            response.put("message", "Attendance record not found!");
            return response;
        }

        attendanceRepository.deleteByEmployeeIdAndAttendanceDate(employeeId, date);

        response.put("success", true);
        response.put("message", "Attendance record deleted successfully!");
        return response;
    }

    public Map<String, Object> getTodayAttendance(String employeeId) {
        Map<String, Object> response = new HashMap<>();
        LocalDate today = LocalDate.now();

        Optional<Attendance> todayAttendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, today);

        if (todayAttendance.isPresent()) {
            Attendance attendance = todayAttendance.get();
            response.put("hasCheckedIn", attendance.getCheckInTime() != null);
            response.put("hasCheckedOut", attendance.getCheckOutTime() != null);
            response.put("checkInTime", attendance.getCheckInTime());
            response.put("checkOutTime", attendance.getCheckOutTime());
            response.put("status", attendance.getStatus());
            response.put("workHours", attendance.getWorkHours());
            response.put("notes", attendance.getNotes());
        } else {
            response.put("hasCheckedIn", false);
            response.put("hasCheckedOut", false);
            response.put("status", "NOT_MARKED");
        }

        return response;
    }

    public List<Attendance> getAttendanceHistory(String employeeId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
        }
        return attendanceRepository.findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                employeeId, startDate, endDate);
    }

    public Map<String, Object> getAttendanceStatistics(String employeeId) {
        Map<String, Object> stats = new HashMap<>();

        Long presentDays = attendanceRepository.countPresentDaysThisMonth(employeeId);
        Long totalDays = attendanceRepository.countTotalAttendanceDaysThisMonth(employeeId);

        // Use actual total days if available, otherwise default to 22
        Long totalWorkingDays = (totalDays != null && totalDays > 0) ? totalDays : 22L;

        stats.put("presentDays", presentDays != null ? presentDays : 0L);
        stats.put("totalWorkingDays", totalWorkingDays);
        stats.put("attendancePercentage", calculateAttendancePercentage(presentDays, totalWorkingDays));

        return stats;
    }

    private double calculateAttendancePercentage(Long presentDays, Long totalWorkingDays) {
        if (totalWorkingDays == null || totalWorkingDays == 0) {
            return 0.0;
        }
        double present = (presentDays != null) ? presentDays.doubleValue() : 0.0;
        double percentage = (present / totalWorkingDays.doubleValue()) * 100.0;
        return Math.round(percentage * 100.0) / 100.0; // Round to 2 decimal places
    }

    public List<Attendance> getMonthlyAttendance(String employeeId, int year, int month) {
        return attendanceRepository.findMonthlyAttendance(employeeId, year, month);
    }
//=================================================
public Map<String, Object> searchAttendanceForHREnhanced(String employeeId, String employeeName,
                                                         LocalDate fromDate, LocalDate toDate) {
    Map<String, Object> response = new HashMap<>();

    try {
        System.out.println("=== SERVICE LAYER: Processing HR Attendance Search ===");
        System.out.println("Employee ID: " + employeeId);
        System.out.println("Employee Name: " + employeeName);
        System.out.println("Date Range: " + fromDate + " to " + toDate);

        // STEP 1: Input Validation (Business Logic)
        if ((employeeId == null || employeeId.trim().isEmpty()) &&
                (employeeName == null || employeeName.trim().isEmpty())) {
            response.put("success", false);
            response.put("message", "Please provide either Employee ID or Employee Name");
            return response;
        }

        if (fromDate.isAfter(toDate)) {
            response.put("success", false);
            response.put("message", "From date cannot be after to date");
            return response;
        }

        // STEP 2: Determine Search Strategy
        List<Attendance> attendanceRecords;
        boolean isIndividualSearch = false;

        if (employeeId != null && !employeeId.trim().isEmpty()) {
            // Search by Employee ID - INDIVIDUAL EMPLOYEE
            System.out.println("Searching by Employee ID: " + employeeId);
            isIndividualSearch = true;

            attendanceRecords = attendanceRepository
                    .findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                            employeeId.trim(), fromDate, toDate);

        } else {
            // Search by Employee Name
            String searchName = (employeeName != null) ? employeeName.trim() : "";
            System.out.println("Searching by Employee Name: " + searchName);

            attendanceRecords = attendanceRepository
                    .findByEmployeeNameAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                            searchName, fromDate, toDate);
        }

        System.out.println("Found " + attendanceRecords.size() + " attendance records");
        System.out.println("Is Individual Search: " + isIndividualSearch);

        // STEP 3: Process Results and Calculate Summary
        Map<String, Object> summary;
        if (isIndividualSearch) {
            // INDIVIDUAL EMPLOYEE SEARCH - calculate only for this employee
            summary = calculateIndividualEmployeeSummary(employeeId, attendanceRecords, fromDate, toDate);
        } else {
            // MULTIPLE EMPLOYEES SEARCH - calculate for all employees
            summary = calculateAllEmployeesSummary(attendanceRecords, fromDate, toDate);
        }

        // STEP 4: Prepare Response
        response.put("success", true);
        response.put("attendanceRecords", attendanceRecords);
        response.put("summary", summary);
        response.put("searchCriteria", Map.of(
                "employeeId", employeeId != null ? employeeId : "",
                "employeeName", employeeName != null ? employeeName : "",
                "fromDate", fromDate.toString(),
                "toDate", toDate.toString(),
                "searchType", isIndividualSearch ? "INDIVIDUAL" : "MULTIPLE"
        ));

        System.out.println("=== SERVICE LAYER: Search Completed Successfully ===");

    } catch (Exception e) {
        System.err.println("=== SERVICE LAYER: Error occurred ===");
        System.err.println("Error: " + e.getMessage());
        e.printStackTrace();
        response.put("success", false);
        response.put("message", "Error searching attendance: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
    }

    return response;
}

    /**
     * Calculate summary for INDIVIDUAL employee search
     */
    private Map<String, Object> calculateIndividualEmployeeSummary(String employeeId,
                                                                   List<Attendance> records,
                                                                   LocalDate fromDate, LocalDate toDate) {
        Map<String, Object> summary = new HashMap<>();

        System.out.println("=== INDIVIDUAL EMPLOYEE CALCULATION ===");
        System.out.println("Employee ID: " + employeeId);
        System.out.println("Records found: " + records.size());

        // Get employee details
        String employeeName = getEmployeeDetails(employeeId);

        // Calculate total working days in the period (excluding weekends)
        long totalWorkingDaysInPeriod = calculateWorkingDaysInPeriod(fromDate, toDate);

        System.out.println("From Date: " + fromDate + " To Date: " + toDate);
        System.out.println("Total Working Days in Period: " + totalWorkingDaysInPeriod);

        // Calculate actual attended days from records - ONLY FOR THIS EMPLOYEE
        long totalDaysWithRecords = records.size();
        long presentDays = records.stream()
                .filter(att -> "PRESENT".equals(att.getStatus()) || "LATE".equals(att.getStatus()))
                .count();

        System.out.println("Present Days (from records): " + presentDays);

        // Calculate absent days for THIS EMPLOYEE ONLY
        long absentDays = totalWorkingDaysInPeriod - presentDays;

        // Ensure absent days is not negative
        if (absentDays < 0) {
            absentDays = 0;
        }

        System.out.println("Calculated Absent Days for " + employeeId + ": " + absentDays);

        long halfDays = records.stream()
                .filter(att -> "HALF_DAY".equals(att.getStatus()))
                .count();

        double totalWorkHours = records.stream()
                .mapToDouble(att -> att.getWorkHours() != null ? att.getWorkHours() : 0.0)
                .sum();

        double averageDailyHours = totalDaysWithRecords > 0 ? totalWorkHours / totalDaysWithRecords : 0.0;

        // Calculate attendance rate for THIS EMPLOYEE
        double attendanceRate = totalWorkingDaysInPeriod > 0 ?
                (presentDays * 100.0 / totalWorkingDaysInPeriod) : 0.0;

        System.out.println("Attendance Rate for " + employeeId + ": " + attendanceRate + "%");
        System.out.println("=== END INDIVIDUAL CALCULATION ===");

        // Performance indicators
        String performanceIndicator;
        if (attendanceRate >= 90) performanceIndicator = "Excellent";
        else if (attendanceRate >= 80) performanceIndicator = "Good";
        else if (attendanceRate >= 70) performanceIndicator = "Average";
        else performanceIndicator = "Needs Improvement";

        // For individual search, show stats only for this employee
        summary.put("employeeId", employeeId);
        summary.put("employeeName", employeeName);
        summary.put("searchPeriod", fromDate + " to " + toDate);
        summary.put("searchType", "Individual Employee Search");
        summary.put("totalWorkingDaysInPeriod", totalWorkingDaysInPeriod);

        // INDIVIDUAL EMPLOYEE STATS ONLY - CRITICAL FIX
        summary.put("totalEmployees", 1); // Only this employee
        summary.put("totalRecords", totalDaysWithRecords);
        summary.put("presentDays", presentDays);
        summary.put("absentDays", absentDays);
        summary.put("halfDays", halfDays);
        summary.put("totalWorkHours", Math.round(totalWorkHours * 100.0) / 100.0);
        summary.put("averageDailyHours", Math.round(averageDailyHours * 100.0) / 100.0);
        summary.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0);
        summary.put("performanceIndicator", performanceIndicator);

        // Add explicit individual employee info
        summary.put("searchedEmployeeId", employeeId);
        summary.put("searchedEmployeeName", employeeName);
        summary.put("isIndividualSearch", true);

        return summary;
    }

    /**
     * Debug method to check the summary content
     */
    private void debugSummary(String methodName, Map<String, Object> summary) {
        System.out.println("=== DEBUG SUMMARY: " + methodName + " ===");
        System.out.println("Total Employees: " + summary.get("totalEmployees"));
        System.out.println("Absent Days: " + summary.get("absentDays"));
        System.out.println("Is Individual Search: " + summary.get("isIndividualSearch"));
        System.out.println("All Summary Keys: " + summary.keySet());
        System.out.println("=== END DEBUG SUMMARY ===");
    }



    // Enhanced employee details lookup
    private String getEmployeeDetails(String employeeId) {
        try {
            // FLOW: Service calls EmployeeRepository
            Optional<com.clothsphere.model.HR.Employee> employee = employeeRepository.findEmployeeById(employeeId);
            if (employee.isPresent()) {
                return employee.get().getFullName();
            } else {
                return "Employee Not Found";
            }
        } catch (Exception e) {
            System.err.println("Error fetching employee details: " + e.getMessage());
            return "Unknown Employee";
        }
    }

    /**
     * Search all employees attendance with date range and optional employee ID filter
     */
    public Map<String, Object> searchAllEmployeesAttendance(LocalDate fromDate, LocalDate toDate, String employeeId) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("=== SERVICE: Searching All Employees Attendance ===");
            System.out.println("Date Range: " + fromDate + " to " + toDate);
            System.out.println("Employee ID Filter: " + employeeId);

            // Validate date range
            if (fromDate.isAfter(toDate)) {
                response.put("success", false);
                response.put("message", "From date cannot be after to date");
                return response;
            }

            List<Attendance> attendanceRecords;

            if (employeeId != null && !employeeId.trim().isEmpty()) {
                // Filter by specific employee ID
                attendanceRecords = attendanceRepository
                        .findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                                employeeId.trim(), fromDate, toDate);
            } else {
                // Get all employees attendance
                attendanceRecords = attendanceRepository
                        .findAllEmployeesAttendanceBetweenDates(fromDate, toDate);
            }

            System.out.println("Found " + attendanceRecords.size() + " attendance records");

            // Calculate summary for all employees - UPDATED WITH CORRECT ABSENT DAYS
            Map<String, Object> summary = calculateAllEmployeesSummary(attendanceRecords, fromDate, toDate);

            response.put("success", true);
            response.put("attendanceRecords", attendanceRecords);
            response.put("summary", summary);
            response.put("searchCriteria", Map.of(
                    "fromDate", fromDate.toString(),
                    "toDate", toDate.toString(),
                    "employeeId", employeeId != null ? employeeId : "ALL"
            ));

            System.out.println("=== SERVICE: Search Completed Successfully ===");

        } catch (Exception e) {
            System.err.println("=== SERVICE: Error occurred ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error searching attendance: " + e.getMessage());
        }

        return response;
    }


    /**
     * Calculate summary for all employees - FIXED ABSENT DAYS CALCULATION
     */
    private Map<String, Object> calculateAllEmployeesSummary(List<Attendance> records,
                                                             LocalDate fromDate, LocalDate toDate) {
        Map<String, Object> summary = new HashMap<>();

        // Calculate total working days in period
        long totalWorkingDaysInPeriod = calculateWorkingDaysInPeriod(fromDate, toDate);

        // Get all employees to identify absent ones
        List<String> allEmployeeIds = employeeRepository.findAllEmployeeIds();

        // Group records by employee
        Map<String, List<Attendance>> employeeAttendanceMap = records.stream()
                .collect(Collectors.groupingBy(Attendance::getEmployeeId));

        // Calculate overall statistics - FIXED ABSENT DAYS
        long totalRecords = records.size();
        long totalPresentDays = records.stream()
                .filter(att -> "PRESENT".equals(att.getStatus()) || "LATE".equals(att.getStatus()))
                .count();

        // Calculate total absent days across all employees
        long totalAbsentDays = 0;
        for (String employeeId : allEmployeeIds) {
            List<Attendance> empRecords = employeeAttendanceMap.getOrDefault(employeeId, new ArrayList<>());
            long empPresentDays = empRecords.stream()
                    .filter(att -> "PRESENT".equals(att.getStatus()) || "LATE".equals(att.getStatus()))
                    .count();
            long empAbsentDays = totalWorkingDaysInPeriod - empPresentDays;
            if (empAbsentDays > 0) {
                totalAbsentDays += empAbsentDays;
            }
        }

        long halfDays = records.stream()
                .filter(att -> "HALF_DAY".equals(att.getStatus()))
                .count();

        double totalWorkHours = records.stream()
                .mapToDouble(att -> att.getWorkHours() != null ? att.getWorkHours() : 0.0)
                .sum();

        // Calculate overall attendance rate
        long totalPotentialDays = allEmployeeIds.size() * totalWorkingDaysInPeriod;
        double overallAttendanceRate = totalPotentialDays > 0 ?
                (totalPresentDays * 100.0 / totalPotentialDays) : 0.0;

        // Calculate employee-wise summary
        List<Map<String, Object>> employeeSummaries = new ArrayList<>();

        for (String employeeId : allEmployeeIds) {
            List<Attendance> empRecords = employeeAttendanceMap.getOrDefault(employeeId, new ArrayList<>());

            Map<String, Object> empSummary = new HashMap<>();
            empSummary.put("employeeId", employeeId);
            empSummary.put("employeeName", getEmployeeDetails(employeeId));
            empSummary.put("totalDays", empRecords.size());

            long empPresentDays = empRecords.stream()
                    .filter(att -> "PRESENT".equals(att.getStatus()) || "LATE".equals(att.getStatus()))
                    .count();

            long empAbsentDays = totalWorkingDaysInPeriod - empPresentDays;
            if (empAbsentDays < 0) empAbsentDays = 0;

            double empAttendanceRate = totalWorkingDaysInPeriod > 0 ?
                    (empPresentDays * 100.0 / totalWorkingDaysInPeriod) : 0.0;

            empSummary.put("presentDays", empPresentDays);
            empSummary.put("absentDays", empAbsentDays);
            empSummary.put("totalWorkHours", empRecords.stream()
                    .mapToDouble(att -> att.getWorkHours() != null ? att.getWorkHours() : 0.0)
                    .sum());
            empSummary.put("attendanceRate", Math.round(empAttendanceRate * 100.0) / 100.0);
            empSummary.put("isFullyAbsent", empPresentDays == 0);

            employeeSummaries.add(empSummary);
        }

        summary.put("totalEmployees", allEmployeeIds.size());
        summary.put("totalRecords", totalRecords);
        summary.put("totalPresentDays", totalPresentDays);
        summary.put("totalAbsentDays", totalAbsentDays);
        summary.put("totalHalfDays", halfDays);
        summary.put("totalWorkHours", Math.round(totalWorkHours * 100.0) / 100.0);
        summary.put("attendanceRate", Math.round(overallAttendanceRate * 100.0) / 100.0);
        summary.put("employeeSummaries", employeeSummaries);
        summary.put("searchPeriod", fromDate + " to " + toDate);
        summary.put("totalWorkingDaysInPeriod", totalWorkingDaysInPeriod);

        return summary;
    }
    //===========================================================================================================================

    // Add these methods to your existing AttendanceService class

    /**
     * Get attendance rate for HR dashboard
     */
    public Map<String, Object> getHRAttendanceOverview(LocalDate fromDate, LocalDate toDate) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get all attendance records in date range
            List<Attendance> allRecords = attendanceRepository
                    .findAllEmployeesAttendanceBetweenDates(fromDate, toDate);

            // Get all employees
            List<String> allEmployeeIds = employeeRepository.findAllEmployeeIds();

            // Calculate overall statistics
            Map<String, Object> overallStats = calculateOverallAttendanceStats(allRecords, allEmployeeIds, fromDate, toDate);

            // Calculate employee-wise statistics
            List<Map<String, Object>> employeeStats = calculateEmployeeWiseStats(allRecords, allEmployeeIds, fromDate, toDate);

            // Identify absent employees (no records in date range)
            List<Map<String, Object>> absentEmployees = identifyAbsentEmployees(allRecords, allEmployeeIds, fromDate, toDate);

            response.put("success", true);
            response.put("overallStats", overallStats);
            response.put("employeeStats", employeeStats);
            response.put("absentEmployees", absentEmployees);
            response.put("searchPeriod", fromDate + " to " + toDate);
            response.put("totalEmployees", allEmployeeIds.size());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error generating attendance overview: " + e.getMessage());
        }

        return response;
    }

    private Map<String, Object> calculateOverallAttendanceStats(List<Attendance> records,
                                                                List<String> allEmployeeIds,
                                                                LocalDate fromDate, LocalDate toDate) {
        Map<String, Object> stats = new HashMap<>();

        long totalDays = records.size();
        long presentDays = records.stream()
                .filter(att -> "PRESENT".equals(att.getStatus()) || "LATE".equals(att.getStatus()))
                .count();
        long absentDays = records.stream()
                .filter(att -> "ABSENT".equals(att.getStatus()))
                .count();

        double totalWorkHours = records.stream()
                .mapToDouble(att -> att.getWorkHours() != null ? att.getWorkHours() : 0.0)
                .sum();

        // Calculate potential working days
        long workingDaysInPeriod = calculateWorkingDaysInPeriod(fromDate, toDate);
        long totalPotentialDays = allEmployeeIds.size() * workingDaysInPeriod;

        double overallAttendanceRate = totalPotentialDays > 0 ?
                (presentDays * 100.0 / totalPotentialDays) : 0.0;

        stats.put("totalEmployees", allEmployeeIds.size());
        stats.put("totalRecords", totalDays);
        stats.put("presentDays", presentDays);
        stats.put("absentDays", absentDays);
        stats.put("totalWorkHours", Math.round(totalWorkHours * 100.0) / 100.0);
        stats.put("overallAttendanceRate", Math.round(overallAttendanceRate * 100.0) / 100.0);
        stats.put("workingDaysInPeriod", workingDaysInPeriod);
        stats.put("totalPotentialDays", totalPotentialDays);

        return stats;
    }

    private List<Map<String, Object>> calculateEmployeeWiseStats(List<Attendance> records,
                                                                 List<String> allEmployeeIds,
                                                                 LocalDate fromDate, LocalDate toDate) {
        List<Map<String, Object>> employeeStats = new ArrayList<>();
        long workingDaysInPeriod = calculateWorkingDaysInPeriod(fromDate, toDate);

        for (String employeeId : allEmployeeIds) {
            Map<String, Object> empStats = new HashMap<>();

            List<Attendance> empRecords = records.stream()
                    .filter(r -> r.getEmployeeId().equals(employeeId))
                    .collect(Collectors.toList());

            long empPresentDays = empRecords.stream()
                    .filter(att -> "PRESENT".equals(att.getStatus()) || "LATE".equals(att.getStatus()))
                    .count();

            double empWorkHours = empRecords.stream()
                    .mapToDouble(att -> att.getWorkHours() != null ? att.getWorkHours() : 0.0)
                    .sum();

            double empAttendanceRate = workingDaysInPeriod > 0 ?
                    (empPresentDays * 100.0 / workingDaysInPeriod) : 0.0;

            empStats.put("employeeId", employeeId);
            empStats.put("employeeName", getEmployeeDetails(employeeId));
            empStats.put("presentDays", empPresentDays);
            empStats.put("absentDays", workingDaysInPeriod - empPresentDays);
            empStats.put("totalWorkHours", Math.round(empWorkHours * 100.0) / 100.0);
            empStats.put("attendanceRate", Math.round(empAttendanceRate * 100.0) / 100.0);
            empStats.put("isAbsent", empPresentDays == 0);

            employeeStats.add(empStats);
        }

        // Sort by attendance rate (ascending)
        employeeStats.sort((a, b) -> Double.compare(
                (Double) a.get("attendanceRate"),
                (Double) b.get("attendanceRate")
        ));

        return employeeStats;
    }

    private List<Map<String, Object>> identifyAbsentEmployees(List<Attendance> records,
                                                              List<String> allEmployeeIds,
                                                              LocalDate fromDate, LocalDate toDate) {
        List<Map<String, Object>> absentEmployees = new ArrayList<>();

        for (String employeeId : allEmployeeIds) {
            boolean hasRecords = records.stream()
                    .anyMatch(r -> r.getEmployeeId().equals(employeeId));

            if (!hasRecords) {
                Map<String, Object> absentEmp = new HashMap<>();
                absentEmp.put("employeeId", employeeId);
                absentEmp.put("employeeName", getEmployeeDetails(employeeId));
                absentEmp.put("absencePeriod", fromDate + " to " + toDate);
                absentEmp.put("totalDays", calculateWorkingDaysInPeriod(fromDate, toDate));
                absentEmployees.add(absentEmp);
            }
        }

        return absentEmployees;
    }

    /**
     * Calculate working days in a period (excluding weekends)
     */
    private long calculateWorkingDaysInPeriod(LocalDate fromDate, LocalDate toDate) {
        long workingDays = 0;
        LocalDate date = fromDate;

        System.out.println("=== WORKING DAYS CALCULATION ===");
        System.out.println("Period: " + fromDate + " to " + toDate);

        while (!date.isAfter(toDate)) {
            // Exclude weekends (Saturday = 6, Sunday = 7)
            if (date.getDayOfWeek().getValue() < 6) {
                workingDays++;
                System.out.println(date + " - " + date.getDayOfWeek() + " - WORKING DAY");
            } else {
                System.out.println(date + " - " + date.getDayOfWeek() + " - WEEKEND");
            }
            date = date.plusDays(1);
        }

        System.out.println("Total Working Days: " + workingDays);
        System.out.println("=== END WORKING DAYS CALCULATION ===");

        return workingDays;
    }

}