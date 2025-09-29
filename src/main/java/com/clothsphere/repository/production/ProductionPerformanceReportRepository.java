package com.clothsphere.repository.production;

import com.clothsphere.model.production.ProductionPerformanceReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionPerformanceReportRepository extends JpaRepository<ProductionPerformanceReport, Long> {

    List<ProductionPerformanceReport> findTop6ByOrderByGeneratedAtDesc();

    List<ProductionPerformanceReport> findByScheduleIdOrderByGeneratedAtDesc(Long scheduleId);
}
