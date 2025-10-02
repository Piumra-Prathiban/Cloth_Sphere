package com.clothsphere.repository.Production;

import com.clothsphere.model.Production.ProductionSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductionScheduleRepository extends JpaRepository<ProductionSchedule, String> {

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