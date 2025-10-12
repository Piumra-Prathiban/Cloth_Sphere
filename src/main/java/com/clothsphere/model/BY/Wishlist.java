package com.clothsphere.model.BY;

import com.clothsphere.model.CPM.Product;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"buyer_id", "product_id"})
})
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id")
    private Long wishlistId;

    @Column(name = "buyer_id", nullable = false, length = 10)
    private String buyerId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    // Transient field for displaying product details (fetched via service)
    @Transient
    private Product product;

    // Constructors
    public Wishlist() {
        this.addedAt = LocalDateTime.now();
    }

    public Wishlist(String buyerId, Long productId) {
        this.buyerId = buyerId;
        this.productId = productId;
        this.addedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getWishlistId() {
        return wishlistId;
    }

    public void setWishlistId(Long wishlistId) {
        this.wishlistId = wishlistId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public String toString() {
        return "Wishlist{" +
                "wishlistId=" + wishlistId +
                ", buyerId='" + buyerId + '\'' +
                ", productId=" + productId +
                ", addedAt=" + addedAt +
                '}';
    }
}
