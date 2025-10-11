package com.clothsphere.service.FM;

import com.clothsphere.model.FM.*;
import com.clothsphere.repository.HR.EmployeeRepository;
import com.clothsphere.repository.FM.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

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

    @Autowired
    private EmployeeRepository employeeRepository;

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
     * Create a new production order using manual insert
     */
    @Transactional
    public ProductionOrder createOrder(ProductionOrder order) {
        if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
            order.setOrderId(generateNextOrderId());
        }

        // Use manual insert method
        orderRepository.insertProductionOrder(
                order.getOrderId(),
                order.getProductName(),
                order.getProductType(),
                order.getQuantity(),
                order.getOrderDate(),
                order.getDeadline(),
                order.getPriority(),
                order.getStatus(),
                order.getCustomerName(),
                order.getCustomerId(),
                order.getCompletedQuantity() != null ? order.getCompletedQuantity() : 0,
                order.getNotes()
        );

        return order;
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
     * Update production order using manual update
     */
    @Transactional
    public ProductionOrder updateOrder(ProductionOrder order) {
        int rowsAffected = orderRepository.updateProductionOrder(
                order.getOrderId(),
                order.getProductName(),
                order.getProductType(),
                order.getQuantity(),
                order.getDeadline(),
                order.getPriority(),
                order.getStatus(),
                order.getCustomerName(),
                order.getCompletedQuantity(),
                order.getNotes()
        );

        if (rowsAffected > 0) {
            return order;
        }
        return null;
    }

    /**
     * Delete production order using manual delete
     */
    @Transactional
    public boolean deleteOrder(String orderId) {
        try {
            int rowsAffected = orderRepository.deleteProductionOrder(orderId);
            return rowsAffected > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Update order status only
     */
    @Transactional
    public boolean updateOrderStatus(String orderId, String status) {
        int rowsAffected = orderRepository.updateOrderStatus(orderId, status);
        return rowsAffected > 0;
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
     * Create production schedule using manual insert
     */
    @Transactional
    public ProductionSchedule createSchedule(ProductionSchedule schedule) {
        if (schedule.getScheduleId() == null || schedule.getScheduleId().isEmpty()) {
            schedule.setScheduleId(generateNextScheduleId());
        }

        scheduleRepository.insertProductionSchedule(
                schedule.getScheduleId(),
                schedule.getOrderId(),
                schedule.getWorkstationId(),
                schedule.getScheduledDate(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getEstimatedHours(),
                schedule.getActualHours(),
                schedule.getAssignedQuantity(),
                schedule.getCompletedQuantity() != null ? schedule.getCompletedQuantity() : 0,
                schedule.getStatus(),
                schedule.getShift(),
                schedule.getNotes()
        );

        return schedule;
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
     * Update schedule using manual update
     */
    @Transactional
    public ProductionSchedule updateSchedule(ProductionSchedule schedule) {
        int rowsAffected = scheduleRepository.updateProductionSchedule(
                schedule.getScheduleId(),
                schedule.getOrderId(),
                schedule.getWorkstationId(),
                schedule.getScheduledDate(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getEstimatedHours(),
                schedule.getActualHours(),
                schedule.getAssignedQuantity(),
                schedule.getCompletedQuantity(),
                schedule.getStatus(),
                schedule.getShift(),
                schedule.getNotes()
        );

        if (rowsAffected > 0) {
            return schedule;
        }
        return null;
    }

    /**
     * Delete schedule using manual delete
     */
    @Transactional
    public boolean deleteSchedule(String scheduleId) {
        try {
            int rowsAffected = scheduleRepository.deleteProductionSchedule(scheduleId);
            return rowsAffected > 0;
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
     * Create workstation using manual insert
     */
    @Transactional
    public WorkStation createWorkstation(WorkStation workStation) {
        if (workStation.getWorkstationId() == null || workStation.getWorkstationId().isEmpty()) {
            workStation.setWorkstationId(generateNextWorkstationId());
        }

        workStationRepository.insertWorkStation(
                workStation.getWorkstationId(),
                workStation.getWorkstationName(),
                workStation.getWorkstationType(),
                workStation.getCapacity(),
                workStation.getCurrentLoad() != null ? workStation.getCurrentLoad() : 0,
                workStation.getStatus(),
                workStation.getLocation(),
                workStation.getSupervisorId(),
                workStation.getSupervisorName(),
                workStation.getEquipmentDetails()
        );

        return workStation;
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
     * Update workstation using manual update
     */
    @Transactional
    public WorkStation updateWorkstation(WorkStation workStation) {
        int rowsAffected = workStationRepository.updateWorkStation(
                workStation.getWorkstationId(),
                workStation.getWorkstationName(),
                workStation.getWorkstationType(),
                workStation.getCapacity(),
                workStation.getCurrentLoad(),
                workStation.getStatus(),
                workStation.getLocation(),
                workStation.getSupervisorId(),
                workStation.getSupervisorName(),
                workStation.getEquipmentDetails()
        );

        if (rowsAffected > 0) {
            return workStation;
        }
        return null;
    }

    /**
     * Delete workstation using manual delete
     */
    @Transactional
    public boolean deleteWorkstation(String workstationId) {
        try {
            int rowsAffected = workStationRepository.deleteWorkStation(workstationId);
            return rowsAffected > 0;
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
     * Create staff assignment using manual insert
     */
    @Transactional
    public StaffAssignment createAssignment(StaffAssignment assignment) {
        if (assignment.getAssignmentId() == null || assignment.getAssignmentId().isEmpty()) {
            assignment.setAssignmentId(generateNextAssignmentId());
        }

        staffAssignmentRepository.insertStaffAssignment(
                assignment.getAssignmentId(),
                assignment.getScheduleId(),
                assignment.getEmployeeId(),
                assignment.getWorkstationId(),
                assignment.getAssignmentDate(),
                assignment.getShift(),
                assignment.getRole(),
                assignment.getStatus(),
                assignment.getAssignedQuantity(),
                assignment.getCompletedQuantity() != null ? assignment.getCompletedQuantity() : 0,
                assignment.getStartTime(),
                assignment.getEndTime(),
                assignment.getNotes()
        );

        return assignment;
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
     * Update assignment using manual update
     */
    @Transactional
    public StaffAssignment updateAssignment(StaffAssignment assignment) {
        int rowsAffected = staffAssignmentRepository.updateStaffAssignment(
                assignment.getAssignmentId(),
                assignment.getScheduleId(),
                assignment.getEmployeeId(),
                assignment.getWorkstationId(),
                assignment.getAssignmentDate(),
                assignment.getShift(),
                assignment.getRole(),
                assignment.getStatus(),
                assignment.getAssignedQuantity(),
                assignment.getCompletedQuantity(),
                assignment.getStartTime(),
                assignment.getEndTime(),
                assignment.getNotes()
        );

        if (rowsAffected > 0) {
            return assignment;
        }
        return null;
    }

    /**
     * Delete assignment using manual delete
     */
    @Transactional
    public boolean deleteAssignment(String assignmentId) {
        try {
            int rowsAffected = staffAssignmentRepository.deleteStaffAssignment(assignmentId);
            return rowsAffected > 0;
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
     * Create performance metric using manual insert
     */
    @Transactional
    public PerformanceMetrics createMetric(PerformanceMetrics metric) {
        if (metric.getMetricId() == null || metric.getMetricId().isEmpty()) {
            metric.setMetricId(generateNextMetricId());
        }
        metric.calculateMetrics();

        metricsRepository.insertPerformanceMetric(
                metric.getMetricId(),
                metric.getRecordDate(),
                metric.getWorkstationId(),
                metric.getEmployeeId(),
                metric.getOrderId(),
                metric.getScheduleId(),
                metric.getTargetQuantity(),
                metric.getActualQuantity(),
                metric.getDefectQuantity() != null ? metric.getDefectQuantity() : 0,
                metric.getEfficiencyRate(),
                metric.getQualityRate(),
                metric.getDowntimeHours() != null ? metric.getDowntimeHours() : 0.0,
                metric.getWorkingHours(),
                metric.getOvertimeHours() != null ? metric.getOvertimeHours() : 0.0,
                metric.getDelayHours() != null ? metric.getDelayHours() : 0.0,
                metric.getRemarks()
        );

        return metric;
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