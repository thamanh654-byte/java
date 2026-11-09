package com.store.model;

/**
 * POJO Entity đại diện cho bảng products trong cơ sở dữ liệu
 */
public class Product {
    private int id;
    private String name;
    private double price;
    private int quantity;
    private int categoryId;
    private String categoryName; // Hỗ trợ hiển thị tên danh mục
    private boolean status;      // true: đang kinh doanh, false: ngừng kinh doanh (xóa mềm)

    public Product() {
    }

    public Product(int id, String name, double price, int quantity, int categoryId, boolean status) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.categoryId = categoryId;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}

