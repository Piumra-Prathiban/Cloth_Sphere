package com.clothsphere.repository.HR;

import com.clothsphere.model.HR.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Find attendance by employee ID and date
    Optional<Attendance> findByEmployeeIdAndAttendanceDate(String employeeId, LocalDate attendanceDate);

    // Find all attendance records for an employee
    List<Attendance> findByEmployeeIdOrderByAttendanceDateDesc(String employeeId);

    // Find attendance records for an employee in date range
    List<Attendance> findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
            String employeeId, LocalDate startDate, LocalDate endDate);

    // Find today's attendance for an employee
    @Query("SELECT a FROM Attendance a WHERE a.employeeId = :employeeId AND a.attendanceDate = CURRENT_DATE")
    Optional<Attendance> findTodayAttendance(@Param("employeeId") String employeeId);

    // Get monthly attendance summary
    @Query("SELECT a FROM Attendance a WHERE a.employeeId = :employeeId AND YEAR(a.attendanceDate) = :year AND MONTH(a.attendanceDate) = :month ORDER BY a.attendanceDate DESC")
    List<Attendance> findMonthlyAttendance(@Param("employeeId") String employeeId,
                                           @Param("year") int year,
                                           @Param("month") int month);

    // Count present days in current month - SIMPLIFIED VERSION
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employeeId = :employeeId AND YEAR(a.attendanceDate) = YEAR(CURRENT_DATE) AND MONTH(a.attendanceDate) = MONTH(CURRENT_DATE) AND a.status = 'PRESENT'")
    Long countPresentDaysThisMonth(@Param("employeeId") String employeeId);

    // Count total working days in current month - SIMPLIFIED VERSION
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employeeId = :employeeId AND YEAR(a.attendanceDate) = YEAR(CURRENT_DATE) AND MONTH(a.attendanceDate) = MONTH(CURRENT_DATE)")
    Long countTotalAttendanceDaysThisMonth(@Param("employeeId") String employeeId);

    // Alternative: Count working days using native query (if you need to exclude weekends)
    @Query(value = "SELECT COUNT(DISTINCT attendance_date) FROM attendance WHERE employee_id = :employeeId AND YEAR(attendance_date) = YEAR(CURRENT_DATE) AND MONTH(attendance_date) = MONTH(CURRENT_DATE) AND DAYOFWEEK(attendance_date) NOT IN (1, 7)", nativeQuery = true)
    Long countWorkingDaysThisMonthNative(@Param("employeeId") String employeeId);
}