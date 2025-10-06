package com.clothsphere.model.SOM;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@IdClass(OrderId.class)
public class Order {

    @Id
    @Column(name = "order_type", length = 20)
    private String orderType; // "PHYSICAL" or "ONLINE"

    @Id
    @Column(name = "order_id", length = 10)
    private String orderId;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_email", length = 100)
    private String customerEmail;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "customer_address", columnDefinition = "TEXT")
    private String customerAddress;

    @Column(name = "product_type", nullable = false, length = 50)
    private String productType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @Column(name = "discount_percentage")
    private Double discountPercentage;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "order_notes", columnDefinition = "TEXT")
    private String orderNotes;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // "PENDING", "IN_PRODUCTION", "READY_TO_SHIP"

    @Column(name = "place_date", nullable = false)
    private LocalDateTime placeDate;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    // Constructors
    public Order() {}

    public Order(String orderType, String orderId, String customerName, String customerEmail,
                 String customerPhone, String customerAddress, String productType,
                 Integer quantity, Double unitPrice, Double discountPercentage,
                 Double totalAmount, String orderNotes, String status,
                 LocalDateTime placeDate, String createdBy) {
        this.orderType = orderType;
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.customerAddress = customerAddress;
        this.productType = productType;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discountPercentage = discountPercentage;
        this.totalAmount = totalAmount;
        this.orderNotes = orderNotes;
        this.status = status;
        this.placeDate = placeDate;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public Double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Double discountPercentage) { this.discountPercentage = discountPercentage; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getOrderNotes() { return orderNotes; }
    public void setOrderNotes(String orderNotes) { this.orderNotes = orderNotes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getPlaceDate() { return placeDate; }
    public void setPlaceDate(LocalDateTime placeDate) { this.placeDate = placeDate; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}