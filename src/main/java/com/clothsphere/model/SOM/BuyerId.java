package com.clothsphere.model.SOM;

import java.io.Serializable;
import java.util.Objects;

public class BuyerId implements Serializable {

    private String buyerId;
    private String email;

    public BuyerId() {}

    public BuyerId(String buyerId, String email) {
        this.buyerId = buyerId;
        this.email = email;
    }

    // Getters and Setters
    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuyerId buyerId1 = (BuyerId) o;
        return Objects.equals(buyerId, buyerId1.buyerId) &&
                Objects.equals(email, buyerId1.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buyerId, email);
    }
}