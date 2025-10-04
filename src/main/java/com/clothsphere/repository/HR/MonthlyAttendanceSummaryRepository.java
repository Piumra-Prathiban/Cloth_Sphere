package com.clothsphere.repository.HR;

import com.clothsphere.model.HR.MonthlyAttendanceSummary;
import com.clothsphere.model.HR.MonthlyAttendanceSummaryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyAttendanceSummaryRepository extends JpaRepository<MonthlyAttendanceSummary, MonthlyAttendanceSummaryId> {

    // Manual INSERT query
    @Modifying
    @Query(value = "INSERT INTO monthly_attendance_summary " +
            "(employee_id, month_year, total_days_in_month, present_days, absent_days, total_work_hours, attendance_rate, created_at, updated_at) " +
            "VALUES (:employeeId, :monthYear, :totalDays, :presentDays, :absentDays, :totalWorkHours, :attendanceRate, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            nativeQuery = true)
    void insertMonthlySummary(@Param("employeeId") String employeeId,
                              @Param("monthYear") String monthYear,
                              @Param("totalDays") int totalDays,
                              @Param("presentDays") int presentDays,
                              @Param("absentDays") int absentDays,
                              @Param("totalWorkHours") double totalWorkHours,
                              @Param("attendanceRate") double attendanceRate);

    // Manual UPDATE query
    @Modifying
    @Query(value = "UPDATE monthly_attendance_summary SET " +
            "present_days = :presentDays, " +
            "absent_days = :absentDays, " +
            "total_work_hours = :totalWorkHours, " +
            "attendance_rate = :attendanceRate, " +
            "updated_at = CURRENT_TIMESTAMP " +
            "WHERE employee_id = :employeeId AND month_year = :monthYear",
            nativeQuery = true)
    void updateMonthlySummary(@Param("employeeId") String employeeId,
                              @Param("monthYear") String monthYear,
                              @Param("presentDays") int presentDays,
                              @Param("absentDays") int absentDays,
                              @Param("totalWorkHours") double totalWorkHours,
                              @Param("attendanceRate") double attendanceRate);

    // Manual DELETE query
    @Modifying
    @Query(value = "DELETE FROM monthly_attendance_summary WHERE employee_id = :employeeId AND month_year = :monthYear",
            nativeQuery = true)
    void deleteMonthlySummary(@Param("employeeId") String employeeId,
                              @Param("monthYear") String monthYear);

    // Find by employee and month
    @Query(value = "SELECT * FROM monthly_attendance_summary WHERE employee_id = :employeeId AND month_year = :monthYear",
            nativeQuery = true)
    Optional<MonthlyAttendanceSummary> findByEmployeeAndMonth(@Param("employeeId") String employeeId,
                                                              @Param("monthYear") String monthYear);

    // Get all summaries for a specific month
    @Query(value = "SELECT * FROM monthly_attendance_summary WHERE month_year = :monthYear ORDER BY employee_id",
            nativeQuery = true)
    List<MonthlyAttendanceSummary> findAllByMonth(@Param("monthYear") String monthYear);

    // Get all summaries for an employee
    @Query(value = "SELECT * FROM monthly_attendance_summary WHERE employee_id = :employeeId ORDER BY month_year DESC",
            nativeQuery = true)
    List<MonthlyAttendanceSummary> findAllByEmployee(@Param("employeeId") String employeeId);

    // Find employees with low attendance rate
    @Query(value = "SELECT * FROM monthly_attendance_summary WHERE month_year = :monthYear AND attendance_rate < :threshold ORDER BY attendance_rate ASC",
            nativeQuery = true)
    List<MonthlyAttendanceSummary> findLowAttendanceEmployees(@Param("monthYear") String monthYear,
                                                              @Param("threshold") double threshold);
}