package com.clothsphere.repository.HR;

import com.clothsphere.model.HR.Department;
import com.clothsphere.model.HR.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    @Query("SELECT e FROM Employee e WHERE e.username = :username")
    Employee findByUsername(@Param("username") String username);

    @Query("SELECT e FROM Employee e WHERE e.email = :email")
    Employee findByEmail(@Param("email") String email);

    @Query("SELECT e FROM Employee e WHERE e.department = :department")
    List<Employee> findByDepartment(@Param("department") Department department);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department = :department")
    Long countByDepartment(@Param("department") Department department);

    @Query("SELECT e.id FROM Employee e ORDER BY e.id")
    List<String> findAllEmployeeIds();


}