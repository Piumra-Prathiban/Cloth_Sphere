package com.clothsphere.service.Production;

import com.clothsphere.model.Production.*;
import com.clothsphere.repository.Production.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductionService {

    @Autowired
    private ProductionOrderRepository orderRepository;

    @Autowired
    private ProductionScheduleRepository scheduleRepository;

    @Autowired
    private WorkStationRepository workStationRepository;

    @Autowired
    private StaffAssignmentRepository staffAssignmentRepository;

    @Autowired
    private PerformanceMetricsRepository metricsRepository;

    // ===================== PRODUCTION ORDER MANAGEMENT =====================

    /**
     * Generate next production order ID (po001, po002, etc.)
     */
    public String generateNextOrderId() {
        List<String> existingIds = orderRepository.findAllOrderIds();
        if (existingIds.isEmpty()) {
            return "po001";
        }

        int maxNumber = 0;
        for (String id : existingIds) {
            if (id != null && id.startsWith("po") && id.length() == 5) {
                try {
                    int number = Integer.parseInt(id.substring(2));
                    maxNumber = Math.max(maxNumber, number);
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return String.format("po%03d", maxNumber + 1);
    }

    /**
     * Create a new production order
     */
    public ProductionOrder createOrder(ProductionOrder order) {
        if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
            order.setOrderId(generateNextOrderId());
        }
        return orderRepository.save(order);
    }

    /**
     * Get all production orders
     */
    public List<ProductionOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Get order by ID
     */
    public ProductionOrder getOrderById(String orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    /**
     * Update production order
     */
    public ProductionOrder updateOrder(ProductionOrder order) {
        return orderRepository.save(order);
    }

    /**
     * Delete production order
     */
    public boolean deleteOrder(String orderId) {
        try {
            orderRepository.deleteById(orderId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get orders by status
     */
    public List<ProductionOrder> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * Get delayed orders
     */
    public List<ProductionOrder> getDelayedOrders() {
        return orderRepository.findDelayedOrders();
    }

    /**
     * Get upcoming orders (within next N days)
     */
    public List<ProductionOrder> getUpcomingOrders(int days) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(days);
        return orderRepository.findUpcomingOrders(today, futureDate);
    }

    // ===================== PRODUCTION SCHEDULING =====================

    /**
     * Generate next schedule ID (sch001, sch002, etc.)
     */
    public String generateNextScheduleId() {
        List<String> existingIds = scheduleRepository.findAllScheduleIds();
        if (existingIds.isEmpty()) {
            return "sch001";
        }

        int maxNumber = 0;
        for (String id : existingIds) {
            if (id != null && id.startsWith("sch") && id.length() == 6) {
                try {
                    int number = Integer.parseInt(id.substring(3));
                    maxNumber = Math.max(maxNumber, number);
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return String.format("sch%03d", maxNumber + 1);
    }

    /**
     * Create production schedule
     */
    public ProductionSchedule createSchedule(ProductionSchedule schedule) {
        if (schedule.getScheduleId() == null || schedule.getScheduleId().isEmpty()) {
            schedule.setScheduleId(generateNextScheduleId());
        }
        return scheduleRepository.save(schedule);
    }

    /**
     * Get all schedules
     */
    public List<ProductionSchedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    /**
     * Get schedule by ID
     */
    public ProductionSchedule getScheduleById(String scheduleId) {
        return scheduleRepository.findById(scheduleId).orElse(null);
    }

    /**
     * Get schedules for a specific order
     */
    public List<ProductionSchedule> getSchedulesByOrder(String orderId) {
        return scheduleRepository.findByOrderId(orderId);
    }

    /**
     * Get today's schedules
     */
    public List<ProductionSchedule> getTodaySchedules() {
        return scheduleRepository.findTodaySchedules(LocalDate.now());
    }

    /**
     * Update schedule
     */
    public ProductionSchedule updateSchedule(ProductionSchedule schedule) {
        return scheduleRepository.save(schedule);
    }

    /**
     * Delete schedule
     */
    public boolean deleteSchedule(String scheduleId) {
        try {
            scheduleRepository.deleteById(scheduleId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ===================== WORKSTATION MANAGEMENT =====================

    /**
     * Generate next workstation ID (ws001, ws002, etc.)
     */
    public String generateNextWorkstationId() {
        List<String> existingIds = workStationRepository.findAllWorkstationIds();
        if (existingIds.isEmpty()) {
            return "ws001";
        }

        int maxNumber = 0;
        for (String id : existingIds) {
            if (id != null && id.startsWith("ws") && id.length() == 5) {
                try {
                    int number = Integer.parseInt(id.substring(2));
                    maxNumber = Math.max(maxNumber, number);
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return String.format("ws%03d", maxNumber + 1);
    }

    /**
     * Create workstation
     */
    public WorkStation createWorkstation(WorkStation workStation) {
        if (workStation.getWorkstationId() == null || workStation.getWorkstationId().isEmpty()) {
            workStation.setWorkstationId(generateNextWorkstationId());
        }
        return workStationRepository.save(workStation);
    }

    /**
     * Get all workstations
     */
    public List<WorkStation> getAllWorkstations() {
        return workStationRepository.findAll();
    }

    /**
     * Get workstation by ID
     */
    public WorkStation getWorkstationById(String workstationId) {
        return workStationRepository.findById(workstationId).orElse(null);
    }

    /**
     * Get active workstations
     */
    public List<WorkStation> getActiveWorkstations() {
        return workStationRepository.findActiveWorkstations();
    }

    /**
     * Get workstations with available capacity
     */
    public List<WorkStation> getWorkstationsWithCapacity() {
        return workStationRepository.findWorkstationsWithCapacity();
    }

    /**
     * Update workstation
     */
    public WorkStation updateWorkstation(WorkStation workStation) {
        return workStationRepository.save(workStation);
    }

    /**
     * Delete workstation
     */
    public boolean deleteWorkstation(String workstationId) {
        try {
            workStationRepository.deleteById(workstationId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ===================== STAFF ASSIGNMENT =====================

    /**
     * Generate next assignment ID (sa001, sa002, etc.)
     */
    public String generateNextAssignmentId() {
        List<String> existingIds = staffAssignmentRepository.findAllAssignmentIds();
        if (existingIds.isEmpty()) {
            return "sa001";
        }

        int maxNumber = 0;
        for (String id : existingIds) {
            if (id != null && id.startsWith("sa") && id.length() == 5) {
                try {
                    int number = Integer.parseInt(id.substring(2));
                    maxNumber = Math.max(maxNumber, number);
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return String.format("sa%03d", maxNumber + 1);
    }

    /**
     * Create staff assignment
     */
    public StaffAssignment createAssignment(StaffAssignment assignment) {
        if (assignment.getAssignmentId() == null || assignment.getAssignmentId().isEmpty()) {
            assignment.setAssignmentId(generateNextAssignmentId());
        }
        return staffAssignmentRepository.save(assignment);
    }

    /**
     * Get all assignments
     */
    public List<StaffAssignment> getAllAssignments() {
        return staffAssignmentRepository.findAll();
    }

    /**
     * Get assignment by ID
     */
    public StaffAssignment getAssignmentById(String assignmentId) {
        return staffAssignmentRepository.findById(assignmentId).orElse(null);
    }

    /**
     * Get assignments by employee
     */
    public List<StaffAssignment> getAssignmentsByEmployee(String employeeId) {
        return staffAssignmentRepository.findByEmployeeId(employeeId);
    }

    /**
     * Get today's assignments
     */
    public List<StaffAssignment> getTodayAssignments() {
        return staffAssignmentRepository.findTodayAssignments(LocalDate.now());
    }

    /**
     * Update assignment
     */
    public StaffAssignment updateAssignment(StaffAssignment assignment) {
        return staffAssignmentRepository.save(assignment);
    }

    /**
     * Delete assignment
     */
    public boolean deleteAssignment(String assignmentId) {
        try {
            staffAssignmentRepository.deleteById(assignmentId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ===================== PERFORMANCE METRICS =====================

    /**
     * Generate next metric ID (pm001, pm002, etc.)
     */
    public String generateNextMetricId() {
        List<String> existingIds = metricsRepository.findAllMetricIds();
        if (existingIds.isEmpty()) {
            return "pm001";
        }

        int maxNumber = 0;
        for (String id : existingIds) {
            if (id != null && id.startsWith("pm") && id.length() == 5) {
                try {
                    int number = Integer.parseInt(id.substring(2));
                    maxNumber = Math.max(maxNumber, number);
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return String.format("pm%03d", maxNumber + 1);
    }

    /**
     * Create performance metric
     */
    public PerformanceMetrics createMetric(PerformanceMetrics metric) {
        if (metric.getMetricId() == null || metric.getMetricId().isEmpty()) {
            metric.setMetricId(generateNextMetricId());
        }
        metric.calculateMetrics();
        return metricsRepository.save(metric);
    }

    /**
     * Get all metrics
     */
    public List<PerformanceMetrics> getAllMetrics() {
        return metricsRepository.findAll();
    }

    /**
     * Get metrics by date range
     */
    public List<PerformanceMetrics> getMetricsByDateRange(LocalDate startDate, LocalDate endDate) {
        return metricsRepository.findMetricsByDateRange(startDate, endDate);
    }

    /**
     * Get today's metrics
     */
    public List<PerformanceMetrics> getTodayMetrics() {
        return metricsRepository.findTodayMetrics(LocalDate.now());
    }

    /**
     * Calculate average efficiency by workstation
     */
    public Double getAverageEfficiencyByWorkstation(String workstationId) {
        return metricsRepository.calculateAverageEfficiencyByWorkstation(workstationId);
    }

    /**
     * Get dashboard statistics
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Order statistics
        stats.put("totalOrders", orderRepository.count());
        stats.put("pendingOrders", orderRepository.countByStatus("PENDING"));
        stats.put("inProgressOrders", orderRepository.countByStatus("IN_PROGRESS"));
        stats.put("completedOrders", orderRepository.countByStatus("COMPLETED"));
        stats.put("delayedOrders", orderRepository.findDelayedOrders().size());

        // Schedule statistics
        stats.put("totalSchedules", scheduleRepository.count());
        stats.put("todaySchedules", getTodaySchedules().size());

        // Workstation statistics
        stats.put("totalWorkstations", workStationRepository.count());
        stats.put("activeWorkstations", workStationRepository.countByStatus("ACTIVE"));

        // Staff assignment statistics
        stats.put("totalAssignments", staffAssignmentRepository.count());
        stats.put("todayAssignments", getTodayAssignments().size());

        return stats;
    }
}