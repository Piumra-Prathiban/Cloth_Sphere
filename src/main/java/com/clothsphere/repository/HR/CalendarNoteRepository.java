package com.clothsphere.repository.HR;

import com.clothsphere.model.HR.CalendarNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CalendarNoteRepository extends JpaRepository<CalendarNote, Long> {

    // Find all notes for a specific user on a specific date
    List<CalendarNote> findByUserNameAndNoteDate(String userName, LocalDate noteDate);

    // Find all notes for a user within a date range
    List<CalendarNote> findByUserNameAndNoteDateBetween(String userName, LocalDate startDate, LocalDate endDate);

    // Find all notes for a user (all dates)
    List<CalendarNote> findByUserName(String userName);

    // Delete a specific note by user, date, and text
    @Modifying
    @Query("DELETE FROM CalendarNote cn WHERE cn.userName = :userName AND cn.noteDate = :noteDate AND cn.noteText = :noteText")
    void deleteByUserNameAndNoteDateAndNoteText(@Param("userName") String userName,
                                                @Param("noteDate") LocalDate noteDate,
                                                @Param("noteText") String noteText);

    // Check if a note already exists for a user on a specific date
    boolean existsByUserNameAndNoteDateAndNoteText(String userName, LocalDate noteDate, String noteText);

    // Count notes for a user on a specific date
    long countByUserNameAndNoteDate(String userName, LocalDate noteDate);
}