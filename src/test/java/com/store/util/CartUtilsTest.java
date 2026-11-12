package com.store.util;

import com.store.exception.InvalidInputException;
import com.store.model.OrderDetail;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kiểm thử việc chuẩn hóa giỏ hàng trước khi ghi hóa đơn.
 */
class CartUtilsTest {

    @Test
    void mergesDuplicatedProductLines() throws Exception {
        List<OrderDetail> input = Arrays.asList(
                new OrderDetail(1, 2, 100),
                new OrderDetail(2, 1, 200),
                new OrderDetail(1, 3, 100)
        );

        List<OrderDetail> result = CartUtils.normalize(input);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getProductId());
        assertEquals(5, result.get(0).getQuantity());
        assertEquals(700.0, sum(result), 1e-9);
    }

    @Test
    void rejectsEmptyCart() {
        assertThrows(InvalidInputException.class, () -> CartUtils.normalize(List.of()));
        assertThrows(InvalidInputException.class, () -> CartUtils.normalize(null));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(InvalidInputException.class,
                () -> CartUtils.normalize(List.of(new OrderDetail(1, 0, 100))));
        assertThrows(InvalidInputException.class,
                () -> CartUtils.normalize(List.of(new OrderDetail(1, -2, 100))));
    }

    @Test
    void rejectsInvalidProductId() {
        assertThrows(InvalidInputException.class,
                () -> CartUtils.normalize(List.of(new OrderDetail(0, 1, 100))));
    }

    @Test
    void doesNotMutateInputList() throws Exception {
        OrderDetail line = new OrderDetail(1, 2, 100);
        List<OrderDetail> input = List.of(line);

        List<OrderDetail> result = CartUtils.normalize(input);

        assertEquals(2, line.getQuantity(), "Không được sửa danh sách gốc");
        assertEquals(2, result.get(0).getQuantity());
        assertTrue(result.get(0) != line, "Phải trả về bản sao");
    }

    private double sum(List<OrderDetail> details) {
        double total = 0;
        for (OrderDetail d : details) {
            total += d.getSubTotal();
        }
        return total;
    }
}
