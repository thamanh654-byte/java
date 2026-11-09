package com.store.service;

import com.store.config.DatabaseConnection;
import com.store.dao.OrderDAO;
import com.store.dao.OrderDAOImpl;
import com.store.dao.ProductDAO;
import com.store.dao.ProductDAOImpl;
import com.store.exception.InvalidInputException;
import com.store.exception.OutOfStockException;
import com.store.exception.ProductNotFoundException;
import com.store.model.Order;
import com.store.model.OrderDetail;
import com.store.model.Product;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Lớp Service xử lý toàn bộ logic nghiệp vụ đơn hàng & Quản lý JDBC Transaction thủ công
 */
public class OrderService {
    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;

    public OrderService() {
        this.orderDAO = new OrderDAOImpl();
        this.productDAO = new ProductDAOImpl();
    }

    /**
     * Nghiệp vụ thanh toán với Transaction thủ công bằng JDBC:
     * 1. setAutoCommit(false) để mở Transaction.
     * 2. Kiểm tra tồn kho từng sản phẩm (ném OutOfStockException nếu thiếu).
     * 3. Lưu vào orders, order_details và trừ tồn kho ở products.
     * 4. Thành công -> commit().
     * 5. Gặp bất kỳ ngoại lệ nào -> rollback() đảm bảo tính toàn vẹn dữ liệu tuyệt đối.
     */
    public int createOrderWithTransaction(String customerName, List<OrderDetail> cartItems) 
            throws InvalidInputException, ProductNotFoundException, OutOfStockException, SQLException {
        
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new InvalidInputException("Tên khách hàng không được để trống!");
        }
        if (cartItems == null || cartItems.isEmpty()) {
            throw new InvalidInputException("Giỏ hàng đang trống, không thể thanh toán!");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            
            // BẮT ĐẦU TRANSACTION THỦ CÔNG
            conn.setAutoCommit(false);

            double totalAmount = 0.0;

            // 1. Kiểm tra tồn kho của từng sản phẩm trong giỏ hàng
            for (OrderDetail item : cartItems) {
                Product p = productDAO.getProductById(conn, item.getProductId());
                if (p == null || !p.isStatus()) {
                    throw new ProductNotFoundException("Sản phẩm ID " + item.getProductId() + " không tồn tại hoặc đã ngừng kinh doanh!");
                }
                if (p.getQuantity() < item.getQuantity()) {
                    throw new OutOfStockException("Sản phẩm [" + p.getName() + "] không đủ hàng tồn kho! Còn lại: " 
                            + p.getQuantity() + " | Yêu cầu: " + item.getQuantity());
                }

                // Cập nhật giá theo thời điểm mua
                item.setUnitPrice(p.getPrice());
                item.setProductName(p.getName());
                totalAmount += item.getSubTotal();
            }

            // 2. Thêm hóa đơn (Order)
            Order order = new Order(customerName.trim(), totalAmount);
            int generatedOrderId = orderDAO.insertOrder(conn, order);

            // 3. Thêm chi tiết hóa đơn (OrderDetail) & Cập nhật trừ số lượng kho (Product)
            for (OrderDetail item : cartItems) {
                item.setOrderId(generatedOrderId);
                orderDAO.insertOrderDetail(conn, item);
                productDAO.decreaseStock(conn, item.getProductId(), item.getQuantity());
            }

            // 4. Nếu toàn bộ trơn tru -> COMMIT TRANSACTION
            conn.commit();
            return generatedOrderId;

        } catch (Exception ex) {
            // NẾU CÓ BẤT KỲ LỖI NÀO PHÁT SINH -> ROLLBACK TOÀN BỘ
            if (conn != null) {
                try {
                    System.err.println("⚠️ Đang thực hiện ROLLBACK transaction do phát sinh lỗi: " + ex.getMessage());
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("❌ Lỗi khi rollback: " + rollbackEx.getMessage());
                }
            }
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Trả lại chế độ auto-commit ban đầu
                    conn.close();             // Đóng kết nối
                } catch (SQLException closeEx) {
                    System.err.println("❌ Lỗi đóng kết nối: " + closeEx.getMessage());
                }
            }
        }
    }

    public List<Order> getAllOrders() throws SQLException {
        return orderDAO.getAllOrders();
    }

    public List<OrderDetail> getOrderDetails(int orderId) throws SQLException {
        return orderDAO.getOrderDetailsByOrderId(orderId);
    }

    public double getTotalRevenue() throws SQLException {
        return orderDAO.getTotalRevenue();
    }
}

