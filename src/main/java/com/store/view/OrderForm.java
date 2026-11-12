package com.store.view;

import com.store.exception.InvalidInputException;
import com.store.exception.OutOfStockException;
import com.store.exception.ProductNotFoundException;
import com.store.model.Order;
import com.store.model.OrderDetail;
import com.store.model.Product;
import com.store.service.OrderService;
import com.store.service.ProductService;
import com.store.util.MoneyUtils;
import com.store.util.UiUtils;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * FORM 3: Giao dien Ban Hang & Lap Hoa Don (JDBC Transaction)
 * Layout: BorderLayout chinh, JSplitPane doc chia phan tren/duoi
 */
public final class OrderForm extends JFrame {
    private static final long serialVersionUID = 1L;

    private final transient OrderService orderService;
    private final transient ProductService productService;
    private final transient NumberFormat currencyFormatter;

    // Components nhap don hang
    private transient JTextField txtCustomerName;
    private transient JComboBox<ProductComboItem> cmbProduct;
    private transient JSpinner spnQuantity;
    private transient JButton btnAddToCart;
    private transient JButton btnRemoveFromCart;

    // Components gio hang
    private transient JTable tblCart;
    private transient DefaultTableModel cartTableModel;
    private transient JLabel lblTotalCartAmount;
    private transient JButton btnCheckout;
    private transient List<OrderDetail> cartItems = new ArrayList<>();
    private transient double currentTotal = 0.0;

    // Components lich su hoa don & thong ke
    private transient JTable tblOrders;
    private transient DefaultTableModel orderTableModel;
    private transient JTable tblOrderDetails;
    private transient DefaultTableModel orderDetailTableModel;
    private transient JLabel lblRevenue;
    private transient JButton btnBackToProducts;

    public OrderForm() {
        this.orderService = new OrderService();
        this.productService = new ProductService();
        this.currencyFormatter = NumberFormat.getCurrencyInstance(MoneyUtils.VIETNAM);
        this.currencyFormatter.setMaximumFractionDigits(0);

        initComponents();
        loadProductComboBox();
        loadOrdersHistory();
        updateRevenue();
    }

    private void initComponents() {
        setTitle("Bán hàng - Hệ thống Quản lý cửa hàng");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(960, 640));

        JPanel headerPanel = UiUtils.header(
                "BÁN HÀNG & LẬP HÓA ĐƠN",
                "Bước 3/3  ·  Lập hóa đơn, trừ kho và xem doanh thu");

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        navPanel.setOpaque(false);
        btnBackToProducts = UiUtils.button("<  Quản lý sản phẩm", UiUtils.NEUTRAL);
        navPanel.add(btnBackToProducts);
        headerPanel.add(navPanel, BorderLayout.WEST);

        // ============================================================
        // PHAN TREN: Lap Hoa Don Moi
        // ============================================================
        JPanel billingPanel = buildBillingPanel();

        // ============================================================
        // PHAN DUOI: Lich Su Hoa Don & Thong Ke
        // ============================================================
        JPanel historyPanel = buildHistoryPanel();

        // JSplitPane doc chia phan tren / phan duoi
        JSplitPane splitMain = new JSplitPane(JSplitPane.VERTICAL_SPLIT, billingPanel, historyPanel);
        splitMain.setResizeWeight(0.48);
        splitMain.setDividerSize(6);
        splitMain.setOneTouchExpandable(true);

