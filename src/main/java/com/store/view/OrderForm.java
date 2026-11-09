package com.store.view;

import com.store.exception.InvalidInputException;
import com.store.exception.OutOfStockException;
import com.store.exception.ProductNotFoundException;
import com.store.model.Order;
import com.store.model.OrderDetail;
import com.store.model.Product;
import com.store.service.OrderService;
import com.store.service.ProductService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * FORM 3: Giao diện Bán Hàng & Lập Hóa Đơn (JDBC Transaction)
 */
public class OrderForm extends JFrame {
    private final OrderService orderService;
    private final ProductService productService;
    private final NumberFormat currencyFormatter;

    // Components Đặt hàng
    private JTextField txtCustomerName;
    private JComboBox<ProductComboItem> cmbProduct;
    private JSpinner spnQuantity;
    private JButton btnAddToCart;
    private JButton btnRemoveFromCart;

    // Components Giỏ hàng
    private JTable tblCart;
    private DefaultTableModel cartTableModel;
    private JLabel lblTotalCartAmount;
    private JButton btnCheckout;
    private final List<OrderDetail> cartItems = new ArrayList<>();
    private double currentTotal = 0.0;

    // Components Lịch sử hóa đơn & Thống kê
    private JTable tblOrders;
    private DefaultTableModel orderTableModel;
    private JTable tblOrderDetails;
    private DefaultTableModel orderDetailTableModel;
    private JLabel lblRevenue;
    private JButton btnBackToProducts;

    public OrderForm() {
        this.orderService = new OrderService();
        this.productService = new ProductService();
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        initComponents();
        loadProductComboBox();
        loadOrdersHistory();
        updateRevenue();
    }

