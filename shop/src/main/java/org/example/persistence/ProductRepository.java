package org.example.persistence;

import org.example.model.Product;

import java.nio.file.Path;

public class ProductRepository extends ListFileRepository<Product> {

    public ProductRepository(Path filePath) {
        super(filePath);
    }
}
