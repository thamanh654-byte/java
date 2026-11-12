package com.store.util;

import com.store.exception.InvalidInputException;
import com.store.model.OrderDetail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Chuẩn hóa giỏ hàng trước khi thanh toán.
 *
 * <p>Hai lỗi nghiệp vụ được xử lý tại đây:</p>
 * <ul>
 *   <li>Một sản phẩm xuất hiện nhiều dòng trong giỏ sẽ gây vi phạm khóa chính
 *       {@code order_details(order_id, product_id)} -> gộp thành một dòng.</li>
 *   <li>Số lượng &lt;= 0 sẽ làm "trừ kho âm" (thành cộng kho) -> chặn ngay từ đầu.</li>
 * </ul>
 */
public final class CartUtils {

    private CartUtils() {
    }

    /**
     * Gộp các dòng trùng {@code productId} và kiểm tra số lượng hợp lệ.
     * Trả về danh sách mới (không sửa trực tiếp danh sách của phía gọi).
     */
    public static List<OrderDetail> normalize(List<OrderDetail> cartItems) throws InvalidInputException {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new InvalidInputException("Giỏ hàng đang trống, không thể thanh toán!");
        }

        Map<Integer, OrderDetail> merged = new LinkedHashMap<>();
        for (OrderDetail item : cartItems) {
            if (item == null) {
                continue;
            }
            if (item.getProductId() <= 0) {
                throw new InvalidInputException("Mã sản phẩm không hợp lệ: " + item.getProductId());
            }
            if (item.getQuantity() <= 0) {
                throw new InvalidInputException("Số lượng mua của sản phẩm ID " + item.getProductId()
                        + " không hợp lệ (phải lớn hơn 0)!");
            }

            OrderDetail existing = merged.get(item.getProductId());
            if (existing == null) {
                OrderDetail copy = new OrderDetail(item.getProductId(), item.getQuantity(), item.getUnitPrice());
                copy.setProductName(item.getProductName());
                merged.put(item.getProductId(), copy);
            } else {
                long totalQuantity = (long) existing.getQuantity() + item.getQuantity();
                if (totalQuantity > Integer.MAX_VALUE) {
                    throw new InvalidInputException("Số lượng mua của sản phẩm ID " + item.getProductId()
                            + " quá lớn!");
                }
                existing.setQuantity((int) totalQuantity);
                existing.setUnitPrice(item.getUnitPrice());
            }
        }

        if (merged.isEmpty()) {
            throw new InvalidInputException("Giỏ hàng đang trống, không thể thanh toán!");
        }
        return new ArrayList<>(merged.values());
    }
}
