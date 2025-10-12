package com.clothsphere.model.BY;

import com.clothsphere.model.CPM.Product;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"buyer_id", "product_id"})
})
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;

    @Column(name = "buyer_id", nullable = false, length = 10)
    private String buyerId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    // Transient fields for displaying product details (fetched via service)
    @Transient
    private Product product;

    // Constructors
    public Cart() {
        this.addedAt = LocalDateTime.now();
    }

    public Cart(String buyerId, Long productId, Integer quantity) {
        this.buyerId = buyerId;
        this.productId = productId;
        this.quantity = quantity;
        this.addedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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
        return "Cart{" +
                "cartId=" + cartId +
                ", buyerId='" + buyerId + '\'' +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", addedAt=" + addedAt +
                '}';
    }
}
