package com.store;

import com.store.config.DatabaseConnection;
import com.store.view.ConsoleMenu;
import com.store.view.MainFrame;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Điểm khởi chạy (Main Entry Point) của ứng dụng Store Management
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("🚀 Đang khởi động hệ thống Quản Lý Cửa Hàng (Store Management)...");

        // 1. Kiểm tra kết nối Database trước khi khởi chạy
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Kết nối cơ sở dữ liệu MySQL (quanlycuahang) thành công!");
                
                // Kiểm tra xem người dùng muốn chạy giao diện dòng lệnh hay đồ họa Desktop
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
                    System.out.println("🖥️ Đang mở Giao diện đồ họa Desktop (Java Swing GUI Mode)...");
                    // Thiết lập giao diện theo chuẩn hệ điều hành Windows hiện đại
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Exception ignored) {
                    }

                    SwingUtilities.invokeLater(() -> {
                        MainFrame frame = new MainFrame();
                        frame.setVisible(true);
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("\n❌ KHÔNG THỂ KẾT NỐI ĐẾN CƠ SỞ DỮ LIỆU MYSQL!");
            System.err.println("Chi tiết lỗi: " + e.getMessage());
            System.err.println("\n💡 Hướng dẫn kiểm tra:");
            System.err.println(" 1. Mở XAMPP Control Panel và bật 'Start' module MySQL (Port 3306).");
            System.err.println(" 2. Kiểm tra database 'quanlycuahang' đã được tạo trên phpMyAdmin chưa.");
            System.err.println(" 3. Bạn có thể import file 'schema.sql' có sẵn trong thư mục dự án để tạo bảng.");
            
            // Hiển thị hộp thoại cảnh báo lỗi kết nối nếu đang mở ở chế độ Window
            JOptionPane.showMessageDialog(null,
                    "Không thể kết nối đến MySQL Database 'quanlycuahang'!\n\n" +
                    "Vui lòng đảm bảo XAMPP MySQL đang Start ở Port 3306.\nChi tiết lỗi: " + e.getMessage(),
                    "Lỗi Kết Nối Cơ Sở Dữ Liệu",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