    private void initComponents() {
        setTitle("FORM 3: BÁN HÀNG & LẬP HÓA ĐƠN (JDBC TRANSACTION)");
        setSize(1100, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        // 1. Header & Điều Hướng
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 135, 84));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 55));

        JLabel lblTitle = new JLabel("QUẢN LÝ BÁN HÀNG & LẬP HÓA ĐƠN", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.CENTER);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        navPanel.setBackground(new Color(25, 135, 84));
        btnBackToProducts = new JButton("<< Về Quản Lý Sản Phẩm");
        styleButton(btnBackToProducts);
        btnBackToProducts.setPreferredSize(new Dimension(200, 36));
        navPanel.add(btnBackToProducts);
        headerPanel.add(navPanel, BorderLayout.WEST);

        // 2. Khu vực Lập Hóa Đơn (Phía Trên)
        JPanel billingPanel = new JPanel(new BorderLayout(8, 8));
        billingPanel.setBorder(new TitledBorder("Tạo Hóa Đơn Mới"));
        billingPanel.setPreferredSize(new Dimension(0, 310));

        // Form chọn món và thông tin khách
        JPanel inputOrderPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        row1.add(new JLabel("Họ tên khách hàng (*):"));
        txtCustomerName = new JTextField(22);
        txtCustomerName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        row1.add(txtCustomerName);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        row2.add(new JLabel("Chọn sản phẩm:"));
        cmbProduct = new JComboBox<>();
        cmbProduct.setPreferredSize(new Dimension(320, 30));
        row2.add(cmbProduct);

        row2.add(new JLabel("Số lượng:"));
        spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 500, 1));
        spnQuantity.setPreferredSize(new Dimension(75, 30));
        row2.add(spnQuantity);

        btnAddToCart = new JButton("Thêm Vào Giỏ");
        styleButton(btnAddToCart);
        btnAddToCart.setPreferredSize(new Dimension(140, 32));
        row2.add(btnAddToCart);

        btnRemoveFromCart = new JButton("Xóa Món Chọn");
        styleButton(btnRemoveFromCart);
        btnRemoveFromCart.setPreferredSize(new Dimension(130, 32));
        row2.add(btnRemoveFromCart);

        inputOrderPanel.add(row1);
        inputOrderPanel.add(row2);

        // Bảng Giỏ hàng
        cartTableModel = new DefaultTableModel(
                new Object[]{"Mã SP", "Tên Sản Phẩm", "Đơn Giá", "Số Lượng", "Thành Tiền"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblCart = new JTable(cartTableModel);
        tblCart.setRowHeight(26);
        tblCart.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblCart.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        JScrollPane scrollCart = new JScrollPane(tblCart);

        // Thanh thanh toán
        JPanel checkoutPanel = new JPanel(new BorderLayout(10, 10));
        checkoutPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        lblTotalCartAmount = new JLabel("Tổng thành tiền: 0 VNĐ");
        lblTotalCartAmount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalCartAmount.setForeground(new Color(220, 53, 69));

        btnCheckout = new JButton("THANH TOÁN & LẬP HÓA ĐƠN (TRANSACTION)");
        styleButton(btnCheckout);
        btnCheckout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCheckout.setPreferredSize(new Dimension(380, 42));

        checkoutPanel.add(lblTotalCartAmount, BorderLayout.WEST);
        checkoutPanel.add(btnCheckout, BorderLayout.EAST);

        billingPanel.add(inputOrderPanel, BorderLayout.NORTH);
        billingPanel.add(scrollCart, BorderLayout.CENTER);
        billingPanel.add(checkoutPanel, BorderLayout.SOUTH);

        // 3. Khu vực Lịch Sử Hóa Đơn & Doanh Thu (Phía Dưới)
        JPanel historyContainer = new JPanel(new BorderLayout(5, 5));
        historyContainer.setBorder(new TitledBorder("Lịch Sử Hóa Đơn & Thống Kê"));

        JPanel revenuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        revenuePanel.setBackground(new Color(240, 248, 240));
        lblRevenue = new JLabel("TỔNG DOANH THU TOÀN CỬA HÀNG: 0 VNĐ");
        lblRevenue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblRevenue.setForeground(new Color(30, 130, 30));
        revenuePanel.add(lblRevenue);

        // Chia 2 bảng: Danh sách hóa đơn và Chi tiết món
        orderTableModel = new DefaultTableModel(
                new Object[]{"Mã HĐ", "Khách Hàng", "Tổng Tiền", "Ngày Tạo"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblOrders = new JTable(orderTableModel);
        tblOrders.setRowHeight(24);
        JScrollPane scrollOrders = new JScrollPane(tblOrders);
        scrollOrders.setBorder(new TitledBorder("Các hóa đơn gần đây (Click để xem chi tiết)"));

        orderDetailTableModel = new DefaultTableModel(
                new Object[]{"Mã SP", "Tên Sản Phẩm", "SL", "Đơn Giá", "Thành Tiền"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblOrderDetails = new JTable(orderDetailTableModel);
        tblOrderDetails.setRowHeight(24);
        JScrollPane scrollDetails = new JScrollPane(tblOrderDetails);
        scrollDetails.setBorder(new TitledBorder("Chi tiết các món trong hóa đơn được chọn"));

        JSplitPane splitHistory = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollOrders, scrollDetails);
        splitHistory.setResizeWeight(0.55);

        historyContainer.add(revenuePanel, BorderLayout.NORTH);
        historyContainer.add(splitHistory, BorderLayout.CENTER);

        // Sự kiện các nút
        btnAddToCart.addActionListener(e -> handleAddToCart());
        btnRemoveFromCart.addActionListener(e -> handleRemoveFromCart());
        btnCheckout.addActionListener(e -> handleCheckout());

        tblOrders.getSelectionModel().addListSelectionListener(e -> {
            int row = tblOrders.getSelectedRow();
            if (row >= 0) {
                int orderId = (int) tblOrders.getValueAt(row, 0);
                loadOrderDetails(orderId);
            }
        });

        btnBackToProducts.addActionListener(e -> {
            this.dispose();
            SwingUtilities.invokeLater(() -> new ProductManagementForm().setVisible(true));
        });

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(billingPanel, BorderLayout.CENTER);
        mainPanel.add(historyContainer, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void styleButton(JButton btn) {
        btn.setForeground(Color.BLACK);
        btn.setBackground(new Color(230, 230, 230));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void loadProductComboBox() {
        cmbProduct.removeAllItems();
        try {
            List<Product> list = productService.listAllProducts();
            for (Product p : list) {
                cmbProduct.addItem(new ProductComboItem(p.getId(), p.getName(), p.getPrice(), p.getQuantity()));
            }
        } catch (Exception e) {
            System.err.println("Lỗi load sản phẩm: " + e.getMessage());
        }
    }

    private void handleAddToCart() {
        ProductComboItem item = (ProductComboItem) cmbProduct.getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 sản phẩm!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int qty = (int) spnQuantity.getValue();
        if (qty > item.getQuantity()) {
            JOptionPane.showMessageDialog(this,
                    "Số lượng tồn kho không đủ! Hiện tại chỉ còn: " + item.getQuantity(),
                    "Kho không đủ hàng",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean exists = false;
        for (OrderDetail d : cartItems) {
            if (d.getProductId() == item.getId()) {
                if (d.getQuantity() + qty > item.getQuantity()) {
                    JOptionPane.showMessageDialog(this,
                            "Tổng số lượng mua (" + (d.getQuantity() + qty) + ") vượt quá kho còn lại (" + item.getQuantity() + ")!",
                            "Cảnh báo",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                d.setQuantity(d.getQuantity() + qty);
                exists = true;
                break;
            }
        }

        if (!exists) {
            OrderDetail detail = new OrderDetail(item.getId(), qty, item.getPrice());
            detail.setProductName(item.getName());
            cartItems.add(detail);
        }

        refreshCart();
    }

    private void handleRemoveFromCart() {
        int row = tblCart.getSelectedRow();
        if (row >= 0) {
            cartItems.remove(row);
            refreshCart();
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 món trong giỏ hàng để xóa!", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refreshCart() {
        cartTableModel.setRowCount(0);
        currentTotal = 0.0;
        for (OrderDetail d : cartItems) {
            cartTableModel.addRow(new Object[]{
                    d.getProductId(),
                    d.getProductName(),
                    currencyFormatter.format(d.getUnitPrice()),
                    d.getQuantity(),
                    currencyFormatter.format(d.getSubTotal())
            });
            currentTotal += d.getSubTotal();
        }
        lblTotalCartAmount.setText("Tổng thành tiền: " + currencyFormatter.format(currentTotal));
    }

    private void handleCheckout() {
        String customer = txtCustomerName.getText().trim();
        if (customer.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập họ tên khách hàng!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống! Vui lòng chọn món trước.", "Giỏ hàng rỗng", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Xác nhận tạo hóa đơn cho khách: " + customer + "\nTổng tiền thanh toán: " + currencyFormatter.format(currentTotal),
                "Xác nhận thanh toán (Transaction)",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int orderId = orderService.createOrderWithTransaction(customer, new ArrayList<>(cartItems));

                JOptionPane.showMessageDialog(this,
                        "THANH TOÁN THÀNH CÔNG!\n" +
                        "Mã hóa đơn: #" + orderId + "\n" +
                        "Dữ liệu đã được COMMIT an toàn vào MySQL và tồn kho đã được trừ.",
                        "Giao dịch hoàn tất",
                        JOptionPane.INFORMATION_MESSAGE);

                cartItems.clear();
                refreshCart();
                txtCustomerName.setText("");
                spnQuantity.setValue(1);

                loadProductComboBox();
                loadOrdersHistory();
                updateRevenue();

            } catch (OutOfStockException | ProductNotFoundException | InvalidInputException ex) {
                JOptionPane.showMessageDialog(this,
                        "GIAO DỊCH THẤT BẠI: " + ex.getMessage() + "\n" +
                        "Hệ thống đã ROLLBACK toàn bộ dữ liệu!",
                        "Lỗi giao dịch",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadOrdersHistory() {
        orderTableModel.setRowCount(0);
        orderDetailTableModel.setRowCount(0);
        try {
            List<Order> list = orderService.getAllOrders();
            for (Order o : list) {
                orderTableModel.addRow(new Object[]{
                        o.getId(),
                        o.getCustomerName(),
                        currencyFormatter.format(o.getTotalAmount()),
                        o.getCreatedAt()
                });
            }
        } catch (Exception e) {
            System.err.println("Lỗi load hóa đơn: " + e.getMessage());
        }
    }

    private void loadOrderDetails(int orderId) {
        orderDetailTableModel.setRowCount(0);
        try {
            List<OrderDetail> details = orderService.getOrderDetails(orderId);
            for (OrderDetail d : details) {
                orderDetailTableModel.addRow(new Object[]{
                        d.getProductId(),
                        d.getProductName(),
                        d.getQuantity(),
                        currencyFormatter.format(d.getUnitPrice()),
                        currencyFormatter.format(d.getSubTotal())
                });
            }
        } catch (Exception e) {
            System.err.println("Lỗi load chi tiết hóa đơn: " + e.getMessage());
        }
    }

    private void updateRevenue() {
        try {
            double rev = orderService.getTotalRevenue();
            lblRevenue.setText("TỔNG DOANH THU TOÀN CỬA HÀNG: " + currencyFormatter.format(rev));
        } catch (Exception e) {
            System.err.println("Lỗi tính doanh thu: " + e.getMessage());
        }
    }

    public static class ProductComboItem {
        private final int id;
        private final String name;
        private final double price;
        private final int quantity;

        public ProductComboItem(int id, String name, double price, int quantity) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }

        @Override
        public String toString() {
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return name + " (" + nf.format(price) + " | Kho: " + quantity + ")";
        }
    }
}
