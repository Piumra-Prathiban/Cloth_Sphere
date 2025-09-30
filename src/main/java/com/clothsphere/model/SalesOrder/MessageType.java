package com.clothsphere.model.SalesOrder;

public enum MessageType {
    INFO("Information"),
    STATUS_UPDATE("Status Update"),
    URGENT("Urgent"),
    DELIVERY_UPDATE("Delivery Update"),
    QUERY("Query"),
    CONFIRMATION("Confirmation");

    private final String displayName;

    MessageType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}