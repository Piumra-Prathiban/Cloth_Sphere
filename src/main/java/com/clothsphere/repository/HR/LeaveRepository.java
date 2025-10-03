package com.clothsphere.repository.HR;

import com.clothsphere.model.HR.Leave;
import com.clothsphere.model.HR.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {

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
    // Add these methods to your existing LeaveRepository interface

    // Find all leaves ordered by request date
    List<Leave> findAllByOrderByRequestDateDesc();

    // Count leaves by status
    long countByStatus(String status);

    // Find leaves by employee ID (using join)
    @Query("SELECT l FROM Leave l WHERE l.employee.id = :employeeId ORDER BY l.requestDate DESC")
    List<Leave> findByEmployeeId(@Param("employeeId") String employeeId);
}