package org.example.cli;

import org.example.model.Cart;
import org.example.model.Configuration;
import org.example.model.OrderItem;
import org.example.model.Product;
import org.example.model.enums.ConfigType;
import org.example.service.ProductManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

class CartMenu {
    private final ProductManager productManager;
    private final Cart cart;
    private final Scanner scanner;

    CartMenu(ProductManager productManager, Cart cart, Scanner scanner) {
        this.productManager = productManager;
        this.cart = cart;
        this.scanner = scanner;
    }

    void view() {
        while (true) {
            printCartContents();

            System.out.print("""

                    1. Add product to cart
                    2. Update item quantity
                    3. Remove item
                    0. Back to main menu
                    Choose an option:\s""");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> addProductToCart();
                case "2" -> updateCartItemQuantity();
                case "3" -> removeCartItem();
                case "0", "" -> {
                    return;
                }
                default -> System.out.println("Unknown option, please choose a number from the menu.");
            }
        }
    }

    void printCartContents() {
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
                    i + 1, item.product().getId(), item.product().getName(), item.quantity(),
                    CliUtils.formatPrice(item.getLineTotal()));
            for (Configuration configuration : item.selectedConfigurations()) {
                System.out.printf("    + %-20s +%s PLN%n", configuration.name(), CliUtils.formatPrice(configuration.price()));
            }
        }

        System.out.printf("%nTotal: %s PLN%n", CliUtils.formatPrice(cart.getTotal()));
    }

    private void addProductToCart() {
        System.out.print("Enter product id: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) {
            return;
        }

        Optional<Product> found = productManager.getProductById(id);
        if (found.isEmpty()) {
            System.out.printf("No product found with id: %s%n", id);
            return;
        }

        addProductToCart(found.get());
    }

    void addProductToCart(Product product) {
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
                System.out.printf("  %d. %-20s +%s PLN%n", i + 1, configuration.name(), CliUtils.formatPrice(configuration.price()));
            }

            if (allowsMultiple) {
                System.out.print("""
                        0. Skip
                        Choose option numbers separated by commas (or 0 to skip):\s""");
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
        if (input.isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.printf("Invalid quantity: %s%n", input);
            return -1;
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
        if (quantity < 0) {
            return;
        }

        try {
            cart.updateQuantityAt(index, quantity);
            System.out.println(quantity == 0 ? "Item removed from cart." : "Quantity updated.");
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Invalid cart item number.");
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
            System.out.println("Invalid cart item number.");
        }
    }

    private int readCartItemIndex() {
        System.out.print("Enter cart item number: ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(input) - 1;
        } catch (NumberFormatException e) {
            System.out.printf("Invalid cart item number: %s%n", input);
            return -1;
        }
    }
}
