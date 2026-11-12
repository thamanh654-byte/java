package com.store.view;

import com.store.config.DatabaseConnection;
import com.store.util.UiUtils;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * FORM 1: Giao diện Đăng nhập hệ thống (Java Swing JFrame)
 */
public final class LoginForm extends JFrame {
    private static final long serialVersionUID = 1L;

    /** Tài khoản demo (đăng nhập cục bộ, không lưu mật khẩu người dùng). */
    private static final String DEMO_USER = "admin";
    private static final String DEMO_PASSWORD = "123456";
    private static final String ROOT_USER = "root";
    private static final String ROOT_PASSWORD = "";

    private transient JTextField txtUsername;
    private transient JPasswordField txtPassword;
    private transient JButton btnLogin;
    private transient JButton btnExit;

    public LoginForm() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Đăng nhập - Hệ thống Quản lý cửa hàng");
        setSize(480, 340);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UiUtils.BG);

        JPanel headerPanel = UiUtils.header(
                "QUẢN LÝ CỬA HÀNG",
                "Bước 1/3  ·  Đăng nhập hệ thống");

        // Form nhập liệu
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UiUtils.CARD);
        formPanel.setBorder(new EmptyBorder(24, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtUsername = new JTextField(15);
        UiUtils.pad(txtUsername);
        txtUsername.setText("admin");

        txtPassword = new JPasswordField(15);
        UiUtils.pad(txtPassword);
        txtPassword.setText("123456");

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.4;
        formPanel.add(UiUtils.label("Tên đăng nhập"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.6;
        formPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.4;
        formPanel.add(UiUtils.label("Mật khẩu"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.6;
        formPanel.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(UiUtils.muted("Tài khoản demo: admin    ·    Mật khẩu: 123456"), gbc);

        // Nút chức năng
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 14));
        btnPanel.setBackground(UiUtils.BG);

        btnLogin = UiUtils.button("Đăng nhập", UiUtils.PRIMARY);
        btnLogin.setPreferredSize(new Dimension(150, 40));

        btnExit = UiUtils.button("Thoát", UiUtils.NEUTRAL);
        btnExit.setPreferredSize(new Dimension(110, 40));

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

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập đầy đủ Tên đăng nhập và Mật khẩu!",
                    "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (!DatabaseConnection.getInstance().testConnection()) {
                JOptionPane.showMessageDialog(this,
                        "Không thể kết nối đến cơ sở dữ liệu MySQL! Vui lòng kiểm tra XAMPP.",
                        "Lỗi Database",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi kết nối MySQL: " + ex.getMessage(),
                    "Lỗi Database",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (isValidCredential(username, password)) {
            JOptionPane.showMessageDialog(this,
                    "Đăng nhập thành công! Chúc bạn một ngày làm việc hiệu quả.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            this.dispose();
            SwingUtilities.invokeLater(() -> new ProductManagementForm().setVisible(true));
        } else {
            JOptionPane.showMessageDialog(this,
                    "Sai tên đăng nhập hoặc mật khẩu!\n(Gợi ý tài khoản: admin / 123456)",
                    "Đăng nhập thất bại",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isValidCredential(String username, String password) {
        return (DEMO_USER.equalsIgnoreCase(username) && DEMO_PASSWORD.equals(password))
                || (ROOT_USER.equalsIgnoreCase(username) && ROOT_PASSWORD.equals(password));
    }
}
