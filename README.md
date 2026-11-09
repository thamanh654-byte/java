# 🏪 Quản Lý Cửa Hàng (Store Management)

Ứng dụng quản lý cửa hàng và lập hóa đơn bán hàng bằng **Java (Core + Swing GUI + Console)** kết nối cơ sở dữ liệu **MySQL** trên **XAMPP**, xây dựng theo kiến trúc chuẩn 3 lớp (**3-Tier Architecture / MVC + DAO Pattern**).

---

## 📌 Tính năng chính

- **Quản lý sản phẩm (CRUD):**
  - Hiển thị danh sách sản phẩm theo dạng bảng trực quan (cảnh báo tồn kho sắp hết khi số lượng $\le 5$).
  - Thêm mới sản phẩm, cập nhật thông tin và cập nhật trạng thái ngừng kinh doanh (Xóa mềm - `status = false`).
- **Nghiệp vụ Bán hàng & Quản lý Transaction JDBC:**
  - Kiểm tra tồn kho của từng sản phẩm trong giỏ hàng.
  - Quản lý Transaction thủ công bằng JDBC (`setAutoCommit(false)`): Thêm vào bảng `orders`, `order_details` và trừ kho tương ứng ở `products`.
  - Tự động gọi `commit()` khi hoàn tất thành công hoặc `rollback()` khi gặp bất kỳ sự cố nào (`OutOfStockException`, `ProductNotFoundException`,...).
- **Thống kê:**
  - Xem danh sách lịch sử các hóa đơn đã tạo và chi tiết từng hóa đơn.
  - Tính toán tổng doanh thu toàn cửa hàng theo thời gian thực.
- **2 Chế độ giao diện:**
  - **Desktop GUI:** Giao diện đồ họa hiện đại bằng Java Swing (`JFrame`, `JTable`, `JTabbedPane`,...).
  - **Console Mode:** Giao diện dòng lệnh thân thiện.

---

## 🏗️ Cấu trúc thư mục

```text
com.store
├── config          # Quản lý kết nối JDBC (DatabaseConnection - Singleton Pattern)
├── model           # POJO Entities (Product, Category, Order, OrderDetail)
├── dao             # Tầng truy xuất dữ liệu (ProductDAO, OrderDAO, CategoryDAO)
├── service         # Tầng xử lý nghiệp vụ & JDBC Transaction (ProductService, OrderService)
├── exception       # Các ngoại lệ tùy biến (OutOfStockException, InvalidInputException,...)
├── view            # Giao diện người dùng (MainFrame - Swing GUI & ConsoleMenu - CLI)
└── Main.java       # Điểm khởi chạy chương trình
```

---

## 🚀 Hướng dẫn cài đặt & Khởi chạy

### 1. Chuẩn bị Cơ sở dữ liệu (MySQL / XAMPP)
- Mở **XAMPP Control Panel** và khởi động **MySQL** (Port 3306).
- Truy cập `http://localhost/phpmyadmin/` và import file [`schema.sql`](schema.sql) để tạo cơ sở dữ liệu `quanlycuahang` cùng các bảng và dữ liệu mẫu ban đầu.

### 2. Khởi chạy ứng dụng
- **Chạy trực tiếp Giao diện Đồ họa Desktop (GUI):**
  - Click đúp vào file `run_gui.bat` (hoặc chạy `.\run_gui.bat` trên Terminal).
- **Chạy qua Menu lựa chọn (GUI hoặc Console):**
  - Chạy `.\run.bat` trên Terminal.
- **Biên dịch qua Maven:**
  ```powershell
  mvn clean compile
  mvn exec:java
  ```

