package com.clothsphere.controller.SalesOrder;

import com.clothsphere.model.SalesOrder.*;
import com.clothsphere.service.SalesOrder.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/sales-orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Dashboard - Main page
    @GetMapping
    public String dashboard(Model model) {
        Map<String, Object> stats = orderService.getDashboardStats();
        List<Order> recentOrders = orderService.getRecentOrders();
        List<Order> overdueOrders = orderService.getOverdueOrders();

        model.addAttribute("stats", stats);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("overdueOrders", overdueOrders);
        model.addAttribute("orderStatuses", OrderStatus.values());

        return "sales-orders/dashboard";
    }

    // List all orders
    @GetMapping("/list")
    public String listOrders(
            @RequestParam(required = false) String buyerName,
            @RequestParam(required = false) OrderStatus orderStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        List<Order> orders;
        if (buyerName != null || orderStatus != null || startDate != null || endDate != null) {
            orders = orderService.searchOrders(buyerName, orderStatus, startDate, endDate);
        } else {
            orders = orderService.getAllOrders();
        }

        model.addAttribute("orders", orders);
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("buyerName", buyerName);
        model.addAttribute("orderStatus", orderStatus);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "sales-orders/list";
    }

    // Show create form
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("order", new Order());
        model.addAttribute("buyer", new Buyer());
        model.addAttribute("orderStatuses", OrderStatus.values());
        return "sales-orders/create";
    }

    // Create order using request parameters
    @PostMapping("/create")
    public String createOrder(@RequestParam String buyerName,
                              @RequestParam String buyerEmail,
                              @RequestParam(required = false) String buyerPhone,
                              @RequestParam(required = false) String buyerAddress,
                              @RequestParam String productDetails,
                              @RequestParam Integer quantity,
                              @RequestParam BigDecimal unitPrice,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deliveryDeadline,
                              @RequestParam(required = false) String notes,
                              RedirectAttributes redirectAttributes,
                              Model model) {

        try {
            Order savedOrder = orderService.createSimpleOrder(
                    buyerName, buyerEmail, buyerPhone, buyerAddress,
                    productDetails, quantity, unitPrice, deliveryDeadline, notes
            );

            redirectAttributes.addFlashAttribute("successMessage",
                    "Order created successfully with Order Number: " + savedOrder.getOrderNumber());
            return "redirect:/sales-orders/view/" + savedOrder.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error creating order: " + e.getMessage());
            model.addAttribute("orderStatuses", OrderStatus.values());
            return "sales-orders/create";
        }
    }

    // View order details
    @GetMapping("/view/{id}")
    public String viewOrder(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Order> orderOpt = orderService.getOrderById(id);

        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            model.addAttribute("order", order);
            model.addAttribute("orderStatuses", OrderStatus.values());
            return "sales-orders/view";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Order not found with ID: " + id);
            return "redirect:/sales-orders/list";
        }
    }

    // Show edit form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Order> orderOpt = orderService.getOrderById(id);

        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            model.addAttribute("order", order);
            model.addAttribute("orderStatuses", OrderStatus.values());
            return "sales-orders/edit";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Order not found with ID: " + id);
            return "redirect:/sales-orders/list";
        }
    }

    // Update order using request parameters
    @PostMapping("/edit/{id}")
    public String updateOrder(@PathVariable Long id,
                              @RequestParam String buyerName,
                              @RequestParam String buyerEmail,
                              @RequestParam(required = false) String buyerPhone,
                              @RequestParam(required = false) String buyerAddress,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deliveryDeadline,
                              @RequestParam(required = false) String notes,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        try {
            Optional<Order> orderOpt = orderService.getOrderById(id);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();

                // Update buyer information
                order.getBuyer().setName(buyerName);
                order.getBuyer().setEmail(buyerEmail);
                order.getBuyer().setPhone(buyerPhone);
                order.getBuyer().setAddress(buyerAddress);

                // Update order information
                order.setDeliveryDeadline(deliveryDeadline);
                order.setNotes(notes);

                Order updatedOrder = orderService.updateOrder(order);
                redirectAttributes.addFlashAttribute("successMessage", "Order updated successfully!");
                return "redirect:/sales-orders/view/" + updatedOrder.getId();
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Order not found!");
                return "redirect:/sales-orders/list";
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating order: " + e.getMessage());
            return "sales-orders/edit";
        }
    }

    // Update order status only
    @PostMapping("/update-status/{id}")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam OrderStatus newStatus,
                                    RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, newStatus);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Order status updated to: " + newStatus.getDisplayName());
            return "redirect:/sales-orders/view/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error updating order status: " + e.getMessage());
            return "redirect:/sales-orders/view/" + id;
        }
    }

    // Delete order
    @PostMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = orderService.deleteOrder(id);
            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage", "Order deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Order not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error deleting order: " + e.getMessage());
        }
        return "redirect:/sales-orders/list";
    }

    // Generate sales report
    @GetMapping("/reports/sales")
    public String salesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        Map<String, Object> salesReport = orderService.generateSalesReport(startDate, endDate);

        model.addAttribute("salesReport", salesReport);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("orderStatuses", OrderStatus.values());

        return "sales-orders/sales-report";
    }

    // REST endpoints for AJAX calls
    @GetMapping("/search/buyer")
    @ResponseBody
    public List<Order> searchOrdersByBuyer(@RequestParam String buyerName) {
        return orderService.getOrdersByBuyerName(buyerName);
    }

    @GetMapping("/search/status")
    @ResponseBody
    public List<Order> getOrdersByStatus(@RequestParam OrderStatus status) {
        return orderService.getOrdersByStatus(status);
    }

    @GetMapping("/search/order-number")
    @ResponseBody
    public Order getOrderByNumber(@RequestParam String orderNumber) {
        Optional<Order> order = orderService.getOrderByNumber(orderNumber);
        return order.orElse(null);
    }
}