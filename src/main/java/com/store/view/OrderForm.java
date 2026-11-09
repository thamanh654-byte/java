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
 * FORM 3: Giao dien Ban Hang & Lap Hoa Don (JDBC Transaction)
 * Layout: BorderLayout chinh, JSplitPane doc chia phan tren/duoi
 */
public class OrderForm extends JFrame {
    private final OrderService orderService;
    private final ProductService productService;
    private final NumberFormat currencyFormatter;

    // Components nhap don hang
    private JTextField txtCustomerName;
    private JComboBox<ProductComboItem> cmbProduct;
    private JSpinner spnQuantity;
    private JButton btnAddToCart;
    private JButton btnRemoveFromCart;

    // Components gio hang
    private JTable tblCart;
    private DefaultTableModel cartTableModel;
    private JLabel lblTotalCartAmount;
    private JButton btnCheckout;
    private final List<OrderDetail> cartItems = new ArrayList<>();
    private double currentTotal = 0.0;

    // Components lich su hoa don & thong ke
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
        setTitle("FORM 3: BAN HANG & LAP HOA DON");
        setSize(1150, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));

        // ============================================================
        // NORTH: Header xanh la
        // ============================================================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 135, 84));
        headerPanel.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel lblTitle = new JLabel("QUAN LY BAN HANG & LAP HOA DON", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.CENTER);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        navPanel.setBackground(new Color(25, 135, 84));
        btnBackToProducts = new JButton("<< Ve Quan Ly San Pham");
        styleButton(btnBackToProducts);
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
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(25, 135, 84), 1),
            "TAO HOA DON MOI",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13), new Color(25, 135, 84)
        ));
        panel.setBackground(Color.WHITE);

        // --- Hang nhap lieu ---
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new EmptyBorder(6, 8, 4, 8));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 6, 5, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        // Hang 1: Ho ten khach hang
        JLabel lbCustomer = new JLabel("Ho ten khach hang (*):");
        lbCustomer.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        inputPanel.add(lbCustomer, g);

        txtCustomerName = new JTextField(28);
        txtCustomerName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g.gridx = 1; g.gridy = 0; g.weightx = 1.0; g.gridwidth = 4;
        inputPanel.add(txtCustomerName, g);

        // Hang 2: Chon san pham / so luong / them gio
        g.gridwidth = 1; g.weightx = 0;
        JLabel lbProduct = new JLabel("Chon san pham:");
        lbProduct.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g.gridx = 0; g.gridy = 1;
        inputPanel.add(lbProduct, g);

        cmbProduct = new JComboBox<>();
        cmbProduct.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g.gridx = 1; g.gridy = 1; g.weightx = 1.0;
        inputPanel.add(cmbProduct, g);

        JLabel lbQty = new JLabel("So luong:");
        lbQty.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g.gridx = 2; g.gridy = 1; g.weightx = 0;
        inputPanel.add(lbQty, g);

        spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 500, 1));
        spnQuantity.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spnQuantity.setPreferredSize(new Dimension(80, 30));
        g.gridx = 3; g.gridy = 1; g.weightx = 0;
        inputPanel.add(spnQuantity, g);

        JPanel btnCartPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnCartPanel.setBackground(Color.WHITE);
        btnAddToCart = new JButton("Them Vao Gio");
        styleButton(btnAddToCart);
        btnRemoveFromCart = new JButton("Xoa Mon Chon");
        styleButton(btnRemoveFromCart);
        btnCartPanel.add(btnAddToCart);
        btnCartPanel.add(btnRemoveFromCart);
        g.gridx = 4; g.gridy = 1; g.weightx = 0;
        inputPanel.add(btnCartPanel, g);

        // --- Bang gio hang ---
        cartTableModel = new DefaultTableModel(
            new Object[]{"Ma SP", "Ten San Pham", "Don Gia", "So Luong", "Thanh Tien"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblCart = new JTable(cartTableModel);
        tblCart.setRowHeight(26);
        tblCart.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblCart.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblCart.setSelectionBackground(new Color(173, 216, 230));
        JScrollPane scrollCart = new JScrollPane(tblCart);

        // --- Thanh tong tien & thanh toan ---
        JPanel checkoutStrip = new JPanel(new BorderLayout(10, 0));
        checkoutStrip.setBackground(new Color(248, 249, 250));
        checkoutStrip.setBorder(new EmptyBorder(8, 12, 8, 12));

        lblTotalCartAmount = new JLabel("Tong tien: 0 VND");
        lblTotalCartAmount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalCartAmount.setForeground(new Color(220, 53, 69));
        checkoutStrip.add(lblTotalCartAmount, BorderLayout.WEST);

        btnCheckout = new JButton("XAC NHAN THANH TOAN (LUU DB)");
        styleButton(btnCheckout);
        btnCheckout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCheckout.setBackground(new Color(25, 135, 84));
        btnCheckout.setForeground(Color.WHITE);
        btnCheckout.setPreferredSize(new Dimension(340, 40));
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
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(108, 117, 125), 1),
            "LICH SU HOA DON & THONG KE",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13), new Color(50, 50, 50)
        ));
        panel.setBackground(Color.WHITE);

        // Thanh doanh thu
        JPanel revenueStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        revenueStrip.setBackground(new Color(232, 245, 232));
        revenueStrip.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(25, 135, 84)));
        lblRevenue = new JLabel("TONG DOANH THU CUA HANG: 0 VND");
        lblRevenue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblRevenue.setForeground(new Color(20, 110, 20));
        revenueStrip.add(lblRevenue);

        // Bang danh sach hoa don
        orderTableModel = new DefaultTableModel(
            new Object[]{"Ma HD", "Khach Hang", "Tong Tien", "Ngay Tao"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblOrders = new JTable(orderTableModel);
        tblOrders.setRowHeight(24);
        tblOrders.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblOrders.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblOrders.setSelectionBackground(new Color(173, 216, 230));
        JScrollPane scrollOrders = new JScrollPane(tblOrders);
        scrollOrders.setBorder(BorderFactory.createTitledBorder("Danh sach hoa don da ban (Click de xem chi tiet)"));

        // Bang chi tiet mon
        orderDetailTableModel = new DefaultTableModel(
            new Object[]{"Ma SP", "Ten San Pham", "SL", "Don Gia", "Thanh Tien"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblOrderDetails = new JTable(orderDetailTableModel);
        tblOrderDetails.setRowHeight(24);
        tblOrderDetails.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblOrderDetails.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        JScrollPane scrollDetails = new JScrollPane(tblOrderDetails);
        scrollDetails.setBorder(BorderFactory.createTitledBorder("Chi tiet mon trong hoa don duoc chon"));

        JSplitPane splitTables = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollOrders, scrollDetails);
        splitTables.setResizeWeight(0.5);
        splitTables.setDividerSize(5);

        panel.add(revenueStrip, BorderLayout.NORTH);
        panel.add(splitTables, BorderLayout.CENTER);
        return panel;
    }

    // ------------------------------------------------------------------
    // Helper: style button chuan
    // ------------------------------------------------------------------
    private void styleButton(JButton btn) {
        btn.setForeground(Color.BLACK);
        btn.setBackground(new Color(230, 230, 230));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
            JOptionPane.showMessageDialog(this, "Vui long chon 1 san pham!", "Thong bao", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int qty = (int) spnQuantity.getValue();
        if (qty > item.getQuantity()) {
            JOptionPane.showMessageDialog(this,
                "So luong ton kho khong du! Hien tai chi con: " + item.getQuantity(),
                "Kho khong du hang", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean exists = false;
        for (OrderDetail d : cartItems) {
            if (d.getProductId() == item.getId()) {
                if (d.getQuantity() + qty > item.getQuantity()) {
                    JOptionPane.showMessageDialog(this,
                        "Tong so luong mua (" + (d.getQuantity() + qty) + ") vuot qua kho con lai (" + item.getQuantity() + ")!",
                        "Canh bao", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Vui long chon 1 mon trong gio hang de xoa!", "Chua chon", JOptionPane.WARNING_MESSAGE);
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
        lblTotalCartAmount.setText("Tong tien: " + currencyFormatter.format(currentTotal));
    }

    private void handleCheckout() {
        String customer = txtCustomerName.getText().trim();
        if (customer.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap ho ten khach hang!", "Thieu thong tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Gio hang dang trong! Vui long chon mon truoc.", "Gio hang rong", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Xac nhan tao hoa don cho khach: " + customer + "\nTong tien thanh toan: " + currencyFormatter.format(currentTotal),
            "Xac nhan thanh toan (Transaction)",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int orderId = orderService.createOrderWithTransaction(customer, new ArrayList<>(cartItems));
                JOptionPane.showMessageDialog(this,
                    "THANH TOAN THANH CONG!\n" +
                    "Ma hoa don: #" + orderId + "\n" +
                    "Du lieu da duoc COMMIT an toan vao MySQL va ton kho da duoc tru.",
                    "Giao dich hoan tat", JOptionPane.INFORMATION_MESSAGE);

                cartItems.clear();
                refreshCart();
                txtCustomerName.setText("");
                spnQuantity.setValue(1);
                loadProductComboBox();
                loadOrdersHistory();
                updateRevenue();

            } catch (OutOfStockException | ProductNotFoundException | InvalidInputException ex) {
                JOptionPane.showMessageDialog(this,
                    "GIAO DICH THAT BAI: " + ex.getMessage() + "\nHe thong da ROLLBACK toan bo du lieu!",
                    "Loi giao dich", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi he thong: " + ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
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
            lblRevenue.setText("TONG DOANH THU CUA HANG: " + currencyFormatter.format(rev));
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
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return name + " (" + nf.format(price) + " | Kho: " + quantity + ")";
        }
    }
}
