package com.store.exception;

/**
 * Bắn ra khi dữ liệu người dùng nhập không hợp lệ (chuỗi rỗng, số âm, ...)
 */
public class InvalidInputException extends Exception {
    public InvalidInputException(String message) {
        super(message);
    }
}

