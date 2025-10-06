package com.clothsphere.model.SOM;

import java.io.Serializable;
import java.util.Objects;

public class OrderId implements Serializable {

    private String orderType;
    private String orderId;

    public OrderId() {}

    public OrderId(String orderType, String orderId) {
        this.orderType = orderType;
        this.orderId = orderId;
    }

    // Getters and Setters
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderId orderId1 = (OrderId) o;
        return Objects.equals(orderType, orderId1.orderType) &&
                Objects.equals(orderId, orderId1.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderType, orderId);
    }
}