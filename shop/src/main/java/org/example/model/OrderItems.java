package org.example.model;

import java.math.BigDecimal;
import java.util.List;

public final class OrderItems {

    private OrderItems() {
    }

    public static BigDecimal total(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
