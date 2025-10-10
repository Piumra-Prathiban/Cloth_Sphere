package com.clothsphere.repository.HR;

import com.clothsphere.model.HR.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {

    // Custom query to find department by name
    @Query("SELECT d FROM Department d WHERE d.departmentName = :departmentName")
    Department findByDepartmentName(@Param("departmentName") String departmentName);

    // Custom query to find all departments ordered by name
    @Query("SELECT d FROM Department d ORDER BY d.departmentName")
    List<Department> findAllOrderedByName();

    // Custom query to count all departments
    @Query("SELECT COUNT(d) FROM Department d")
    Long countAllDepartments();

    // Custom query to get all department IDs
    @Query("SELECT d.id FROM Department d ORDER BY d.id DESC")
    List<String> findAllDepartmentIds();

    // Custom query to find department by ID (replacing automatic findById)
    @Query("SELECT d FROM Department d WHERE d.id = :id")
    Optional<Department> findDepartmentById(@Param("id") String id);

    // Custom query to check if department exists by ID (replacing automatic existsById)
    @Query("SELECT COUNT(d) > 0 FROM Department d WHERE d.id = :id")
    boolean existsDepartmentById(@Param("id") String id);

    // Custom INSERT query for creating new department
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO department (id, department_name, description, salary_budget, manager_id) " +
            "VALUES (:id, :departmentName, :description, :salaryBudget, :managerId)",
            nativeQuery = true)
    int insertDepartment(@Param("id") String id,
                         @Param("departmentName") String departmentName,
                         @Param("description") String description,
                         @Param("salaryBudget") Double salaryBudget,
                         @Param("managerId") String managerId);

    // Custom UPDATE query for updating department
    @Modifying
    @Transactional
    @Query("UPDATE Department d SET d.departmentName = :departmentName, " +
            "d.description = :description, " +
            "d.salaryBudget = :salaryBudget, " +
            "d.managerId = :managerId " +
            "WHERE d.id = :id")
    int updateDepartment(@Param("id") String id,
                         @Param("departmentName") String departmentName,
                         @Param("description") String description,
                         @Param("salaryBudget") Double salaryBudget,
                         @Param("managerId") String managerId);

    // Custom DELETE query for deleting department by ID
    @Modifying
    @Transactional
    @Query("DELETE FROM Department d WHERE d.id = :id")
    int deleteDepartmentById(@Param("id") String id);

    // Custom query to find all departments (replacing automatic findAll)
    @Query("SELECT d FROM Department d")
    List<Department> findAllDepartments();

    // Custom query to find departments by manager ID
    @Query("SELECT d FROM Department d WHERE d.managerId = :managerId")
    List<Department> findDepartmentsByManagerId(@Param("managerId") String managerId);

    // Custom query to get department count by manager ID
    @Query("SELECT COUNT(d) FROM Department d WHERE d.managerId = :managerId")
    Long countDepartmentsByManagerId(@Param("managerId") String managerId);

    // Custom query to check if department name exists excluding specific department
    @Query("SELECT COUNT(d) > 0 FROM Department d WHERE d.departmentName = :departmentName AND d.id != :excludeId")
    boolean existsByDepartmentNameExcludingId(@Param("departmentName") String departmentName,
                                              @Param("excludeId") String excludeId);

    // Custom query to check if department name exists (any department)
    @Query("SELECT COUNT(d) > 0 FROM Department d WHERE d.departmentName = :departmentName")
    boolean existsByDepartmentName(@Param("departmentName") String departmentName);
}