package com.clothsphere.repository;

import com.clothsphere.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {

    @Query("SELECT d FROM Department d WHERE d.departmentName = :departmentName")
    Department findByDepartmentName(@Param("departmentName") String departmentName);

    @Query("SELECT d FROM Department d ORDER BY d.departmentName")
    List<Department> findAllOrderedByName();

    @Query("SELECT COUNT(d) FROM Department d")
    Long countAllDepartments();

    @Query("SELECT d.id FROM Department d ORDER BY d.id DESC")
    List<String> findAllDepartmentIds();
}