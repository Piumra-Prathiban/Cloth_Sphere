package com.clothsphere.controller.HR;

import com.clothsphere.model.HR.CalendarNote;
import com.clothsphere.model.SystemUser;
import com.clothsphere.service.HR.CalendarNoteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar-notes")
public class CalendarNoteController {

    @Autowired
    private CalendarNoteService calendarNoteService;

    // Get notes for a specific date
    @GetMapping("/{date}")
    public ResponseEntity<List<CalendarNote>> getNotesForDate(
            @PathVariable String date,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            LocalDate noteDate = LocalDate.parse(date);
            List<CalendarNote> notes = calendarNoteService.getNotesForDate(currentUser.getUserName(), noteDate);
            return new ResponseEntity<>(notes, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error fetching notes for date " + date + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get notes for a date range
    @GetMapping("/range/{startDate}/{endDate}")
    public ResponseEntity<List<CalendarNote>> getNotesForDateRange(
            @PathVariable String startDate,
            @PathVariable String endDate,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<CalendarNote> notes = calendarNoteService.getNotesForDateRange(currentUser.getUserName(), start, end);
            return new ResponseEntity<>(notes, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error fetching notes for date range " + startDate + " to " + endDate + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Add a new note
    // In CalendarNoteController.java - Update the addNote method
    @PostMapping
    public ResponseEntity<Map<String, Object>> addNote(
            @RequestBody Map<String, String> noteData,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> response = new HashMap<>();

        try {
            String dateStr = noteData.get("noteDate");
            String noteText = noteData.get("noteText");

            System.out.println("=== ADDING CALENDAR NOTE ===");
            System.out.println("User: " + currentUser.getUserName());
            System.out.println("Date: " + dateStr);
            System.out.println("Note Text: " + noteText);

            if (dateStr == null || noteText == null || noteText.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Date and note text are required");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            LocalDate noteDate = LocalDate.parse(dateStr);

            // Use the manual query method
            CalendarNote savedNote = calendarNoteService.addNote(currentUser.getUserName(), noteDate, noteText.trim());

            if (savedNote != null) {
                System.out.println("Note added successfully with ID: " + savedNote.getId());
                response.put("success", true);
                response.put("message", "Note added successfully");
                response.put("note", savedNote);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                System.out.println("Note already exists or insertion failed");
                response.put("success", false);
                response.put("message", "Note already exists");
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }

        } catch (Exception e) {
            System.out.println("Error adding note: " + e.getMessage());
            e.printStackTrace(); // Add detailed error logging
            response.put("success", false);
            response.put("message", "Error adding note: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete a note
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteNote(
            @RequestParam String date,
            @RequestParam String noteText,
            HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> response = new HashMap<>();

        try {
            LocalDate noteDate = LocalDate.parse(date);

            // Use the manual query method
            boolean success = calendarNoteService.deleteNote(currentUser.getUserName(), noteDate, noteText);

            if (success) {
                response.put("success", true);
                response.put("message", "Note deleted successfully");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "Failed to delete note");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            System.out.println("Error deleting note: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error deleting note: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all user notes
    @GetMapping("/user/all")
    public ResponseEntity<List<CalendarNote>> getAllUserNotes(HttpSession session) {

        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");
        if (currentUser == null || !"hr-manager".equals(currentUser.getRole())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            List<CalendarNote> notes = calendarNoteService.getAllUserNotes(currentUser.getUserName());
            return new ResponseEntity<>(notes, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error fetching all user notes: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}