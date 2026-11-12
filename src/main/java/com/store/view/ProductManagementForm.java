package com.store.view;

import com.store.dao.CategoryDAO;
import com.store.dao.CategoryDAOImpl;
import com.store.exception.InvalidInputException;
import com.store.exception.ProductNotFoundException;
import com.store.model.Category;
import com.store.model.Product;
import com.store.service.ProductService;
import com.store.util.MoneyUtils;
import com.store.util.UiUtils;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.List;

/**
 * FORM 2: Giao diện Quản Lý Sản Phẩm (CRUD & Tìm Kiếm)
 */
public final class ProductManagementForm extends JFrame {
    private static final long serialVersionUID = 1L;

    private final transient ProductService productService;
    private final transient CategoryDAO categoryDAO;
    private final transient NumberFormat currencyFormatter;

    // Components Tìm Kiếm
    private transient JTextField txtSearch;
    private transient JButton btnSearch;
    private transient JButton btnRefreshAll;

    // Components Bảng JTable
    private transient JTable tblProducts;
    private transient DefaultTableModel productTableModel;
    /** Danh sách sản phẩm đang hiển thị (giữ dữ liệu gốc để không phải parse lại từ chuỗi). */
    private transient List<Product> visibleProducts = new java.util.ArrayList<>();

    // Components Form Nhập Liệu
    private transient JTextField txtProdId;
    private transient JTextField txtProdName;
    private transient JTextField txtProdPrice;
    private transient JTextField txtProdQuantity;
    private transient JComboBox<CategoryItem> cmbCategory;

    // Buttons CRUD & Điều Hướng
    private transient JButton btnAdd;
    private transient JButton btnUpdate;
    private transient JButton btnDelete;
    private transient JButton btnClear;
    private transient JButton btnOpenBillingForm;
    private transient JButton btnLogout;

    public ProductManagementForm() {
        this.productService = new ProductService();
        this.categoryDAO = new CategoryDAOImpl();
        this.currencyFormatter = NumberFormat.getCurrencyInstance(MoneyUtils.VIETNAM);
        this.currencyFormatter.setMaximumFractionDigits(0);

        initComponents();
        loadCategories();
        loadProductData("");
    }

