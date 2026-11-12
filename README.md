# Quản Lý Cửa Hàng (Store Management)

Ứng dụng quản lý cửa hàng và lập hóa đơn bán hàng bằng **Java 17** (Java Swing + Console) kết nối **MySQL** trên **XAMPP**, xây dựng theo kiến trúc 3 lớp (**MVC + DAO Pattern**).

Toàn bộ chữ trên giao diện là **tiếng Việt có dấu**. Giao diện đồ họa đi theo luồng 3 bước để dễ thao tác và dễ trình bày:

| Bước | Màn hình | Việc làm |
| --- | --- | --- |
| 1/3 | Đăng nhập | Xác thực tài khoản, kiểm tra kết nối MySQL |
| 2/3 | Quản lý sản phẩm | Thêm, sửa, tìm kiếm, ngừng kinh doanh |
| 3/3 | Bán hàng & lập hóa đơn | Lập hóa đơn, trừ kho, xem doanh thu |

---

## Tính năng chính

- **Quản lý sản phẩm (CRUD):**
  - Bảng sản phẩm có dòng xen kẽ màu, **tô nền hồng khi tồn kho từ 5 trở xuống**.
  - Thêm mới, cập nhật và ngừng kinh doanh (xóa mềm - `status = false`, dữ liệu vẫn được giữ lại).
  - Tìm kiếm theo tên sản phẩm hoặc tên danh mục.
  - Ô nhập tiền chấp nhận nhiều cách gõ: `1500000`, `1.500.000`, `1,500,000`.
- **Bán hàng với Transaction JDBC:**
  - Tự gộp các dòng trùng một sản phẩm trong giỏ và từ chối số lượng từ 0 trở xuống.
  - Một transaction thủ công (`setAutoCommit(false)`): ghi `orders`, ghi `order_details` theo lô (batch) và trừ kho `products`.
  - Thành công thì `commit()`, gặp lỗi (`OutOfStockException`, `ProductNotFoundException`, ...) thì `rollback()` toàn bộ.
  - Khóa dòng `SELECT ... FOR UPDATE` kết hợp `UPDATE ... WHERE quantity >= ?` nên **không thể bán vượt số lượng tồn kho**, kể cả khi có nhiều giao dịch cùng lúc.
- **Thống kê:** lịch sử hóa đơn kèm chi tiết từng hóa đơn, tổng doanh thu và tổng số hóa đơn.
- **Hai chế độ chạy:** giao diện Swing (mặc định) hoặc menu dòng lệnh (Console).

---

## Cấu trúc thư mục

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

## Hướng dẫn cài đặt & khởi chạy

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

Cách nhanh nhất là dùng các file `.bat` (đã biên dịch sẵn trong thư mục `bin/`):

- **Giao diện đồ họa:** click đúp `run_gui.bat`.
- **Chọn GUI hoặc Console:** chạy `run.bat`, nhập `1` hoặc `2`.
- **Biên dịch lại từ mã nguồn:** chạy `build.bat` (dùng các thư viện trong `lib/`).

Hoặc dùng Maven:

```powershell
mvn clean package    # tạo file target/store-management.jar (đã kèm mọi thư viện)
mvn exec:java        # chạy giao diện đồ họa
mvn test             # chạy unit test
```

Chạy chế độ Console từ file jar:

```powershell
java -jar target/store-management.jar --console
```

### 4. Tài khoản đăng nhập demo

| Tên đăng nhập | Mật khẩu |
| --- | --- |
| `admin` | `123456` |
| `root` | (để trống) |

---

## Cơ sở dữ liệu

Database `quanlycuahang` gồm 4 bảng:

| Bảng | Khóa chính | Nội dung |
| --- | --- | --- |
| `categories` | `category_id` | Danh mục sản phẩm |
| `products` | `product_id` | Sản phẩm, giá bán, giá nhập, tồn kho, trạng thái |
| `orders` | `order_id` | Hóa đơn: khách hàng, tổng tiền, thời điểm tạo |
| `order_details` | `order_id` + `product_id` | Từng món trong hóa đơn: số lượng, đơn giá |

`products.category_id` tham chiếu tới `categories`. `order_details` tham chiếu tới cả `orders` (xóa hóa đơn sẽ xóa theo chi tiết) và `products`.

---

## Ghi chú kỹ thuật

- **Connection Pool (HikariCP):** tái sử dụng kết nối thay vì mở mới mỗi truy vấn, giảm độ trễ khi tải bảng và thống kê. Pool được đóng khi thoát chương trình nhờ shutdown hook.
- **Cấu hình linh hoạt:** thông tin kết nối lấy theo thứ tự biến môi trường (`STORE_DB_URL`, `STORE_DB_USER`, `STORE_DB_PASSWORD`), rồi tới `db.properties`, cuối cùng là giá trị mặc định của XAMPP.
- **Tiền tệ:** `MoneyUtils` định dạng và đọc số tiền theo chuẩn Việt Nam, làm tròn 2 chữ số thập phân khi cộng dồn.
- **Giỏ hàng:** `CartUtils.normalize()` gộp dòng trùng sản phẩm và chặn số lượng không hợp lệ trước khi mở transaction, tránh vi phạm khóa chính của `order_details`.
- **Giá nhập:** khi thêm hoặc sửa sản phẩm, `import_price` được tính bằng 70% giá bán.

