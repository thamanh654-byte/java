package com.store.exception;

/**
 * Bắn ra khi tìm kiếm hoặc thanh toán sản phẩm không tồn tại hoặc đã ngừng kinh doanh (status = false)
 */
public class ProductNotFoundException extends Exception {
    public ProductNotFoundException(String message) {
        super(message);
    }
}

