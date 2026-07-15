package org.example;

import org.example.cli.ShopCli;
import org.example.generators.ProductGenerator;
import org.example.persistence.OrderRepository;
import org.example.persistence.ProductRepository;
import org.example.service.OrderProcessor;
import org.example.service.ProductManager;

import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Path dataDir = Path.of("shop-data");
        ProductRepository productRepository = new ProductRepository(dataDir.resolve("products.dat"));
        OrderRepository orderRepository = new OrderRepository(dataDir.resolve("orders.dat"));

        ProductManager manager = new ProductManager(productRepository);
        if (manager.getProducts().isEmpty()) {
            System.out.println("No persisted products found, generating sample catalog.");
            ProductGenerator.generateProducts().forEach(manager::addProduct);
        } else {
            System.out.println("Loaded persisted products from " + dataDir.resolve("products.dat"));
        }

        OrderProcessor orderProcessor = new OrderProcessor(manager, orderRepository);

        try (Scanner scanner = new Scanner(System.in)) {
            new ShopCli(manager, orderProcessor, scanner).run();
        }
    }
}
