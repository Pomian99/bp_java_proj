package org.example.service;

import org.example.exception.InsufficientStockException;
import org.example.exception.NegativePriceException;
import org.example.model.OrderItem;
import org.example.model.Product;
import org.example.model.discount.AppliedDiscount;
import org.example.model.discount.Discount;
import org.example.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public class ProductManager {
    private final List<Product> products = new ArrayList<>();
    private final List<Discount> discounts = new ArrayList<>();
    private final ProductRepository repository;

    public ProductManager(ProductRepository repository) {
        this.repository = repository;
        products.addAll(repository.load());
    }

    /**
     * Discounts are kept in memory only (not persisted alongside products),
     * added through this API the same way products are added.
     */
    public synchronized void addDiscount(Discount discount) {
        Objects.requireNonNull(discount, "Discount cannot be null");
        discounts.add(discount);
    }

    public synchronized void removeDiscount(Discount discount) {
        discounts.remove(discount);
    }

    public synchronized List<Discount> getActiveDiscounts() {
        Instant now = Instant.now();
        return discounts.stream()
                .filter(discount -> discount.getExpiresAt().isAfter(now))
                .toList();
    }

    /**
     * Picks the best qualifying Discount for the given cart contents (lowest
     * resulting total). A candidate that would push the total below zero is
     * skipped with a console message instead of being applied.
     */
    public synchronized Optional<AppliedDiscount> getBestDiscount(List<OrderItem> items) {
        BigDecimal itemsTotal = items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        AppliedDiscount best = null;
        BigDecimal bestResultingTotal = null;

        for (Discount discount : getActiveDiscounts()) {
            if (!discount.getCondition().test(items)) {
                continue;
            }

            BigDecimal amount = discount.reduce(itemsTotal);
            BigDecimal resultingTotal = itemsTotal.subtract(amount);

            try {
                requireNonNegative(resultingTotal, discount.getDescription());
            } catch (NegativePriceException e) {
                System.out.println("Skipped discount: " + e.getMessage());
                continue;
            }

            if (best == null || resultingTotal.compareTo(bestResultingTotal) < 0) {
                best = new AppliedDiscount(discount.getDescription(), amount);
                bestResultingTotal = resultingTotal;
            }
        }

        return Optional.ofNullable(best);
    }

    private void requireNonNegative(BigDecimal resultingTotal, String description) {
        if (resultingTotal.signum() < 0) {
            throw new NegativePriceException(
                    "Discount '" + description + "' would result in a negative price and was not applied.");
        }
    }

    public synchronized List<Product> getProducts() {
        return List.copyOf(products);
    }

    public synchronized Optional<Product> getProductById(String id) {
        return products.stream().filter(product -> product.getId().equals(id)).findFirst();
    }

    public synchronized void addProduct(Product product) {
        Objects.requireNonNull(product, "Product cannot be null");
        if (products.stream().anyMatch(product1 -> product1.getId().equals(product.getId()))) {
            throw new IllegalArgumentException("Product with id " + product.getId() + " already exists");
        }

        products.add(product);
        persist();
    }

    public synchronized void removeProduct(String id) {
        Product removed = requireProduct(id);
        products.remove(removed);
        persist();
    }

    public synchronized void updateProduct(Product product) {
        Objects.requireNonNull(product, "Product cannot be null");

        Product existing = requireProduct(product.getId());
        existing.setBasePrice(product.getBasePrice());
        existing.setAvailableQuantity(product.getAvailableQuantity());
        persist();
    }

    public synchronized void reserveStockForOrder(List<OrderItem> items) {
        for (OrderItem item : items) {
            Product product = requireProduct(item.product().getId());
            if (product.getAvailableQuantity() < item.quantity()) {
                throw new InsufficientStockException(
                        "Not enough stock for product '" + product.getName() + "' (id=" + product.getId() + "): "
                                + "requested " + item.quantity() + ", available " + product.getAvailableQuantity());
            }
        }

        for (OrderItem item : items) {
            Product product = requireProduct(item.product().getId());
            product.setAvailableQuantity(product.getAvailableQuantity() - item.quantity());
        }

        persist();
    }

    public synchronized void adjustStock(String id, int delta) {
        Product product = requireProduct(id);
        int newQuantity = product.getAvailableQuantity() + delta;
        if (newQuantity < 0) {
            throw new IllegalArgumentException(
                    "Adjustment would make stock negative for product " + id + ": "
                            + product.getAvailableQuantity() + " + (" + delta + ")");
        }
        product.setAvailableQuantity(newQuantity);
        persist();
    }

    public synchronized void persist() {
        repository.save(products);
    }

    private Product requireProduct(String id) {
        return products.stream()
                .filter(product -> product.matchId(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Product with id " + id + " does not exist"));
    }
}
