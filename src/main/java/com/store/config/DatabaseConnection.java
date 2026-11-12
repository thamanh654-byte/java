package com.store.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Quản lý kết nối JDBC theo mẫu thiết kế Singleton (Singleton Pattern) + Connection Pool (HikariCP).
 *
 * <p>Thông số kết nối được nạp theo thứ tự ưu tiên:</p>
 * <ol>
 *   <li>Biến môi trường: {@code STORE_DB_URL}, {@code STORE_DB_USER}, {@code STORE_DB_PASSWORD}</li>
 *   <li>File cấu hình {@code db.properties} (nằm trên classpath)</li>
 *   <li>Giá trị mặc định của XAMPP: localhost:3306, root, mật khẩu rỗng</li>
 * </ol>
 *
 * <p>Pool giúp tái sử dụng Connection thay vì mở mới liên tục, giảm độ trễ khi load bảng / thống kê.</p>
 */
public final class DatabaseConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/quanlycuahang?useSSL=false&allowPublicKeyRetrieval=true"
                    + "&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static final String CONFIG_FILE = "db.properties";

    private static volatile DatabaseConnection instance;

    private final HikariDataSource dataSource;

    private DatabaseConnection() {
        Properties props = loadExternalConfig();

        String url = pick(System.getenv("STORE_DB_URL"), props.getProperty("db.url"), DEFAULT_URL);
        String user = pick(System.getenv("STORE_DB_USER"), props.getProperty("db.user"), DEFAULT_USER);
        String password = pick(System.getenv("STORE_DB_PASSWORD"), props.getProperty("db.password"), DEFAULT_PASSWORD);

        try {
            // Nạp driver sớm để báo lỗi rõ ràng nếu thiếu mysql-connector trên classpath
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Không tìm thấy MySQL Driver (mysql-connector-j). Hãy kiểm tra lại classpath/lib!", e);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setPoolName("store-pool");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10_000);
        config.setIdleTimeout(60_000);
        config.setMaxLifetime(30 * 60_000L);
        // Câu lệnh kiểm tra connection còn sống trước khi cấp phát
        config.setConnectionTestQuery("SELECT 1");

        this.dataSource = new HikariDataSource(config);
    }

    public static DatabaseConnection getInstance() {
        DatabaseConnection local = instance;
        if (local == null) {
            synchronized (DatabaseConnection.class) {
                local = instance;
                if (local == null) {
                    local = new DatabaseConnection();
                    instance = local;
                }
            }
        }
        return local;
    }

    /**
     * Mượn một Connection từ pool. Phía gọi dùng try-with-resources để trả connection về pool.
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Kiểm tra nhanh kết nối CSDL (dùng cho màn hình đăng nhập / khởi động).
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && conn.isValid(3);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Đóng pool khi tắt ứng dụng (giải phóng tài nguyên).
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private static String pick(String envValue, String fileValue, String fallback) {
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue.trim();
        }
        return fallback;
    }

    private static Properties loadExternalConfig() {
        Properties props = new Properties();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = DatabaseConnection.class.getClassLoader();
        }
        try (InputStream in = cl.getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    props.load(reader);
                }
            }
        } catch (IOException ignored) {
            // Không có file cấu hình -> dùng giá trị mặc định
        }
        return props;
    }
}


