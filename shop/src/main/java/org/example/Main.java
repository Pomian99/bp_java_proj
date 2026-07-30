package org.example;

import org.example.cli.ShopCli;
import org.example.generators.DiscountGenerator;
import org.example.generators.ProductGenerator;
import org.example.repository.FileOrderRepository;
import org.example.repository.FileProductRepository;
import org.example.repository.OrderRepository;
import org.example.repository.ProductRepository;
import org.example.service.OrderProcessor;
import org.example.service.ProductManager;

import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ShopServices services = initializeShopServices(Path.of("shop-data"));

        try (Scanner scanner = new Scanner(System.in)) {
            new ShopCli(services.productManager(), services.orderProcessor(), scanner).run();
        } finally {
            services.orderProcessor().shutdown();
        }
    }

    private static ShopServices initializeShopServices(Path dataDir) {
        ProductRepository productRepository = new FileProductRepository(dataDir.resolve("products.dat"));
        OrderRepository orderRepository = new FileOrderRepository(dataDir.resolve("orders.dat"));

        ProductManager productManager = new ProductManager(productRepository);
        if (productManager.getProducts().isEmpty()) {
            System.out.println("No persisted products found, generating sample catalog.");
            ProductGenerator.generateProducts().forEach(productManager::addProduct);
        } else {
            System.out.println("Loaded persisted products from " + dataDir.resolve("products.dat"));
        }

        DiscountGenerator.generateDiscounts().forEach(productManager::addDiscount);

        OrderProcessor orderProcessor = new OrderProcessor(productManager, orderRepository);

        return new ShopServices(productManager, orderProcessor);
    }

    private record ShopServices(ProductManager productManager, OrderProcessor orderProcessor) {
    }
}
