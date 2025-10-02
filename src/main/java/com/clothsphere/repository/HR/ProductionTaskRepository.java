package com.clothsphere.repository.HR;

import com.clothsphere.model.HR.Department;
import com.clothsphere.model.HR.ProductionTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductionTaskRepository extends JpaRepository<ProductionTask, String> {

    @Query("SELECT p FROM ProductionTask p WHERE p.department = :department ORDER BY p.priority, p.deadline")
    List<ProductionTask> findByDepartmentOrderByPriorityAndDeadline(@Param("department") Department department);

    @Query("SELECT p FROM ProductionTask p WHERE p.priority = :priority ORDER BY p.deadline")
    List<ProductionTask> findByPriorityOrderByDeadline(@Param("priority") String priority);

    @Query("SELECT p FROM ProductionTask p WHERE p.status = :status ORDER BY p.priority, p.deadline")
    List<ProductionTask> findByStatusOrderByPriorityAndDeadline(@Param("status") String status);

    @Query("SELECT p FROM ProductionTask p WHERE p.deadline < :date AND p.status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY p.priority, p.deadline")
    List<ProductionTask> findOverdueTasks(@Param("date") LocalDate date);

    @Query("SELECT p FROM ProductionTask p WHERE p.deadline BETWEEN :startDate AND :endDate ORDER BY p.priority, p.deadline")
    List<ProductionTask> findTasksByDeadlineRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(p) FROM ProductionTask p WHERE p.department = :department")
    Long countByDepartment(@Param("department") Department department);

    @Query("SELECT COUNT(p) FROM ProductionTask p WHERE p.status = :status")
    Long countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(p) FROM ProductionTask p WHERE p.priority = :priority")
    Long countByPriority(@Param("priority") String priority);

    @Query("SELECT COUNT(p) FROM ProductionTask p WHERE p.department = :department AND p.status = :status")
    Long countByDepartmentAndStatus(@Param("department") Department department, @Param("status") String status);

    @Query("SELECT p.taskId FROM ProductionTask p ORDER BY p.taskId DESC")
    List<String> findAllTaskIds();

    @Query("SELECT p FROM ProductionTask p ORDER BY p.createdDate DESC, p.priority")
    List<ProductionTask> findAllOrderByCreatedDateDesc();

    @Query("SELECT p FROM ProductionTask p WHERE p.department.id = :departmentId ORDER BY p.priority, p.deadline")
    List<ProductionTask> findByDepartmentIdOrderByPriorityAndDeadline(@Param("departmentId") String departmentId);

    @Query("SELECT p FROM ProductionTask p WHERE LOWER(p.taskName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY p.priority, p.deadline")
    List<ProductionTask> findByTaskNameOrDescriptionContainingIgnoreCase(@Param("keyword") String keyword);
}