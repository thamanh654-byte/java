# 🏪 Quản Lý Cửa Hàng (Store Management)

Ứng dụng quản lý cửa hàng và lập hóa đơn bán hàng bằng **Java 17 (Core + Swing GUI + Console)** kết nối cơ sở dữ liệu **MySQL** trên **XAMPP**, xây dựng theo kiến trúc chuẩn 3 lớp (**3-Tier Architecture / MVC + DAO Pattern**).

---

## 📌 Tính năng chính

- **Quản lý sản phẩm (CRUD):**
  - Hiển thị danh sách sản phẩm dạng bảng trực quan (tô nền hồng cảnh báo khi tồn kho <= 5).
  - Thêm mới sản phẩm, cập nhật thông tin và ngừng kinh doanh (Xóa mềm - `status = false`).
  - Tìm kiếm theo tên sản phẩm hoặc tên danh mục; nhập tiền linh hoạt (`1500000`, `1.500.000`).
- **Nghiệp vụ Bán hàng & Quản lý Transaction JDBC:**
  - Tự gộp các dòng trùng sản phẩm trong giỏ và chặn số lượng <= 0 trước khi mở transaction.
  - Quản lý Transaction thủ công bằng JDBC (`setAutoCommit(false)`): ghi `orders`, ghi `order_details` (batch) và trừ kho trong `products`.
  - Tự động gọi `commit()` khi thành công hoặc `rollback()` khi gặp bất kỳ sự cố nào (`OutOfStockException`, `ProductNotFoundException`, ...).
  - Khóa dòng `SELECT ... FOR UPDATE` + `UPDATE ... WHERE quantity >= ?` => không thể bán vượt tồn kho (chống oversell khi nhiều giao dịch đồng thời).
- **Thống kê:**
  - Xem danh sách lịch sử các hóa đơn đã tạo và chi tiết từng hóa đơn.
  - Tính toán tổng doanh thu / tổng số hóa đơn toàn cửa hàng theo thời gian thực.
- **2 Chế độ giao diện:**
  - **Desktop GUI:** Java Swing 3 Form (Đăng nhập -> Quản lý sản phẩm -> Bán hàng).
  - **Console Mode:** Giao diện dòng lệnh thân thiện.

---

## 🏗️ Cấu trúc thư mục

```text
src/main/java/com.store
├── config          # Kết nối JDBC (DatabaseConnection - Singleton + HikariCP Pool)
├── model           # POJO Entities (Product, Category, Order, OrderDetail)
├── dao             # Tầng truy xuất dữ liệu (ProductDAO, OrderDAO, CategoryDAO)
├── service         # Tầng xử lý nghiệp vụ & JDBC Transaction (ProductService, OrderService)
├── util            # Tiện ích dùng chung (MoneyUtils, CartUtils, UiUtils)
├── exception       # Các ngoại lệ tùy biến (OutOfStockException, InvalidInputException,...)
├── view            # Giao diện (LoginForm, ProductManagementForm, OrderForm, ConsoleMenu)
└── Main.java       # Điểm khởi chạy chương trình

src/main/resources/db.properties   # Cấu hình kết nối MySQL
src/test/java                      # Unit test JUnit 5 (MoneyUtils, CartUtils)
```

---

## 🚀 Hướng dẫn cài đặt & Khởi chạy

### 1. Chuẩn bị Cơ sở dữ liệu (MySQL / XAMPP)
- Mở **XAMPP Control Panel** và khởi động **MySQL** (Port 3306).
- Truy cập `http://localhost/phpmyadmin/` và import file [`schema.sql`](schema.sql) để tạo database `quanlycuahang` cùng bảng và dữ liệu mẫu.
  > `schema.sql` dùng `INSERT IGNORE` nên import lại nhiều lần không bị nhân đôi dữ liệu.

### 2. Cấu hình kết nối
Sửa file [`src/main/resources/db.properties`](src/main/resources/db.properties), hoặc dùng biến môi trường `STORE_DB_URL` / `STORE_DB_USER` / `STORE_DB_PASSWORD`:

```properties
db.url=jdbc:mysql://localhost:3306/quanlycuahang?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8
db.user=root
db.password=
```

### 3. Khởi chạy ứng dụng
- **Chạy trực tiếp Giao diện Đồ họa Desktop (GUI):** click đúp `run_gui.bat`.
- **Chạy qua Menu lựa chọn (GUI hoặc Console):** chạy `run.bat`.
- **Biên dịch bằng script:** chạy `build.bat` (dùng các jar trong `lib/`).
- **Biên dịch & kiểm thử qua Maven:**
  ```powershell
  mvn clean package     # tạo target/store-management.jar (fat jar)
  mvn exec:java         # chạy GUI
  mvn test              # chạy unit test
  ```
  Chạy Console Mode: `java -cp target/classes;target/dependency/* com.store.Main --console`

### 4. Tài khoản đăng nhập demo
`admin / 123456` (hoặc `root` với mật khẩu rỗng).

---

## ⚙️ Ghi chú kỹ thuật

- **Connection Pool (HikariCP):** dùng lại connection thay vì mở mới mỗi truy vấn; pool đóng gọn khi thoát nhờ shutdown hook.
- **Tiền tệ:** luôn định dạng qua `MoneyUtils.format()`; nhập liệu chấp nhận `1500000`, `1.500.000`, `1,500,000`.
- **Giỏ hàng:** `CartUtils.normalize()` gộp dòng trùng + chặn số lượng <= 0 trước khi mở transaction.

