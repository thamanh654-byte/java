package com.store.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Quản lý kết nối JDBC theo mẫu thiết kế Singleton (Singleton Pattern).
 * Đảm bảo URL kết nối theo đúng thông số XAMPP cục bộ và hỗ trợ cấp phát Connection
 * cho cả các truy vấn thông thường lẫn Transaction thủ công.
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private static final String URL = "jdbc:mysql://localhost:3306/quanlycuahang?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Không tìm thấy MySQL Driver: " + e.getMessage());
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Mở một Connection mới đến MySQL database 'quanlycuahang'.
     * Phía gọi có thể dùng try-with-resources để tự động đóng hoặc quản lý commit/rollback thủ công.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

