package com.clothsphere.model.CPM;

import com.clothsphere.model.PM.Product;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_pricing_history")
public class ProductPricingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "old_price", nullable = false)
    private Double oldPrice;

    @Column(name = "new_price", nullable = false)
    private Double newPrice;

    @Column(name = "change_reason", length = 500)
    private String changeReason;

    @Column(name = "changed_by", length = 50, nullable = false)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    // Constructors
    public ProductPricingHistory() {
        this.changedAt = LocalDateTime.now();
    }

    public ProductPricingHistory(String productId, Double oldPrice, Double newPrice,
                                String changeReason, String changedBy) {
        this.productId = productId;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.changeReason = changeReason;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Double getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(Double oldPrice) {
        this.oldPrice = oldPrice;
    }

    public Double getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(Double newPrice) {
        this.newPrice = newPrice;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    @Override
    public String toString() {
        return "ProductPricingHistory{" +
                "historyId=" + historyId +
                ", productId='" + productId + '\'' +
                ", oldPrice=" + oldPrice +
                ", newPrice=" + newPrice +
                ", changedBy='" + changedBy + '\'' +
                ", changedAt=" + changedAt +
                '}';
    }
}
