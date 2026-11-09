package com.store;

import com.store.config.DatabaseConnection;
import com.store.view.ConsoleMenu;
import com.store.view.LoginForm;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Điểm khởi chạy (Main Entry Point) của ứng dụng Store Management
 * Khởi chạy mặc định bằng Giao diện Đồ họa Desktop (Java Swing 3 Form)
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("🚀 Đang khởi động ứng dụng Quản Lý Cửa Hàng (Store Management)...");

        // 1. Kiểm tra kết nối Database trước khi khởi chạy
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Kết nối cơ sở dữ liệu MySQL (quanlycuahang) thành công!");

                // Kiểm tra cờ tham số nếu muốn chạy chế độ Console
                boolean runConsole = false;
                for (String arg : args) {
                    if ("--console".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg)) {
                        runConsole = true;
                        break;
                    }
                }

                if (runConsole) {
                    System.out.println("💻 Đang mở Giao diện dòng lệnh (Console Mode)...");
                    ConsoleMenu menu = new ConsoleMenu();
                    menu.start();
                } else {
                    System.out.println("🖥️ Đang khởi chạy Giao diện Đồ họa Desktop (Java Swing JFrame)...");
                    
                    // Thiết lập giao diện theo chuẩn hệ điều hành hiện đại (System Look and Feel)
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Exception ignored) {
                    }

                    // Khởi chạy FORM 1: Đăng nhập
                    SwingUtilities.invokeLater(() -> {
                        LoginForm loginForm = new LoginForm();
                        loginForm.setVisible(true);
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("\n❌ KHÔNG THỂ KẾT NỐI ĐẾN CƠ SỞ DỮ LIỆU MYSQL!");
            System.err.println("Chi tiết lỗi: " + e.getMessage());
            System.err.println("\n💡 Hướng dẫn xử lý:");
            System.err.println(" 1. Mở XAMPP Control Panel và đảm bảo dịch vụ MySQL đang chạy (Start ở cổng 3306).");
            System.err.println(" 2. Kiểm tra Database 'quanlycuahang' đã được tạo trên phpMyAdmin chưa.");
            System.err.println(" 3. Kiểm tra user/password trong com.store.config.DatabaseConnection.java.");

            // Hiển thị hộp thoại thông báo lỗi trực quan
            JOptionPane.showMessageDialog(null,
                    "Không thể kết nối đến MySQL Database 'quanlycuahang'!\n\n" +
                    "Vui lòng kiểm tra lại dịch vụ MySQL trên XAMPP (Port 3306).\nChi tiết lỗi: " + e.getMessage(),
                    "Lỗi Kết Nối MySQL",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
