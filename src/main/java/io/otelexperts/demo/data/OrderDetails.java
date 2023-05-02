package io.otelexperts.demo.data;

import java.math.BigDecimal;

public class OrderDetails {

    private int orderId = 0;

    private double amount = 0;

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
