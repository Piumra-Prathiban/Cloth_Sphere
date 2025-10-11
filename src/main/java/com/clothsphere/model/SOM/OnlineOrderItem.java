package com.clothsphere.model.SOM;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "online_order_items")
public class OnlineOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OnlineOrder order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    // Product snapshot at time of order (in case product details change later)
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "product_code", nullable = false, length = 50)
    private String productCode;

    @Column(name = "product_image_path", length = 500)
    private String productImagePath;

    // Pricing details
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "item_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal itemTotal;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Constructors
    public OnlineOrderItem() {
        this.createdAt = LocalDateTime.now();
    }

    public OnlineOrderItem(OnlineOrder order, Long productId, String productName, String productCode,
                          String productImagePath, BigDecimal unitPrice, Integer quantity,
                          BigDecimal discountPercentage) {
        this.order = order;
        this.productId = productId;
        this.productName = productName;
        this.productCode = productCode;
        this.productImagePath = productImagePath;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.discountPercentage = discountPercentage != null ? discountPercentage : BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        calculateItemTotal();
    }

    // Getters and Setters
    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public OnlineOrder getOrder() {
        return order;
    }

    public void setOrder(OnlineOrder order) {
        this.order = order;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductImagePath() {
        return productImagePath;
    }

    public void setProductImagePath(String productImagePath) {
        this.productImagePath = productImagePath;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        calculateItemTotal();
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
        calculateItemTotal();
    }

    public BigDecimal getItemTotal() {
        return itemTotal;
    }

    public void setItemTotal(BigDecimal itemTotal) {
        this.itemTotal = itemTotal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Helper methods
    public void calculateItemTotal() {
        if (unitPrice != null && quantity != null) {
            BigDecimal subtotal = unitPrice.multiply(new BigDecimal(quantity));

            if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountAmount = subtotal.multiply(discountPercentage).divide(new BigDecimal(100));
                this.itemTotal = subtotal.subtract(discountAmount);
            } else {
                this.itemTotal = subtotal;
            }
        }
    }

    @Override
    public String toString() {
        return "OnlineOrderItem{" +
                "orderItemId=" + orderItemId +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", itemTotal=" + itemTotal +
                '}';
    }
}
