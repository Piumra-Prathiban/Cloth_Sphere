package com.clothsphere.service.HR;

import com.clothsphere.model.HR.CalendarNote;
import com.clothsphere.repository.HR.CalendarNoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CalendarNoteService {

    @Autowired
    private CalendarNoteRepository calendarNoteRepository;

    // Get all notes for a user on a specific date using manual query
    @Transactional(readOnly = true)
    public List<CalendarNote> getNotesForDate(String userName, LocalDate date) {
        try {
            return calendarNoteRepository.findByUserNameAndNoteDate(userName, date);
        } catch (Exception e) {
            System.err.println("Error fetching notes for date: " + e.getMessage());
            return List.of();
        }
    }

    // Get all notes for a user within a date range using manual query
    @Transactional(readOnly = true)
    public List<CalendarNote> getNotesForDateRange(String userName, LocalDate startDate, LocalDate endDate) {
        try {
            return calendarNoteRepository.findByUserNameAndNoteDateBetween(userName, startDate, endDate);
        } catch (Exception e) {
            System.err.println("Error fetching notes for date range: " + e.getMessage());
            return List.of();
        }
    }

    // Get all notes for a user using manual query
    @Transactional(readOnly = true)
    public List<CalendarNote> getAllUserNotes(String userName) {
        try {
            return calendarNoteRepository.findByUserName(userName);
        } catch (Exception e) {
            System.err.println("Error fetching all user notes: " + e.getMessage());
            return List.of();
        }
    }

    // Add a new note using manual INSERT query
    @Transactional
    public CalendarNote addNote(String userName, LocalDate noteDate, String noteText) {
        try {
            // Trim and clean the note text
            String cleanedNoteText = noteText.trim();

            // Check if note already exists to avoid duplicates
            boolean noteExists = calendarNoteRepository.existsByUserNameAndNoteDateAndNoteText(
                    userName, noteDate, cleanedNoteText);

            System.out.println("=== ADDING CALENDAR NOTE ===");
            System.out.println("User: " + userName);
            System.out.println("Date: " + noteDate);
            System.out.println("Note Text: " + cleanedNoteText);
            System.out.println("Note Already Exists: " + noteExists);

            if (noteExists) {
                System.out.println("Note already exists - skipping insertion");
                return null;
            }

            // Use manual INSERT query instead of JPA save
            LocalDateTime now = LocalDateTime.now();
            int result = calendarNoteRepository.insertCalendarNote(userName, noteDate, cleanedNoteText, now, now);

            if (result > 0) {
                // Retrieve the newly created note
                List<CalendarNote> notes = calendarNoteRepository.findByUserNameAndNoteDate(userName, noteDate);
                return notes.stream()
                        .filter(note -> note.getNoteText().equals(cleanedNoteText))
                        .findFirst()
                        .orElse(null);
            }

            System.out.println("Note insertion failed");
            return null;

        } catch (Exception e) {
            System.err.println("Error adding note: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Alternative method using manual INSERT query (if you need to keep this option)
    @Transactional
    public CalendarNote addNoteWithInsert(String userName, LocalDate noteDate, String noteText) {
        try {
            // Check if note already exists to avoid duplicates
            if (!calendarNoteRepository.existsByUserNameAndNoteDateAndNoteText(userName, noteDate, noteText)) {
                LocalDateTime now = LocalDateTime.now();
                int result = calendarNoteRepository.insertCalendarNote(userName, noteDate, noteText, now, now);

                if (result > 0) {
                    // Retrieve the newly created note
                    List<CalendarNote> notes = calendarNoteRepository.findByUserNameAndNoteDate(userName, noteDate);
                    return notes.stream()
                            .filter(note -> note.getNoteText().equals(noteText))
                            .findFirst()
                            .orElse(null);
                }
            }
            return null; // Note already exists or insertion failed
        } catch (Exception e) {
            System.err.println("Error adding note: " + e.getMessage());
            throw e;
        }
    }

    // Delete a note using manual DELETE query
    @Transactional
    public boolean deleteNote(String userName, LocalDate noteDate, String noteText) {
        try {
            int result = calendarNoteRepository.deleteByUserNameAndNoteDateAndNoteText(userName, noteDate, noteText);
            return result > 0;
        } catch (Exception e) {
            System.err.println("Error deleting note: " + e.getMessage());
            return false;
        }
    }

    // Delete note by ID using manual DELETE query
    @Transactional
    public boolean deleteNoteById(Long noteId, String userName) {
        try {
            int result = calendarNoteRepository.deleteCalendarNoteById(noteId, userName);
            return result > 0;
        } catch (Exception e) {
            System.err.println("Error deleting note by ID: " + e.getMessage());
            return false;
        }
    }

    // Update a note using manual UPDATE query
    @Transactional
    public CalendarNote updateNote(Long noteId, String userName, String newNoteText) {
        try {
            LocalDateTime updatedAt = LocalDateTime.now();
            int result = calendarNoteRepository.updateCalendarNote(noteId, userName, newNoteText, updatedAt);

            if (result > 0) {
                // Retrieve the updated note
                return calendarNoteRepository.findByIdAndUserName(noteId, userName).orElse(null);
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error updating note: " + e.getMessage());
            return null;
        }
    }

    // Alternative update method using manual UPDATE query (if you need to keep this option)
    @Transactional
    public CalendarNote updateNoteWithQuery(Long noteId, String userName, String newNoteText) {
        try {
            Optional<CalendarNote> existingNote = calendarNoteRepository.findByIdAndUserName(noteId, userName);
            if (existingNote.isPresent()) {
                LocalDateTime updatedAt = LocalDateTime.now();
                int result = calendarNoteRepository.updateCalendarNote(noteId, userName, newNoteText, updatedAt);

                if (result > 0) {
                    return calendarNoteRepository.findByIdAndUserName(noteId, userName).orElse(null);
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error updating note: " + e.getMessage());
            return null;
        }
    }

    // Check if user has notes on a specific date using manual query
    @Transactional(readOnly = true)
    public boolean hasNotesOnDate(String userName, LocalDate date) {
        try {
            return calendarNoteRepository.hasNotesOnDate(userName, date);
        } catch (Exception e) {
            System.err.println("Error checking notes on date: " + e.getMessage());
            return false;
        }
    }

    // Get note count for a user on a specific date using manual query
    @Transactional(readOnly = true)
    public long getNoteCountForDate(String userName, LocalDate date) {
        try {
            return calendarNoteRepository.countByUserNameAndNoteDate(userName, date);
        } catch (Exception e) {
            System.err.println("Error getting note count: " + e.getMessage());
            return 0;
        }
    }

    // Search notes by text content
    @Transactional(readOnly = true)
    public List<CalendarNote> searchNotes(String userName, String searchText) {
        try {
            return calendarNoteRepository.findByUserNameAndNoteTextContaining(userName, searchText);
        } catch (Exception e) {
            System.err.println("Error searching notes: " + e.getMessage());
            return List.of();
        }
    }

    // Get note by ID with user validation
    @Transactional(readOnly = true)
    public CalendarNote getNoteByIdAndUser(Long noteId, String userName) {
        try {
            return calendarNoteRepository.findByIdAndUserName(noteId, userName).orElse(null);
        } catch (Exception e) {
            System.err.println("Error getting note by ID: " + e.getMessage());
            return null;
        }
    }

    // Delete all notes for a user on a specific date
    @Transactional
    public boolean deleteAllNotesForDate(String userName, LocalDate noteDate) {
        try {
            int result = calendarNoteRepository.deleteAllByUserNameAndNoteDate(userName, noteDate);
            return result > 0;
        } catch (Exception e) {
            System.err.println("Error deleting all notes for date: " + e.getMessage());
            return false;
        }
    }
}