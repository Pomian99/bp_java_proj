package org.example.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Cart {
    private final List<OrderItem> items = new ArrayList<>();

    public void addProduct(Product product, List<Configuration> configurations, int quantity) {
        OrderItem newItem = new OrderItem(product, configurations, quantity);

        for (int i = 0; i < items.size(); i++) {
            OrderItem existing = items.get(i);
            if (existing.equals(newItem)) {
                items.set(i, existing.withQuantity(existing.getQuantity() + quantity));
                return;
            }
        }
        items.add(newItem);
    }

    public void removeProduct(String productId) {
        items.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public void updateQuantity(String productId, int newQuantity) {
        if (newQuantity <= 0) {
            removeProduct(productId);
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            if (item.getProduct().getId().equals(productId)) {
                items.set(i, item.withQuantity(newQuantity));
                return;
            }
        }
        throw new NoSuchElementException("No cart item for product id: " + productId);
    }

    public List<OrderItem> viewCart() {
        return List.copyOf(items);
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }

    public Order checkout(Customer customer) {
        requireNonEmpty();
        Order order = new Order(customer, List.copyOf(items));
        clear();
        return order;
    }

    private void requireNonEmpty() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty cart");
        }
    }
}