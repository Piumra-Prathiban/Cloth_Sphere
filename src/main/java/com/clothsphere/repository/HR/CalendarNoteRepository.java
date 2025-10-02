package com.clothsphere.repository.HR;

import com.clothsphere.model.HR.CalendarNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarNoteRepository extends JpaRepository<CalendarNote, Long> {

    // Custom INSERT query for creating new calendar note
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO calendar_notes (user_name, note_date, note_text, created_at, updated_at) " +
            "VALUES (:userName, :noteDate, :noteText, :createdAt, :updatedAt)",
            nativeQuery = true)
    int insertCalendarNote(@Param("userName") String userName,
                           @Param("noteDate") LocalDate noteDate,
                           @Param("noteText") String noteText,
                           @Param("createdAt") java.time.LocalDateTime createdAt,
                           @Param("updatedAt") java.time.LocalDateTime updatedAt);

    // Custom UPDATE query for updating calendar note text
    @Modifying
    @Transactional
    @Query("UPDATE CalendarNote cn SET cn.noteText = :noteText, cn.updatedAt = :updatedAt WHERE cn.id = :id AND cn.userName = :userName")
    int updateCalendarNote(@Param("id") Long id,
                           @Param("userName") String userName,
                           @Param("noteText") String noteText,
                           @Param("updatedAt") java.time.LocalDateTime updatedAt);

    // Custom DELETE query for deleting calendar note by ID
    @Modifying
    @Transactional
    @Query("DELETE FROM CalendarNote cn WHERE cn.id = :id AND cn.userName = :userName")
    int deleteCalendarNoteById(@Param("id") Long id, @Param("userName") String userName);

    // Custom DELETE query for deleting note by user, date, and text
    @Modifying
    @Transactional
    @Query("DELETE FROM CalendarNote cn WHERE cn.userName = :userName AND cn.noteDate = :noteDate AND cn.noteText = :noteText")
    int deleteByUserNameAndNoteDateAndNoteText(@Param("userName") String userName,
                                               @Param("noteDate") LocalDate noteDate,
                                               @Param("noteText") String noteText);

    // Custom query to find all notes for a specific user on a specific date
    @Query("SELECT cn FROM CalendarNote cn WHERE cn.userName = :userName AND cn.noteDate = :noteDate ORDER BY cn.createdAt DESC")
    List<CalendarNote> findByUserNameAndNoteDate(@Param("userName") String userName,
                                                 @Param("noteDate") LocalDate noteDate);

    // Custom query to find all notes for a user within a date range
    @Query("SELECT cn FROM CalendarNote cn WHERE cn.userName = :userName AND cn.noteDate BETWEEN :startDate AND :endDate ORDER BY cn.noteDate, cn.createdAt DESC")
    List<CalendarNote> findByUserNameAndNoteDateBetween(@Param("userName") String userName,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    // Custom query to find all notes for a user (all dates)
    @Query("SELECT cn FROM CalendarNote cn WHERE cn.userName = :userName ORDER BY cn.noteDate DESC, cn.createdAt DESC")
    List<CalendarNote> findByUserName(@Param("userName") String userName);

    // Custom query to find calendar note by ID (replacing automatic findById)
    @Query("SELECT cn FROM CalendarNote cn WHERE cn.id = :id")
    Optional<CalendarNote> findCalendarNoteById(@Param("id") Long id);

    // Custom query to check if calendar note exists by ID (replacing automatic existsById)
    @Query("SELECT COUNT(cn) > 0 FROM CalendarNote cn WHERE cn.id = :id")
    boolean existsCalendarNoteById(@Param("id") Long id);

    // Custom query to check if a note already exists for a user on a specific date
    @Query("SELECT COUNT(cn) > 0 FROM CalendarNote cn WHERE cn.userName = :userName AND cn.noteDate = :noteDate AND cn.noteText = :noteText")
    boolean existsByUserNameAndNoteDateAndNoteText(@Param("userName") String userName,
                                                   @Param("noteDate") LocalDate noteDate,
                                                   @Param("noteText") String noteText);

    // Custom query to count notes for a user on a specific date
    @Query("SELECT COUNT(cn) FROM CalendarNote cn WHERE cn.userName = :userName AND cn.noteDate = :noteDate")
    long countByUserNameAndNoteDate(@Param("userName") String userName, @Param("noteDate") LocalDate noteDate);

    // Custom query to check if user has any notes on a specific date
    @Query("SELECT COUNT(cn) > 0 FROM CalendarNote cn WHERE cn.userName = :userName AND cn.noteDate = :noteDate")
    boolean hasNotesOnDate(@Param("userName") String userName, @Param("noteDate") LocalDate noteDate);

    // Custom query to get note by ID with user validation
    @Query("SELECT cn FROM CalendarNote cn WHERE cn.id = :id AND cn.userName = :userName")
    Optional<CalendarNote> findByIdAndUserName(@Param("id") Long id, @Param("userName") String userName);

    // Custom query to delete all notes for a user on a specific date
    @Modifying
    @Transactional
    @Query("DELETE FROM CalendarNote cn WHERE cn.userName = :userName AND cn.noteDate = :noteDate")
    int deleteAllByUserNameAndNoteDate(@Param("userName") String userName, @Param("noteDate") LocalDate noteDate);

    // Custom query to delete all notes for a user
    @Modifying
    @Transactional
    @Query("DELETE FROM CalendarNote cn WHERE cn.userName = :userName")
    int deleteAllByUserName(@Param("userName") String userName);

    // Custom query to get all calendar notes (replacing automatic findAll)
    @Query("SELECT cn FROM CalendarNote cn ORDER BY cn.noteDate DESC, cn.createdAt DESC")
    List<CalendarNote> findAllCalendarNotes();

    // Custom query to find notes by text content (search functionality)
    @Query("SELECT cn FROM CalendarNote cn WHERE cn.userName = :userName AND cn.noteText LIKE %:searchText% ORDER BY cn.noteDate DESC")
    List<CalendarNote> findByUserNameAndNoteTextContaining(@Param("userName") String userName,
                                                           @Param("searchText") String searchText);
}