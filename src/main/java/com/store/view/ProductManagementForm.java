package com.store.view;

import com.store.dao.CategoryDAO;
import com.store.dao.CategoryDAOImpl;
import com.store.exception.InvalidInputException;
import com.store.exception.ProductNotFoundException;
import com.store.model.Category;
import com.store.model.Product;
import com.store.service.ProductService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * FORM 2: Giao diện Quản Lý Sản Phẩm (Java Swing JFrame)
 * Đầy đủ chức năng: Thêm, Sửa, Xóa (Xóa mềm), Tìm kiếm và hiển thị bảng JTable
 */
public class ProductManagementForm extends JFrame {
    private final ProductService productService;
    private final CategoryDAO categoryDAO;
    private final NumberFormat currencyFormatter;

    // Components Tìm Kiếm
    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnRefreshAll;

    // Components Bảng JTable
    private JTable tblProducts;
    private DefaultTableModel productTableModel;

    // Components Form Nhập Liệu
    private JTextField txtProdId;
    private JTextField txtProdName;
    private JTextField txtProdPrice;
    private JTextField txtProdQuantity;
    private JComboBox<CategoryItem> cmbCategory;

    // Buttons CRUD & Chuyển Form
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnOpenBillingForm;
    private JButton btnLogout;

    public ProductManagementForm() {
        this.productService = new ProductService();
        this.categoryDAO = new CategoryDAOImpl();
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        initComponents();
        loadCategories();
        loadProductData("");
    }

