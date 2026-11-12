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
import com.store.util.CartUtils;
import com.store.util.MoneyUtils;

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
        this(new OrderDAOImpl(), new ProductDAOImpl());
    }

    /** Constructor cho phép truyền DAO vào (dễ viết test / thay thế implementation). */
    public OrderService(OrderDAO orderDAO, ProductDAO productDAO) {
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
    }

    /**
     * Nghiệp vụ thanh toán với Transaction thủ công bằng JDBC:
     * 1. setAutoCommit(false) để mở Transaction.
     * 2. Khóa dòng sản phẩm (SELECT ... FOR UPDATE) và kiểm tra tồn kho
     *    (ném OutOfStockException nếu thiếu).
     * 3. Lưu vào orders, order_details và trừ tồn kho ở products (trừ nguyên tử, có điều kiện).
     * 4. Thành công -> commit().
     * 5. Gặp bất kỳ ngoại lệ nào -> rollback() đảm bảo tính toàn vẹn dữ liệu tuyệt đối.
     */
    public int createOrderWithTransaction(String customerName, List<OrderDetail> cartItems)
            throws InvalidInputException, ProductNotFoundException, OutOfStockException, SQLException {

        if (customerName == null || customerName.trim().isEmpty()) {
            throw new InvalidInputException("Tên khách hàng không được để trống!");
        }
        // Gộp các dòng trùng sản phẩm + chặn số lượng <= 0 (tránh "trừ kho âm")
        List<OrderDetail> items = CartUtils.normalize(cartItems);

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();

            // BẮT ĐẦU TRANSACTION THỦ CÔNG
            conn.setAutoCommit(false);
            try {
                double totalAmount = 0.0;

                // 1. Kiểm tra tồn kho của từng sản phẩm trong giỏ hàng
                for (OrderDetail item : items) {
                    // FOR UPDATE: giữ khóa dòng đến khi commit/rollback -> không bị giao dịch khác chen ngang
                    Product p = productDAO.getProductByIdForUpdate(conn, item.getProductId());
                    if (p == null || !p.isStatus()) {
                        throw new ProductNotFoundException("Sản phẩm ID " + item.getProductId()
                                + " không tồn tại hoặc đã ngừng kinh doanh!");
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
                totalAmount = MoneyUtils.round(totalAmount);

                // 2. Thêm hóa đơn (Order)
                Order order = new Order(customerName.trim(), totalAmount);
                int generatedOrderId = orderDAO.insertOrder(conn, order);

                // 3. Ghi toàn bộ chi tiết hóa đơn trong 1 batch, rồi trừ kho
                for (OrderDetail item : items) {
                    item.setOrderId(generatedOrderId);
                }
                orderDAO.insertOrderDetails(conn, items);

                for (OrderDetail item : items) {
                    if (!productDAO.decreaseStock(conn, item.getProductId(), item.getQuantity())) {
                        // Bị giao dịch khác lấy mất hàng trong lúc đang xử lý
                        throw new OutOfStockException("Sản phẩm [" + item.getProductName()
                                + "] vừa hết hàng, vui lòng thử lại!");
                    }
                }

                // 4. Nếu toàn bộ trơn tru -> COMMIT TRANSACTION
                conn.commit();
                return generatedOrderId;

            } catch (Exception ex) {
                // NẾU CÓ BẤT KỲ LỖI NÀO PHÁT SINH -> ROLLBACK TOÀN BỘ
                try {
                    System.err.println("⚠️ Đang thực hiện ROLLBACK transaction do phát sinh lỗi: " + ex.getMessage());
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("❌ Lỗi khi rollback: " + rollbackEx.getMessage());
                    ex.addSuppressed(rollbackEx);
                }
                throw ex;
            } finally {
                try {
                    conn.setAutoCommit(previousAutoCommit); // Trả lại chế độ auto-commit ban đầu
                } catch (SQLException closeEx) {
                    System.err.println("❌ Lỗi khôi phục auto-commit: " + closeEx.getMessage());
                }
            }
            // try-with-resources tự đóng (trả connection về pool)
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

    public int getOrderCount() throws SQLException {
        return orderDAO.countOrders();
    }
}


