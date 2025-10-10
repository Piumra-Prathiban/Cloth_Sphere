package com.clothsphere.repository.SOM;

import com.clothsphere.model.SOM.SummaryReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SummaryReportRepository extends JpaRepository<SummaryReport, Long> {

    // Manual INSERT query for creating new summary report
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO summary_reports (report_name, report_type, period_start, period_end, " +
            "total_orders, total_revenue, total_customers, average_order_value, success_rate, " +
            "report_data, generated_by, generated_at, file_path) " +
            "VALUES (:reportName, :reportType, :periodStart, :periodEnd, :totalOrders, " +
            ":totalRevenue, :totalCustomers, :averageOrderValue, :successRate, " +
            ":reportData, :generatedBy, :generatedAt, :filePath)",
            nativeQuery = true)
    int insertSummaryReport(@Param("reportName") String reportName,
                            @Param("reportType") String reportType,
                            @Param("periodStart") LocalDateTime periodStart,
                            @Param("periodEnd") LocalDateTime periodEnd,
                            @Param("totalOrders") Integer totalOrders,
                            @Param("totalRevenue") Double totalRevenue,
                            @Param("totalCustomers") Integer totalCustomers,
                            @Param("averageOrderValue") Double averageOrderValue,
                            @Param("successRate") Double successRate,
                            @Param("reportData") String reportData,
                            @Param("generatedBy") String generatedBy,
                            @Param("generatedAt") LocalDateTime generatedAt,
                            @Param("filePath") String filePath);

    // Manual UPDATE query for updating report data
    @Modifying
    @Transactional
    @Query(value = "UPDATE summary_reports SET report_data = :reportData, " +
            "total_orders = :totalOrders, total_revenue = :totalRevenue, " +
            "total_customers = :totalCustomers, average_order_value = :averageOrderValue, " +
            "success_rate = :successRate WHERE id = :id",
            nativeQuery = true)
    int updateReportData(@Param("id") Long id,
                         @Param("reportData") String reportData,
                         @Param("totalOrders") Integer totalOrders,
                         @Param("totalRevenue") Double totalRevenue,
                         @Param("totalCustomers") Integer totalCustomers,
                         @Param("averageOrderValue") Double averageOrderValue,
                         @Param("successRate") Double successRate);

    // Manual UPDATE query for updating file path
    @Modifying
    @Transactional
    @Query(value = "UPDATE summary_reports SET file_path = :filePath WHERE id = :id",
            nativeQuery = true)
    int updateReportFilePath(@Param("id") Long id,
                             @Param("filePath") String filePath);

    // Manual DELETE query
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM summary_reports WHERE id = :id", nativeQuery = true)
    int deleteSummaryReport(@Param("id") Long id);

    // Manual SELECT queries
    @Query(value = "SELECT * FROM summary_reports WHERE report_type = :reportType " +
            "ORDER BY generated_at DESC", nativeQuery = true)
    List<SummaryReport> findByReportTypeOrderByGeneratedAtDesc(@Param("reportType") String reportType);

    @Query(value = "SELECT * FROM summary_reports WHERE generated_at BETWEEN :startDate AND :endDate " +
            "ORDER BY generated_at DESC", nativeQuery = true)
    List<SummaryReport> findReportsByDateRange(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT * FROM summary_reports WHERE report_type = :reportType " +
            "ORDER BY generated_at DESC LIMIT 1", nativeQuery = true)
    Optional<SummaryReport> findTopByReportTypeOrderByGeneratedAtDesc(@Param("reportType") String reportType);

    @Query(value = "SELECT * FROM summary_reports WHERE generated_by = :generatedBy " +
            "ORDER BY generated_at DESC", nativeQuery = true)
    List<SummaryReport> findByGeneratedByOrderByGeneratedAtDesc(@Param("generatedBy") String generatedBy);

    @Query(value = "SELECT * FROM summary_reports WHERE period_start >= :startDate " +
            "AND period_end <= :endDate ORDER BY generated_at DESC", nativeQuery = true)
    List<SummaryReport> findReportsByPeriod(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    // Manual query to check if report exists
    @Query(value = "SELECT COUNT(*) FROM summary_reports WHERE id = :id", nativeQuery = true)
    int checkReportExists(@Param("id") Long id);

    // Manual query to get latest report ID
    @Query(value = "SELECT MAX(id) FROM summary_reports", nativeQuery = true)
    Long findMaxReportId();

    // Manual query to delete old reports
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM summary_reports WHERE generated_at < :cutoffDate",
            nativeQuery = true)
    int deleteOldReports(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Manual query to get report count by type
    @Query(value = "SELECT COUNT(*) FROM summary_reports WHERE report_type = :reportType",
            nativeQuery = true)
    int countReportsByType(@Param("reportType") String reportType);

    // Manual query to get all reports
    @Query(value = "SELECT * FROM summary_reports ORDER BY generated_at DESC",
            nativeQuery = true)
    List<SummaryReport> findAllOrderByGeneratedAtDesc();
}