    private void initComponents() {
        setTitle("FORM 2: QUẢN LÝ SẢN PHẨM (CRUD & TÌM KIẾM)");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        // 1. Header & Navigation Panel
        JPanel topContainer = new JPanel(new BorderLayout(5, 5));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 119, 242));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 55));
        JLabel lblHeader = new JLabel("📦 QUẢN LÝ SẢN PHẨM & KHO HÀNG", JLabel.CENTER);
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerPanel.add(lblHeader, BorderLayout.CENTER);

        // Thanh điều hướng bên phải
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        navPanel.setBackground(new Color(24, 119, 242));
        btnOpenBillingForm = new JButton("🛒 Mở Form Bán Hàng >>");
        btnOpenBillingForm.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnOpenBillingForm.setBackground(new Color(40, 167, 69));
        btnOpenBillingForm.setForeground(Color.WHITE);

        btnLogout = new JButton("🚪 Đăng Xuất");
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnLogout.setBackground(new Color(220, 53, 69));
        btnLogout.setForeground(Color.WHITE);

        navPanel.add(btnOpenBillingForm);
        navPanel.add(btnLogout);
        headerPanel.add(navPanel, BorderLayout.EAST);

        // Thanh tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        searchPanel.setBorder(new TitledBorder("🔍 Tìm kiếm sản phẩm theo Tên hoặc Danh mục (Kết nối SQL)"));
        
        searchPanel.add(new JLabel("Từ khóa tìm kiếm:"));
        txtSearch = new JTextField(25);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchPanel.add(txtSearch);

        btnSearch = new JButton("🔎 Tìm Kiếm");
        btnSearch.setBackground(new Color(0, 123, 255));
        btnSearch.setForeground(Color.WHITE);
        searchPanel.add(btnSearch);

        btnRefreshAll = new JButton("🔄 Xem Tất Cả");
        searchPanel.add(btnRefreshAll);

        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(searchPanel, BorderLayout.SOUTH);

        // 2. Form Nhập Liệu CRUD (Bên Trái)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder("Thông tin chi tiết sản phẩm"));
        formPanel.setPreferredSize(new Dimension(380, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtProdId = new JTextField();
        txtProdId.setEditable(false);
        txtProdId.setBackground(new Color(245, 245, 245));

        txtProdName = new JTextField();
        txtProdPrice = new JTextField();
        txtProdQuantity = new JTextField();
        cmbCategory = new JComboBox<>();

        int r = 0;
        addFormRow(formPanel, gbc, "Mã SP (product_id):", txtProdId, r++);
        addFormRow(formPanel, gbc, "Tên sản phẩm (*):", txtProdName, r++);
        addFormRow(formPanel, gbc, "Đơn giá (VNĐ) (*):", txtProdPrice, r++);
        addFormRow(formPanel, gbc, "Số lượng tồn (*):", txtProdQuantity, r++);
        addFormRow(formPanel, gbc, "Danh mục:", cmbCategory, r++);

        // Các nút thao tác CRUD
        JPanel btnCrudPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnAdd = new JButton("➕ Thêm Mới");
        btnUpdate = new JButton("✏️ Cập Nhật");
        btnDelete = new JButton("🚫 Ngừng Bán (Xóa)");
        btnClear = new JButton("🔄 Làm Mới");

        btnAdd.setBackground(new Color(40, 167, 69));
        btnAdd.setForeground(Color.WHITE);
        btnUpdate.setBackground(new Color(0, 123, 255));
        btnUpdate.setForeground(Color.WHITE);
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);

        btnCrudPanel.add(btnAdd);
        btnCrudPanel.add(btnUpdate);
        btnCrudPanel.add(btnDelete);
        btnCrudPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 2;
        gbc.insets = new Insets(18, 8, 8, 8);
        formPanel.add(btnCrudPanel, gbc);

        // 3. Bảng JTable hiển thị sản phẩm (Bên Phải)
        productTableModel = new DefaultTableModel(
                new Object[]{"Mã SP", "Tên Sản Phẩm", "Đơn Giá", "Tồn Kho", "Danh Mục", "Cảnh Báo"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tblProducts = new JTable(productTableModel);
        tblProducts.setRowHeight(28);
        tblProducts.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblProducts.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Cảnh báo màu đỏ nhạt nếu tồn kho <= 5
        tblProducts.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int rowIdx, int colIdx) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIdx, colIdx);
                if (!isSelected) {
                    Object qtyObj = table.getValueAt(rowIdx, 3);
                    if (qtyObj instanceof Integer qty && qty <= 5) {
                        c.setBackground(new Color(255, 235, 235));
                        c.setForeground(new Color(180, 0, 0));
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                }
                return c;
            }
        });

        JScrollPane scrollTable = new JScrollPane(tblProducts);
        scrollTable.setBorder(new TitledBorder("Danh sách sản phẩm (Màu hồng cảnh báo: Tồn kho <= 5)"));

        // Sự kiện click bảng
        tblProducts.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = tblProducts.getSelectedRow();
            if (selectedRow >= 0) {
                txtProdId.setText(tblProducts.getValueAt(selectedRow, 0).toString());
                txtProdName.setText(tblProducts.getValueAt(selectedRow, 1).toString());
                
                String priceStr = tblProducts.getValueAt(selectedRow, 2).toString();
                txtProdPrice.setText(priceStr.replaceAll("[^0-9]", ""));
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
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có muốn đăng xuất khỏi hệ thống?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
                SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
            }
        });

        mainPanel.add(topContainer, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.WEST);
        mainPanel.add(scrollTable, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, String label, JComponent comp, int r) {
        gbc.gridy = r;
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.65;
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
            double price = Double.parseDouble(txtProdPrice.getText().trim());
            int quantity = Integer.parseInt(txtProdQuantity.getText().trim());
            CategoryItem cat = (CategoryItem) cmbCategory.getSelectedItem();
            int catId = cat != null ? cat.getId() : 1;

            productService.addProduct(name, price, quantity, catId);
            JOptionPane.showMessageDialog(this, "✅ Thêm sản phẩm thành công vào cơ sở dữ liệu!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadProductData("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "⚠️ Đơn giá và số lượng phải là số hợp lệ!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
        } catch (InvalidInputException ex) {
            JOptionPane.showMessageDialog(this, "⚠️ " + ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
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
            double price = Double.parseDouble(txtProdPrice.getText().trim());
            int quantity = Integer.parseInt(txtProdQuantity.getText().trim());
            CategoryItem cat = (CategoryItem) cmbCategory.getSelectedItem();
            int catId = cat != null ? cat.getId() : 1;

            productService.updateProduct(id, name, price, quantity, catId);
            JOptionPane.showMessageDialog(this, "✅ Cập nhật sản phẩm ID " + id + " thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadProductData("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "⚠️ Đơn giá và số lượng phải là số hợp lệ!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
        } catch (ProductNotFoundException | InvalidInputException ex) {
            JOptionPane.showMessageDialog(this, "⚠️ " + ex.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "✅ Đã chuyển trạng thái ngừng bán thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadProductData("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Lỗi: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void loadProductData(String keyword) {
        productTableModel.setRowCount(0);
        try {
            List<Product> list;
            if (keyword != null && !keyword.trim().isEmpty()) {
                list = productService.searchProducts(keyword);
            } else {
                list = productService.listAllProducts();
            }

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
            cmbCategory.addItem(new CategoryItem(1, "Điện thoại"));
            cmbCategory.addItem(new CategoryItem(2, "Phụ kiện"));
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
