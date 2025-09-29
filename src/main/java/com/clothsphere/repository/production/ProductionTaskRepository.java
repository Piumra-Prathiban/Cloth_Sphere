package com.clothsphere.repository.production;

import com.clothsphere.model.production.ProductionTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductionTaskRepository extends JpaRepository<ProductionTask, Long> {

    List<ProductionTask> findByScheduleId(Long scheduleId);

    List<ProductionTask> findByUrgentTrueOrderByDueDateAsc();

    List<ProductionTask> findTop10ByOrderByDueDateAsc();

    List<ProductionTask> findByScheduleIdAndDueDateBetween(Long scheduleId, LocalDate start, LocalDate end);
}
