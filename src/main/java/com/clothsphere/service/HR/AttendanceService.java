package com.clothsphere.service.HR;

import com.clothsphere.model.HR.Attendance;
import com.clothsphere.repository.HR.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    // Check in for today - FIXED VERSION
    public Map<String, Object> checkIn(String employeeId, String notes) {
        Map<String, Object> response = new HashMap<>();
        LocalDate today = LocalDate.now();

        // Check if ANY attendance record exists for today
        Optional<Attendance> existingAttendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, today);

        if (existingAttendance.isPresent()) {
            Attendance attendance = existingAttendance.get();

            // CRITICAL FIX: Check if already checked in
            if (attendance.getCheckInTime() != null) {
                response.put("success", false);
                response.put("message", "You have already checked in today at " + attendance.getCheckInTime());
                return response;
            }

            // Update existing record
            attendance.setCheckInTime(LocalTime.now());
            attendance.setStatus("PRESENT");
            if (notes != null && !notes.trim().isEmpty()) {
                attendance.setNotes(notes);
            }
            attendance.setUpdatedAt(LocalDateTime.now());
            attendanceRepository.save(attendance);
        } else {
            // Create new record
            Attendance newAttendance = new Attendance();
            newAttendance.setEmployeeId(employeeId);
            newAttendance.setAttendanceDate(today);
            newAttendance.setCheckInTime(LocalTime.now());
            newAttendance.setStatus("PRESENT");
            if (notes != null && !notes.trim().isEmpty()) {
                newAttendance.setNotes(notes);
            }
            attendanceRepository.save(newAttendance);
        }

        response.put("success", true);
        response.put("message", "Check-in successful!");
        response.put("checkInTime", LocalTime.now().toString());
        return response;
    }

    // Check out for today - FIXED VERSION
    public Map<String, Object> checkOut(String employeeId, String notes) {
        Map<String, Object> response = new HashMap<>();
        LocalDate today = LocalDate.now();

        Optional<Attendance> existingAttendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, today);

        if (existingAttendance.isEmpty()) {
            response.put("success", false);
            response.put("message", "No check-in found for today. Please check in first.");
            return response;
        }

        Attendance attendance = existingAttendance.get();

        // Check if check-in exists
        if (attendance.getCheckInTime() == null) {
            response.put("success", false);
            response.put("message", "You must check in before checking out.");
            return response;
        }

        // CRITICAL FIX: Check if already checked out
        if (attendance.getCheckOutTime() != null) {
            response.put("success", false);
            response.put("message", "You have already checked out today at " + attendance.getCheckOutTime());
            return response;
        }

        attendance.setCheckOutTime(LocalTime.now());
        attendance.calculateWorkHours();

        if (notes != null && !notes.trim().isEmpty()) {
            String existingNotes = attendance.getNotes();
            if (existingNotes != null && !existingNotes.trim().isEmpty()) {
                attendance.setNotes(existingNotes + "; " + notes);
            } else {
                attendance.setNotes(notes);
            }
        }

        attendance.setUpdatedAt(LocalDateTime.now());
        attendanceRepository.save(attendance);

        response.put("success", true);
        response.put("message", "Check-out successful!");
        response.put("checkOutTime", LocalTime.now().toString());
        response.put("workHours", attendance.getWorkHours());
        return response;
    }

    // Get today's attendance status
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

    // Get attendance history
    public List<Attendance> getAttendanceHistory(String employeeId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            // Default to last 30 days
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
        }
        return attendanceRepository.findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                employeeId, startDate, endDate);
    }

    // Get attendance statistics
    public Map<String, Object> getAttendanceStatistics(String employeeId) {
        Map<String, Object> stats = new HashMap<>();

        Long presentDays = attendanceRepository.countPresentDaysThisMonth(employeeId);
        Long totalDays = attendanceRepository.countTotalAttendanceDaysThisMonth(employeeId);
        Long totalWorkingDays = totalDays != null && totalDays > 0 ? totalDays : 22L;

        stats.put("presentDays", presentDays != null ? presentDays : 0);
        stats.put("totalWorkingDays", totalWorkingDays);
        stats.put("attendancePercentage", calculateAttendancePercentage(presentDays, totalWorkingDays));

        return stats;
    }

    private double calculateAttendancePercentage(Long presentDays, Long totalWorkingDays) {
        if (totalWorkingDays == null || totalWorkingDays == 0) {
            return 0.0;
        }
        double present = presentDays != null ? presentDays.doubleValue() : 0.0;
        return (present / totalWorkingDays.doubleValue()) * 100.0;
    }

    // Mark manual attendance (for corrections)
    public Map<String, Object> markManualAttendance(String employeeId, LocalDate date,
                                                    LocalTime checkIn, LocalTime checkOut,
                                                    String status, String notes) {
        Map<String, Object> response = new HashMap<>();

        Optional<Attendance> existingAttendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, date);
        Attendance attendance;

        if (existingAttendance.isPresent()) {
            attendance = existingAttendance.get();
        } else {
            attendance = new Attendance();
            attendance.setEmployeeId(employeeId);
            attendance.setAttendanceDate(date);
        }

        attendance.setCheckInTime(checkIn);
        attendance.setCheckOutTime(checkOut);
        attendance.setStatus(status);
        attendance.setNotes(notes);
        attendance.calculateWorkHours();
        attendance.setUpdatedAt(LocalDateTime.now());

        attendanceRepository.save(attendance);

        response.put("success", true);
        response.put("message", "Attendance marked successfully!");
        return response;
    }

    // Get monthly attendance summary
    public List<Attendance> getMonthlyAttendance(String employeeId, int year, int month) {
        return attendanceRepository.findMonthlyAttendance(employeeId, year, month);
    }
}