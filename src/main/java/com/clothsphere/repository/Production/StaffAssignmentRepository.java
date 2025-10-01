package com.clothsphere.repository.Production;

import com.clothsphere.model.HR.Employee;
import com.clothsphere.model.Production.StaffAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StaffAssignmentRepository extends JpaRepository<StaffAssignment, String> {

    // Find assignments by employee
    List<StaffAssignment> findByEmployee(Employee employee);

    // Find assignments by employee ID (convenience method)
    @Query("SELECT sa FROM StaffAssignment sa WHERE sa.employee.id = :employeeId")
    List<StaffAssignment> findByEmployeeId(@Param("employeeId") String employeeId);

    // Find assignments by schedule
    List<StaffAssignment> findByScheduleId(String scheduleId);

    // Find assignments by workstation
    List<StaffAssignment> findByWorkstationId(String workstationId);

    // Find assignments by status
    List<StaffAssignment> findByStatus(String status);

    // Find assignments for a specific date
    List<StaffAssignment> findByAssignmentDate(LocalDate date);

    // Find assignments by shift
    List<StaffAssignment> findByShift(String shift);

    // Find assignments by workstation and date
    @Query("SELECT sa FROM StaffAssignment sa WHERE sa.workstationId = :workstationId AND sa.assignmentDate = :date")
    List<StaffAssignment> findByWorkstationAndDate(@Param("workstationId") String workstationId,
                                                   @Param("date") LocalDate date);

    // Find assignments by employee and date range
    @Query("SELECT sa FROM StaffAssignment sa WHERE sa.employee.id = :employeeId AND sa.assignmentDate BETWEEN :startDate AND :endDate")
    List<StaffAssignment> findByEmployeeAndDateRange(@Param("employeeId") String employeeId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    // Get all assignment IDs for ID generation
    @Query("SELECT sa.assignmentId FROM StaffAssignment sa")
    List<String> findAllAssignmentIds();

    // Count assignments by status
    @Query("SELECT COUNT(sa) FROM StaffAssignment sa WHERE sa.status = :status")
    Long countByStatus(@Param("status") String status);

    // Find today's assignments
    @Query("SELECT sa FROM StaffAssignment sa WHERE sa.assignmentDate = :today")
    List<StaffAssignment> findTodayAssignments(@Param("today") LocalDate today);

    // Find active assignments for an employee
    @Query("SELECT sa FROM StaffAssignment sa WHERE sa.employee.id = :employeeId AND sa.status IN ('ASSIGNED', 'WORKING')")
    List<StaffAssignment> findActiveAssignmentsByEmployee(@Param("employeeId") String employeeId);
}