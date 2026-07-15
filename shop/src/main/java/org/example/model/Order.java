package org.example.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A finalized, immutable purchase: customer, ordered items and the moment it
 * was placed. Created via Cart.checkout(), then handed to OrderProcessor.
 */
public record Order(String id, Customer customer, List<OrderItem> items,
                    LocalDateTime orderDate) implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final AtomicLong ID_COUNTER = new AtomicLong(1);

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

    public static void seedIdCounterFrom(List<Order> existingOrders) {
        long maxSeen = existingOrders.stream()
                .mapToLong(order -> parseSequenceNumber(order.id()))
                .max()
                .orElse(0);
        ID_COUNTER.updateAndGet(current -> Math.max(current, maxSeen + 1));
    }

    private static long parseSequenceNumber(String id) {
        try {
            return Long.parseLong(id.substring(id.lastIndexOf('-') + 1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String generateId() {
        return String.format("ORDER-%04d", ID_COUNTER.getAndIncrement());
    }
}