package com.clothsphere.model.SOM;

import jakarta.persistence.*;
@Entity
@Table(name = "buyers")
@IdClass(BuyerId.class)
public class Buyer {

    // PIUMARA:- ONLINE REGISTER BUYERS PID IS OBY(online Buyer)(OBY01, OBY02 like vise) not LBY(Local Buyer)

    @Id
    @Column(name = "buyer_id", length = 10)
    private String buyerId;

    @Id
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "company", length = 100)
    private String company;

    // Constructors
    public Buyer() {}

    public Buyer(String buyerId, String email, String customerName, String phone, String address, String company) {
        this.buyerId = buyerId;
        this.email = email;
        this.customerName = customerName;
        this.phone = phone;
        this.address = address;
        this.company = company;
    }

    // Getters and Setters
    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
}