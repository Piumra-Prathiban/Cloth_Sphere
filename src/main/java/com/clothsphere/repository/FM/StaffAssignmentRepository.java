package com.clothsphere.repository.FM;

import com.clothsphere.model.HR.Employee;
import com.clothsphere.model.FM.StaffAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StaffAssignmentRepository extends JpaRepository<StaffAssignment, String> {

    // ===================== MANUAL INSERT/UPDATE/DELETE =====================

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO staff_assignment (assignment_id, schedule_id, employee_id, workstation_id, " +
            "assignment_date, shift, role, status, assigned_quantity, completed_quantity, " +
            "start_time, end_time, created_at, updated_at, notes) VALUES (:assignmentId, :scheduleId, " +
            ":employeeId, :workstationId, :assignmentDate, :shift, :role, :status, :assignedQuantity, " +
            ":completedQuantity, :startTime, :endTime, NOW(), NOW(), :notes)", nativeQuery = true)
    void insertStaffAssignment(@Param("assignmentId") String assignmentId,
                               @Param("scheduleId") String scheduleId,
                               @Param("employeeId") String employeeId,
                               @Param("workstationId") String workstationId,
                               @Param("assignmentDate") LocalDate assignmentDate,
                               @Param("shift") String shift,
                               @Param("role") String role,
                               @Param("status") String status,
                               @Param("assignedQuantity") Integer assignedQuantity,
                               @Param("completedQuantity") Integer completedQuantity,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime,
                               @Param("notes") String notes);

    @Modifying
    @Transactional
    @Query(value = "UPDATE staff_assignment SET schedule_id = :scheduleId, employee_id = :employeeId, " +
            "workstation_id = :workstationId, assignment_date = :assignmentDate, shift = :shift, " +
            "role = :role, status = :status, assigned_quantity = :assignedQuantity, " +
            "completed_quantity = :completedQuantity, start_time = :startTime, end_time = :endTime, " +
            "updated_at = NOW(), notes = :notes WHERE assignment_id = :assignmentId", nativeQuery = true)
    int updateStaffAssignment(@Param("assignmentId") String assignmentId,
                              @Param("scheduleId") String scheduleId,
                              @Param("employeeId") String employeeId,
                              @Param("workstationId") String workstationId,
                              @Param("assignmentDate") LocalDate assignmentDate,
                              @Param("shift") String shift,
                              @Param("role") String role,
                              @Param("status") String status,
                              @Param("assignedQuantity") Integer assignedQuantity,
                              @Param("completedQuantity") Integer completedQuantity,
                              @Param("startTime") LocalDateTime startTime,
                              @Param("endTime") LocalDateTime endTime,
                              @Param("notes") String notes);

    @Modifying
    @Transactional
    @Query(value = "UPDATE staff_assignment SET status = :status, updated_at = NOW() WHERE assignment_id = :assignmentId", nativeQuery = true)
    int updateAssignmentStatus(@Param("assignmentId") String assignmentId, @Param("status") String status);

    @Modifying
    @Transactional
    @Query(value = "UPDATE staff_assignment SET completed_quantity = :completedQuantity, updated_at = NOW() WHERE assignment_id = :assignmentId", nativeQuery = true)
    int updateAssignmentCompletedQuantity(@Param("assignmentId") String assignmentId, @Param("completedQuantity") Integer completedQuantity);

    @Modifying
    @Transactional
    @Query(value = "UPDATE staff_assignment SET start_time = :startTime, end_time = :endTime, updated_at = NOW() WHERE assignment_id = :assignmentId", nativeQuery = true)
    int updateAssignmentTimes(@Param("assignmentId") String assignmentId,
                              @Param("startTime") LocalDateTime startTime,
                              @Param("endTime") LocalDateTime endTime);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM staff_assignment WHERE assignment_id = :assignmentId", nativeQuery = true)
    int deleteStaffAssignment(@Param("assignmentId") String assignmentId);

    // ===================== EXISTING QUERY METHODS =====================

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