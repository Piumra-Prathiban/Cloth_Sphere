package com.clothsphere.controller.FM;

import com.clothsphere.model.HR.Employee;
import com.clothsphere.model.FM.*;
import com.clothsphere.repository.HR.EmployeeRepository;
import com.clothsphere.service.FM.ProductionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/production")
public class ProductionController {

    @Autowired
    private ProductionService productionService;

    @Autowired
    private EmployeeRepository employeeRepository;

    // ===================== DASHBOARD =====================

    /**
     * Show Factory Manager Dashboard
     */
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        // Check if user is logged in (you can add role check for factory-manager)
        Object currentUser = session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/systemUserLogin";
        }

        try {
            // Get dashboard statistics
            Map<String, Object> stats = productionService.getDashboardStats();
            model.addAttribute("stats", stats);

            // Get recent data
            model.addAttribute("recentOrders", productionService.getAllOrders());
            model.addAttribute("todaySchedules", productionService.getTodaySchedules());
            model.addAttribute("activeWorkstations", productionService.getActiveWorkstations());
            model.addAttribute("todayAssignments", productionService.getTodayAssignments());

            return "productionDashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "error";
        }
    }

    // ===================== PRODUCTION ORDER APIs =====================

    /**
     * Get all production orders
     */
    @GetMapping("/api/orders")
    @ResponseBody
    public ResponseEntity<List<ProductionOrder>> getAllOrders() {
        try {
            List<ProductionOrder> orders = productionService.getAllOrders();
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get single production order
     */
    @GetMapping("/api/orders/{id}")
    @ResponseBody
    public ResponseEntity<ProductionOrder> getOrder(@PathVariable String id) {
        try {
            ProductionOrder order = productionService.getOrderById(id);
            if (order != null) {
                return new ResponseEntity<>(order, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create new production order
     */
    @PostMapping("/api/orders")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody ProductionOrder order) {
        Map<String, Object> response = new HashMap<>();
        try {
            ProductionOrder created = productionService.createOrder(order);
            response.put("success", true);
            response.put("message", "Order created successfully");
            response.put("order", created);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating order: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update production order
     */
    @PutMapping("/api/orders/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateOrder(@PathVariable String id,
                                                           @RequestBody ProductionOrder order) {
        Map<String, Object> response = new HashMap<>();
        try {
            order.setOrderId(id);
            ProductionOrder updated = productionService.updateOrder(order);
            response.put("success", true);
            response.put("message", "Order updated successfully");
            response.put("order", updated);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating order: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete production order
     */
    @DeleteMapping("/api/orders/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteOrder(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean deleted = productionService.deleteOrder(id);
            if (deleted) {
                response.put("success", true);
                response.put("message", "Order deleted successfully");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            response.put("success", false);
            response.put("message", "Order not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting order: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===================== PRODUCTION SCHEDULE APIs =====================

    /**
     * Get all schedules
     */
    @GetMapping("/api/schedules")
    @ResponseBody
    public ResponseEntity<List<ProductionSchedule>> getAllSchedules() {
        try {
            List<ProductionSchedule> schedules = productionService.getAllSchedules();
            return new ResponseEntity<>(schedules, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get schedule by ID
     */
    @GetMapping("/api/schedules/{id}")
    @ResponseBody
    public ResponseEntity<ProductionSchedule> getSchedule(@PathVariable String id) {
        try {
            ProductionSchedule schedule = productionService.getScheduleById(id);
            if (schedule != null) {
                return new ResponseEntity<>(schedule, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create production schedule
     */
    @PostMapping("/api/schedules")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createSchedule(@RequestBody ProductionSchedule schedule) {
        Map<String, Object> response = new HashMap<>();
        try {
            ProductionSchedule created = productionService.createSchedule(schedule);
            response.put("success", true);
            response.put("message", "Schedule created successfully");
            response.put("schedule", created);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating schedule: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update schedule
     */
    @PutMapping("/api/schedules/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateSchedule(@PathVariable String id,
                                                              @RequestBody ProductionSchedule schedule) {
        Map<String, Object> response = new HashMap<>();
        try {
            schedule.setScheduleId(id);
            ProductionSchedule updated = productionService.updateSchedule(schedule);
            response.put("success", true);
            response.put("message", "Schedule updated successfully");
            response.put("schedule", updated);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating schedule: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete schedule
     */
    @DeleteMapping("/api/schedules/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteSchedule(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean deleted = productionService.deleteSchedule(id);
            if (deleted) {
                response.put("success", true);
                response.put("message", "Schedule deleted successfully");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            response.put("success", false);
            response.put("message", "Schedule not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting schedule: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===================== WORKSTATION APIs =====================

    /**
     * Get all workstations
     */
    @GetMapping("/api/workstations")
    @ResponseBody
    public ResponseEntity<List<WorkStation>> getAllWorkstations() {
        try {
            List<WorkStation> workstations = productionService.getAllWorkstations();
            return new ResponseEntity<>(workstations, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get active workstations with capacity
     */
    @GetMapping("/api/workstations/available")
    @ResponseBody
    public ResponseEntity<List<WorkStation>> getAvailableWorkstations() {
        try {
            List<WorkStation> workstations = productionService.getWorkstationsWithCapacity();
            return new ResponseEntity<>(workstations, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create workstation
     */
    @PostMapping("/api/workstations")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createWorkstation(@RequestBody WorkStation workstation) {
        Map<String, Object> response = new HashMap<>();
        try {
            WorkStation created = productionService.createWorkstation(workstation);
            response.put("success", true);
            response.put("message", "Workstation created successfully");
            response.put("workstation", created);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating workstation: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update workstation
     */
    @PutMapping("/api/workstations/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateWorkstation(@PathVariable String id,
                                                                 @RequestBody WorkStation workstation) {
        Map<String, Object> response = new HashMap<>();
        try {
            workstation.setWorkstationId(id);
            WorkStation updated = productionService.updateWorkstation(workstation);
            response.put("success", true);
            response.put("message", "Workstation updated successfully");
            response.put("workstation", updated);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating workstation: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===================== STAFF ASSIGNMENT APIs =====================

    /**
     * Get all staff assignments
     */
    @GetMapping("/api/assignments")
    @ResponseBody
    public ResponseEntity<List<StaffAssignment>> getAllAssignments() {
        try {
            List<StaffAssignment> assignments = productionService.getAllAssignments();
            return new ResponseEntity<>(assignments, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create staff assignment
     */
    @PostMapping("/api/assignments")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createAssignment(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            StaffAssignment assignment = new StaffAssignment();
            assignment.setScheduleId((String) requestData.get("scheduleId"));
            assignment.setWorkstationId((String) requestData.get("workstationId"));
            assignment.setAssignmentDate(LocalDate.parse((String) requestData.get("assignmentDate")));
            assignment.setShift((String) requestData.get("shift"));
            assignment.setRole((String) requestData.get("role"));
            assignment.setStatus((String) requestData.get("status"));
            assignment.setAssignedQuantity((Integer) requestData.get("assignedQuantity"));

            // Get employee from repository
            String employeeId = (String) requestData.get("employeeId");
            Employee employee = employeeRepository.findById(employeeId).orElse(null);
            if (employee == null) {
                response.put("success", false);
                response.put("message", "Employee not found with ID: " + employeeId);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            assignment.setEmployee(employee);

            StaffAssignment created = productionService.createAssignment(assignment);
            response.put("success", true);
            response.put("message", "Assignment created successfully");
            response.put("assignment", created);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating assignment: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all employees (for assignment creation)
     */
    @GetMapping("/api/employees")
    @ResponseBody
    public ResponseEntity<List<Employee>> getAllEmployees() {
        try {
            List<Employee> employees = employeeRepository.findAll();
            return new ResponseEntity<>(employees, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===================== PERFORMANCE METRICS APIs =====================

    /**
     * Get all performance metrics
     */
    @GetMapping("/api/metrics")
    @ResponseBody
    public ResponseEntity<List<PerformanceMetrics>> getAllMetrics() {
        try {
            List<PerformanceMetrics> metrics = productionService.getAllMetrics();
            return new ResponseEntity<>(metrics, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create performance metric
     */
    @PostMapping("/api/metrics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createMetric(@RequestBody PerformanceMetrics metric) {
        Map<String, Object> response = new HashMap<>();
        try {
            PerformanceMetrics created = productionService.createMetric(metric);
            response.put("success", true);
            response.put("message", "Metric recorded successfully");
            response.put("metric", created);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error recording metric: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get dashboard statistics
     */
    @GetMapping("/api/dashboard/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = productionService.getDashboardStats();
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}