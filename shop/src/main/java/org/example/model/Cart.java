package org.example.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Cart {
    private final List<OrderItem> items = new ArrayList<>();

    public void addProduct(Product product, List<Configuration> configurations, int quantity) {
        OrderItem newItem = new OrderItem(product, configurations, quantity);

        for (int i = 0; i < items.size(); i++) {
            OrderItem existing = items.get(i);
            if (existing.equals(newItem)) {
                items.set(i, existing.withQuantity(existing.quantity() + quantity));
                return;
            }
        }
        items.add(newItem);
    }

    public void removeAt(int index) {
        checkIndex(index);
        items.remove(index);
    }

    public void updateQuantityAt(int index, int newQuantity) {
        checkIndex(index);
        if (newQuantity <= 0) {
            items.remove(index);
            return;
        }
        items.set(index, items.get(index).withQuantity(newQuantity));
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= items.size()) {
            throw new IndexOutOfBoundsException("No cart item at index: " + index);
        }
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

    public Order toOrder(Customer customer) {
        requireNonEmpty();
        return new Order(customer, List.copyOf(items));
    }

    private void requireNonEmpty() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty cart");
        }
    }
}