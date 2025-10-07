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

    // Generate summary report
    public SummaryReport generateSummaryReport(String reportType, LocalDateTime startDate,
                                               LocalDateTime endDate, String generatedBy) {
        try {
            // Get orders within the date range
            List<Order> orders = getOrdersByDateRange(startDate, endDate);

            // Calculate summary statistics
            Map<String, Object> summaryData = calculateSummaryStatistics(orders, startDate, endDate);

            // Create report
            SummaryReport report = new SummaryReport();
            report.setReportName(generateReportName(reportType, startDate, endDate));
            report.setReportType(reportType);
            report.setPeriodStart(startDate);
            report.setPeriodEnd(endDate);
            report.setGeneratedBy(generatedBy);

            // Set calculated values
            report.setTotalOrders((Integer) summaryData.get("totalOrders"));
            report.setTotalRevenue((Double) summaryData.get("totalRevenue"));
            report.setTotalCustomers((Integer) summaryData.get("totalCustomers"));
            report.setAverageOrderValue((Double) summaryData.get("averageOrderValue"));
            report.setSuccessRate((Double) summaryData.get("successRate"));

            // Set detailed report data as JSON
            report.setReportData(objectMapper.writeValueAsString(summaryData.get("detailedData")));

            // Generate CSV file
            String csvFilePath = generateCSVReport(orders, report);
            report.setFilePath(csvFilePath);

            return summaryReportRepository.save(report);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate summary report: " + e.getMessage(), e);
        }
    }

    // Get orders by date range
    private List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        // This is a simplified implementation - you might need to adjust based on your OrderRepository
        List<Order> allOrders = orderRepository.findAllOrderByPlaceDateDesc();
        return allOrders.stream()
                .filter(order -> !order.getPlaceDate().isBefore(startDate) &&
                        !order.getPlaceDate().isAfter(endDate))
                .collect(Collectors.toList());
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

    // Get all reports
    public List<SummaryReport> getAllReports() {
        return summaryReportRepository.findAll();
    }

    // Get report by ID
    public Optional<SummaryReport> getReportById(Long id) {
        return summaryReportRepository.findById(id);
    }

    // Get reports by type
    public List<SummaryReport> getReportsByType(String reportType) {
        return summaryReportRepository.findByReportTypeOrderByGeneratedAtDesc(reportType);
    }

    // Delete report
    public void deleteReport(Long id) {
        summaryReportRepository.deleteById(id);
    }

    // Get quick stats for dashboard
    public Map<String, Object> getQuickStats(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = getOrdersByDateRange(startDate, endDate);
        return calculateSummaryStatistics(orders, startDate, endDate);
    }
}