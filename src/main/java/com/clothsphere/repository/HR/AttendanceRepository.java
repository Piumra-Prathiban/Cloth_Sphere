package com.clothsphere.repository.HR;

import com.clothsphere.model.HR.Attendance;
import com.clothsphere.model.HR.AttendanceId;
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
public interface AttendanceRepository extends JpaRepository<Attendance, AttendanceId> {

    // Manual INSERT query - KEEP AS IS
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

    // Manual UPDATE query for check-in - KEEP AS IS
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

    // Manual UPDATE query for check-out - KEEP AS IS
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

    // Manual DELETE query by employee ID and date - KEEP AS IS
    @Modifying
    @Query("DELETE FROM Attendance a WHERE a.employeeId = :employeeId AND a.attendanceDate = :attendanceDate")
    void deleteByEmployeeIdAndAttendanceDate(@Param("employeeId") String employeeId,
                                             @Param("attendanceDate") LocalDate attendanceDate);

    // Find attendance by employee ID and date - KEEP AS IS
    @Query("SELECT a FROM Attendance a WHERE a.employeeId = :employeeId AND a.attendanceDate = :attendanceDate")
    Optional<Attendance> findByEmployeeIdAndAttendanceDate(@Param("employeeId") String employeeId,
                                                           @Param("attendanceDate") LocalDate attendanceDate);

    // Find all attendance records for an employee - KEEP AS IS
    @Query(value = "SELECT * FROM attendance WHERE employee_id = :employeeId ORDER BY attendance_date DESC",
            nativeQuery = true)
    List<Attendance> findByEmployeeIdOrderByAttendanceDateDesc(@Param("employeeId") String employeeId);

    // Find attendance records for an employee in date range - KEEP AS IS
    @Query(value = "SELECT * FROM attendance WHERE employee_id = :employeeId AND attendance_date BETWEEN :startDate AND :endDate ORDER BY attendance_date DESC",
            nativeQuery = true)
    List<Attendance> findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
            @Param("employeeId") String employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Find today's attendance for an employee - KEEP AS IS
    @Query(value = "SELECT * FROM attendance WHERE employee_id = :employeeId AND attendance_date = CURRENT_DATE",
            nativeQuery = true)
    Optional<Attendance> findTodayAttendance(@Param("employeeId") String employeeId);

    // Get monthly attendance summary - KEEP AS IS
    @Query(value = "SELECT * FROM attendance WHERE employee_id = :employeeId AND YEAR(attendance_date) = :year AND MONTH(attendance_date) = :month ORDER BY attendance_date DESC",
            nativeQuery = true)
    List<Attendance> findMonthlyAttendance(@Param("employeeId") String employeeId,
                                           @Param("year") int year,
                                           @Param("month") int month);

    // Count present days in current month - FIXED (Using JPQL)
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employeeId = :employeeId AND YEAR(a.attendanceDate) = YEAR(CURRENT_DATE) AND MONTH(a.attendanceDate) = MONTH(CURRENT_DATE) AND a.status = 'PRESENT'")
    Long countPresentDaysThisMonth(@Param("employeeId") String employeeId);

    // Count total working days in current month - FIXED (Using JPQL)
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employeeId = :employeeId AND YEAR(a.attendanceDate) = YEAR(CURRENT_DATE) AND MONTH(a.attendanceDate) = MONTH(CURRENT_DATE)")
    Long countTotalAttendanceDaysThisMonth(@Param("employeeId") String employeeId);

    // Find attendance records by employee name and date range
    @Query(value = "SELECT a.* FROM attendance a " +
            "JOIN employees e ON a.employee_id = e.employee_id " +
            "WHERE LOWER(e.full_name) LIKE LOWER(CONCAT('%', :employeeName, '%')) " +
            "AND a.attendance_date BETWEEN :startDate AND :endDate " +
            "ORDER BY a.attendance_date DESC",
            nativeQuery = true)
    List<Attendance> findByEmployeeNameAndAttendanceDateBetweenOrderByAttendanceDateDesc(
            @Param("employeeName") String employeeName,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Find all employees attendance between dates
    @Query(value = "SELECT a.* FROM attendance a " +
            "WHERE a.attendance_date BETWEEN :startDate AND :endDate " +
            "ORDER BY a.employee_id, a.attendance_date DESC",
            nativeQuery = true)
    List<Attendance> findAllEmployeesAttendanceBetweenDates(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}