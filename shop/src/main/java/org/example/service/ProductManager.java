package org.example.service;

import org.example.exception.InsufficientStockException;
import org.example.model.OrderItem;
import org.example.model.Product;
import org.example.repository.ProductRepository;

import java.util.*;

public class ProductManager {
    private final List<Product> products = new ArrayList<>();
    private final ProductRepository repository;

    public ProductManager(ProductRepository repository) {
        this.repository = repository;
        products.addAll(repository.load());
    }

    public synchronized List<Product> getProducts() {
        return List.copyOf(products);
    }

    public synchronized Optional<Product> getProductById(String id) {
        return products.stream().filter(product -> product.getId().equals(id)).findFirst();
    }

    public synchronized void addProduct(Product product) {
        Objects.requireNonNull(product, "Product cannot be null");
        if (products.stream().anyMatch(product1 -> product1.getId().equals(product.getId())))
        {
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
