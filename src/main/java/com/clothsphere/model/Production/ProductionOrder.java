package com.clothsphere.model.Production;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "production_order")
public class ProductionOrder {

    @Id
    @Column(name = "order_id", length = 10, nullable = false)
    private String orderId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "product_type", length = 100)
    private String productType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "deadline", nullable = false)
    private LocalDate deadline;

    @Column(name = "priority", length = 20)
    private String priority; // HIGH, MEDIUM, LOW

    @Column(name = "status", length = 30)
    private String status; // PENDING, IN_PROGRESS, COMPLETED, DELAYED

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "customer_id", length = 10)
    private String customerId;

    @Column(name = "completed_quantity")
    private Integer completedQuantity = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "notes", length = 500)
    private String notes;

    // Default constructor
    public ProductionOrder() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Parameterized constructor
    public ProductionOrder(String orderId, String productName, String productType, Integer quantity,
                          LocalDate orderDate, LocalDate deadline, String priority, String status,
                          String customerName, String customerId) {
        this.orderId = orderId;
        this.productName = productName;
        this.productType = productType;
        this.quantity = quantity;
        this.orderDate = orderDate;
        this.deadline = deadline;
        this.priority = priority;
        this.status = status;
        this.customerName = customerName;
        this.customerId = customerId;
        this.completedQuantity = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public Integer getCompletedQuantity() { return completedQuantity; }
    public void setCompletedQuantity(Integer completedQuantity) {
        this.completedQuantity = completedQuantity;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // Calculated field for progress percentage
    @Transient
    public Double getProgressPercentage() {
        if (quantity == null || quantity == 0) return 0.0;
        return (completedQuantity * 100.0) / quantity;
    }

    @Override
    public String toString() {
        return "ProductionOrder{" +
                "orderId='" + orderId + '\'' +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", status='" + status + '\'' +
                ", deadline=" + deadline +
                ", priority='" + priority + '\'' +
                '}';
    }
}