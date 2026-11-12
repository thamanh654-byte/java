package com.store.exception;

/**
 * Bắn ra khi số lượng sản phẩm khách đặt mua vượt quá số lượng tồn kho hiện có
 */
public class OutOfStockException extends Exception {
    private static final long serialVersionUID = 1L;

    public OutOfStockException(String message) {
        super(message);
    }
}

