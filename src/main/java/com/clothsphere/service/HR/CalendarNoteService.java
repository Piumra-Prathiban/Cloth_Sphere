package com.clothsphere.service.HR;

import com.clothsphere.model.HR.CalendarNote;
import com.clothsphere.repository.HR.CalendarNoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CalendarNoteService {

    @Autowired
    private CalendarNoteRepository calendarNoteRepository;

    // Get all notes for a user on a specific date
    public List<CalendarNote> getNotesForDate(String userName, LocalDate date) {
        return calendarNoteRepository.findByUserNameAndNoteDate(userName, date);
    }

    // Get all notes for a user within a date range
    public List<CalendarNote> getNotesForDateRange(String userName, LocalDate startDate, LocalDate endDate) {
        return calendarNoteRepository.findByUserNameAndNoteDateBetween(userName, startDate, endDate);
    }

    // Get all notes for a user
    public List<CalendarNote> getAllUserNotes(String userName) {
        return calendarNoteRepository.findByUserName(userName);
    }

    // Add a new note
    @Transactional
    public CalendarNote addNote(String userName, LocalDate noteDate, String noteText) {
        try {
            // Check if note already exists to avoid duplicates
            if (!calendarNoteRepository.existsByUserNameAndNoteDateAndNoteText(userName, noteDate, noteText)) {
                CalendarNote note = new CalendarNote(userName, noteDate, noteText);
                return calendarNoteRepository.save(note);
            }
            return null; // Note already exists
        } catch (Exception e) {
            System.err.println("Error adding note: " + e.getMessage());
            throw e;
        }
    }

    // Delete a note
    @Transactional
    public boolean deleteNote(String userName, LocalDate noteDate, String noteText) {
        try {
            calendarNoteRepository.deleteByUserNameAndNoteDateAndNoteText(userName, noteDate, noteText);
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting note: " + e.getMessage());
            return false;
        }
    }

    // Delete note by ID
    @Transactional
    public boolean deleteNoteById(Long noteId, String userName) {
        try {
            Optional<CalendarNote> note = calendarNoteRepository.findById(noteId);
            if (note.isPresent() && note.get().getUserName().equals(userName)) {
                calendarNoteRepository.deleteById(noteId);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting note by ID: " + e.getMessage());
            return false;
        }
    }

    // Update a note
    @Transactional
    public CalendarNote updateNote(Long noteId, String userName, String newNoteText) {
        try {
            Optional<CalendarNote> existingNote = calendarNoteRepository.findById(noteId);
            if (existingNote.isPresent() && existingNote.get().getUserName().equals(userName)) {
                CalendarNote note = existingNote.get();
                note.setNoteText(newNoteText);
                return calendarNoteRepository.save(note);
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error updating note: " + e.getMessage());
            return null;
        }
    }

    // Check if user has notes on a specific date
    public boolean hasNotesOnDate(String userName, LocalDate date) {
        return calendarNoteRepository.countByUserNameAndNoteDate(userName, date) > 0;
    }

    // Get note count for a user on a specific date
    public long getNoteCountForDate(String userName, LocalDate date) {
        return calendarNoteRepository.countByUserNameAndNoteDate(userName, date);
    }
}