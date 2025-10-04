package com.clothsphere.repository.HR;

import com.clothsphere.model.HR.Department;
import com.clothsphere.model.HR.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    // Custom query to find employee by username
    @Query("SELECT e FROM Employee e WHERE e.username = :username")
    Employee findByUsername(@Param("username") String username);

    // Custom query to find employee by email
    @Query("SELECT e FROM Employee e WHERE e.email = :email")
    Employee findByEmail(@Param("email") String email);

    // Custom query to find employees by department
    @Query("SELECT e FROM Employee e WHERE e.department = :department")
    List<Employee> findByDepartment(@Param("department") Department department);

    // Custom query to count employees by department
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department = :department")
    Long countByDepartment(@Param("department") Department department);

    // Custom query to get all employee IDs
    @Query("SELECT e.id FROM Employee e ORDER BY e.id")
    List<String> findAllEmployeeIds();

    // Custom INSERT query for creating new employee
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO employee (id, full_name, address, phone_number, email, date_of_birth, " +
            "qualification1, qualification2, qualification3, username, password, department_id) " +
            "VALUES (:id, :fullName, :address, :phoneNumber, :email, :dateOfBirth, " +
            ":qualification1, :qualification2, :qualification3, :username, :password, :departmentId)",
            nativeQuery = true)
    int insertEmployee(@Param("id") String id,
                       @Param("fullName") String fullName,
                       @Param("address") String address,
                       @Param("phoneNumber") String phoneNumber,
                       @Param("email") String email,
                       @Param("dateOfBirth") LocalDate dateOfBirth,
                       @Param("qualification1") String qualification1,
                       @Param("qualification2") String qualification2,
                       @Param("qualification3") String qualification3,
                       @Param("username") String username,
                       @Param("password") String password,
                       @Param("departmentId") String departmentId);

    // Custom UPDATE query for updating employee
    @Modifying
    @Transactional
    @Query("UPDATE Employee e SET e.fullName = :fullName, " +
            "e.address = :address, " +
            "e.phoneNumber = :phoneNumber, " +
            "e.email = :email, " +
            "e.dateOfBirth = :dateOfBirth, " +
            "e.qualification1 = :qualification1, " +
            "e.qualification2 = :qualification2, " +
            "e.qualification3 = :qualification3, " +
            "e.department = :department " +
            "WHERE e.id = :id")
    int updateEmployee(@Param("id") String id,
                       @Param("fullName") String fullName,
                       @Param("address") String address,
                       @Param("phoneNumber") String phoneNumber,
                       @Param("email") String email,
                       @Param("dateOfBirth") LocalDate dateOfBirth,
                       @Param("qualification1") String qualification1,
                       @Param("qualification2") String qualification2,
                       @Param("qualification3") String qualification3,
                       @Param("department") Department department);

    // Custom UPDATE query for updating employee password
    @Modifying
    @Transactional
    @Query("UPDATE Employee e SET e.password = :password WHERE e.id = :id")
    int updateEmployeePassword(@Param("id") String id, @Param("password") String password);

    // Custom DELETE query for deleting employee by ID
    @Modifying
    @Transactional
    @Query("DELETE FROM Employee e WHERE e.id = :id")
    int deleteEmployeeById(@Param("id") String id);

    // Custom query to find employee by ID (replacing automatic findById)
    @Query("SELECT e FROM Employee e WHERE e.id = :id")
    Optional<Employee> findEmployeeById(@Param("id") String id);

    // Custom query to check if employee exists by ID (replacing automatic existsById)
    @Query("SELECT COUNT(e) > 0 FROM Employee e WHERE e.id = :id")
    boolean existsEmployeeById(@Param("id") String id);

    // Custom query to get all employees (replacing automatic findAll)
    @Query("SELECT e FROM Employee e ORDER BY e.fullName")
    List<Employee> findAllEmployees();

    // Custom query to find employees by department ID
    @Query("SELECT e FROM Employee e WHERE e.department.id = :departmentId")
    List<Employee> findByDepartmentId(@Param("departmentId") String departmentId);

    // Custom query to count employees by department ID
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId")
    Long countByDepartmentId(@Param("departmentId") String departmentId);

    // Custom query to check if username exists excluding specific employee
    @Query("SELECT COUNT(e) > 0 FROM Employee e WHERE e.username = :username AND e.id != :excludeId")
    boolean existsByUsernameExcludingId(@Param("username") String username,
                                        @Param("excludeId") String excludeId);

    // Custom query to check if email exists excluding specific employee
    @Query("SELECT COUNT(e) > 0 FROM Employee e WHERE e.email = :email AND e.id != :excludeId")
    boolean existsByEmailExcludingId(@Param("email") String email,
                                     @Param("excludeId") String excludeId);

    // Custom query to check if username exists (any employee)
    @Query("SELECT COUNT(e) > 0 FROM Employee e WHERE e.username = :username")
    boolean existsByUsername(@Param("username") String username);

    // Custom query to check if email exists (any employee)
    @Query("SELECT COUNT(e) > 0 FROM Employee e WHERE e.email = :email")
    boolean existsByEmail(@Param("email") String email);


}