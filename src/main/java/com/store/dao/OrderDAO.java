package com.store.dao;

import com.store.model.Order;
import com.store.model.OrderDetail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Giao diện DAO quản lý tạo đơn hàng, chi tiết đơn hàng và truy vấn doanh thu
 */
public interface OrderDAO {
    int insertOrder(Connection conn, Order order) throws SQLException;
    void insertOrderDetail(Connection conn, OrderDetail detail) throws SQLException;
    List<Order> getAllOrders() throws SQLException;
    List<OrderDetail> getOrderDetailsByOrderId(int orderId) throws SQLException;
    double getTotalRevenue() throws SQLException;
}