    private void initComponents() {
        setTitle("Quản lý sản phẩm - Hệ thống Quản lý cửa hàng");
        setSize(1120, 700);
        setMinimumSize(new Dimension(960, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UiUtils.BG);

        // 1. Thanh tiêu đề + nút điều hướng
        JPanel headerPanel = UiUtils.header(
                "QUẢN LÝ SẢN PHẨM",
                "Bước 2/3  ·  Thêm, sửa, tìm kiếm và ngừng kinh doanh sản phẩm");

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navPanel.setOpaque(false);

        btnOpenBillingForm = UiUtils.button("Sang bán hàng  >", UiUtils.ACCENT);
        btnLogout = UiUtils.button("Đăng xuất", UiUtils.NEUTRAL);
        navPanel.add(btnOpenBillingForm);
        navPanel.add(btnLogout);
        headerPanel.add(navPanel, BorderLayout.EAST);

        // Thanh tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(UiUtils.CARD);
        searchPanel.setBorder(new EmptyBorder(2, 8, 2, 8));

        searchPanel.add(UiUtils.label("Tìm theo tên sản phẩm hoặc danh mục"));
        txtSearch = new JTextField(24);
        UiUtils.pad(txtSearch);
        searchPanel.add(txtSearch);

        btnSearch = UiUtils.button("Tìm kiếm", UiUtils.PRIMARY);
        searchPanel.add(btnSearch);

        btnRefreshAll = UiUtils.button("Xem tất cả", UiUtils.NEUTRAL);
        searchPanel.add(btnRefreshAll);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(searchPanel, BorderLayout.SOUTH);

        // 2. Form nhập liệu (bên trái)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UiUtils.CARD);
        formPanel.setBorder(UiUtils.cardBorder("Thông tin sản phẩm"));
        formPanel.setPreferredSize(new Dimension(360, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtProdId = new JTextField();
        txtProdId.setEditable(false);
        txtProdId.setBackground(new Color(245, 246, 248));

        txtProdName = new JTextField();
        txtProdPrice = new JTextField();
        txtProdQuantity = new JTextField();
        cmbCategory = new JComboBox<>();

        int r = 0;
        addFormRow(formPanel, gbc, "Mã sản phẩm", txtProdId, r++);
        addFormRow(formPanel, gbc, "Tên sản phẩm", txtProdName, r++);
        addFormRow(formPanel, gbc, "Đơn giá (đ)", txtProdPrice, r++);
        addFormRow(formPanel, gbc, "Số lượng tồn", txtProdQuantity, r++);
        addFormRow(formPanel, gbc, "Danh mục", cmbCategory, r++);

        gbc.gridx = 0; gbc.gridy = r++; gbc.gridwidth = 2;
        gbc.insets = new Insets(2, 8, 2, 8);
        formPanel.add(UiUtils.muted("Mã sản phẩm tự sinh. Bấm một dòng trong bảng để sửa."), gbc);

        // Các nút thao tác
        JPanel btnCrudPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnCrudPanel.setOpaque(false);
        btnAdd = UiUtils.button("Thêm mới", UiUtils.ACCENT);
        btnUpdate = UiUtils.button("Cập nhật", UiUtils.PRIMARY);
        btnDelete = UiUtils.button("Ngừng bán", UiUtils.DANGER);
        btnClear = UiUtils.button("Làm mới", UiUtils.NEUTRAL);

        btnCrudPanel.add(btnAdd);
        btnCrudPanel.add(btnUpdate);
        btnCrudPanel.add(btnDelete);
        btnCrudPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 8, 8, 8);
        formPanel.add(btnCrudPanel, gbc);

        // 3. Bảng sản phẩm (bên phải)
        productTableModel = new DefaultTableModel(
                new Object[]{"Mã", "Tên sản phẩm", "Đơn giá", "Tồn kho", "Danh mục", "Tình trạng"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tblProducts = new JTable(productTableModel);
        UiUtils.styleTable(tblProducts);
        UiUtils.applyLowStockRenderer(tblProducts, 3);

        JScrollPane scrollTable = new JScrollPane(tblProducts);
        scrollTable.setBorder(UiUtils.cardBorder("Danh sách sản phẩm  ·  dòng nền hồng = sắp hết hàng (tồn kho ≤ 5)"));
        scrollTable.getViewport().setBackground(UiUtils.CARD);

        // Sự kiện click bảng
        tblProducts.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int selectedRow = tblProducts.getSelectedRow();
            if (selectedRow >= 0 && selectedRow < visibleProducts.size()) {
                Product p = visibleProducts.get(selectedRow);
                txtProdId.setText(String.valueOf(p.getId()));
                txtProdName.setText(p.getName());
                // Dùng giá trị gốc từ DB (không parse lại từ chuỗi đã định dạng)
                txtProdPrice.setText(String.valueOf(Math.round(p.getPrice())));
                txtProdQuantity.setText(String.valueOf(p.getQuantity()));
                selectCategoryById(p.getCategoryId());
            }
        });

        // Bắt sự kiện CRUD & Tìm kiếm
        btnSearch.addActionListener(e -> handleSearch());
        txtSearch.addActionListener(e -> handleSearch());
        btnRefreshAll.addActionListener(e -> {
            txtSearch.setText("");
            loadProductData("");
        });

        btnAdd.addActionListener(e -> handleAdd());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnClear.addActionListener(e -> clearForm());

        // Điều hướng form
        btnOpenBillingForm.addActionListener(e -> {
            this.dispose();
            SwingUtilities.invokeLater(() -> new OrderForm().setVisible(true));
        });

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn đăng xuất khỏi hệ thống?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
                SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
            }
        });

        mainPanel.add(topContainer, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.WEST);
        mainPanel.add(scrollTable, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(10, 12, 12, 12));
        content.add(formPanel, BorderLayout.WEST);
        content.add(scrollTable, BorderLayout.CENTER);

