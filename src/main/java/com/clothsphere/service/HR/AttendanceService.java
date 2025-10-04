package com.clothsphere.service.HR;

import com.clothsphere.model.HR.Attendance;
import com.clothsphere.repository.HR.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

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
}