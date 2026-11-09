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
        setTitle("DANG NHAP HE THONG - QUAN LY CUA HANG");
        setSize(460, 360);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(13, 110, 253)); // #0d6efd
        headerPanel.setPreferredSize(new Dimension(getWidth(), 70));
        headerPanel.setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("DANG NHAP HE THONG", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.CENTER);

        JLabel lblSub = new JLabel("He Thong Quan Ly Cua Hang & Ban Hang (Store Management)", JLabel.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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

        JLabel lblUser = new JLabel("Ten dang nhap:");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtUsername = new JTextField(15);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setText("admin");

        JLabel lblPass = new JLabel("Mat khau:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtPassword = new JPasswordField(15);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setText("123456");

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        formPanel.add(lblUser, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.65;
        formPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        formPanel.add(lblPass, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.65;
        formPanel.add(txtPassword, gbc);

        JLabel lblHint = new JLabel("[!] Tai khoan mac dinh: admin | Mat khau: 123456");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(new Color(100, 100, 100));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(lblHint, gbc);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 12));
        btnPanel.setBackground(Color.WHITE);

        btnLogin = new JButton("Dang Nhap");
        styleButton(btnLogin, new Color(13, 110, 253), Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(140, 38));

        btnExit = new JButton("Thoat");
        styleButton(btnExit, new Color(108, 117, 125), Color.WHITE);
        btnExit.setPreferredSize(new Dimension(100, 38));

        btnPanel.add(btnLogin);
        btnPanel.add(btnExit);

        btnLogin.addActionListener(e -> handleLogin());
        btnExit.addActionListener(e -> System.exit(0));
        txtPassword.addActionListener(e -> handleLogin());

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(bg.darker(), 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui long nhap day du Ten dang nhap va Mat khau!",
                    "Thieu thong tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            if (conn == null || conn.isClosed()) {
                JOptionPane.showMessageDialog(this,
                        "Khong the ket noi den co so du lieu MySQL! Vui long kiem tra XAMPP.",
                        "Loi Database",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Loi ket noi MySQL: " + ex.getMessage(),
                    "Loi Database",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (("admin".equalsIgnoreCase(username) && "123456".equals(password)) ||
            ("root".equalsIgnoreCase(username) && "".equals(password))) {
            JOptionPane.showMessageDialog(this,
                    "Dang nhap thanh cong! Chuc ban mot ngay lam viec hieu qua.",
                    "Thanh cong",
                    JOptionPane.INFORMATION_MESSAGE);

            this.dispose();

            SwingUtilities.invokeLater(() -> {
                ProductManagementForm productForm = new ProductManagementForm();
                productForm.setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this,
                    "Sai ten dang nhap hoac mat khau!\n(Goi y: admin / 123456)",
                    "Dang nhap that bai",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
