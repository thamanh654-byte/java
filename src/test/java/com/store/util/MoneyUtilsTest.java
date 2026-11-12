package com.store.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Kiểm thử logic đọc/định dạng tiền tệ (không cần kết nối database).
 */
class MoneyUtilsTest {

    @Test
    void parseAcceptsPlainNumber() {
        assertEquals(1500000.0, MoneyUtils.parse("1500000"));
    }

    @Test
    void parseAcceptsVietnameseThousandsSeparator() {
        assertEquals(1500000.0, MoneyUtils.parse("1.500.000"));
    }

    @Test
    void parseAcceptsCommaAsThousandsSeparator() {
        assertEquals(1500000.0, MoneyUtils.parse("1,500,000"));
    }

    @Test
    void parseAcceptsVietnameseDecimalComma() {
        assertEquals(1500000.5, MoneyUtils.parse("1.500.000,50"));
    }

    @Test
    void parseTrimsWhitespaceAndCurrencySymbol() {
        assertEquals(25000.0, MoneyUtils.parse(" 25.000 đ "));
    }

    @Test
    void parseRejectsEmptyInput() {
        assertThrows(NumberFormatException.class, () -> MoneyUtils.parse("   "));
        assertThrows(NumberFormatException.class, () -> MoneyUtils.parse(null));
    }

    @Test
    void parseRejectsGarbage() {
        assertThrows(NumberFormatException.class, () -> MoneyUtils.parse("abc"));
    }

    @Test
    void formatUsesVietnameseCurrency() {
        String formatted = MoneyUtils.format(1500000);
        // Kết quả mong đợi: "1.500.000 ₫" (có thể là "₫" hoặc "đ" tuỳ JDK)
        assertEquals(true, formatted.contains("1.500.000"), "Không đúng định dạng: " + formatted);
    }

    @Test
    void roundKeepsTwoDecimals() {
        assertEquals(0.3, MoneyUtils.round(0.1 + 0.2), 1e-9);
    }
}
