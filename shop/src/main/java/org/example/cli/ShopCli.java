package org.example.cli;

import org.example.exception.InsufficientStockException;
import org.example.model.Cart;
import org.example.model.Configuration;
import org.example.model.Customer;
import org.example.model.Order;
import org.example.model.OrderItem;
import org.example.model.Product;
import org.example.model.enums.ConfigType;
import org.example.service.OrderProcessor;
import org.example.service.ProductManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
        System.out.print("""

                === Main menu ===
                1. Browse products
                2. Cart
                3. Place order
                4. Exit
                Choose an option: """);
    }

    private void browseProducts() {
        List<Product> products = productManager.getProducts();
        if (products.isEmpty()) {
            System.out.println("No products available.");
            return;
        }

        while (true) {
            System.out.print("""

                    === Products ===
                    """);
            for (Product product : products) {
                System.out.printf("%-14s %-11s %-25s %10s PLN  (stock: %d)%n",
                        product.getId(), product.getType(), product.getName(),
                        formatPrice(product.getPrice()), product.getAvailableQuantity());
            }

            System.out.print("""

                    Enter a product id to view details, or press Enter to go back:
                    > """);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return;
            }

            Optional<Product> found = productManager.getProductById(input);
            if (found.isPresent()) {
                showProductDetails(found.get());
            } else {
                System.out.printf("No product found with id: %s%n", input);
            }
        }
    }

    private void showProductDetails(Product product) {
        System.out.printf("""

                === %s ===
                Id: %s
                Type: %s
                Price: %s PLN
                Available quantity: %d
                """,
                product.getName(), product.getId(), product.getType(),
                formatPrice(product.getPrice()), product.getAvailableQuantity());

        List<Configuration> configurations = product.getAvailableConfiguration();
        if (configurations != null && !configurations.isEmpty()) {
            System.out.println("Available configurations:");
            Map<ConfigType, List<Configuration>> byType = configurations.stream()
                    .collect(Collectors.groupingBy(Configuration::type, LinkedHashMap::new, Collectors.toList()));
            for (Map.Entry<ConfigType, List<Configuration>> entry : byType.entrySet()) {
                System.out.printf("  %s:%n", entry.getKey());
                for (Configuration configuration : entry.getValue()) {
                    System.out.printf("    - %-20s +%s PLN%n", configuration.name(), formatPrice(configuration.price()));
                }
            }
        }

        while (true) {
            System.out.print("""

                    1. Add to cart
                    2. Back to product list
                    Choose an option: """);

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    addProductToCart(product);
                    return;
                }
                case "2", "" -> {
                    return;
                }
                default -> System.out.println("Unknown option, please choose a number from the menu.");
            }
        }
    }

    private void viewCart() {
        while (true) {
            printCartContents();

            System.out.print("""

                    1. Add product to cart
                    2. Update item quantity
                    3. Remove item
                    4. Back to main menu
                    Choose an option: """);

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> addProductToCart();
                case "2" -> updateCartItemQuantity();
                case "3" -> removeCartItem();
                case "4" -> {
                    return;
                }
                default -> System.out.println("Unknown option, please choose a number from the menu.");
            }
        }
    }

    private void printCartContents() {
        System.out.print("""

                === Cart ===
                """);

        List<OrderItem> items = cart.viewCart();
        if (items.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            System.out.printf("%d. %-14s %-25s x%-3d %10s PLN%n",
                    i + 1, item.product().getId(), item.product().getName(), item.quantity(), formatPrice(item.getLineTotal()));
            for (Configuration configuration : item.selectedConfigurations()) {
                System.out.printf("    + %-20s +%s PLN%n", configuration.name(), formatPrice(configuration.price()));
            }
        }

        System.out.printf("%nTotal: %s PLN%n", formatPrice(cart.getTotal()));
    }

    private void addProductToCart() {
        System.out.print("Enter product id: ");
        String id = scanner.nextLine().trim();

        Optional<Product> found = productManager.getProductById(id);
        if (found.isEmpty()) {
            System.out.printf("No product found with id: %s%n", id);
            return;
        }

        addProductToCart(found.get());
    }

    private void addProductToCart(Product product) {
        List<Configuration> selected = selectConfigurations(product);

        int quantity = readQuantity();
        if (quantity <= 0) {
            System.out.println("Quantity must be greater than zero, cancelled.");
            return;
        }

        try {
            cart.addProduct(product, selected, quantity);
            System.out.printf("Added to cart: %s%n", product.getName());
        } catch (IllegalArgumentException e) {
            System.out.printf("Could not add product to cart: %s%n", e.getMessage());
        }
    }

    private List<Configuration> selectConfigurations(Product product) {
        List<Configuration> available = product.getAvailableConfiguration();
        if (available == null || available.isEmpty()) {
            return List.of();
        }

        List<Configuration> selected = new ArrayList<>();
        Map<ConfigType, List<Configuration>> byType = available.stream()
                .collect(Collectors.groupingBy(Configuration::type, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<ConfigType, List<Configuration>> entry : byType.entrySet()) {
            List<Configuration> options = entry.getValue();
            boolean allowsMultiple = options.stream().anyMatch(Configuration::multipleChoice);

            System.out.printf("%n%s options:%n", entry.getKey());
            for (int i = 0; i < options.size(); i++) {
                Configuration configuration = options.get(i);
                System.out.printf("  %d. %-20s +%s PLN%n", i + 1, configuration.name(), formatPrice(configuration.price()));
            }

            if (allowsMultiple) {
                System.out.print("""
                        0. Skip
                        Choose option numbers separated by commas (or 0 to skip): """);
                String input = scanner.nextLine().trim();
                if (input.isEmpty() || input.equals("0")) {
                    continue;
                }
                for (String part : input.split(",")) {
                    selectByIndex(options, part.trim()).ifPresent(selected::add);
                }
            } else {
                selected.add(requireOneChoice(options));
            }
        }

        return selected;
    }

    private Configuration requireOneChoice(List<Configuration> options) {
        while (true) {
            System.out.print("Choose one option: ");
            Optional<Configuration> choice = selectByIndex(options, scanner.nextLine().trim());
            if (choice.isPresent()) {
                return choice.get();
            }
        }
    }

    private Optional<Configuration> selectByIndex(List<Configuration> options, String input) {
        try {
            int index = Integer.parseInt(input);
            if (index >= 1 && index <= options.size()) {
                return Optional.of(options.get(index - 1));
            }
        } catch (NumberFormatException ignored) {
            // fall through to the invalid-option message below
        }
        System.out.printf("Invalid option: %s, skipped.%n", input);
        return Optional.empty();
    }

    private int readQuantity() {
        System.out.print("Enter quantity: ");
        String input = scanner.nextLine().trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.printf("Invalid quantity: %s%n", input);
            return 0;
        }
    }

    private void updateCartItemQuantity() {
        if (cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }

        int index = readCartItemIndex();
        if (index < 0) {
            return;
        }
        int quantity = readQuantity();

        try {
            cart.updateQuantityAt(index, quantity);
            System.out.println("Quantity updated.");
        } catch (IndexOutOfBoundsException e) {
            System.out.println(e.getMessage());
        }
    }

    private void removeCartItem() {
        if (cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }

        int index = readCartItemIndex();
        if (index < 0) {
            return;
        }

        try {
            cart.removeAt(index);
            System.out.println("Removed from cart.");
        } catch (IndexOutOfBoundsException e) {
            System.out.println(e.getMessage());
        }
    }

    private int readCartItemIndex() {
        System.out.print("Enter cart item number: ");
        String input = scanner.nextLine().trim();
        try {
            return Integer.parseInt(input) - 1;
        } catch (NumberFormatException e) {
            System.out.printf("Invalid cart item number: %s%n", input);
            return -1;
        }
    }

    private void checkout() {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty, nothing to order.");
            return;
        }

        printCartContents();
        Customer customer = readCustomer();
        Order order = cart.toOrder(customer);

        try {
            orderProcessor.processOrder(order);
            cart.clear();
            System.out.printf("%nOrder placed successfully!%n%n%s%n", orderProcessor.generateInvoice(order));
        } catch (InsufficientStockException e) {
            System.out.printf("%nOrder could not be processed: %s%n", e.getMessage());
        }
    }

    private Customer readCustomer() {
        System.out.print("""

                Please provide your details for the order.
                Name: """);
        String name = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Address: ");
        String address = scanner.nextLine().trim();

        return new Customer(name, email, address);
    }

    private String formatPrice(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP).toString();
    }
}
