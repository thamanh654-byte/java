package com.store.view;

import com.store.exception.InvalidInputException;
import com.store.exception.OutOfStockException;
import com.store.exception.ProductNotFoundException;
import com.store.model.Order;
import com.store.model.OrderDetail;
import com.store.model.Product;
import com.store.service.OrderService;
import com.store.service.ProductService;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Giao diện Console Menu người dùng thân thiện, định dạng bảng rõ ràng và bắt lỗi đầy đủ
 */
public class ConsoleMenu {
    private final ProductService productService;
    private final OrderService orderService;
    private final Scanner scanner;
    private final NumberFormat currencyFormatter;

    public ConsoleMenu() {
        this.productService = new ProductService();
        this.orderService = new OrderService();
        this.scanner = new Scanner(System.in);
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    public void start() {
        while (true) {
            printMainMenu();
            int choice = readInt("👉 Lựa chọn của bạn: ");
            switch (choice) {
                case 1 -> handleDisplayProducts();
                case 2 -> handleAddProduct();
                case 3 -> handleUpdateProduct();
                case 4 -> handleSoftDeleteProduct();
                case 5 -> handleCheckout();
                case 6 -> handleViewOrders();
                case 7 -> handleViewRevenue();
                case 0 -> {
                    System.out.println("\n👋 Cảm ơn bạn đã sử dụng hệ thống Quản Lý Cửa Hàng. Tạm biệt!");
                    return;
                }
                default -> System.out.println("❌ Lựa chọn không hợp lệ, vui lòng chọn lại!");
            }
            pressEnterToContinue();
        }
    }

    private void printMainMenu() {
        System.out.println("\n=======================================================");
        System.out.println("       HỆ THỐNG QUẢN LÝ CỬA HÀNG (STORE MANAGEMENT)    ");
        System.out.println("=======================================================");
        System.out.println(" 1. Xem danh sách sản phẩm (Cảnh báo tồn kho)");
        System.out.println(" 2. Thêm mới sản phẩm");
        System.out.println(" 3. Cập nhật thông tin sản phẩm");
        System.out.println(" 4. Xóa sản phẩm (Xóa mềm - Ngừng kinh doanh)");
        System.out.println(" 5. Lập hóa đơn bán hàng (Transaction JDBC)");
        System.out.println(" 6. Danh sách hóa đơn đã tạo");
        System.out.println(" 7. Thống kê tổng doanh thu");
        System.out.println(" 0. Thoát chương trình");
        System.out.println("=======================================================");
    }

    private void handleDisplayProducts() {
        try {
            List<Product> list = productService.listAllProducts();
            if (list.isEmpty()) {
                System.out.println("ℹ️ Hiện chưa có sản phẩm nào trong cửa hàng.");
                return;
            }
            System.out.println("\n" + "=".repeat(85));
            System.out.printf("| %-4s | %-25s | %-16s | %-6s | %-15s | %-8s |\n",
                    "ID", "Tên Sản Phẩm", "Đơn Giá", "Tồn", "Danh Mục", "Cảnh Báo");
            System.out.println("=".repeat(85));

            for (Product p : list) {
                String warning = (p.getQuantity() <= 5) ? "⚠️ SẮP HẾT" : "Đủ hàng";
                System.out.printf("| %-4d | %-25s | %-16s | %-6d | %-15s | %-8s |\n",
                        p.getId(),
                        truncate(p.getName(), 25),
                        currencyFormatter.format(p.getPrice()),
                        p.getQuantity(),
                        p.getCategoryName() != null ? truncate(p.getCategoryName(), 15) : "Chưa phân loại",
                        warning);
            }
            System.out.println("=".repeat(85));
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tải danh sách sản phẩm: " + e.getMessage());
        }
    }

    private void handleAddProduct() {
        System.out.println("\n--- [ THÊM SẢN PHẨM MỚI ] ---");
        try {
            String name = readString("Nhập tên sản phẩm: ");
            double price = readDouble("Nhập đơn giá (VNĐ): ");
            int quantity = readInt("Nhập số lượng tồn kho ban đầu: ");
            int categoryId = readInt("Nhập ID danh mục (ví dụ 1: Điện thoại, 2: Phụ kiện): ");

            productService.addProduct(name, price, quantity, categoryId);
            System.out.println("✅ Thêm sản phẩm mới thành công!");
        } catch (InvalidInputException e) {
            System.out.println("⚠️ Dữ liệu không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    private void handleUpdateProduct() {
        System.out.println("\n--- [ CẬP NHẬT SẢN PHẨM ] ---");
        try {
            int id = readInt("Nhập ID sản phẩm cần cập nhật: ");
            Product current = productService.getProductById(id);
            System.out.println("-> Đang cập nhật cho: " + current.getName());

            String name = readString("Tên mới (Enter để giữ nguyên '" + current.getName() + "'): ");
            if (name.trim().isEmpty()) name = current.getName();

            String priceStr = readString("Đơn giá mới (Enter để giữ nguyên '" + current.getPrice() + "'): ");
            double price = priceStr.trim().isEmpty() ? current.getPrice() : Double.parseDouble(priceStr);

            String qtyStr = readString("Số lượng tồn mới (Enter để giữ nguyên '" + current.getQuantity() + "'): ");
            int quantity = qtyStr.trim().isEmpty() ? current.getQuantity() : Integer.parseInt(qtyStr);

            String catStr = readString("Mã danh mục mới (Enter để giữ nguyên '" + current.getCategoryId() + "'): ");
            int categoryId = catStr.trim().isEmpty() ? current.getCategoryId() : Integer.parseInt(catStr);

            productService.updateProduct(id, name, price, quantity, categoryId);
            System.out.println("✅ Cập nhật sản phẩm thành công!");
        } catch (ProductNotFoundException | InvalidInputException e) {
            System.out.println("⚠️ " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Vui lòng nhập đúng định dạng số!");
        } catch (Exception e) {
            System.err.println("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    private void handleSoftDeleteProduct() {
        System.out.println("\n--- [ XÓA MỀM SẢN PHẨM (NGỪNG KINH DOANH) ] ---");
        try {
            int id = readInt("Nhập ID sản phẩm cần ngừng bán: ");
            Product current = productService.getProductById(id);

            String confirm = readString("Bạn có chắc chắn muốn ngừng bán '" + current.getName() + "'? (Y/N): ");
            if (confirm.equalsIgnoreCase("Y")) {
                productService.softDeleteProduct(id);
                System.out.println("✅ Đã cập nhật trạng thái ngừng kinh doanh cho sản phẩm ID " + id);
            } else {
                System.out.println("ℹ️ Đã hủy thao tác xóa.");
            }
        } catch (ProductNotFoundException e) {
            System.out.println("⚠️ " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    private void handleCheckout() {
        System.out.println("\n--- [ LẬP HÓA ĐƠN BÁN HÀNG (TRANSACTION) ] ---");
        try {
            String customerName = readString("Nhập họ tên khách hàng: ");
            List<OrderDetail> cart = new ArrayList<>();

            while (true) {
                int productId = readInt("Nhập ID sản phẩm cần mua (Nhập 0 để kết thúc chọn): ");
                if (productId == 0) {
                    break;
                }

                Product p = productService.getProductById(productId);
                System.out.println("-> Đã chọn: " + p.getName() + " | Đơn giá: " 
                        + currencyFormatter.format(p.getPrice()) + " | Còn lại: " + p.getQuantity());

                int quantity = readInt("Nhập số lượng muốn mua: ");
                if (quantity <= 0) {
                    System.out.println("⚠️ Số lượng mua phải lớn hơn 0!");
                    continue;
                }

                cart.add(new OrderDetail(productId, quantity, p.getPrice()));
                System.out.println("✔️ Đã thêm vào giỏ hàng.");
            }

            if (cart.isEmpty()) {
                System.out.println("ℹ️ Hủy thanh toán vì giỏ hàng trống.");
                return;
            }

            // Gọi Transaction Service
            int orderId = orderService.createOrderWithTransaction(customerName, cart);
            System.out.println("\n🎉 THANH TOÁN THÀNH CÔNG! Mã hóa đơn: #" + orderId);
            System.out.println("✅ Dữ liệu hóa đơn và tồn kho đã được COMMIT vào Database an toàn!");

        } catch (OutOfStockException | ProductNotFoundException | InvalidInputException e) {
            System.err.println("\n❌ GIAO DỊCH THẤT BẠI: " + e.getMessage());
            System.err.println("⚠️ Hệ thống đã tự động ROLLBACK: Không có hóa đơn nào được lưu và tồn kho được giữ nguyên trạng thái cũ.");
        } catch (Exception e) {
            System.err.println("❌ Lỗi nghiêm trọng: " + e.getMessage());
        }
    }

    private void handleViewOrders() {
        System.out.println("\n--- [ DANH SÁCH HÓA ĐƠN ĐÃ TẠO ] ---");
        try {
            List<Order> orders = orderService.getAllOrders();
            if (orders.isEmpty()) {
                System.out.println("ℹ️ Chưa có hóa đơn nào.");
                return;
            }

            for (Order o : orders) {
                System.out.println("\n" + "-".repeat(60));
                System.out.printf("Mã HĐ: #%-5d | Khách hàng: %-18s | Ngày tạo: %s\n",
                        o.getId(), o.getCustomerName(), o.getCreatedAt());
                System.out.printf("Tổng thanh toán: %s\n", currencyFormatter.format(o.getTotalAmount()));
                System.out.println("Chi tiết các món:");
                
                List<OrderDetail> details = orderService.getOrderDetails(o.getId());
                for (OrderDetail d : details) {
                    System.out.printf("   + %-20s x %-3d | Đơn giá: %-12s | Thành tiền: %s\n",
                            truncate(d.getProductName(), 20),
                            d.getQuantity(),
                            currencyFormatter.format(d.getUnitPrice()),
                            currencyFormatter.format(d.getSubTotal()));
                }
            }
            System.out.println("-".repeat(60));
        } catch (Exception e) {
            System.err.println("❌ Lỗi hiển thị danh sách hóa đơn: " + e.getMessage());
        }
    }

    private void handleViewRevenue() {
        System.out.println("\n--- [ THỐNG KÊ DOANH THU ] ---");
        try {
            double total = orderService.getTotalRevenue();
            System.out.println("=================================================");
            System.out.println(" 💰 TỔNG DOANH THU TOÀN CỬA HÀNG: " + currencyFormatter.format(total));
            System.out.println("=================================================");
        } catch (Exception e) {
            System.err.println("❌ Lỗi tính toán doanh thu: " + e.getMessage());
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Vui lòng nhập số nguyên hợp lệ!");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Vui lòng nhập số thực hợp lệ!");
            }
        }
    }

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private void pressEnterToContinue() {
        System.out.print("\n👉 Nhấn [Enter] để quay lại menu chính...");
        scanner.nextLine();
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}

