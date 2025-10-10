package com.clothsphere.service.SOM;

import com.clothsphere.model.SOM.Order;
import com.clothsphere.model.SOM.SummaryReport;
import com.clothsphere.repository.SOM.OrderRepository;
import com.clothsphere.repository.SOM.SummaryReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SummaryReportService {

    @Autowired
    private SummaryReportRepository summaryReportRepository;

    @Autowired
    private OrderRepository orderRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Generate summary report using manual insert
    @Transactional
    public SummaryReport generateSummaryReport(String reportType, LocalDateTime startDate,
                                               LocalDateTime endDate, String generatedBy) {
        try {
            // Get orders within the date range using manual query
            List<Order> orders = orderRepository.findOrdersByDateRange(startDate, endDate);

            // Calculate summary statistics
            Map<String, Object> summaryData = calculateSummaryStatistics(orders, startDate, endDate);

            // Create report object
            SummaryReport report = new SummaryReport();
            report.setReportName(generateReportName(reportType, startDate, endDate));
            report.setReportType(reportType);
            report.setPeriodStart(startDate);
            report.setPeriodEnd(endDate);
            report.setGeneratedBy(generatedBy);
            report.setGeneratedAt(LocalDateTime.now());

            // Set calculated values
            report.setTotalOrders((Integer) summaryData.get("totalOrders"));
            report.setTotalRevenue((Double) summaryData.get("totalRevenue"));
            report.setTotalCustomers((Integer) summaryData.get("totalCustomers"));
            report.setAverageOrderValue((Double) summaryData.get("averageOrderValue"));
            report.setSuccessRate((Double) summaryData.get("successRate"));

            // Set detailed report data as JSON
            String reportDataJson = objectMapper.writeValueAsString(summaryData.get("detailedData"));
            report.setReportData(reportDataJson);

            // Generate CSV file
            String csvFilePath = generateCSVReport(orders, report);
            report.setFilePath(csvFilePath);

            // Use manual insert query
            int result = summaryReportRepository.insertSummaryReport(
                    report.getReportName(),
                    report.getReportType(),
                    report.getPeriodStart(),
                    report.getPeriodEnd(),
                    report.getTotalOrders(),
                    report.getTotalRevenue(),
                    report.getTotalCustomers(),
                    report.getAverageOrderValue(),
                    report.getSuccessRate(),
                    report.getReportData(),
                    report.getGeneratedBy(),
                    report.getGeneratedAt(),
                    report.getFilePath()
            );

            if (result > 0) {
                // Get the generated ID
                Long generatedId = summaryReportRepository.findMaxReportId();
                report.setId(generatedId);
                System.out.println("Summary report created successfully with ID: " + generatedId);
                return report;
            } else {
                throw new RuntimeException("Failed to insert summary report");
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate summary report: " + e.getMessage(), e);
        }
    }

    // Update report data using manual update
    @Transactional
    public SummaryReport updateReportData(Long reportId, String updatedBy) {
        // Check if report exists
        int exists = summaryReportRepository.checkReportExists(reportId);
        if (exists == 0) {
            throw new RuntimeException("Report not found with ID: " + reportId);
        }

        // Get the existing report
        Optional<SummaryReport> existingReport = summaryReportRepository.findById(reportId);
        if (existingReport.isPresent()) {
            SummaryReport report = existingReport.get();

            // Re-calculate statistics for the report period
            List<Order> orders = orderRepository.findOrdersByDateRange(report.getPeriodStart(), report.getPeriodEnd());
            Map<String, Object> summaryData = calculateSummaryStatistics(orders, report.getPeriodStart(), report.getPeriodEnd());

            // Update values
            Integer totalOrders = (Integer) summaryData.get("totalOrders");
            Double totalRevenue = (Double) summaryData.get("totalRevenue");
            Integer totalCustomers = (Integer) summaryData.get("totalCustomers");
            Double averageOrderValue = (Double) summaryData.get("averageOrderValue");
            Double successRate = (Double) summaryData.get("successRate");
            String reportDataJson;

            try {
                reportDataJson = objectMapper.writeValueAsString(summaryData.get("detailedData"));
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize report data: " + e.getMessage());
            }

            // Use manual update query
            int result = summaryReportRepository.updateReportData(
                    reportId,
                    reportDataJson,
                    totalOrders,
                    totalRevenue,
                    totalCustomers,
                    averageOrderValue,
                    successRate
            );

            if (result > 0) {
                report.setTotalOrders(totalOrders);
                report.setTotalRevenue(totalRevenue);
                report.setTotalCustomers(totalCustomers);
                report.setAverageOrderValue(averageOrderValue);
                report.setSuccessRate(successRate);
                report.setReportData(reportDataJson);
                return report;
            } else {
                throw new RuntimeException("Failed to update report data");
            }
        }
        throw new RuntimeException("Report not found with ID: " + reportId);
    }

    // Update report file path
    @Transactional
    public boolean updateReportFilePath(Long reportId, String newFilePath) {
        // Check if report exists
        int exists = summaryReportRepository.checkReportExists(reportId);
        if (exists == 0) {
            throw new RuntimeException("Report not found with ID: " + reportId);
        }

        // Use manual update query
        int result = summaryReportRepository.updateReportFilePath(reportId, newFilePath);
        return result > 0;
    }

    // Calculate summary statistics
    private Map<String, Object> calculateSummaryStatistics(List<Order> orders,
                                                           LocalDateTime startDate,
                                                           LocalDateTime endDate) {
        Map<String, Object> result = new HashMap<>();

        // Basic statistics
        int totalOrders = orders.size();
        double totalRevenue = orders.stream().mapToDouble(Order::getTotalAmount).sum();
        long totalCustomers = orders.stream().map(Order::getCustomerEmail).distinct().count();
        double averageOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;
        long completedOrders = orders.stream().filter(o -> "READY_TO_SHIP".equals(o.getStatus())).count();
        double successRate = totalOrders > 0 ? (double) completedOrders / totalOrders * 100 : 0;

        result.put("totalOrders", totalOrders);
        result.put("totalRevenue", totalRevenue);
        result.put("totalCustomers", (int) totalCustomers);
        result.put("averageOrderValue", averageOrderValue);
        result.put("successRate", successRate);

        // Monthly breakdown
        Map<String, Object> monthlyData = calculateMonthlyBreakdown(orders);
        result.put("detailedData", monthlyData);

        return result;
    }

    // Calculate monthly breakdown
    private Map<String, Object> calculateMonthlyBreakdown(List<Order> orders) {
        Map<String, Object> monthlyData = new LinkedHashMap<>();

        // Group orders by month
        Map<String, List<Order>> ordersByMonth = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getPlaceDate().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // Calculate monthly statistics
        ArrayNode monthlyStats = objectMapper.createArrayNode();
        for (Map.Entry<String, List<Order>> entry : ordersByMonth.entrySet()) {
            String month = entry.getKey();
            List<Order> monthlyOrders = entry.getValue();

            double monthlyRevenue = monthlyOrders.stream().mapToDouble(Order::getTotalAmount).sum();
            int monthlyOrdersCount = monthlyOrders.size();
            long monthlyCustomers = monthlyOrders.stream().map(Order::getCustomerEmail).distinct().count();

            ObjectNode monthData = objectMapper.createObjectNode();
            monthData.put("month", month);
            monthData.put("orders", monthlyOrdersCount);
            monthData.put("revenue", monthlyRevenue);
            monthData.put("customers", monthlyCustomers);
            monthData.put("averageOrderValue", monthlyOrdersCount > 0 ? monthlyRevenue / monthlyOrdersCount : 0);

            monthlyStats.add(monthData);
        }

        monthlyData.put("monthlyBreakdown", monthlyStats);

        // Product performance
        Map<String, Object> productPerformance = calculateProductPerformance(orders);
        monthlyData.put("productPerformance", productPerformance);

        // Status distribution
        Map<String, Object> statusDistribution = calculateStatusDistribution(orders);
        monthlyData.put("statusDistribution", statusDistribution);

        return monthlyData;
    }

    // Calculate product performance
    private Map<String, Object> calculateProductPerformance(List<Order> orders) {
        Map<String, Object> productPerformance = new HashMap<>();

        Map<String, Double> revenueByProduct = orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getProductType,
                        Collectors.summingDouble(Order::getTotalAmount)
                ));

        Map<String, Long> quantityByProduct = orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getProductType,
                        Collectors.summingLong(Order::getQuantity)
                ));

        productPerformance.put("revenueByProduct", revenueByProduct);
        productPerformance.put("quantityByProduct", quantityByProduct);

        return productPerformance;
    }

    // Calculate status distribution
    private Map<String, Object> calculateStatusDistribution(List<Order> orders) {
        Map<String, Long> statusCount = orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

        Map<String, Object> statusDistribution = new HashMap<>();
        statusDistribution.put("counts", statusCount);
        statusDistribution.put("total", orders.size());

        return statusDistribution;
    }

    // Generate CSV report
    private String generateCSVReport(List<Order> orders, SummaryReport report) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "order_report_" + timestamp + ".csv";
        Path reportsDir = Paths.get("reports");

        if (!Files.exists(reportsDir)) {
            Files.createDirectories(reportsDir);
        }

        Path filePath = reportsDir.resolve(fileName);

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            // Write header
            writer.write("Order ID,Order Type,Customer Name,Customer Email,Product Type,Quantity,Unit Price,Discount,Total Amount,Status,Order Date\n");

            // Write data
            for (Order order : orders) {
                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,%.2f,%.2f,%.2f,\"%s\",\"%s\"\n",
                        order.getOrderId(),
                        order.getOrderType(),
                        order.getCustomerName(),
                        order.getCustomerEmail(),
                        order.getProductType(),
                        order.getQuantity(),
                        order.getUnitPrice(),
                        order.getDiscountPercentage() != null ? order.getDiscountPercentage() : 0,
                        order.getTotalAmount(),
                        order.getStatus(),
                        order.getPlaceDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                ));
            }
        }

        return filePath.toString();
    }

    // Generate report name
    private String generateReportName(String reportType, LocalDateTime startDate, LocalDateTime endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("%s Report %s to %s",
                reportType,
                startDate.format(formatter),
                endDate.format(formatter));
    }

    // Get all reports using manual query
    public List<SummaryReport> getAllReports() {
        return summaryReportRepository.findAllOrderByGeneratedAtDesc();
    }

    // Get report by ID
    public Optional<SummaryReport> getReportById(Long id) {
        return summaryReportRepository.findById(id);
    }

    // Get reports by type
    public List<SummaryReport> getReportsByType(String reportType) {
        return summaryReportRepository.findByReportTypeOrderByGeneratedAtDesc(reportType);
    }

    // Delete report using manual delete
    @Transactional
    public void deleteReport(Long id) {
        // Check if report exists
        int exists = summaryReportRepository.checkReportExists(id);
        if (exists == 0) {
            throw new RuntimeException("Report not found with ID: " + id);
        }

        // Use manual delete query
        int result = summaryReportRepository.deleteSummaryReport(id);
        if (result == 0) {
            throw new RuntimeException("Failed to delete report");
        }
        System.out.println("Report deleted successfully: " + id);
    }

    // Delete old reports
    @Transactional
    public int deleteOldReports(LocalDateTime cutoffDate) {
        return summaryReportRepository.deleteOldReports(cutoffDate);
    }

    // Get quick stats for dashboard
    public Map<String, Object> getQuickStats(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findOrdersByDateRange(startDate, endDate);
        return calculateSummaryStatistics(orders, startDate, endDate);
    }

    // Get report count by type
    public int getReportCountByType(String reportType) {
        return summaryReportRepository.countReportsByType(reportType);
    }

    // Get latest report by type
    public Optional<SummaryReport> getLatestReportByType(String reportType) {
        return summaryReportRepository.findTopByReportTypeOrderByGeneratedAtDesc(reportType);
    }

    // Get reports by date range
    public List<SummaryReport> getReportsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return summaryReportRepository.findReportsByDateRange(startDate, endDate);
    }

    // Get reports by period
    public List<SummaryReport> getReportsByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return summaryReportRepository.findReportsByPeriod(startDate, endDate);
    }

    // Get reports by user
    public List<SummaryReport> getReportsByUser(String generatedBy) {
        return summaryReportRepository.findByGeneratedByOrderByGeneratedAtDesc(generatedBy);
    }
}