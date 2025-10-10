package com.clothsphere.repository.HR;

import com.clothsphere.model.HR.Leave;
import com.clothsphere.model.HR.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, String> {

    // Find all leave requests by employee
    List<Leave> findByEmployeeOrderByRequestDateDesc(Employee employee);

    // Find leave requests by status for an employee
    List<Leave> findByEmployeeAndStatusOrderByRequestDateDesc(Employee employee, String status);

    // Find pending leave requests for HR manager
    List<Leave> findByStatusOrderByRequestDateDesc(String status);

    // Check for overlapping leave requests
    @Query("SELECT l FROM Leave l WHERE l.employee = :employee AND l.status != 'REJECTED' AND " +
            "((l.startDate BETWEEN :startDate AND :endDate) OR " +
            "(l.endDate BETWEEN :startDate AND :endDate) OR " +
            "(:startDate BETWEEN l.startDate AND l.endDate) OR " +
            "(:endDate BETWEEN l.startDate AND l.endDate))")
    List<Leave> findOverlappingLeaves(@Param("employee") Employee employee,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    // Find leaves within a date range for an employee
    List<Leave> findByEmployeeAndStartDateBetweenOrEndDateBetweenOrderByStartDate(
            Employee employee, LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2);

    // Find all leaves ordered by request date
    List<Leave> findAllByOrderByRequestDateDesc();

    // Count leaves by status
    long countByStatus(String status);

    // Find leaves by employee ID (using join)
    @Query("SELECT l FROM Leave l WHERE l.employee.id = :employeeId ORDER BY l.requestDate DESC")
    List<Leave> findByEmployeeId(@Param("employeeId") String employeeId);

    // MANUAL QUERY METHODS

    // Updated manual INSERT query to include leave_id
    @Modifying
    @Query(value = "INSERT INTO leave_requests (leave_id, reason, start_date, end_date, status, request_date, employee_id, comments) " +
            "VALUES (:leaveId, :reason, :startDate, :endDate, :status, :requestDate, :employeeId, :comments)",
            nativeQuery = true)
    int insertLeave(@Param("leaveId") String leaveId,
                    @Param("reason") String reason,
                    @Param("startDate") LocalDate startDate,
                    @Param("endDate") LocalDate endDate,
                    @Param("status") String status,
                    @Param("requestDate") LocalDateTime requestDate,
                    @Param("employeeId") String employeeId,
                    @Param("comments") String comments);

    // Add method to get next leave ID
    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(leave_id, 4, LEN(leave_id)) AS INT)), 0) + 1 FROM leave_requests WHERE leave_id LIKE 'lev%'",
            nativeQuery = true)
    Long getNextLeaveIdNumber();


    // Manual UPDATE query for status and comments
    @Modifying
    @Query("UPDATE Leave l SET l.status = :status, l.comments = :comments, l.actionDate = :actionDate WHERE l.leaveId = :leaveId")
    int updateLeaveStatus(@Param("leaveId") String  leaveId,
                          @Param("status") String status,
                          @Param("comments") String comments,
                          @Param("actionDate") LocalDateTime actionDate);

    // Manual UPDATE query for full leave details
    @Modifying
    @Query("UPDATE Leave l SET l.reason = :reason, l.startDate = :startDate, l.endDate = :endDate, " +
            "l.status = :status, l.comments = :comments WHERE l.leaveId = :leaveId")
    int updateLeave(@Param("leaveId") String  leaveId,
                    @Param("reason") String reason,
                    @Param("startDate") LocalDate startDate,
                    @Param("endDate") LocalDate endDate,
                    @Param("status") String status,
                    @Param("comments") String comments);

    // Manual DELETE query by ID
    @Modifying
    @Query("DELETE FROM Leave l WHERE l.leaveId = :leaveId")
    int deleteLeaveById(@Param("leaveId") String  leaveId);

    // Manual DELETE query by employee ID and status
    @Modifying
    @Query("DELETE FROM Leave l WHERE l.employee.id = :employeeId AND l.status = :status")
    int deleteLeavesByEmployeeAndStatus(@Param("employeeId") String employeeId,
                                        @Param("status") String status);

    // Manual query to get leave count by employee and status
    @Query("SELECT COUNT(l) FROM Leave l WHERE l.employee.id = :employeeId AND l.status = :status")
    long countByEmployeeIdAndStatus(@Param("employeeId") String employeeId,
                                    @Param("status") String status);

    // Manual query to update action date
    @Modifying
    @Query("UPDATE Leave l SET l.actionDate = :actionDate WHERE l.leaveId = :leaveId")
    int updateActionDate(@Param("leaveId") String  leaveId,
                         @Param("actionDate") LocalDateTime actionDate);
}