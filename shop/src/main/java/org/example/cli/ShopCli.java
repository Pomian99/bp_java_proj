package org.example.cli;

import org.example.model.Cart;
import org.example.model.Configuration;
import org.example.model.Product;
import org.example.model.enums.ConfigType;
import org.example.service.OrderProcessor;
import org.example.service.ProductManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ShopCli {
    private final ProductManager productManager;
    private final OrderProcessor orderProcessor;
    private final Cart cart = new Cart();
    private final Scanner scanner;

    public ShopCli(ProductManager productManager, OrderProcessor orderProcessor, Scanner scanner) {
        this.productManager = productManager;
        this.orderProcessor = orderProcessor;
        this.scanner = scanner;
    }

    public void run() {
        System.out.println("Welcome to the online shop!");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> browseProducts();
                case "2" -> viewCart();
                case "3" -> checkout();
                case "4" -> running = false;
                default -> System.out.println("Unknown option, please choose a number from the menu.");
            }
        }

        System.out.println("Goodbye!");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== Main menu ===");
        System.out.println("1. Browse products");
        System.out.println("2. Cart");
        System.out.println("3. Place order");
        System.out.println("4. Exit");
        System.out.print("Choose an option: ");
    }

    private void browseProducts() {
        List<Product> products = productManager.getProducts();
        if (products.isEmpty()) {
            System.out.println("No products available.");
            return;
        }

        while (true) {
            System.out.println();
            System.out.println("=== Products ===");
            for (Product product : products) {
                System.out.printf("%-14s %-11s %-25s %10s PLN  (stock: %d)%n",
                        product.getId(), product.getType(), product.getName(),
                        formatPrice(product.getPrice()), product.getAvailableQuantity());
            }

            System.out.println();
            System.out.println("Enter a product id to view details, or press Enter to go back:");
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return;
            }

            Optional<Product> found = productManager.getProductById(input);
            if (found.isPresent()) {
                showProductDetails(found.get());
            } else {
                System.out.println("No product found with id: " + input);
            }
        }
    }

    private void showProductDetails(Product product) {
        System.out.println();
        System.out.println("=== " + product.getName() + " ===");
        System.out.println("Id: " + product.getId());
        System.out.println("Type: " + product.getType());
        System.out.println("Price: " + formatPrice(product.getPrice()) + " PLN");
        System.out.println("Available quantity: " + product.getAvailableQuantity());

        List<Configuration> configurations = product.getAvailableConfiguration();
        if (configurations != null && !configurations.isEmpty()) {
            System.out.println("Available configurations:");
            Map<ConfigType, List<Configuration>> byType = configurations.stream()
                    .collect(Collectors.groupingBy(Configuration::type, LinkedHashMap::new, Collectors.toList()));
            for (Map.Entry<ConfigType, List<Configuration>> entry : byType.entrySet()) {
                System.out.println("  " + entry.getKey() + ":");
                for (Configuration configuration : entry.getValue()) {
                    System.out.printf("    - %-20s +%s PLN%n", configuration.name(), formatPrice(configuration.price()));
                }
            }
        }
    }

    private void viewCart() {
        System.out.println("Cart");
    }

    private void checkout() {
        System.out.println("Place order");
    }

    private String formatPrice(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP).toString();
    }
}
