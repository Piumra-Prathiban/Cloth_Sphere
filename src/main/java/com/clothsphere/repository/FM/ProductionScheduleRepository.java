package com.clothsphere.repository.FM;

import com.clothsphere.model.FM.ProductionSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductionScheduleRepository extends JpaRepository<ProductionSchedule, String> {

    // ===================== MANUAL INSERT/UPDATE/DELETE =====================

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO production_schedule (schedule_id, order_id, workstation_id, scheduled_date, " +
            "start_time, end_time, estimated_hours, actual_hours, assigned_quantity, completed_quantity, " +
            "status, shift, created_at, updated_at, notes) VALUES (:scheduleId, :orderId, :workstationId, " +
            ":scheduledDate, :startTime, :endTime, :estimatedHours, :actualHours, :assignedQuantity, " +
            ":completedQuantity, :status, :shift, NOW(), NOW(), :notes)", nativeQuery = true)
    void insertProductionSchedule(@Param("scheduleId") String scheduleId,
                                  @Param("orderId") String orderId,
                                  @Param("workstationId") String workstationId,
                                  @Param("scheduledDate") LocalDate scheduledDate,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime,
                                  @Param("estimatedHours") Double estimatedHours,
                                  @Param("actualHours") Double actualHours,
                                  @Param("assignedQuantity") Integer assignedQuantity,
                                  @Param("completedQuantity") Integer completedQuantity,
                                  @Param("status") String status,
                                  @Param("shift") String shift,
                                  @Param("notes") String notes);

    @Modifying
    @Transactional
    @Query(value = "UPDATE production_schedule SET order_id = :orderId, workstation_id = :workstationId, " +
            "scheduled_date = :scheduledDate, start_time = :startTime, end_time = :endTime, " +
            "estimated_hours = :estimatedHours, actual_hours = :actualHours, assigned_quantity = :assignedQuantity, " +
            "completed_quantity = :completedQuantity, status = :status, shift = :shift, " +
            "updated_at = NOW(), notes = :notes WHERE schedule_id = :scheduleId", nativeQuery = true)
    int updateProductionSchedule(@Param("scheduleId") String scheduleId,
                                 @Param("orderId") String orderId,
                                 @Param("workstationId") String workstationId,
                                 @Param("scheduledDate") LocalDate scheduledDate,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime,
                                 @Param("estimatedHours") Double estimatedHours,
                                 @Param("actualHours") Double actualHours,
                                 @Param("assignedQuantity") Integer assignedQuantity,
                                 @Param("completedQuantity") Integer completedQuantity,
                                 @Param("status") String status,
                                 @Param("shift") String shift,
                                 @Param("notes") String notes);

    @Modifying
    @Transactional
    @Query(value = "UPDATE production_schedule SET status = :status, updated_at = NOW() WHERE schedule_id = :scheduleId", nativeQuery = true)
    int updateScheduleStatus(@Param("scheduleId") String scheduleId, @Param("status") String status);

    @Modifying
    @Transactional
    @Query(value = "UPDATE production_schedule SET completed_quantity = :completedQuantity, updated_at = NOW() WHERE schedule_id = :scheduleId", nativeQuery = true)
    int updateScheduleCompletedQuantity(@Param("scheduleId") String scheduleId, @Param("completedQuantity") Integer completedQuantity);

    @Modifying
    @Transactional
    @Query(value = "UPDATE production_schedule SET actual_hours = :actualHours, updated_at = NOW() WHERE schedule_id = :scheduleId", nativeQuery = true)
    int updateActualHours(@Param("scheduleId") String scheduleId, @Param("actualHours") Double actualHours);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM production_schedule WHERE schedule_id = :scheduleId", nativeQuery = true)
    int deleteProductionSchedule(@Param("scheduleId") String scheduleId);

    // ===================== EXISTING QUERY METHODS =====================

    // Find schedules by order ID
    List<ProductionSchedule> findByOrderId(String orderId);

    // Find schedules by workstation
    List<ProductionSchedule> findByWorkstationId(String workstationId);

    // Find schedules by status
    List<ProductionSchedule> findByStatus(String status);

    // Find schedules for a specific date
    List<ProductionSchedule> findByScheduledDate(LocalDate date);

    // Find schedules by shift
    List<ProductionSchedule> findByShift(String shift);

    // Find schedules within date range
    @Query("SELECT ps FROM ProductionSchedule ps WHERE ps.scheduledDate BETWEEN :startDate AND :endDate")
    List<ProductionSchedule> findSchedulesByDateRange(@Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    // Find schedules by workstation and date
    @Query("SELECT ps FROM ProductionSchedule ps WHERE ps.workstationId = :workstationId AND ps.scheduledDate = :date")
    List<ProductionSchedule> findByWorkstationAndDate(@Param("workstationId") String workstationId,
                                                      @Param("date") LocalDate date);

    // Count schedules by status
    @Query("SELECT COUNT(ps) FROM ProductionSchedule ps WHERE ps.status = :status")
    Long countByStatus(@Param("status") String status);

    // Get all schedule IDs for ID generation
    @Query("SELECT ps.scheduleId FROM ProductionSchedule ps")
    List<String> findAllScheduleIds();

    // Find today's schedules
    @Query("SELECT ps FROM ProductionSchedule ps WHERE ps.scheduledDate = :today")
    List<ProductionSchedule> findTodaySchedules(@Param("today") LocalDate today);

    // Find active schedules for a workstation
    @Query("SELECT ps FROM ProductionSchedule ps WHERE ps.workstationId = :workstationId AND ps.status IN ('SCHEDULED', 'IN_PROGRESS')")
    List<ProductionSchedule> findActiveSchedulesByWorkstation(@Param("workstationId") String workstationId);
}