package com.clothsphere.repository.Production;

import com.clothsphere.model.Production.PerformanceMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PerformanceMetricsRepository extends JpaRepository<PerformanceMetrics, String> {

    // Find metrics by workstation
    List<PerformanceMetrics> findByWorkstationId(String workstationId);

    // Find metrics by employee
    List<PerformanceMetrics> findByEmployeeId(String employeeId);

    // Find metrics by order
    List<PerformanceMetrics> findByOrderId(String orderId);

    // Find metrics by schedule
    List<PerformanceMetrics> findByScheduleId(String scheduleId);

    // Find metrics for a specific date
    List<PerformanceMetrics> findByRecordDate(LocalDate date);

    // Find metrics within date range
    @Query("SELECT pm FROM PerformanceMetrics pm WHERE pm.recordDate BETWEEN :startDate AND :endDate")
    List<PerformanceMetrics> findMetricsByDateRange(@Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    // Find metrics by employee and date range
    @Query("SELECT pm FROM PerformanceMetrics pm WHERE pm.employeeId = :employeeId AND pm.recordDate BETWEEN :startDate AND :endDate")
    List<PerformanceMetrics> findByEmployeeAndDateRange(@Param("employeeId") String employeeId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    // Find metrics by workstation and date range
    @Query("SELECT pm FROM PerformanceMetrics pm WHERE pm.workstationId = :workstationId AND pm.recordDate BETWEEN :startDate AND :endDate")
    List<PerformanceMetrics> findByWorkstationAndDateRange(@Param("workstationId") String workstationId,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);

    // Calculate average efficiency by workstation
    @Query("SELECT AVG(pm.efficiencyRate) FROM PerformanceMetrics pm WHERE pm.workstationId = :workstationId")
    Double calculateAverageEfficiencyByWorkstation(@Param("workstationId") String workstationId);

    // Calculate average quality rate by employee
    @Query("SELECT AVG(pm.qualityRate) FROM PerformanceMetrics pm WHERE pm.employeeId = :employeeId")
    Double calculateAverageQualityByEmployee(@Param("employeeId") String employeeId);

    // Get all metric IDs for ID generation
    @Query("SELECT pm.metricId FROM PerformanceMetrics pm")
    List<String> findAllMetricIds();

    // Find low-performance metrics (efficiency < threshold)
    @Query("SELECT pm FROM PerformanceMetrics pm WHERE pm.efficiencyRate < :threshold")
    List<PerformanceMetrics> findLowPerformanceMetrics(@Param("threshold") Double threshold);

    // Find today's metrics
    @Query("SELECT pm FROM PerformanceMetrics pm WHERE pm.recordDate = :today")
    List<PerformanceMetrics> findTodayMetrics(@Param("today") LocalDate today);
}