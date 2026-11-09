package com.store.view;

import com.store.config.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * FORM 1: Giao diện Đăng nhập hệ thống (Java Swing JFrame)
 */
public class LoginForm extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnExit;

    public LoginForm() {
        initComponents();
    }

    private void initComponents() {
        setTitle("ĐĂNG NHẬP HỆ THỐNG - QUẢN LÝ CỬA HÀNG");
        setSize(460, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(24, 119, 242));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 70));
        headerPanel.setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("🏪 ĐĂNG NHẬP HỆ THỐNG", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.CENTER);

        JLabel lblSub = new JLabel("Hệ Thống Quản Lý Cửa Hàng & Bán Hàng (Store Management)", JLabel.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblSub.setForeground(new Color(230, 240, 255));
        lblSub.setBorder(new EmptyBorder(0, 0, 8, 0));
        headerPanel.add(lblSub, BorderLayout.SOUTH);

        // Form nhập liệu
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(25, 30, 15, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtUsername = new JTextField(15);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setText("admin"); // Gợi ý tài khoản mặc định

        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtPassword = new JPasswordField(15);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setText("123456"); // Gợi ý mật khẩu mặc định

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        formPanel.add(lblUser, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        formPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        formPanel.add(lblPass, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        formPanel.add(txtPassword, gbc);

        // Hướng dẫn tài khoản
        JLabel lblHint = new JLabel("💡 Tài khoản mẫu: admin | Mật khẩu: 123456");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(new Color(100, 100, 100));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(lblHint, gbc);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(Color.WHITE);
        btnLogin = new JButton("🔑 Đăng Nhập");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(24, 119, 242));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(140, 38));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnExit = new JButton("❌ Thoát");
        btnExit.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnExit.setBackground(new Color(230, 230, 230));
        btnExit.setPreferredSize(new Dimension(100, 38));
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnPanel.add(btnLogin);
        btnPanel.add(btnExit);

        // Sự kiện các nút
        btnLogin.addActionListener(e -> handleLogin());
        btnExit.addActionListener(e -> System.exit(0));

        // Nhấn Enter ở ô mật khẩu để đăng nhập luôn
        txtPassword.addActionListener(e -> handleLogin());

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Vui lòng nhập đầy đủ Tên đăng nhập và Mật khẩu!",
                    "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Kiểm tra kết nối MySQL trước
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            if (conn == null || conn.isClosed()) {
                JOptionPane.showMessageDialog(this,
                        "❌ Không thể kết nối cơ sở dữ liệu MySQL! Vui lòng kiểm tra XAMPP.",
                        "Lỗi Database",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ Lỗi kết nối MySQL: " + ex.getMessage(),
                    "Lỗi Database",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Xác thực tài khoản (chấp nhận admin/123456 hoặc root)
        if (("admin".equalsIgnoreCase(username) && "123456".equals(password)) ||
            ("root".equalsIgnoreCase(username) && "".equals(password))) {
            JOptionPane.showMessageDialog(this,
                    "🎉 Đăng nhập thành công!\nChào mừng quản trị viên [" + username + "].",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            this.dispose(); // Đóng form đăng nhập

            // Mở FORM 2: Quản lý sản phẩm
            SwingUtilities.invokeLater(() -> {
                ProductManagementForm productForm = new ProductManagementForm();
                productForm.setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this,
                    "❌ Sai tên đăng nhập hoặc mật khẩu!\n(Gợi ý tài khoản: admin / 123456)",
                    "Đăng nhập thất bại",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
