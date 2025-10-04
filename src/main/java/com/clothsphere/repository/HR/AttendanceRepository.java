package com.clothsphere.repository.HR;

import com.clothsphere.model.HR.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Manual INSERT query
    @Modifying
    @Query(value = "INSERT INTO attendance (employee_id, attendance_date, check_in_time, check_out_time, status, work_hours, notes, created_at, updated_at) " +
            "VALUES (:employeeId, :attendanceDate, :checkInTime, :checkOutTime, :status, :workHours, :notes, :createdAt, :updatedAt)",
            nativeQuery = true)
    void insertAttendance(@Param("employeeId") String employeeId,
                          @Param("attendanceDate") LocalDate attendanceDate,
                          @Param("checkInTime") LocalTime checkInTime,
                          @Param("checkOutTime") LocalTime checkOutTime,
                          @Param("status") String status,
                          @Param("workHours") Double workHours,
                          @Param("notes") String notes,
                          @Param("createdAt") LocalDateTime createdAt,
                          @Param("updatedAt") LocalDateTime updatedAt);

    // Manual UPDATE query
    @Modifying
    @Query(value = "UPDATE attendance SET " +
            "check_in_time = :checkInTime, " +
            "check_out_time = :checkOutTime, " +
            "status = :status, " +
            "work_hours = :workHours, " +
            "notes = :notes, " +
            "updated_at = :updatedAt " +
            "WHERE attendance_id = :attendanceId",
            nativeQuery = true)
    void updateAttendance(@Param("attendanceId") Long attendanceId,
                          @Param("checkInTime") LocalTime checkInTime,
                          @Param("checkOutTime") LocalTime checkOutTime,
                          @Param("status") String status,
                          @Param("workHours") Double workHours,
                          @Param("notes") String notes,
                          @Param("updatedAt") LocalDateTime updatedAt);

    // Manual UPDATE query for check-in
    @Modifying
    @Query(value = "UPDATE attendance SET " +
            "check_in_time = :checkInTime, " +
            "status = :status, " +
            "notes = :notes, " +
            "updated_at = :updatedAt " +
            "WHERE employee_id = :employeeId AND attendance_date = :attendanceDate",
            nativeQuery = true)
    void updateCheckIn(@Param("employeeId") String employeeId,
                       @Param("attendanceDate") LocalDate attendanceDate,
                       @Param("checkInTime") LocalTime checkInTime,
                       @Param("status") String status,
                       @Param("notes") String notes,
                       @Param("updatedAt") LocalDateTime updatedAt);

    // Manual UPDATE query for check-out
    @Modifying
    @Query(value = "UPDATE attendance SET " +
            "check_out_time = :checkOutTime, " +
            "work_hours = :workHours, " +
            "notes = :notes, " +
            "updated_at = :updatedAt " +
            "WHERE employee_id = :employeeId AND attendance_date = :attendanceDate",
            nativeQuery = true)
    void updateCheckOut(@Param("employeeId") String employeeId,
                        @Param("attendanceDate") LocalDate attendanceDate,
                        @Param("checkOutTime") LocalTime checkOutTime,
                        @Param("workHours") Double workHours,
                        @Param("notes") String notes,
                        @Param("updatedAt") LocalDateTime updatedAt);

    // Manual DELETE query by ID
    @Modifying
    @Query(value = "DELETE FROM attendance WHERE attendance_id = :attendanceId", nativeQuery = true)
    void deleteAttendanceById(@Param("attendanceId") Long attendanceId);

    // Manual DELETE query by employee ID and date
    @Modifying
    @Query(value = "DELETE FROM attendance WHERE employee_id = :employeeId AND attendance_date = :attendanceDate", nativeQuery = true)
    void deleteAttendanceByEmployeeAndDate(@Param("employeeId") String employeeId,
                                           @Param("attendanceDate") LocalDate attendanceDate);

    // Manual SELECT query by ID
    @Query(value = "SELECT * FROM attendance WHERE attendance_id = :attendanceId", nativeQuery = true)
    Optional<Attendance> findAttendanceById(@Param("attendanceId") Long attendanceId);

    // Find attendance by employee ID and date
    @Query(value = "SELECT * FROM attendance WHERE employee_id = :employeeId AND attendance_date = :attendanceDate", nativeQuery = true)
    Optional<Attendance> findByEmployeeIdAndAttendanceDate(@Param("employeeId") String employeeId,
                                                           @Param("attendanceDate") LocalDate attendanceDate);

    // Find all attendance records for an employee
    @Query(value = "SELECT * FROM attendance WHERE employee_id = :employeeId ORDER BY attendance_date DESC", nativeQuery = true)
    List<Attendance> findByEmployeeIdOrderByAttendanceDateDesc(@Param("employeeId") String employeeId);

    // Find attendance records for an employee in date range
    @Query(value = "SELECT * FROM attendance WHERE employee_id = :employeeId AND attendance_date BETWEEN :startDate AND :endDate ORDER BY attendance_date DESC", nativeQuery = true)
    List<Attendance> findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
            @Param("employeeId") String employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Find today's attendance for an employee
    @Query(value = "SELECT * FROM attendance WHERE employee_id = :employeeId AND attendance_date = CURRENT_DATE", nativeQuery = true)
    Optional<Attendance> findTodayAttendance(@Param("employeeId") String employeeId);

    // Get monthly attendance summary
    @Query(value = "SELECT * FROM attendance WHERE employee_id = :employeeId AND YEAR(attendance_date) = :year AND MONTH(attendance_date) = :month ORDER BY attendance_date DESC", nativeQuery = true)
    List<Attendance> findMonthlyAttendance(@Param("employeeId") String employeeId,
                                           @Param("year") int year,
                                           @Param("month") int month);

    // Count present days in current month
    @Query(value = "SELECT COUNT(*) FROM attendance WHERE employee_id = :employeeId AND YEAR(attendance_date) = YEAR(CURRENT_DATE) AND MONTH(attendance_date) = MONTH(CURRENT_DATE) AND status = 'PRESENT'", nativeQuery = true)
    Long countPresentDaysThisMonth(@Param("employeeId") String employeeId);

    // Count total working days in current month
    @Query(value = "SELECT COUNT(*) FROM attendance WHERE employee_id = :employeeId AND YEAR(attendance_date) = YEAR(CURRENT_DATE) AND MONTH(attendance_date) = MONTH(CURRENT_DATE)", nativeQuery = true)
    Long countTotalAttendanceDaysThisMonth(@Param("employeeId") String employeeId);

    // Count working days using native query (excluding weekends)
    @Query(value = "SELECT COUNT(DISTINCT attendance_date) FROM attendance WHERE employee_id = :employeeId AND YEAR(attendance_date) = YEAR(CURRENT_DATE) AND MONTH(attendance_date) = MONTH(CURRENT_DATE) AND DAYOFWEEK(attendance_date) NOT IN (1, 7)", nativeQuery = true)
    Long countWorkingDaysThisMonthNative(@Param("employeeId") String employeeId);
}