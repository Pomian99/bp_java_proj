package org.example.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Cart {
    private final List<CartItem> items = new ArrayList<>();

    public void addProduct(Product product, List<Configuration> productConfiguration, int quantity) {
        Objects.requireNonNull(product, "Product cannot be null");

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Optional<CartItem> existingItem = findItemByProductId(product.getId());
        int requestedQuantity = quantity + existingItem
                .map(CartItem::getQuantity)
                .orElse(0);

        validateAvailableQuantity(product, requestedQuantity);

        if (existingItem.isPresent()) {
            existingItem.get().increaseQuantity(quantity);
        } else {
            items.add(new CartItem(product, List.of(), quantity));
        }
    }

    public void removeProduct(String productId) {
        boolean removed = items.removeIf(item -> item.getProduct().getId().equals(productId));

        if (!removed) {
            throw new IllegalArgumentException("Product with id " + productId + " is not in the cart");
        }
    }

    public void changeProductQuantity(String productId, int quantity) {
        CartItem item = findItemByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product with id " + productId + " is not in the cart"));

        validateAvailableQuantity(item.getProduct(), quantity);
        item.changeQuantity(quantity);
    }

    public List<CartItem> getItems() {
        return List.copyOf(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getTotalQuantity() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal checkout() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty cart");
        }

        BigDecimal totalPrice = getTotalPrice();
        clear();
        return totalPrice;
    }

    public void clear() {
        items.clear();
    }

    private Optional<CartItem> findItemByProductId(String productId) {
        return items.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();
    }

    private void validateAvailableQuantity(Product product, int requestedQuantity) {
        if (requestedQuantity > product.getAvailableQuantity()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock for product " + product.getId());
        }
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "Cart is empty";
        }

        String cartItems = items.stream()
                .map(CartItem::toString)
                .collect(Collectors.joining(System.lineSeparator()));

        return cartItems + System.lineSeparator() + String.format("Total: %.2f PLN", getTotalPrice());
    }
}
