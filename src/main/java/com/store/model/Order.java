package com.store.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * POJO Entity đại diện cho bảng orders trong cơ sở dữ liệu
 */
public class Order {
    private int id;
    private String customerName;
    private double totalAmount;
    private Timestamp createdAt;
    private List<OrderDetail> orderDetails = new ArrayList<>();

    public Order() {
    }

    public Order(String customerName, double totalAmount) {
        this.customerName = customerName;
        this.totalAmount = totalAmount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }
}

