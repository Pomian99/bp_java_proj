package org.example.model;

import java.nio.file.Path;

public class ProductRepository extends ListFileRepository<Product> {

    public ProductRepository(Path filePath) {
        super(filePath);
    }
}
