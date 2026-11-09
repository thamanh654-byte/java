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
 * FORM 2: Giao dien Quan Ly San Pham (CRUD & Tim Kiem)
 */
public class ProductManagementForm extends JFrame {
    private final ProductService productService;
    private final CategoryDAO categoryDAO;
    private final NumberFormat currencyFormatter;

    // Components Tim Kiem
    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnRefreshAll;

    // Components Bang JTable
    private JTable tblProducts;
    private DefaultTableModel productTableModel;

    // Components Form Nhap Lieu
    private JTextField txtProdId;
    private JTextField txtProdName;
    private JTextField txtProdPrice;
    private JTextField txtProdQuantity;
    private JComboBox<CategoryItem> cmbCategory;

    // Buttons CRUD & Dieu Huong
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
        setTitle("FORM 2: QUAN LY SAN PHAM (CRUD & TIM KIEM)");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        // 1. Header & Navigation Panel
        JPanel topContainer = new JPanel(new BorderLayout(5, 5));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(13, 110, 253)); // #0d6efd
        headerPanel.setPreferredSize(new Dimension(getWidth(), 55));
        
        JLabel lblHeader = new JLabel("QUAN LY SAN PHAM & KHO HANG", JLabel.CENTER);
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerPanel.add(lblHeader, BorderLayout.CENTER);

        // Thanh dieu huong ben phai
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        navPanel.setBackground(new Color(13, 110, 253));

        btnOpenBillingForm = new JButton("Mo Form Ban Hang >>");
        styleButton(btnOpenBillingForm, new Color(25, 135, 84), Color.WHITE); // #198754
        btnOpenBillingForm.setPreferredSize(new Dimension(190, 36));

        btnLogout = new JButton("Dang Xuat");
        styleButton(btnLogout, new Color(220, 53, 69), Color.WHITE); // #dc3545
        btnLogout.setPreferredSize(new Dimension(110, 36));

        navPanel.add(btnOpenBillingForm);
        navPanel.add(btnLogout);
        headerPanel.add(navPanel, BorderLayout.EAST);

        // Thanh tim kiem
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        searchPanel.setBorder(new TitledBorder("Tim kiem san pham theo Ten hoac Danh muc"));
        
        searchPanel.add(new JLabel("Tu khoa tim kiem:"));
        txtSearch = new JTextField(25);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchPanel.add(txtSearch);

        btnSearch = new JButton("Tim Kiem");
        styleButton(btnSearch, new Color(13, 110, 253), Color.WHITE);
        btnSearch.setPreferredSize(new Dimension(110, 32));
        searchPanel.add(btnSearch);

        btnRefreshAll = new JButton("Xem Tat Ca");
        styleButton(btnRefreshAll, new Color(108, 117, 125), Color.WHITE);
        btnRefreshAll.setPreferredSize(new Dimension(120, 32));
        searchPanel.add(btnRefreshAll);

        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(searchPanel, BorderLayout.SOUTH);

        // 2. Form Nhap Lieu CRUD (Ben Trai)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder("Thong tin chi tiet san pham"));
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
        addFormRow(formPanel, gbc, "Ma SP (product_id):", txtProdId, r++);
        addFormRow(formPanel, gbc, "Ten san pham (*):", txtProdName, r++);
        addFormRow(formPanel, gbc, "Don gia (VND) (*):", txtProdPrice, r++);
        addFormRow(formPanel, gbc, "So luong ton (*):", txtProdQuantity, r++);
        addFormRow(formPanel, gbc, "Danh muc:", cmbCategory, r++);

        // Cac nut thao tac CRUD
        JPanel btnCrudPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnAdd = new JButton("Them Moi");
        btnUpdate = new JButton("Cap Nhat");
        btnDelete = new JButton("Ngung Ban (Xoa)");
        btnClear = new JButton("Lam Moi");

        styleButton(btnAdd, new Color(25, 135, 84), Color.WHITE);    // Xanh la #198754
        styleButton(btnUpdate, new Color(13, 110, 253), Color.WHITE); // Xanh duong #0d6efd
        styleButton(btnDelete, new Color(220, 53, 69), Color.WHITE);  // Do #dc3545
        styleButton(btnClear, new Color(248, 249, 250), Color.BLACK); // Sang, chu den ro rang

        btnCrudPanel.add(btnAdd);
        btnCrudPanel.add(btnUpdate);
        btnCrudPanel.add(btnDelete);
        btnCrudPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 2;
        gbc.insets = new Insets(18, 8, 8, 8);
        formPanel.add(btnCrudPanel, gbc);

        // 3. Bang JTable hien thi san pham (Ben Phai)
        productTableModel = new DefaultTableModel(
                new Object[]{"Ma SP", "Ten San Pham", "Don Gia", "Ton Kho", "Danh Muc", "Canh Bao"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tblProducts = new JTable(productTableModel);
        tblProducts.setRowHeight(28);
        tblProducts.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblProducts.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Canh bao mau hong nhat neu ton kho <= 5
        tblProducts.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int rowIdx, int colIdx) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIdx, colIdx);
                if (!isSelected) {
                    Object qtyObj = table.getValueAt(rowIdx, 3);
                    if (qtyObj instanceof Integer qty && qty <= 5) {
                        c.setBackground(new Color(255, 230, 230));
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
        scrollTable.setBorder(new TitledBorder("Danh sach san pham (Nen hong canh bao: Ton kho <= 5)"));

        // Su kien click bang
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

        // Bat su kien CRUD & Tim kiem
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

        // Dieu huong form
        btnOpenBillingForm.addActionListener(e -> {
            this.dispose();
            SwingUtilities.invokeLater(() -> new OrderForm().setVisible(true));
        });

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Ban co chac muon dang xuat?", "Xac nhan", JOptionPane.YES_NO_OPTION);
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

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createLineBorder(bg.darker(), 1));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
            JOptionPane.showMessageDialog(this, "Them san pham thanh cong vao co so du lieu!", "Thong bao", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadProductData("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Don gia va so luong phai la so hop le!", "Loi nhap lieu", JOptionPane.WARNING_MESSAGE);
        } catch (InvalidInputException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Loi nghiep vu", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage(), "Loi he thong", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        String idText = txtProdId.getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long chon 1 san pham trong bang de cap nhat!", "Chua chon san pham", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Cap nhat san pham ID " + id + " thanh cong!", "Thong bao", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadProductData("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Don gia va so luong phai la so hop le!", "Loi nhap lieu", JOptionPane.WARNING_MESSAGE);
        } catch (ProductNotFoundException | InvalidInputException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Loi", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage(), "Loi he thong", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        String idText = txtProdId.getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long chon 1 san pham de ngung kinh doanh!", "Chua chon san pham", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Ban co chac muon ngung ban san pham '" + txtProdName.getText() + "' (Xoa mem - status=false)?",
                "Xac nhan ngung ban",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int id = Integer.parseInt(idText);
                productService.softDeleteProduct(id);
                JOptionPane.showMessageDialog(this, "Da chuyen trang thai ngung ban thanh cong!", "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadProductData("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage(), "Loi he thong", JOptionPane.ERROR_MESSAGE);
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
                String warning = (p.getQuantity() <= 5) ? "[!] SAP HET HANG" : "Du hang";
                productTableModel.addRow(new Object[]{
                        p.getId(),
                        p.getName(),
                        currencyFormatter.format(p.getPrice()),
                        p.getQuantity(),
                        p.getCategoryName() != null ? p.getCategoryName() : "Khac",
                        warning
                });
            }
        } catch (Exception e) {
            System.err.println("Loi load san pham: " + e.getMessage());
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
            cmbCategory.addItem(new CategoryItem(1, "Dien thoai"));
            cmbCategory.addItem(new CategoryItem(2, "Phu kien"));
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
