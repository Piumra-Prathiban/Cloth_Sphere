package com.clothsphere.repository.Production;

import com.clothsphere.model.Production.PerformanceMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PerformanceMetricsRepository extends JpaRepository<PerformanceMetrics, String> {

    // ===================== MANUAL INSERT/UPDATE/DELETE =====================

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO performance_metrics (metric_id, record_date, workstation_id, employee_id, " +
            "order_id, schedule_id, target_quantity, actual_quantity, defect_quantity, efficiency_rate, " +
            "quality_rate, downtime_hours, working_hours, overtime_hours, delay_hours, remarks, " +
            "created_at, updated_at) VALUES (:metricId, :recordDate, :workstationId, :employeeId, " +
            ":orderId, :scheduleId, :targetQuantity, :actualQuantity, :defectQuantity, :efficiencyRate, " +
            ":qualityRate, :downtimeHours, :workingHours, :overtimeHours, :delayHours, :remarks, " +
            "NOW(), NOW())", nativeQuery = true)
    void insertPerformanceMetric(@Param("metricId") String metricId,
                                 @Param("recordDate") LocalDate recordDate,
                                 @Param("workstationId") String workstationId,
                                 @Param("employeeId") String employeeId,
                                 @Param("orderId") String orderId,
                                 @Param("scheduleId") String scheduleId,
                                 @Param("targetQuantity") Integer targetQuantity,
                                 @Param("actualQuantity") Integer actualQuantity,
                                 @Param("defectQuantity") Integer defectQuantity,
                                 @Param("efficiencyRate") Double efficiencyRate,
                                 @Param("qualityRate") Double qualityRate,
                                 @Param("downtimeHours") Double downtimeHours,
                                 @Param("workingHours") Double workingHours,
                                 @Param("overtimeHours") Double overtimeHours,
                                 @Param("delayHours") Double delayHours,
                                 @Param("remarks") String remarks);

    @Modifying
    @Transactional
    @Query(value = "UPDATE performance_metrics SET record_date = :recordDate, workstation_id = :workstationId, " +
            "employee_id = :employeeId, order_id = :orderId, schedule_id = :scheduleId, " +
            "target_quantity = :targetQuantity, actual_quantity = :actualQuantity, defect_quantity = :defectQuantity, " +
            "efficiency_rate = :efficiencyRate, quality_rate = :qualityRate, downtime_hours = :downtimeHours, " +
            "working_hours = :workingHours, overtime_hours = :overtimeHours, delay_hours = :delayHours, " +
            "remarks = :remarks, updated_at = NOW() WHERE metric_id = :metricId", nativeQuery = true)
    int updatePerformanceMetric(@Param("metricId") String metricId,
                                @Param("recordDate") LocalDate recordDate,
                                @Param("workstationId") String workstationId,
                                @Param("employeeId") String employeeId,
                                @Param("orderId") String orderId,
                                @Param("scheduleId") String scheduleId,
                                @Param("targetQuantity") Integer targetQuantity,
                                @Param("actualQuantity") Integer actualQuantity,
                                @Param("defectQuantity") Integer defectQuantity,
                                @Param("efficiencyRate") Double efficiencyRate,
                                @Param("qualityRate") Double qualityRate,
                                @Param("downtimeHours") Double downtimeHours,
                                @Param("workingHours") Double workingHours,
                                @Param("overtimeHours") Double overtimeHours,
                                @Param("delayHours") Double delayHours,
                                @Param("remarks") String remarks);

    @Modifying
    @Transactional
    @Query(value = "UPDATE performance_metrics SET actual_quantity = :actualQuantity, " +
            "efficiency_rate = (:actualQuantity * 100.0) / target_quantity, updated_at = NOW() " +
            "WHERE metric_id = :metricId", nativeQuery = true)
    int updateActualQuantityAndRecalculate(@Param("metricId") String metricId,
                                           @Param("actualQuantity") Integer actualQuantity);

    @Modifying
    @Transactional
    @Query(value = "UPDATE performance_metrics SET defect_quantity = :defectQuantity, " +
            "quality_rate = ((actual_quantity - :defectQuantity) * 100.0) / actual_quantity, " +
            "updated_at = NOW() WHERE metric_id = :metricId", nativeQuery = true)
    int updateDefectsAndRecalculate(@Param("metricId") String metricId,
                                    @Param("defectQuantity") Integer defectQuantity);

    @Modifying
    @Transactional
    @Query(value = "UPDATE performance_metrics SET downtime_hours = :downtimeHours, " +
            "overtime_hours = :overtimeHours, delay_hours = :delayHours, updated_at = NOW() " +
            "WHERE metric_id = :metricId", nativeQuery = true)
    int updateTimeMetrics(@Param("metricId") String metricId,
                          @Param("downtimeHours") Double downtimeHours,
                          @Param("overtimeHours") Double overtimeHours,
                          @Param("delayHours") Double delayHours);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM performance_metrics WHERE metric_id = :metricId", nativeQuery = true)
    int deletePerformanceMetric(@Param("metricId") String metricId);

    // ===================== EXISTING QUERY METHODS =====================

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