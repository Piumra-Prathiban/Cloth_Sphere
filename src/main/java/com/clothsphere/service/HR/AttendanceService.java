package com.clothsphere.service.HR;

import com.clothsphere.model.HR.Attendance;
import com.clothsphere.repository.HR.AttendanceRepository;
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
        String searchIdentifier;

        if (employeeId != null && !employeeId.trim().isEmpty()) {
            // Search by Employee ID
            System.out.println("Searching by Employee ID: " + employeeId);

            attendanceRecords = attendanceRepository
                    .findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                            employeeId.trim(), fromDate, toDate);
            searchIdentifier = employeeId;

        } else {
            // Search by Employee Name - FIX: Handle null employeeName
            String searchName = (employeeName != null) ? employeeName.trim() : "";
            System.out.println("Searching by Employee Name: " + searchName);

            attendanceRecords = attendanceRepository
                    .findByEmployeeNameAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                            searchName, fromDate, toDate);
            searchIdentifier = searchName;
        }

        System.out.println("Found " + attendanceRecords.size() + " attendance records");

        // STEP 3: Process Results and Calculate Summary
        Map<String, Object> summary = calculateEnhancedAttendanceSummary(
                searchIdentifier, attendanceRecords, fromDate, toDate);

        // STEP 4: Prepare Response
        response.put("success", true);
        response.put("attendanceRecords", attendanceRecords);
        response.put("summary", summary);
        response.put("searchCriteria", Map.of(
                "employeeId", employeeId != null ? employeeId : "",
                "employeeName", employeeName != null ? employeeName : "",
                "fromDate", fromDate.toString(),
                "toDate", toDate.toString()
        ));

        System.out.println("=== SERVICE LAYER: Search Completed Successfully ===");

    } catch (Exception e) {
        System.err.println("=== SERVICE LAYER: Error occurred ===");
        System.err.println("Error: " + e.getMessage());
        e.printStackTrace(); // Add this for better debugging
        response.put("success", false);
        response.put("message", "Error searching attendance: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
    }

    return response;
}

    // Enhanced Summary Calculation with Employee Details
    private Map<String, Object> calculateEnhancedAttendanceSummary(String searchIdentifier,
                                                                   List<Attendance> records,
                                                                   LocalDate fromDate, LocalDate toDate) {
        Map<String, Object> summary = new HashMap<>();

        // Get first record to extract employee info
        String actualEmployeeId = records.isEmpty() ? searchIdentifier : records.get(0).getEmployeeId();

        // FLOW: Service calls EmployeeRepository for employee details
        String employeeName = getEmployeeDetails(actualEmployeeId);

        // Basic employee info
        summary.put("employeeId", actualEmployeeId);
        summary.put("employeeName", employeeName);
        summary.put("searchPeriod", fromDate + " to " + toDate);
        summary.put("searchType", records.isEmpty() ? "No records found" :
                searchIdentifier.equals(actualEmployeeId) ? "ID Search" : "Name Search");

        // Calculate statistics
        long totalDays = records.size();
        long presentDays = records.stream()
                .filter(att -> "PRESENT".equals(att.getStatus()) || "LATE".equals(att.getStatus()))
                .count();
        long absentDays = records.stream()
                .filter(att -> "ABSENT".equals(att.getStatus()))
                .count();
        long halfDays = records.stream()
                .filter(att -> "HALF_DAY".equals(att.getStatus()))
                .count();

        double totalWorkHours = records.stream()
                .mapToDouble(att -> att.getWorkHours() != null ? att.getWorkHours() : 0.0)
                .sum();

        double averageDailyHours = totalDays > 0 ? totalWorkHours / totalDays : 0.0;
        double attendanceRate = totalDays > 0 ? (presentDays * 100.0 / totalDays) : 0.0;

        // Performance indicators
        String performanceIndicator;
        if (attendanceRate >= 90) performanceIndicator = "Excellent";
        else if (attendanceRate >= 80) performanceIndicator = "Good";
        else if (attendanceRate >= 70) performanceIndicator = "Average";
        else performanceIndicator = "Needs Improvement";

        summary.put("totalDays", totalDays);
        summary.put("presentDays", presentDays);
        summary.put("absentDays", absentDays);
        summary.put("halfDays", halfDays);
        summary.put("totalWorkHours", Math.round(totalWorkHours * 100.0) / 100.0);
        summary.put("averageDailyHours", Math.round(averageDailyHours * 100.0) / 100.0);
        summary.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0);
        summary.put("performanceIndicator", performanceIndicator);

        return summary;
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

            // Calculate summary for all employees
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
     * Calculate summary for all employees
     */
    private Map<String, Object> calculateAllEmployeesSummary(List<Attendance> records,
                                                             LocalDate fromDate, LocalDate toDate) {
        Map<String, Object> summary = new HashMap<>();

        // Group records by employee
        Map<String, List<Attendance>> employeeAttendanceMap = records.stream()
                .collect(Collectors.groupingBy(Attendance::getEmployeeId));

        // Calculate overall statistics
        long totalRecords = records.size();
        long presentDays = records.stream()
                .filter(att -> "PRESENT".equals(att.getStatus()) || "LATE".equals(att.getStatus()))
                .count();
        long absentDays = records.stream()
                .filter(att -> "ABSENT".equals(att.getStatus()))
                .count();
        long halfDays = records.stream()
                .filter(att -> "HALF_DAY".equals(att.getStatus()))
                .count();

        double totalWorkHours = records.stream()
                .mapToDouble(att -> att.getWorkHours() != null ? att.getWorkHours() : 0.0)
                .sum();

        // Calculate employee-wise summary
        List<Map<String, Object>> employeeSummaries = new ArrayList<>();

        for (Map.Entry<String, List<Attendance>> entry : employeeAttendanceMap.entrySet()) {
            String empId = entry.getKey();
            List<Attendance> empRecords = entry.getValue();

            Map<String, Object> empSummary = new HashMap<>();
            empSummary.put("employeeId", empId);
            empSummary.put("employeeName", getEmployeeDetails(empId));
            empSummary.put("totalDays", empRecords.size());
            empSummary.put("presentDays", empRecords.stream()
                    .filter(att -> "PRESENT".equals(att.getStatus()) || "LATE".equals(att.getStatus()))
                    .count());
            empSummary.put("absentDays", empRecords.stream()
                    .filter(att -> "ABSENT".equals(att.getStatus()))
                    .count());
            empSummary.put("totalWorkHours", empRecords.stream()
                    .mapToDouble(att -> att.getWorkHours() != null ? att.getWorkHours() : 0.0)
                    .sum());

            employeeSummaries.add(empSummary);
        }

        summary.put("totalEmployees", employeeAttendanceMap.size());
        summary.put("totalRecords", totalRecords);
        summary.put("totalPresentDays", presentDays);
        summary.put("totalAbsentDays", absentDays);
        summary.put("totalHalfDays", halfDays);
        summary.put("totalWorkHours", Math.round(totalWorkHours * 100.0) / 100.0);
        summary.put("employeeSummaries", employeeSummaries);
        summary.put("searchPeriod", fromDate + " to " + toDate);

        return summary;
    }

}