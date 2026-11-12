package com.store.dao;

import com.store.config.DatabaseConnection;
import com.store.model.Order;
import com.store.model.OrderDetail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderDAOImpl - khop voi cau truc thuc te cua MySQL:
 *   orders      : order_id (PK AI), created_at, total_amount, customer_name
 *   order_details: order_id (PK), product_id (PK), quantity, unit_price
 */
public class OrderDAOImpl implements OrderDAO {

    // ----------------------------------------------------------------
    // INSERT hoa don moi, tra ve order_id sinh ra
    // ----------------------------------------------------------------
    @Override
    public int insertOrder(Connection conn, Order order) throws SQLException {
        String sql = "INSERT INTO orders (customer_name, total_amount) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, order.getCustomerName());
            ps.setDouble(2, order.getTotalAmount());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("That bai khi lay order_id phat sinh tu database.");
    }

    // ----------------------------------------------------------------
    // INSERT chi tiet mon hang
    // ----------------------------------------------------------------
    @Override
    public void insertOrderDetail(Connection conn, OrderDetail detail) throws SQLException {
        String sql = "INSERT INTO order_details (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, detail.getOrderId());
            ps.setInt(2, detail.getProductId());
            ps.setInt(3, detail.getQuantity());
            ps.setDouble(4, detail.getUnitPrice());
            ps.executeUpdate();
        }
    }

    /**
     * Thêm nhiều dòng chi tiết trong một lần gửi (batch) thay vì N lần round-trip.
     */
    @Override
    public void insertOrderDetails(Connection conn, List<OrderDetail> details) throws SQLException {
        if (details == null || details.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO order_details (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (OrderDetail detail : details) {
                ps.setInt(1, detail.getOrderId());
                ps.setInt(2, detail.getProductId());
                ps.setInt(3, detail.getQuantity());
                ps.setDouble(4, detail.getUnitPrice());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // ----------------------------------------------------------------
    // Lay danh sach tat ca hoa don (order_id, customer_name, total_amount, created_at)
    // ----------------------------------------------------------------
    @Override
    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        // Dung order_id (khoa chinh thuc te), khong phai 'id'
        String sql = "SELECT order_id, customer_name, total_amount, created_at " +
                     "FROM orders ORDER BY order_id DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("order_id"));           // map sang field id cua model
                order.setCustomerName(rs.getString("customer_name"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setCreatedAt(rs.getTimestamp("created_at"));
                orders.add(order);
            }
        }
        return orders;
    }

    // ----------------------------------------------------------------
    // Lay chi tiet cac mon hang cua 1 hoa don
    // ----------------------------------------------------------------
    @Override
    public List<OrderDetail> getOrderDetailsByOrderId(int orderId) throws SQLException {
        List<OrderDetail> details = new ArrayList<>();
        String sql = "SELECT od.order_id, od.product_id, od.quantity, od.unit_price, " +
                     "       p.name AS product_name " +
                     "FROM order_details od " +
                     "JOIN products p ON od.product_id = p.product_id " +
                     "WHERE od.order_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDetail d = new OrderDetail();
                    d.setOrderId(rs.getInt("order_id"));
                    d.setProductId(rs.getInt("product_id"));
                    d.setProductName(rs.getString("product_name"));
                    d.setQuantity(rs.getInt("quantity"));
                    d.setUnitPrice(rs.getDouble("unit_price"));
                    details.add(d);
                }
            }
        }
        return details;
    }

    // ----------------------------------------------------------------
    // Tinh tong doanh thu
    // ----------------------------------------------------------------
    @Override
    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    // ----------------------------------------------------------------
    // Dem tong so hoa don (dung cho thong ke nhanh, khong can load het du lieu)
    // ----------------------------------------------------------------
    @Override
    public int countOrders() throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
