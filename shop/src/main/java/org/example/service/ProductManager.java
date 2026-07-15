package org.example.service;

import lombok.Getter;
import org.example.model.Product;
import org.example.model.ProductRepository;

import java.util.*;

public class ProductManager {
    @Getter
    private final List<Product> products = new ArrayList<>();
    private final ProductRepository repository;

    public ProductManager(ProductRepository repository) {
        this.repository = repository;
        products.addAll(repository.load());
    }

    public void addProduct(Product product) {
        Objects.requireNonNull(product, "Product cannot be null");
        if (products.stream().anyMatch(product1 -> product1.getId().equals(product.getId())))
        {
            throw new IllegalArgumentException("Product with id " + product.getId() + " already exists");
        }

        products.add(product);
        persist();
    }

    public Optional<Product> getProductById(String id) {
        return products.stream().filter(product -> product.getId().equals(id)).findFirst();
    }

    public void removeProduct(String id) {
        Product removed = products.stream()
                .filter(product -> product.matchId(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Product with id " + id + " does not exist"));

        products.remove(removed);
        persist();
    }

    public void updateProduct(Product product) {
        Objects.requireNonNull(product, "Product cannot be null");

        Product updated = products.stream()
                .filter(product1 -> product1.matchId(product.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Product with id " + product.getId() + " does not exist"));
        products.remove(updated);
        products.add(product);
        persist();
    }

    public void persist() {
        repository.save(products);
    }
}
