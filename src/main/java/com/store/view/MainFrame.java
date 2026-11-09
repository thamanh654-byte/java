package com.store.view;

import com.store.dao.CategoryDAO;
import com.store.dao.CategoryDAOImpl;
import com.store.exception.InvalidInputException;
import com.store.exception.OutOfStockException;
import com.store.exception.ProductNotFoundException;
import com.store.model.Category;
import com.store.model.Order;
import com.store.model.OrderDetail;
import com.store.model.Product;
import com.store.service.OrderService;
import com.store.service.ProductService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Giao diện đồ họa Desktop Java Swing cho Hệ Thống Quản Lý Cửa Hàng
 */
public class MainFrame extends JFrame {
    private final ProductService productService;
    private final OrderService orderService;
    private final CategoryDAO categoryDAO;
    private final NumberFormat currencyFormatter;

    // Components Tab Sản Phẩm
    private JTable tblProducts;
    private DefaultTableModel productTableModel;
    private JTextField txtProdId;
    private JTextField txtProdName;
    private JTextField txtProdPrice;
    private JTextField txtProdQuantity;
    private JComboBox<CategoryItem> cmbCategory;

    // Components Tab Bán Hàng
    private JTextField txtCustomerName;
    private JComboBox<ProductItem> cmbSellProduct;
    private JSpinner spnSellQuantity;
    private JTable tblCart;
    private DefaultTableModel cartTableModel;
    private JLabel lblTotalAmount;
    private final List<OrderDetail> currentCart = new ArrayList<>();
    private double currentCartTotal = 0.0;

    // Components Tab Lịch Sử Hóa Đơn & Doanh Thu
    private JTable tblOrders;
    private DefaultTableModel orderTableModel;
    private JTable tblOrderDetails;
    private DefaultTableModel orderDetailTableModel;
    private JLabel lblTotalRevenue;

    public MainFrame() {
        this.productService = new ProductService();
        this.orderService = new OrderService();
        this.categoryDAO = new CategoryDAOImpl();
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        initComponents();
        loadCategories();
        loadProductData();
        loadOrderData();
        updateRevenueDisplay();
    }

