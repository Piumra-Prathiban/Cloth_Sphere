package com.clothsphere.model.SalesOrder;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "buyers")
public class Buyer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Buyer name is required")
    @Size(max = 100, message = "Buyer name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Size(max = 100, message = "Company name must not exceed 100 characters")
    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Enumerated(EnumType.STRING)
    @Column(name = "buyer_type", nullable = false)
    private BuyerType buyerType = BuyerType.INDIVIDUAL;

    @Column(name = "credit_limit", precision = 12, scale = 2)
    private java.math.BigDecimal creditLimit;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    @Column(name = "last_order_date")
    private LocalDateTime lastOrderDate;

    @Column(name = "total_orders")
    private Integer totalOrders = 0;

    @Column(name = "total_spent", precision = 12, scale = 2)
    private java.math.BigDecimal totalSpent = java.math.BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "preferred_payment_method")
    private String preferredPaymentMethod;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "buyer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Order> orders;

    // Constructors
    public Buyer() {
        this.registrationDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Buyer(String name, String email) {
        this();
        this.name = name;
        this.email = email;
    }

    public Buyer(String name, String email, String phone, String address) {
        this(name, email);
        this.phone = phone;
        this.address = address;
    }

    // Lifecycle callbacks
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Business methods
    public void incrementOrderCount() {
        this.totalOrders = (this.totalOrders == null) ? 1 : this.totalOrders + 1;
        this.lastOrderDate = LocalDateTime.now();
    }

    public void addToTotalSpent(java.math.BigDecimal amount) {
        if (this.totalSpent == null) {
            this.totalSpent = amount;
        } else {
            this.totalSpent = this.totalSpent.add(amount);
        }
    }

    public String getDisplayName() {
        if (buyerType == BuyerType.COMPANY && companyName != null && !companyName.trim().isEmpty()) {
            return companyName + " (" + name + ")";
        }
        return name;
    }

    public boolean canPlaceOrder(java.math.BigDecimal orderAmount) {
        if (!isActive) return false;
        if (creditLimit == null) return true;

        java.math.BigDecimal currentDebt = totalSpent != null ? totalSpent : java.math.BigDecimal.ZERO;
        return currentDebt.add(orderAmount).compareTo(creditLimit) <= 0;
    }

    // Getters and Setters
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public BuyerType getBuyerType() {
        return buyerType;
    }

    public void setBuyerType(BuyerType buyerType) {
        this.buyerType = buyerType;
    }

    public java.math.BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(java.math.BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getLastOrderDate() {
        return lastOrderDate;
    }

    public void setLastOrderDate(LocalDateTime lastOrderDate) {
        this.lastOrderDate = lastOrderDate;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public java.math.BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(java.math.BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPreferredPaymentMethod() {
        return preferredPaymentMethod;
    }

    public void setPreferredPaymentMethod(String preferredPaymentMethod) {
        this.preferredPaymentMethod = preferredPaymentMethod;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    @Override
    public String toString() {
        return "Buyer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", buyerType=" + buyerType +
                ", isActive=" + isActive +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Buyer)) return false;
        Buyer buyer = (Buyer) o;
        return id != null && id.equals(buyer.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

// Enum for buyer types
enum BuyerType {
    INDIVIDUAL("Individual"),
    COMPANY("Company"),
    RETAILER("Retailer"),
    WHOLESALER("Wholesaler"),
    MANUFACTURER("Manufacturer");

    private final String displayName;

    BuyerType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}