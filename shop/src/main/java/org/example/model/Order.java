package org.example.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class Order {
    private static final AtomicLong ID_COUNTER = new AtomicLong(1);

    private final String id;
    private final Customer customer;
    private final List<OrderItem> items;
    private final LocalDateTime orderDate;

    public Order(Customer customer, List<OrderItem> items) {
        this(generateId(), customer, items, LocalDateTime.now());
    }

    public Order(String id, Customer customer, List<OrderItem> items, LocalDateTime orderDate) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Order id must not be blank");
        }
        if (customer == null) {
            throw new IllegalArgumentException("Customer must not be null");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        this.id = id;
        this.customer = customer;
        this.items = List.copyOf(items);
        this.orderDate = orderDate;
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String generateId() {
        return String.format("ORDER-%04d", ID_COUNTER.getAndIncrement());
    }
}