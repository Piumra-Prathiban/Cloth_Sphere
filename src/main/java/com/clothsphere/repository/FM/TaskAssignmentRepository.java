package com.clothsphere.repository.FM;

import com.clothsphere.model.HR.Employee;
import com.clothsphere.model.FM.ProductionTask;
import com.clothsphere.model.FM.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, String> {

    // Custom INSERT query
    @Modifying
    @Query(value = "INSERT INTO task_assignment (assignment_id, employee_id, task_id, department_id, assigned_date, estimated_hours, actual_hours, status, notes, completion_date) " +  // Note: singular 'task_assignment'
            "VALUES (:#{#assignment.assignmentId}, :#{#assignment.employee.id}, :#{#assignment.task.taskId}, " +
            ":#{#assignment.department.id}, :#{#assignment.assignedDate}, :#{#assignment.estimatedHours}, " +
            ":#{#assignment.actualHours}, :#{#assignment.status}, :#{#assignment.notes}, :#{#assignment.completionDate})",
            nativeQuery = true)
    int insertAssignment(@Param("assignment") TaskAssignment assignment);

    // Custom UPDATE query
    @Modifying
    @Query(value = "UPDATE task_assignment SET " +
            "employee_id = COALESCE(:#{#assignment.employee.id}, employee_id), " +
            "task_id = COALESCE(:#{#assignment.task.taskId}, task_id), " +
            "department_id = COALESCE(:#{#assignment.department.id}, department_id), " +
            "estimated_hours = COALESCE(:#{#assignment.estimatedHours}, estimated_hours), " +
            "actual_hours = COALESCE(:#{#assignment.actualHours}, actual_hours), " +
            "status = COALESCE(:#{#assignment.status}, status), " +
            "notes = COALESCE(:#{#assignment.notes}, notes), " +
            "completion_date = COALESCE(:#{#assignment.completionDate}, completion_date) " +
            "WHERE assignment_id = :#{#assignment.assignmentId}",
            nativeQuery = true)
    int updateAssignment(@Param("assignment") TaskAssignment assignment);

    // Custom DELETE query
    @Modifying
    @Query(value = "DELETE FROM task_assignment WHERE assignment_id = :assignmentId", nativeQuery = true)
    void deleteByAssignmentId(@Param("assignmentId") String assignmentId);

    // Custom SELECT by ID query
    @Query(value = "SELECT * FROM task_assignment WHERE assignment_id = :assignmentId", nativeQuery = true)
    Optional<TaskAssignment> findById(@Param("assignmentId") String assignmentId);

    // Custom EXISTS query
    @Query(value = "SELECT COUNT(*) > 0 FROM task_assignment WHERE assignment_id = :assignmentId", nativeQuery = true)
    boolean existsById(@Param("assignmentId") String assignmentId);

    // Custom SELECT ALL query
    @Query(value = "SELECT * FROM task_assignment ORDER BY assigned_date DESC", nativeQuery = true)
    List<TaskAssignment> findAll();

    // Custom COUNT query
    @Query(value = "SELECT COUNT(*) FROM task_assignment", nativeQuery = true)
    Long countAllAssignments();

    // Your existing custom queries (keeping them as they are manually created)
    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.employee = :employee ORDER BY ta.assignedDate DESC")
    List<TaskAssignment> findByEmployee(@Param("employee") Employee employee);

    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.task = :task ORDER BY ta.assignedDate")
    List<TaskAssignment> findByTask(@Param("task") ProductionTask task);

    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.employee.id = :employeeId ORDER BY ta.assignedDate DESC")
    List<TaskAssignment> findByEmployeeId(@Param("employeeId") String employeeId);

    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.task.taskId = :taskId ORDER BY ta.assignedDate")
    List<TaskAssignment> findByTaskId(@Param("taskId") String taskId);

    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.status = :status ORDER BY ta.task.priority, ta.task.deadline")
    List<TaskAssignment> findByStatus(@Param("status") String status);

    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.employee.department.id = :departmentId ORDER BY ta.task.priority, ta.task.deadline")
    List<TaskAssignment> findByDepartmentId(@Param("departmentId") String departmentId);

    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.employee = :employee AND ta.status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY ta.task.priority, ta.task.deadline")
    List<TaskAssignment> findActiveAssignmentsByEmployee(@Param("employee") Employee employee);

    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.employee.id = :employeeId AND ta.status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY ta.task.priority, ta.task.deadline")
    List<TaskAssignment> findActiveAssignmentsByEmployeeId(@Param("employeeId") String employeeId);

    @Query("SELECT COUNT(ta) FROM TaskAssignment ta WHERE ta.employee = :employee AND ta.status NOT IN ('COMPLETED', 'CANCELLED')")
    Long countActiveAssignmentsByEmployee(@Param("employee") Employee employee);

    @Query("SELECT COUNT(ta) FROM TaskAssignment ta WHERE ta.task = :task AND ta.status NOT IN ('COMPLETED', 'CANCELLED')")
    Long countActiveAssignmentsByTask(@Param("task") ProductionTask task);

    @Query("SELECT ta.assignmentId FROM TaskAssignment ta ORDER BY ta.assignmentId DESC")
    List<String> findAllAssignmentIds();

    @Query("SELECT ta FROM TaskAssignment ta LEFT JOIN FETCH ta.employee e LEFT JOIN FETCH e.department LEFT JOIN FETCH ta.task t LEFT JOIN FETCH t.department ORDER BY ta.assignedDate DESC")
    List<TaskAssignment> findAllWithDetails();

    @Query("SELECT ta FROM TaskAssignment ta " +
            "LEFT JOIN FETCH ta.employee e " +
            "LEFT JOIN FETCH ta.task t " +
            "LEFT JOIN FETCH ta.department d " +
            "ORDER BY ta.assignedDate DESC")
    List<TaskAssignment> findAllWithDepartmentDetails();

    // Additional manual queries for statistics
    @Query(value = "SELECT COUNT(*) FROM task_assignment WHERE status = :status", nativeQuery = true)
    Long countByStatus(@Param("status") String status);

    // Custom update for status only
    @Modifying
    @Query(value = "UPDATE task_assignment SET status = :status, completion_date = :completionDate, actual_hours = :actualHours WHERE assignment_id = :assignmentId", nativeQuery = true)
    int updateAssignmentStatus(@Param("assignmentId") String assignmentId,
                               @Param("status") String status,
                               @Param("completionDate") java.time.LocalDate completionDate,
                               @Param("actualHours") Integer actualHours);
}