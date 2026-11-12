package com.store;

import com.store.config.DatabaseConnection;
import com.store.view.ConsoleMenu;
import com.store.view.LoginForm;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.GraphicsEnvironment;

/**
 * Điểm khởi chạy (Main Entry Point) của ứng dụng Store Management
 * Khởi chạy mặc định bằng Giao diện Đồ họa Desktop (Java Swing 3 Form)
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("🚀 Đang khởi động ứng dụng Quản Lý Cửa Hàng (Store Management)...");

        // Đảm bảo pool connection được đóng gọn gàng khi tắt ứng dụng
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection.getInstance()::shutdown, "db-pool-shutdown"));

        boolean runConsole = hasFlag(args, "--console", "-c");

        // 1. Kiểm tra kết nối Database trước khi khởi chạy
        try {
            if (!DatabaseConnection.getInstance().testConnection()) {
                reportConnectionError(runConsole, null);
                return;
            }
            System.out.println("✅ Kết nối cơ sở dữ liệu MySQL (quanlycuahang) thành công!");

            if (runConsole) {
                System.out.println("💻 Đang mở Giao diện dòng lệnh (Console Mode)...");
                new ConsoleMenu().start();
            } else {
                startGui();
            }
        } catch (Exception e) {
            // HikariCP báo lỗi ngay khi khởi tạo pool -> cần bắt cả RuntimeException
            reportConnectionError(runConsole, e);
        }
    }

    private static void startGui() {
        System.out.println("🖥️ Đang khởi chạy Giao diện Đồ họa Desktop (Java Swing JFrame)...");

        // Thiết lập giao diện theo chuẩn hệ điều hành hiện đại (System Look and Feel)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Dùng Look and Feel mặc định nếu hệ thống không hỗ trợ
        }

        // Khởi chạy FORM 1: Đăng nhập
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }

    private static boolean hasFlag(String[] args, String... flags) {
        if (args == null) {
            return false;
        }
        for (String arg : args) {
            for (String flag : flags) {
                if (flag.equalsIgnoreCase(arg)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void reportConnectionError(boolean consoleMode, Exception cause) {
        String detail = cause != null ? cause.getMessage() : "Không kết nối được tới MySQL";
        System.err.println("\n❌ KHÔNG THỂ KẾT NỐI ĐẾN CƠ SỞ DỮ LIỆU MYSQL!");
        System.err.println("Chi tiết lỗi: " + detail);
        System.err.println("\n💡 Hướng dẫn xử lý:");
        System.err.println(" 1. Mở XAMPP Control Panel và đảm bảo dịch vụ MySQL đang chạy (Start ở cổng 3306).");
        System.err.println(" 2. Kiểm tra Database 'quanlycuahang' đã được tạo trên phpMyAdmin chưa.");
        System.err.println(" 3. Kiểm tra user/password trong src/main/resources/db.properties.");

        // Chỉ hiển thị hộp thoại khi có màn hình (tránh HeadlessException ở chế độ console)
        if (!consoleMode && !GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(null,
                    "Không thể kết nối đến MySQL Database 'quanlycuahang'!\n\n"
                            + "Vui lòng kiểm tra lại dịch vụ MySQL trên XAMPP (Port 3306).\nChi tiết lỗi: " + detail,
                    "Lỗi Kết Nối MySQL",
                    JOptionPane.ERROR_MESSAGE);
        }
        System.exit(1);
    }
}

