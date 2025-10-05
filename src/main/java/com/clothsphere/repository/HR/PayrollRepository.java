// PayrollRepository.java
package com.clothsphere.repository.HR;

import com.clothsphere.model.HR.Payroll;
import com.clothsphere.model.HR.PayrollId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, PayrollId> {

    // Manual INSERT query
    @Modifying
    @Query(value = "INSERT INTO payroll (" +
            "employee_id, payroll_month, basic_salary, actual_work_hours, max_work_hours, " +
            "ot_rate, ot_hours, ot_amount, gross_salary, deductions, net_salary, " +
            "attendance_rate, status, created_at, updated_at" +
            ") VALUES (" +
            ":employeeId, :payrollMonth, :basicSalary, :actualWorkHours, :maxWorkHours, " +
            ":otRate, :otHours, :otAmount, :grossSalary, :deductions, :netSalary, " +
            ":attendanceRate, :status, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP" +
            ")", nativeQuery = true)
    void insertPayroll(@Param("employeeId") String employeeId,
                       @Param("payrollMonth") String payrollMonth,
                       @Param("basicSalary") Double basicSalary,
                       @Param("actualWorkHours") Double actualWorkHours,
                       @Param("maxWorkHours") Double maxWorkHours,
                       @Param("otRate") Double otRate,
                       @Param("otHours") Double otHours,
                       @Param("otAmount") Double otAmount,
                       @Param("grossSalary") Double grossSalary,
                       @Param("deductions") Double deductions,
                       @Param("netSalary") Double netSalary,
                       @Param("attendanceRate") Double attendanceRate,
                       @Param("status") String status);

    // Manual UPDATE query
    @Modifying
    @Query(value = "UPDATE payroll SET " +
            "basic_salary = :basicSalary, " +
            "actual_work_hours = :actualWorkHours, " +
            "max_work_hours = :maxWorkHours, " +
            "ot_rate = :otRate, " +
            "ot_hours = :otHours, " +
            "ot_amount = :otAmount, " +
            "gross_salary = :grossSalary, " +
            "deductions = :deductions, " +
            "net_salary = :netSalary, " +
            "attendance_rate = :attendanceRate, " +
            "status = :status, " +
            "updated_at = CURRENT_TIMESTAMP " +
            "WHERE employee_id = :employeeId AND payroll_month = :payrollMonth", nativeQuery = true)
    void updatePayroll(@Param("employeeId") String employeeId,
                       @Param("payrollMonth") String payrollMonth,
                       @Param("basicSalary") Double basicSalary,
                       @Param("actualWorkHours") Double actualWorkHours,
                       @Param("maxWorkHours") Double maxWorkHours,
                       @Param("otRate") Double otRate,
                       @Param("otHours") Double otHours,
                       @Param("otAmount") Double otAmount,
                       @Param("grossSalary") Double grossSalary,
                       @Param("deductions") Double deductions,
                       @Param("netSalary") Double netSalary,
                       @Param("attendanceRate") Double attendanceRate,
                       @Param("status") String status);

    // Manual DELETE query
    @Modifying
    @Query(value = "DELETE FROM payroll WHERE employee_id = :employeeId AND payroll_month = :payrollMonth", nativeQuery = true)
    void deletePayroll(@Param("employeeId") String employeeId,
                       @Param("payrollMonth") String payrollMonth);

    // Find by employee and month
    @Query(value = "SELECT * FROM payroll WHERE employee_id = :employeeId AND payroll_month = :payrollMonth", nativeQuery = true)
    Optional<Payroll> findByEmployeeAndMonth(@Param("employeeId") String employeeId,
                                             @Param("payrollMonth") String payrollMonth);

    // Find all payrolls for a specific month
    @Query(value = "SELECT * FROM payroll WHERE payroll_month = :payrollMonth ORDER BY employee_id", nativeQuery = true)
    List<Payroll> findAllByMonth(@Param("payrollMonth") String payrollMonth);

    // Find all payrolls for an employee
    @Query(value = "SELECT * FROM payroll WHERE employee_id = :employeeId ORDER BY payroll_month DESC", nativeQuery = true)
    List<Payroll> findAllByEmployee(@Param("employeeId") String employeeId);

    // Find payrolls by status
    @Query(value = "SELECT * FROM payroll WHERE status = :status AND payroll_month = :payrollMonth ORDER BY employee_id", nativeQuery = true)
    List<Payroll> findByStatusAndMonth(@Param("status") String status,
                                       @Param("payrollMonth") String payrollMonth);

    // Update payroll status
    @Modifying
    @Query(value = "UPDATE payroll SET status = :status, updated_at = CURRENT_TIMESTAMP " +
            "WHERE employee_id = :employeeId AND payroll_month = :payrollMonth", nativeQuery = true)
    void updatePayrollStatus(@Param("employeeId") String employeeId,
                             @Param("payrollMonth") String payrollMonth,
                             @Param("status") String status);
}