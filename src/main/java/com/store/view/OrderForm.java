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
 */
public class OrderForm extends JFrame {
    private final OrderService orderService;
    private final ProductService productService;
    private final NumberFormat currencyFormatter;

    // Components Dat hang
    private JTextField txtCustomerName;
    private JComboBox<ProductComboItem> cmbProduct;
    private JSpinner spnQuantity;
    private JButton btnAddToCart;
    private JButton btnRemoveFromCart;

    // Components Gio hang
    private JTable tblCart;
    private DefaultTableModel cartTableModel;
    private JLabel lblTotalCartAmount;
    private JButton btnCheckout;
    private final List<OrderDetail> cartItems = new ArrayList<>();
    private double currentTotal = 0.0;

    // Components Lich su hoa don & Thong ke
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
        setTitle("FORM 3: BAN HANG & LAP HOA DON (JDBC TRANSACTION)");
        setSize(1100, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        // 1. Header & Dieu Huong
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 135, 84));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 55));

        JLabel lblTitle = new JLabel("QUAN LY BAN HANG & LAP HOA DON", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.CENTER);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        navPanel.setBackground(new Color(25, 135, 84));
        btnBackToProducts = new JButton("<< Ve Quan Ly San Pham");
        styleButton(btnBackToProducts);
        btnBackToProducts.setPreferredSize(new Dimension(200, 36));
        navPanel.add(btnBackToProducts);
        headerPanel.add(navPanel, BorderLayout.WEST);

        // 2. Khu vuc Lap Hoa Don (Phia Tren)
        JPanel billingPanel = new JPanel(new BorderLayout(8, 8));
        billingPanel.setBorder(new TitledBorder("Tao Hoa Don Moi"));
        billingPanel.setPreferredSize(new Dimension(0, 310));

        // Form chon mon va thong tin khach
        JPanel inputOrderPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        row1.add(new JLabel("Ho ten khach hang (*):"));
        txtCustomerName = new JTextField(22);
        txtCustomerName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        row1.add(txtCustomerName);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        row2.add(new JLabel("Chon san pham:"));
        cmbProduct = new JComboBox<>();
        cmbProduct.setPreferredSize(new Dimension(320, 30));
        row2.add(cmbProduct);

        row2.add(new JLabel("So luong:"));
        spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 500, 1));
        spnQuantity.setPreferredSize(new Dimension(75, 30));
        row2.add(spnQuantity);

        btnAddToCart = new JButton("Them Vao Gio");
        styleButton(btnAddToCart);
        btnAddToCart.setPreferredSize(new Dimension(140, 32));
        row2.add(btnAddToCart);

        btnRemoveFromCart = new JButton("Xoa Mon Chon");
        styleButton(btnRemoveFromCart);
        btnRemoveFromCart.setPreferredSize(new Dimension(130, 32));
        row2.add(btnRemoveFromCart);

        inputOrderPanel.add(row1);
        inputOrderPanel.add(row2);

        // Bang Gio hang
        cartTableModel = new DefaultTableModel(
                new Object[]{"Ma SP", "Ten San Pham", "Don Gia", "So Luong", "Thanh Tien"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblCart = new JTable(cartTableModel);
        tblCart.setRowHeight(26);
        tblCart.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblCart.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        JScrollPane scrollCart = new JScrollPane(tblCart);

        // Thanh thanh toan
        JPanel checkoutPanel = new JPanel(new BorderLayout(10, 10));
        checkoutPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        lblTotalCartAmount = new JLabel("Tong thanh tien: 0 VND");
        lblTotalCartAmount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalCartAmount.setForeground(new Color(220, 53, 69));

        btnCheckout = new JButton("THANH TOAN & LAP HOA DON (TRANSACTION)");
        styleButton(btnCheckout);
        btnCheckout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCheckout.setPreferredSize(new Dimension(380, 42));

        checkoutPanel.add(lblTotalCartAmount, BorderLayout.WEST);
        checkoutPanel.add(btnCheckout, BorderLayout.EAST);

        billingPanel.add(inputOrderPanel, BorderLayout.NORTH);
        billingPanel.add(scrollCart, BorderLayout.CENTER);
        billingPanel.add(checkoutPanel, BorderLayout.SOUTH);

        // 3. Khu vuc Lich Su Hoa Don & Doanh Thu (Phia Duoi)
        JPanel historyContainer = new JPanel(new BorderLayout(5, 5));
        historyContainer.setBorder(new TitledBorder("Lich Su Hoa Don & Thong Ke"));

        JPanel revenuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        revenuePanel.setBackground(new Color(240, 248, 240));
        lblRevenue = new JLabel("TONG DOANH THU TOAN CUA HANG: 0 VND");
        lblRevenue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblRevenue.setForeground(new Color(30, 130, 30));
        revenuePanel.add(lblRevenue);

        // Chia 2 bang: Danh sach hoa don va Chi tiet mon
        orderTableModel = new DefaultTableModel(
                new Object[]{"Ma HD", "Khach Hang", "Tong Tien", "Ngay Tao"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblOrders = new JTable(orderTableModel);
        tblOrders.setRowHeight(24);
        JScrollPane scrollOrders = new JScrollPane(tblOrders);
        scrollOrders.setBorder(new TitledBorder("Cac hoa don gan day (Click de xem chi tiet)"));

        orderDetailTableModel = new DefaultTableModel(
                new Object[]{"Ma SP", "Ten San Pham", "SL", "Don Gia", "Thanh Tien"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblOrderDetails = new JTable(orderDetailTableModel);
        tblOrderDetails.setRowHeight(24);
        JScrollPane scrollDetails = new JScrollPane(tblOrderDetails);
        scrollDetails.setBorder(new TitledBorder("Chi tiet cac mon trong hoa don duoc chon"));

        JSplitPane splitHistory = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollOrders, scrollDetails);
        splitHistory.setResizeWeight(0.55);

        historyContainer.add(revenuePanel, BorderLayout.NORTH);
        historyContainer.add(splitHistory, BorderLayout.CENTER);

        // Su kien cac nut
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
                    "Kho khong du hang",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean exists = false;
        for (OrderDetail d : cartItems) {
            if (d.getProductId() == item.getId()) {
                if (d.getQuantity() + qty > item.getQuantity()) {
                    JOptionPane.showMessageDialog(this,
                            "Tong so luong mua (" + (d.getQuantity() + qty) + ") vuot qua kho con lai (" + item.getQuantity() + ")!",
                            "Canh bao",
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
        lblTotalCartAmount.setText("Tong thanh tien: " + currencyFormatter.format(currentTotal));
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
                        "Giao dich hoan tat",
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
                        "GIAO DICH THAT BAI: " + ex.getMessage() + "\n" +
                        "He thong da ROLLBACK toan bo du lieu!",
                        "Loi giao dich",
                        JOptionPane.ERROR_MESSAGE);
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
            lblRevenue.setText("TONG DOANH THU TOAN CUA HANG: " + currencyFormatter.format(rev));
        } catch (Exception e) {
            System.err.println("Loi tinh doanh thu: " + e.getMessage());
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
