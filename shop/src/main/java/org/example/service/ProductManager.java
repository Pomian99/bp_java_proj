package org.example.service;

import org.example.domain.product.Product;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProductManager {
    private final Map<String, Product> products = new LinkedHashMap<>();

    public void addProduct(Product product) {
        Objects.requireNonNull(product, "Product cannot be null");

        if (products.containsKey(product.getId())) {
            throw new IllegalArgumentException("Product with id " + product.getId() + " already exists");
        }

        products.put(product.getId(), product);
    }

    public Product getProductById(String id) {
        return products.get(id);
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    public void removeProduct(String id) {
        if (!products.containsKey(id)) {
            throw new IllegalArgumentException("Product with id " + id + " does not exist");
        }

        products.remove(id);
    }

    public void updateProduct(Product product) {
        Objects.requireNonNull(product, "Product cannot be null");

        if (!products.containsKey(product.getId())) {
            throw new IllegalArgumentException("Product with id " + product.getId() + " does not exist");
        }

        products.put(product.getId(), product);
    }
}
