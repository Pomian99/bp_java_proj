package org.example.model;

import lombok.Getter;
import lombok.ToString;
import org.example.model.discount.AppliedDiscount;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A finalized, immutable purchase: customer, ordered items and the moment it
 * was placed. Created via Cart.checkout(), then handed to OrderProcessor.
 */
@Getter
@ToString
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final AtomicLong ID_COUNTER = new AtomicLong(1);

    private final String id;
    private final Customer customer;
    private final List<OrderItem> items;
    private final Instant orderDate;
    private final AppliedDiscount appliedDiscount;

    public Order(Customer customer, List<OrderItem> items) {
        this(generateId(), customer, items, Instant.now(), null);
    }

    public Order(Customer customer, List<OrderItem> items, AppliedDiscount appliedDiscount) {
        this(generateId(), customer, items, Instant.now(), appliedDiscount);
    }

    public Order(String id, Customer customer, List<OrderItem> items, Instant orderDate, AppliedDiscount appliedDiscount) {
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
        this.appliedDiscount = appliedDiscount;
    }

    public Optional<AppliedDiscount> getAppliedDiscount() {
        return Optional.ofNullable(appliedDiscount);
    }

    public BigDecimal getTotal() {
        BigDecimal itemsTotal = OrderItems.total(items);
        return appliedDiscount != null ? itemsTotal.subtract(appliedDiscount.amount()) : itemsTotal;
    }

    public static void seedIdCounterFrom(List<Order> existingOrders) {
        long maxSeen = existingOrders.stream()
                .mapToLong(order -> parseSequenceNumber(order.getId()))
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
