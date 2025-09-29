package com.clothsphere.repository.HR;

import com.clothsphere.model.HR.Employee;
import com.clothsphere.model.HR.ProductionTask;
import com.clothsphere.model.HR.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, String> {

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

}