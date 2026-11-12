package com.store.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Tiện ích định dạng / đọc số tiền dùng chung cho toàn bộ ứng dụng.
 * Thay cho việc mỗi Form tự tạo NumberFormat (và dùng constructor Locale đã bị deprecated).
 */
public final class MoneyUtils {

    /** Locale Việt Nam (không dùng new Locale(...) vì đã deprecated từ Java 19). */
    public static final Locale VIETNAM = Locale.forLanguageTag("vi-VN");

    /** NumberFormat không thread-safe nên mỗi thread giữ một instance riêng. */
    private static final ThreadLocal<NumberFormat> CURRENCY = ThreadLocal.withInitial(() -> {
        NumberFormat nf = NumberFormat.getCurrencyInstance(VIETNAM);
        nf.setMaximumFractionDigits(0);
        return nf;
    });

    private MoneyUtils() {
    }

    /** Ví dụ: 1500000 -> "1.500.000 ₫". */
    public static String format(double amount) {
        return CURRENCY.get().format(amount);
    }

    /**
     * Đọc số tiền do người dùng nhập, chấp nhận cả các dạng phổ biến:
     * "1500000", "1.500.000", "1,500,000", "1.500.000,50", "1500000.5".
     *
     * @throws NumberFormatException nếu chuỗi rỗng hoặc chứa ký tự không hợp lệ
     */
    public static double parse(String raw) {
        if (raw == null) {
            throw new NumberFormatException("Giá trị rỗng");
        }
        String s = raw.trim().replace(" ", "").replace("₫", "").replace("đ", "").replace("Đ", "");
        if (s.isEmpty()) {
            throw new NumberFormatException("Giá trị rỗng");
        }
        long commaCount = s.chars().filter(c -> c == ',').count();
        if (commaCount > 1) {
            // Dạng 1,500,000 -> dấu phẩy là phân cách hàng nghìn
            s = s.replace(",", "");
        } else if (commaCount == 1) {
            // Dạng 1.500.000,50 -> bỏ dấu chấm, đổi phẩy thành dấu chấm thập phân
            s = s.replace(".", "").replace(',', '.');
        } else {
            // Không có dấu phẩy -> dấu chấm là phân cách hàng nghìn (chuẩn vi-VN)
            s = s.replace(".", "");
        }
        return Double.parseDouble(s);
    }

    /** Làm tròn 2 chữ số thập phân, tránh sai số của kiểu double khi cộng dồn tiền. */
    public static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
