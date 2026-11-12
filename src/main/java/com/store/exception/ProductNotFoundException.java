package com.store.exception;

/**
 * Bắn ra khi tìm kiếm hoặc thanh toán sản phẩm không tồn tại hoặc đã ngừng kinh doanh (status = false)
 */
public class ProductNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public ProductNotFoundException(String message) {
        super(message);
    }
}

