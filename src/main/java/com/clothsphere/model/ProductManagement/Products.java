package com.clothsphere.model.ProductManagement;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")   //set table name for entity
public class Products {

    @Id //primary key for entity
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

    @Column(length = 255) // store image path
    private String image;

    // ---- Constructors ----
    public Products() {}

    public Products(String name, String code, String category, String description, BigDecimal price, Integer stock, String image) {
        this.name = name;
        this.code = code;
        this.category = category;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.image = image;
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

    public BigDecimal getPrice() {
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

    // ---- Image Getter & Setter ----
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}