package com.clothsphere.controller.HR;

import com.clothsphere.model.HR.Attendance;
import com.clothsphere.service.HR.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    // Check in endpoint
    @PostMapping("/checkin")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkIn(@RequestParam(value = "notes", required = false) String notes,
                                                       HttpSession session) {
        String employeeId = (String) session.getAttribute("employeeId");

        if (employeeId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Employee not logged in"));
        }

        Map<String, Object> result = attendanceService.checkIn(employeeId, notes);
        return ResponseEntity.ok(result);
    }

    // Check out endpoint
    @PostMapping("/checkout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkOut(@RequestParam(value = "notes", required = false) String notes,
                                                        HttpSession session) {
        String employeeId = (String) session.getAttribute("employeeId");

        if (employeeId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Employee not logged in"));
        }

        Map<String, Object> result = attendanceService.checkOut(employeeId, notes);
        return ResponseEntity.ok(result);
    }

    // Get today's attendance status
    @GetMapping("/today")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTodayAttendance(HttpSession session) {
        String employeeId = (String) session.getAttribute("employeeId");

        if (employeeId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Employee not logged in"));
        }

        Map<String, Object> todayAttendance = attendanceService.getTodayAttendance(employeeId);
        return ResponseEntity.ok(todayAttendance);
    }

    // Get attendance history
    @GetMapping("/history")
    @ResponseBody
    public ResponseEntity<List<Attendance>> getAttendanceHistory(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpSession session) {

        String employeeId = (String) session.getAttribute("employeeId");

        if (employeeId == null) {
            return ResponseEntity.badRequest().build();
        }

        List<Attendance> history = attendanceService.getAttendanceHistory(employeeId, startDate, endDate);
        return ResponseEntity.ok(history);
    }

    // Get attendance statistics
    @GetMapping("/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAttendanceStatistics(HttpSession session) {
        String employeeId = (String) session.getAttribute("employeeId");

        if (employeeId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Employee not logged in"));
        }

        Map<String, Object> statistics = attendanceService.getAttendanceStatistics(employeeId);
        return ResponseEntity.ok(statistics);
    }

    // Manual attendance marking (for corrections)
    @PostMapping("/manual")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markManualAttendance(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "checkIn", required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime checkIn,
            @RequestParam(value = "checkOut", required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime checkOut,
            @RequestParam("status") String status,
            @RequestParam(value = "notes", required = false) String notes,
            HttpSession session) {

        String employeeId = (String) session.getAttribute("employeeId");

        if (employeeId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Employee not logged in"));
        }

        Map<String, Object> result = attendanceService.markManualAttendance(employeeId, date, checkIn, checkOut, status, notes);
        return ResponseEntity.ok(result);
    }
}