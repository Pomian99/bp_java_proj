package org.example.repository;

import org.example.model.Product;

import java.nio.file.Path;

public class FileProductRepository extends ListFileRepository<Product> implements ProductRepository {

    public FileProductRepository(Path filePath) {
        super(filePath);
    }
}