    private void initComponents() {
        setTitle("HỆ THỐNG QUẢN LÝ CỬA HÀNG - STORE MANAGEMENT");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(24, 119, 242));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 55));
        JLabel lblHeader = new JLabel("🏪 QUẢN LÝ CỬA HÀNG & BÁN HÀNG", JLabel.CENTER);
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerPanel.add(lblHeader);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tabbedPane.addTab("📦 Quản Lý Sản Phẩm", createProductPanel());
        tabbedPane.addTab("🛒 Lập Hóa Đơn Bán Hàng", createCheckoutPanel());
        tabbedPane.addTab("📊 Lịch Sử Hóa Đơn & Doanh Thu", createOrderHistoryPanel());

        // Lắng nghe sự kiện chuyển tab để làm mới dữ liệu
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                loadProductData();
            } else if (selectedIndex == 1) {
                loadSellProductsCombo();
            } else if (selectedIndex == 2) {
                loadOrderData();
                updateRevenueDisplay();
            }
        });

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(headerPanel, BorderLayout.NORTH);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
    }

    // =========================================================================
    // 1. TAB QUẢN LÝ SẢN PHẨM
    // =========================================================================
    private JPanel createProductPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Form nhập liệu bên trái
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder("Thông tin sản phẩm"));
        formPanel.setPreferredSize(new Dimension(380, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtProdId = new JTextField();
        txtProdId.setEditable(false);
        txtProdId.setBackground(new Color(240, 240, 240));

        txtProdName = new JTextField();
        txtProdPrice = new JTextField();
        txtProdQuantity = new JTextField();
        cmbCategory = new JComboBox<>();

        int row = 0;
        addFormRow(formPanel, gbc, "Mã SP (Tự động):", txtProdId, row++);
        addFormRow(formPanel, gbc, "Tên sản phẩm (*):", txtProdName, row++);
        addFormRow(formPanel, gbc, "Đơn giá (VNĐ) (*):", txtProdPrice, row++);
        addFormRow(formPanel, gbc, "Số lượng tồn (*):", txtProdQuantity, row++);
        addFormRow(formPanel, gbc, "Danh mục:", cmbCategory, row++);

        // Nút chức năng
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        JButton btnAdd = new JButton("➕ Thêm Mới");
        JButton btnUpdate = new JButton("✏️ Cập Nhật");
        JButton btnDelete = new JButton("🚫 Ngừng Bán");
        JButton btnClear = new JButton("🔄 Làm Mới Form");

        btnAdd.setBackground(new Color(40, 167, 69));
        btnAdd.setForeground(Color.WHITE);
        btnUpdate.setBackground(new Color(0, 123, 255));
        btnUpdate.setForeground(Color.WHITE);
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 8, 8, 8);
        formPanel.add(btnPanel, gbc);

        // Bảng sản phẩm bên phải
        productTableModel = new DefaultTableModel(
                new Object[]{"Mã SP", "Tên Sản Phẩm", "Đơn Giá", "Tồn Kho", "Danh Mục", "Tình Trạng"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tblProducts = new JTable(productTableModel);
        tblProducts.setRowHeight(28);
        tblProducts.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblProducts.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Highlight cảnh báo tồn kho <= 5
        tblProducts.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, r, c);
                if (!isSelected) {
                    Object qtyVal = table.getValueAt(r, 3);
                    if (qtyVal instanceof Integer qty && qty <= 5) {
                        comp.setBackground(new Color(255, 235, 235));
                        comp.setForeground(new Color(180, 0, 0));
                    } else {
                        comp.setBackground(Color.WHITE);
                        comp.setForeground(Color.BLACK);
                    }
                }
                return comp;
            }
        });

        JScrollPane scrollTable = new JScrollPane(tblProducts);
        scrollTable.setBorder(new TitledBorder("Danh sách sản phẩm (Màu hồng cảnh báo: Tồn kho <= 5)"));

        // Sự kiện click bảng đẩy dữ liệu lên form
        tblProducts.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = tblProducts.getSelectedRow();
            if (selectedRow >= 0) {
                txtProdId.setText(tblProducts.getValueAt(selectedRow, 0).toString());
                txtProdName.setText(tblProducts.getValueAt(selectedRow, 1).toString());
                
                // Trích xuất giá trị số
                String priceDisplay = tblProducts.getValueAt(selectedRow, 2).toString();
                txtProdPrice.setText(priceDisplay.replaceAll("[^0-9]", ""));
                txtProdQuantity.setText(tblProducts.getValueAt(selectedRow, 3).toString());

                String catName = (String) tblProducts.getValueAt(selectedRow, 4);
                for (int i = 0; i < cmbCategory.getItemCount(); i++) {
                    if (cmbCategory.getItemAt(i).getName().equalsIgnoreCase(catName)) {
                        cmbCategory.setSelectedIndex(i);
                        break;
                    }
                }
            }
        });

        // Bắt sự kiện các nút
        btnAdd.addActionListener(e -> handleAddProduct());
        btnUpdate.addActionListener(e -> handleUpdateProduct());
        btnDelete.addActionListener(e -> handleSoftDeleteProduct());
        btnClear.addActionListener(e -> clearProductForm());

        panel.add(formPanel, BorderLayout.WEST);
        panel.add(scrollTable, BorderLayout.CENTER);
        return panel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, String label, JComponent comp, int r) {
        gbc.gridy = r;
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(comp, gbc);
    }

    private void clearProductForm() {
        txtProdId.setText("");
        txtProdName.setText("");
        txtProdPrice.setText("");
        txtProdQuantity.setText("");
        if (cmbCategory.getItemCount() > 0) cmbCategory.setSelectedIndex(0);
        tblProducts.clearSelection();
    }

    private void handleAddProduct() {
        try {
            String name = txtProdName.getText().trim();
            double price = Double.parseDouble(txtProdPrice.getText().trim());
            int quantity = Integer.parseInt(txtProdQuantity.getText().trim());
            CategoryItem cat = (CategoryItem) cmbCategory.getSelectedItem();
            int catId = cat != null ? cat.getId() : 1;

            productService.addProduct(name, price, quantity, catId);
            JOptionPane.showMessageDialog(this, "✅ Thêm sản phẩm thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            clearProductForm();
            loadProductData();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "⚠️ Đơn giá và số lượng phải là số hợp lệ!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
        } catch (InvalidInputException ex) {
            JOptionPane.showMessageDialog(this, "⚠️ " + ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateProduct() {
        String idText = txtProdId.getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 sản phẩm trong bảng để cập nhật!", "Chưa chọn sản phẩm", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int id = Integer.parseInt(idText);
            String name = txtProdName.getText().trim();
            double price = Double.parseDouble(txtProdPrice.getText().trim());
            int quantity = Integer.parseInt(txtProdQuantity.getText().trim());
            CategoryItem cat = (CategoryItem) cmbCategory.getSelectedItem();
            int catId = cat != null ? cat.getId() : 1;

            productService.updateProduct(id, name, price, quantity, catId);
            JOptionPane.showMessageDialog(this, "✅ Cập nhật sản phẩm thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            clearProductForm();
            loadProductData();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "⚠️ Đơn giá và số lượng phải là số hợp lệ!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
        } catch (ProductNotFoundException | InvalidInputException ex) {
            JOptionPane.showMessageDialog(this, "⚠️ " + ex.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSoftDeleteProduct() {
        String idText = txtProdId.getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 sản phẩm để ngừng kinh doanh!", "Chưa chọn sản phẩm", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn ngừng bán sản phẩm '" + txtProdName.getText() + "' (Xóa mềm)?",
                "Xác nhận ngừng bán",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int id = Integer.parseInt(idText);
                productService.softDeleteProduct(id);
                JOptionPane.showMessageDialog(this, "✅ Đã chuyển trạng thái sang ngừng kinh doanh!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                clearProductForm();
                loadProductData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Lỗi: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadProductData() {
        productTableModel.setRowCount(0);
        try {
            List<Product> list = productService.listAllProducts();
            for (Product p : list) {
                String warning = (p.getQuantity() <= 5) ? "⚠️ SẮP HẾT" : "Đủ hàng";
                productTableModel.addRow(new Object[]{
                        p.getId(),
                        p.getName(),
                        currencyFormatter.format(p.getPrice()),
                        p.getQuantity(),
                        p.getCategoryName() != null ? p.getCategoryName() : "Khác",
                        warning
                });
            }
        } catch (Exception e) {
            System.err.println("Lỗi load sản phẩm: " + e.getMessage());
        }
    }

    private void loadCategories() {
        cmbCategory.removeAllItems();
        try {
            List<Category> list = categoryDAO.getAllCategories();
            for (Category c : list) {
                cmbCategory.addItem(new CategoryItem(c.getId(), c.getName()));
            }
        } catch (Exception e) {
            // Mặc định nếu chưa load được
            cmbCategory.addItem(new CategoryItem(1, "Điện thoại"));
            cmbCategory.addItem(new CategoryItem(2, "Phụ kiện"));
        }
    }

    // =========================================================================
    // 2. TAB LẬP HÓA ĐƠN BÁN HÀNG (TRANSACTION)
    // =========================================================================
    private JPanel createCheckoutPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Phía trên: Chọn khách và thêm món vào giỏ
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        topPanel.setBorder(new TitledBorder("Thông tin đặt hàng"));

        JPanel custRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        custRow.add(new JLabel("Họ tên khách hàng:"));
        txtCustomerName = new JTextField(25);
        custRow.add(txtCustomerName);

        JPanel itemRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        itemRow.add(new JLabel("Chọn sản phẩm:"));
        cmbSellProduct = new JComboBox<>();
        cmbSellProduct.setPreferredSize(new Dimension(300, 28));
        itemRow.add(cmbSellProduct);

        itemRow.add(new JLabel("Số lượng:"));
        spnSellQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spnSellQuantity.setPreferredSize(new Dimension(70, 28));
        itemRow.add(spnSellQuantity);

        JButton btnAddToCart = new JButton("🛒 Thêm Vào Giỏ");
        btnAddToCart.setBackground(new Color(23, 162, 184));
        btnAddToCart.setForeground(Color.WHITE);
        itemRow.add(btnAddToCart);

        JButton btnRemoveCart = new JButton("🗑️ Xóa Món Chọn");
        btnRemoveCart.setBackground(new Color(108, 117, 125));
        btnRemoveCart.setForeground(Color.WHITE);
        itemRow.add(btnRemoveCart);

        topPanel.add(custRow);
        topPanel.add(itemRow);

        // Ở giữa: Bảng giỏ hàng
        cartTableModel = new DefaultTableModel(
                new Object[]{"Mã SP", "Tên Sản Phẩm", "Đơn Giá", "Số Lượng", "Thành Tiền"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblCart = new JTable(cartTableModel);
        tblCart.setRowHeight(28);
        tblCart.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblCart.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        JScrollPane scrollCart = new JScrollPane(tblCart);
        scrollCart.setBorder(new TitledBorder("Chi tiết giỏ hàng hiện tại"));

        // Phía dưới: Tổng tiền & Nút Thanh Toán (Transaction)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        lblTotalAmount = new JLabel("Tổng tiền: 0 ₫");
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotalAmount.setForeground(new Color(220, 53, 69));

        JButton btnCheckout = new JButton("💳 THANH TOÁN & TẠO HÓA ĐƠN");
        btnCheckout.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnCheckout.setBackground(new Color(40, 167, 69));
        btnCheckout.setForeground(Color.WHITE);
        btnCheckout.setPreferredSize(new Dimension(320, 48));

        bottomPanel.add(lblTotalAmount, BorderLayout.WEST);
        bottomPanel.add(btnCheckout, BorderLayout.EAST);

        // Sự kiện các nút giỏ hàng
        btnAddToCart.addActionListener(e -> handleAddToCart());
        btnRemoveCart.addActionListener(e -> handleRemoveFromCart());
        btnCheckout.addActionListener(e -> handleCheckoutTransaction());

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollCart, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadSellProductsCombo() {
        cmbSellProduct.removeAllItems();
        try {
            List<Product> list = productService.listAllProducts();
            for (Product p : list) {
                cmbSellProduct.addItem(new ProductItem(p.getId(), p.getName(), p.getPrice(), p.getQuantity()));
            }
        } catch (Exception e) {
            System.err.println("Lỗi load combobox bán hàng: " + e.getMessage());
        }
    }

    private void handleAddToCart() {
        ProductItem selected = (ProductItem) cmbSellProduct.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sản phẩm!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int qty = (int) spnSellQuantity.getValue();
        if (qty > selected.getQuantity()) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Số lượng tồn kho không đủ! Hiện chỉ còn: " + selected.getQuantity() + " sản phẩm.",
                    "Không đủ hàng", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Kiểm tra xem đã có trong giỏ chưa
        boolean exists = false;
        for (OrderDetail detail : currentCart) {
            if (detail.getProductId() == selected.getId()) {
                if (detail.getQuantity() + qty > selected.getQuantity()) {
                    JOptionPane.showMessageDialog(this, "⚠️ Tổng số lượng mua vượt quá kho hiện có (" + selected.getQuantity() + ")!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                detail.setQuantity(detail.getQuantity() + qty);
                exists = true;
                break;
            }
        }

        if (!exists) {
            OrderDetail item = new OrderDetail(selected.getId(), qty, selected.getPrice());
            item.setProductName(selected.getName());
            currentCart.add(item);
        }

        refreshCartTable();
    }

    private void handleRemoveFromCart() {
        int row = tblCart.getSelectedRow();
        if (row >= 0) {
            currentCart.remove(row);
            refreshCartTable();
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa trong giỏ hàng!", "Chưa chọn dòng", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        currentCartTotal = 0.0;
        for (OrderDetail d : currentCart) {
            cartTableModel.addRow(new Object[]{
                    d.getProductId(),
                    d.getProductName(),
                    currencyFormatter.format(d.getUnitPrice()),
                    d.getQuantity(),
                    currencyFormatter.format(d.getSubTotal())
            });
            currentCartTotal += d.getSubTotal();
        }
        lblTotalAmount.setText("Tổng tiền: " + currencyFormatter.format(currentCartTotal));
    }

    private void handleCheckoutTransaction() {
        String customer = txtCustomerName.getText().trim();
        if (customer.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên khách hàng!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentCart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống! Vui lòng chọn sản phẩm vào giỏ.", "Giỏ hàng trống", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Xác nhận thanh toán cho khách hàng [" + customer + "]\nTổng thanh toán: " + currencyFormatter.format(currentCartTotal),
                "Xác nhận giao dịch (Transaction)",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Gọi Transaction JDBC thủ công tại Service
                int orderId = orderService.createOrderWithTransaction(customer, new ArrayList<>(currentCart));
                
                JOptionPane.showMessageDialog(this,
                        "🎉 THANH TOÁN THÀNH CÔNG!\n" +
                        "Mã hóa đơn: #" + orderId + "\n" +
                        "Dữ liệu đã được COMMIT an toàn vào Database và tồn kho đã được trừ.",
                        "Giao dịch thành công",
                        JOptionPane.INFORMATION_MESSAGE);

                // Dọn dẹp form
                currentCart.clear();
                refreshCartTable();
                txtCustomerName.setText("");
                spnSellQuantity.setValue(1);

                // Làm mới danh sách sản phẩm để cập nhật tồn kho mới
                loadSellProductsCombo();
                loadProductData();

            } catch (OutOfStockException | ProductNotFoundException | InvalidInputException ex) {
                JOptionPane.showMessageDialog(this,
                        "❌ GIAO DỊCH THẤT BẠI: " + ex.getMessage() + "\n" +
                        "⚠️ Hệ thống đã tự động ROLLBACK hoàn toàn dữ liệu!",
                        "Rollback Transaction",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // 3. TAB LỊCH SỬ HÓA ĐƠN & DOANH THU
    // =========================================================================
    private JPanel createOrderHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Phía trên: Thẻ thống kê doanh thu
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(230, 245, 230));
        topPanel.setBorder(new EmptyBorder(12, 18, 12, 18));

        lblTotalRevenue = new JLabel("💰 TỔNG DOANH THU CỬA HÀNG: 0 ₫");
        lblTotalRevenue.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotalRevenue.setForeground(new Color(40, 140, 40));

        JButton btnRefresh = new JButton("🔄 Làm Mới Dữ Liệu");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnRefresh.addActionListener(e -> {
            loadOrderData();
            updateRevenueDisplay();
        });

        topPanel.add(lblTotalRevenue, BorderLayout.WEST);
        topPanel.add(btnRefresh, BorderLayout.EAST);

        // Ở giữa: SplitPane chứa bảng danh sách hóa đơn (trên/trái) và chi tiết (dưới/phải)
        orderTableModel = new DefaultTableModel(
                new Object[]{"Mã HĐ", "Tên Khách Hàng", "Tổng Tiền", "Ngày Tạo"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblOrders = new JTable(orderTableModel);
        tblOrders.setRowHeight(26);
        tblOrders.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblOrders.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        JScrollPane scrollOrders = new JScrollPane(tblOrders);
        scrollOrders.setBorder(new TitledBorder("Danh sách hóa đơn đã lập"));

        orderDetailTableModel = new DefaultTableModel(
                new Object[]{"Mã SP", "Tên Sản Phẩm", "Số Lượng", "Đơn Giá", "Thành Tiền"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblOrderDetails = new JTable(orderDetailTableModel);
        tblOrderDetails.setRowHeight(26);
        tblOrderDetails.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblOrderDetails.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        JScrollPane scrollDetails = new JScrollPane(tblOrderDetails);
        scrollDetails.setBorder(new TitledBorder("Chi tiết hóa đơn được chọn"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollOrders, scrollDetails);
        splitPane.setResizeWeight(0.5);

        // Bắt sự kiện chọn hóa đơn để tải chi tiết
        tblOrders.getSelectionModel().addListSelectionListener(e -> {
            int row = tblOrders.getSelectedRow();
            if (row >= 0) {
                int orderId = (int) tblOrders.getValueAt(row, 0);
                loadOrderDetails(orderId);
            }
        });

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadOrderData() {
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
            List<OrderDetail> list = orderService.getOrderDetails(orderId);
            for (OrderDetail d : list) {
                orderDetailTableModel.addRow(new Object[]{
                        d.getProductId(),
                        d.getProductName(),
                        d.getQuantity(),
                        currencyFormatter.format(d.getUnitPrice()),
                        currencyFormatter.format(d.getSubTotal())
                });
            }
        } catch (Exception e) {
            System.err.println("Lỗi load chi tiết: " + e.getMessage());
        }
    }

    private void updateRevenueDisplay() {
        try {
            double rev = orderService.getTotalRevenue();
            lblTotalRevenue.setText("💰 TỔNG DOANH THU CỬA HÀNG: " + currencyFormatter.format(rev));
        } catch (Exception e) {
            System.err.println("Lỗi tính doanh thu: " + e.getMessage());
        }
    }

    // =========================================================================
    // LỚP PHỤ TRỢ CHO COMBOBOX
    // =========================================================================
    public static class CategoryItem {
        private final int id;
        private final String name;
        public CategoryItem(int id, String name) { this.id = id; this.name = name; }
        public int getId() { return id; }
        public String getName() { return name; }
        @Override
        public String toString() { return name; }
    }

    public static class ProductItem {
        private final int id;
        private final String name;
        private final double price;
        private final int quantity;

        public ProductItem(int id, String name, double price, int quantity) {
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
            return name + " (" + nf.format(price) + " | Còn: " + quantity + ")";
        }
    }
}

