package com.clothsphere.model.ProductManagement;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Products {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    private String category;

    @Column(length = 500)
    private String description;

    @Column(precision = 10, scale = 2)  // ✅ SQL Server -> DECIMAL(10,2)
    private BigDecimal price;

    private Integer stock;

    // ---- Constructors ----
    public Products() {}

    public Products(String name, String code, String category, String description, BigDecimal price, Integer stock) {
        this.name = name;
        this.code = code;
        this.category = category;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    // ---- Getters & Setters ----
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {   // ✅ return BigDecimal, not Double
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