        // ============================================================
        // Lap rap khung chinh
        // ============================================================
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UiUtils.BG);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(splitMain, BorderLayout.CENTER);
        setContentPane(mainPanel);

        // ============================================================
        // Su kien
        // ============================================================
        btnAddToCart.addActionListener(e -> handleAddToCart());
        btnRemoveFromCart.addActionListener(e -> handleRemoveFromCart());
        btnCheckout.addActionListener(e -> handleCheckout());

        tblOrders.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblOrders.getSelectedRow();
                if (row >= 0) {
                    int orderId = (int) tblOrders.getValueAt(row, 0);
                    loadOrderDetails(orderId);
                }
            }
        });

        btnBackToProducts.addActionListener(e -> {
            this.dispose();
            SwingUtilities.invokeLater(() -> new ProductManagementForm().setVisible(true));
        });
    }

    // ------------------------------------------------------------------
    // Panel LAP HOA DON MOI (phan tren split)
    // ------------------------------------------------------------------
    private JPanel buildBillingPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(UiUtils.cardBorder("Tạo hóa đơn mới"));
        panel.setBackground(UiUtils.CARD);

        // --- Hàng nhập liệu ---
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setOpaque(false);
        inputPanel.setBorder(new EmptyBorder(4, 6, 2, 6));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 6, 5, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        // Hàng 1: họ tên khách hàng
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        inputPanel.add(UiUtils.label("Khách hàng"), g);

        txtCustomerName = new JTextField(28);
        UiUtils.pad(txtCustomerName);
        g.gridx = 1; g.gridy = 0; g.weightx = 1.0; g.gridwidth = 4;
        inputPanel.add(txtCustomerName, g);

        // Hàng 2: chọn sản phẩm / số lượng / thêm giỏ
        g.gridwidth = 1; g.weightx = 0;
        g.gridx = 0; g.gridy = 1;
        inputPanel.add(UiUtils.label("Sản phẩm"), g);

        cmbProduct = new JComboBox<>();
        UiUtils.pad(cmbProduct);
        g.gridx = 1; g.gridy = 1; g.weightx = 1.0;
        inputPanel.add(cmbProduct, g);

        g.gridx = 2; g.gridy = 1; g.weightx = 0;
        inputPanel.add(UiUtils.label("Số lượng"), g);

        spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 500, 1));
        UiUtils.pad(spnQuantity);
        spnQuantity.setPreferredSize(new Dimension(80, 30));
        g.gridx = 3; g.gridy = 1; g.weightx = 0;
        inputPanel.add(spnQuantity, g);

        JPanel btnCartPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnCartPanel.setOpaque(false);
        btnAddToCart = UiUtils.button("Thêm vào giỏ", UiUtils.PRIMARY);
        btnRemoveFromCart = UiUtils.button("Xóa món", UiUtils.NEUTRAL);
        btnCartPanel.add(btnAddToCart);
        btnCartPanel.add(btnRemoveFromCart);
        g.gridx = 4; g.gridy = 1; g.weightx = 0;
        inputPanel.add(btnCartPanel, g);

        // --- Bang gio hang ---
        cartTableModel = new DefaultTableModel(
            new Object[]{"Mã SP", "Tên sản phẩm", "Đơn giá", "Số lượng", "Thành tiền"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblCart = new JTable(cartTableModel);
        UiUtils.styleTable(tblCart);
        UiUtils.applyStripeRenderer(tblCart);
        JScrollPane scrollCart = new JScrollPane(tblCart);
        scrollCart.getViewport().setBackground(UiUtils.CARD);

        // --- Thanh tổng tiền & thanh toán ---
        JPanel checkoutStrip = new JPanel(new BorderLayout(10, 0));
        checkoutStrip.setBackground(new Color(248, 250, 253));
        checkoutStrip.setBorder(new EmptyBorder(8, 12, 8, 12));

        lblTotalCartAmount = new JLabel("Tổng tiền: 0 đ");
        lblTotalCartAmount.setFont(UiUtils.FONT_MONEY);
        lblTotalCartAmount.setForeground(UiUtils.DANGER);
        checkoutStrip.add(lblTotalCartAmount, BorderLayout.WEST);

        btnCheckout = UiUtils.button("Thanh toán & lưu hóa đơn", UiUtils.ACCENT);
        btnCheckout.setPreferredSize(new Dimension(260, 42));
        checkoutStrip.add(btnCheckout, BorderLayout.EAST);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollCart, BorderLayout.CENTER);
        panel.add(checkoutStrip, BorderLayout.SOUTH);
        return panel;
    }

    // ------------------------------------------------------------------
    // Panel LICH SU HOA DON & THONG KE (phan duoi split)
    // ------------------------------------------------------------------
    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(UiUtils.cardBorder("Lịch sử hóa đơn & thống kê"));
        panel.setBackground(UiUtils.CARD);

        // Thanh doanh thu
        JPanel revenueStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        revenueStrip.setBackground(new Color(232, 245, 237));
        lblRevenue = new JLabel("Tổng doanh thu cửa hàng: 0 đ");
        lblRevenue.setFont(UiUtils.FONT_MONEY);
        lblRevenue.setForeground(new Color(16, 120, 72));
        revenueStrip.add(lblRevenue);

        // Bảng danh sách hóa đơn
        orderTableModel = new DefaultTableModel(
            new Object[]{"Mã HĐ", "Khách hàng", "Tổng tiền", "Ngày tạo"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblOrders = new JTable(orderTableModel);
        UiUtils.styleTable(tblOrders);
        UiUtils.applyStripeRenderer(tblOrders);
        JScrollPane scrollOrders = new JScrollPane(tblOrders);
        scrollOrders.setBorder(UiUtils.cardBorder("Hóa đơn đã bán  ·  bấm một dòng để xem chi tiết"));
        scrollOrders.getViewport().setBackground(UiUtils.CARD);

        // Bảng chi tiết món
        orderDetailTableModel = new DefaultTableModel(
            new Object[]{"Mã SP", "Tên sản phẩm", "Số lượng", "Đơn giá", "Thành tiền"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblOrderDetails = new JTable(orderDetailTableModel);
        UiUtils.styleTable(tblOrderDetails);
        UiUtils.applyStripeRenderer(tblOrderDetails);
        JScrollPane scrollDetails = new JScrollPane(tblOrderDetails);
        scrollDetails.setBorder(UiUtils.cardBorder("Chi tiết món của hóa đơn được chọn"));
        scrollDetails.getViewport().setBackground(UiUtils.CARD);

        JSplitPane splitTables = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollOrders, scrollDetails);
        splitTables.setResizeWeight(0.5);
        splitTables.setDividerSize(5);

        panel.add(revenueStrip, BorderLayout.NORTH);
        panel.add(splitTables, BorderLayout.CENTER);
        return panel;
    }

    // ------------------------------------------------------------------
    // Business logic
    // ------------------------------------------------------------------
    private void loadProductComboBox() {
        cmbProduct.removeAllItems();
        try {
            List<Product> list = productService.listAllProducts();
            for (Product p : list) {
                cmbProduct.addItem(new ProductComboItem(p.getId(), p.getName(), p.getPrice(), p.getQuantity()));
            }
        } catch (Exception e) {
            System.err.println("Loi load san pham: " + e.getMessage());
        }
    }

    private void handleAddToCart() {
        ProductComboItem item = (ProductComboItem) cmbProduct.getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sản phẩm!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int qty = (int) spnQuantity.getValue();
        if (qty > item.getQuantity()) {
            JOptionPane.showMessageDialog(this,
                "Không đủ hàng trong kho! Hiện chỉ còn: " + item.getQuantity(),
                "Không đủ hàng", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean exists = false;
        for (OrderDetail d : cartItems) {
            if (d.getProductId() == item.getId()) {
                if (d.getQuantity() + qty > item.getQuantity()) {
                    JOptionPane.showMessageDialog(this,
                        "Tổng số lượng mua (" + (d.getQuantity() + qty) + ") vượt quá kho còn lại (" + item.getQuantity() + ")!",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một món trong giỏ hàng để xóa!", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
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
        lblTotalCartAmount.setText("Tổng tiền: " + currencyFormatter.format(currentTotal));
    }

    private void handleCheckout() {
        String customer = txtCustomerName.getText().trim();
        if (customer.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập họ tên khách hàng!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống! Vui lòng chọn món trước.", "Giỏ hàng trống", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Xác nhận tạo hóa đơn cho khách: " + customer + "\nTổng tiền thanh toán: " + currencyFormatter.format(currentTotal),
            "Xác nhận thanh toán",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int orderId = orderService.createOrderWithTransaction(customer, new ArrayList<>(cartItems));
                JOptionPane.showMessageDialog(this,
                    "Thanh toán thành công!\n" +
                    "Mã hóa đơn: #" + orderId + "\n" +
                    "Hóa đơn đã được lưu vào MySQL và tồn kho đã được trừ.",
                    "Giao dịch hoàn tất", JOptionPane.INFORMATION_MESSAGE);

                cartItems.clear();
                refreshCart();
                txtCustomerName.setText("");
                spnQuantity.setValue(1);
                loadProductComboBox();
                loadOrdersHistory();
                updateRevenue();

            } catch (OutOfStockException | ProductNotFoundException | InvalidInputException ex) {
                JOptionPane.showMessageDialog(this,
                    "Giao dịch thất bại: " + ex.getMessage() + "\nHệ thống đã hoàn tác toàn bộ, dữ liệu không bị thay đổi.",
                    "Lỗi giao dịch", JOptionPane.ERROR_MESSAGE);
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
                    o.getCreatedAt() == null ? "" : o.getCreatedAt().toString().substring(0, 16)
                });
            }
        } catch (Exception e) {
            System.err.println("Loi load hoa don: " + e.getMessage());
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
            System.err.println("Loi load chi tiet hoa don: " + e.getMessage());
        }
    }

    private void updateRevenue() {
        try {
            double rev = orderService.getTotalRevenue();
            lblRevenue.setText("Tổng doanh thu cửa hàng: " + currencyFormatter.format(rev));
        } catch (Exception e) {
            System.err.println("Loi tinh doanh thu: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Inner class
    // ------------------------------------------------------------------
    public static class ProductComboItem {
        private final int id;
        private final String name;
        private final double price;
        private final int quantity;

        public ProductComboItem(int id, String name, double price, int quantity) {
            this.id = id; this.name = name; this.price = price; this.quantity = quantity;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }

        @Override
        public String toString() {
            return name + " (" + MoneyUtils.format(price) + " | Kho: " + quantity + ")";
        }
    }
}
