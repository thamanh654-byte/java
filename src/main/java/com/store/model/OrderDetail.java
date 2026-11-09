package com.store.model;

/**
 * POJO Entity đại diện cho bảng order_details trong cơ sở dữ liệu
 */
public class OrderDetail {
    private int id;
    private int orderId;
    private int productId;
    private String productName; // Hỗ trợ hiển thị tên sản phẩm trên hóa đơn
    private int quantity;
    private double unitPrice;

    public OrderDetail() {
    }

    public OrderDetail(int productId, int quantity, double unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getSubTotal() {
        return this.quantity * this.unitPrice;
    }
}