        mainPanel.add(topContainer, BorderLayout.NORTH);
        mainPanel.add(content, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, String label, JComponent comp, int r) {
        gbc.gridy = r;
        gbc.gridx = 0;
        gbc.weightx = 0.4;
        gbc.gridwidth = 1;
        panel.add(UiUtils.label(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.6;
        UiUtils.pad(comp);
        panel.add(comp, gbc);
    }

    private void clearForm() {
        txtProdId.setText("");
        txtProdName.setText("");
        txtProdPrice.setText("");
        txtProdQuantity.setText("");
        if (cmbCategory.getItemCount() > 0) cmbCategory.setSelectedIndex(0);
        tblProducts.clearSelection();
    }

    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        loadProductData(keyword);
    }

    private void handleAdd() {
        try {
            String name = txtProdName.getText().trim();
            double price = MoneyUtils.parse(txtProdPrice.getText());
            int quantity = Integer.parseInt(txtProdQuantity.getText().trim());
            CategoryItem cat = (CategoryItem) cmbCategory.getSelectedItem();
            int catId = cat != null ? cat.getId() : 0;

            productService.addProduct(name, price, quantity, catId);
            JOptionPane.showMessageDialog(this, "Thêm sản phẩm thành công vào cơ sở dữ liệu!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadProductData("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Đơn giá và số lượng phải là số hợp lệ!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
        } catch (InvalidInputException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        String idText = txtProdId.getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 sản phẩm trong bảng để cập nhật!", "Chưa chọn sản phẩm", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int id = Integer.parseInt(idText);
            String name = txtProdName.getText().trim();
            double price = MoneyUtils.parse(txtProdPrice.getText());
            int quantity = Integer.parseInt(txtProdQuantity.getText().trim());
            CategoryItem cat = (CategoryItem) cmbCategory.getSelectedItem();
            int catId = cat != null ? cat.getId() : 0;

            productService.updateProduct(id, name, price, quantity, catId);
            JOptionPane.showMessageDialog(this, "Cập nhật sản phẩm ID " + id + " thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadProductData("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Đơn giá và số lượng phải là số hợp lệ!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
        } catch (ProductNotFoundException | InvalidInputException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        String idText = txtProdId.getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 sản phẩm để ngừng kinh doanh!", "Chưa chọn sản phẩm", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn ngừng bán sản phẩm '" + txtProdName.getText() + "' (Xóa mềm - status=false)?",
                "Xác nhận ngừng bán",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int id = Integer.parseInt(idText);
                productService.softDeleteProduct(id);
                JOptionPane.showMessageDialog(this, "Đã chuyển trạng thái ngừng bán thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadProductData("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void loadProductData(String keyword) {
        productTableModel.setRowCount(0);
        visibleProducts.clear();
        try {
            List<Product> list;
            if (keyword != null && !keyword.trim().isEmpty()) {
                list = productService.searchProducts(keyword);
            } else {
                list = productService.listAllProducts();
            }

            for (Product p : list) {
                String warning = (p.getQuantity() <= UiUtils.LOW_STOCK_THRESHOLD) ? "Sắp hết hàng" : "Đủ hàng";
                productTableModel.addRow(new Object[]{
                        p.getId(),
                        p.getName(),
                        currencyFormatter.format(p.getPrice()),
                        p.getQuantity(),
                        p.getCategoryName() != null ? p.getCategoryName() : "Khác",
                        warning
                });
                visibleProducts.add(p);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không tải được danh sách sản phẩm: " + e.getMessage(),
                    "Lỗi tải dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Chọn đúng danh mục trong combobox theo category_id. */
    private void selectCategoryById(int categoryId) {
        for (int i = 0; i < cmbCategory.getItemCount(); i++) {
            if (cmbCategory.getItemAt(i).getId() == categoryId) {
                cmbCategory.setSelectedIndex(i);
                return;
            }
        }
        if (cmbCategory.getItemCount() > 0) {
            cmbCategory.setSelectedIndex(0);
        }
    }

    private void loadCategories() {
        cmbCategory.removeAllItems();
        // Mục 0 = chưa phân loại (lưu NULL vào DB)
        cmbCategory.addItem(new CategoryItem(0, "-- Chưa phân loại --"));
        try {
            List<Category> list = categoryDAO.getAllCategories();
            for (Category c : list) {
                cmbCategory.addItem(new CategoryItem(c.getId(), c.getName()));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Không tải được danh mục: " + e.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static class CategoryItem {
        private final int id;
        private final String name;
        public CategoryItem(int id, String name) { this.id = id; this.name = name; }
        public int getId() { return id; }
        public String getName() { return name; }
        @Override
        public String toString() { return name; }
    }
}
