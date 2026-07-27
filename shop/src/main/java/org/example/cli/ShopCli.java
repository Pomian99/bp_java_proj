package org.example.cli;

import org.example.exception.InsufficientStockException;
import org.example.model.Cart;
import org.example.model.Configuration;
import org.example.model.Customer;
import org.example.model.Order;
import org.example.model.OrderItem;
import org.example.model.Product;
import org.example.model.enums.ConfigType;
import org.example.model.enums.ProductType;
import org.example.service.OrderProcessor;
import org.example.service.ProductManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ShopCli {
    private enum SortKey {
        NAME("Name"), PRICE("Price"), TYPE("Type");

        private final String label;

        SortKey(String label) {
            this.label = label;
        }
    }

    private final ProductManager productManager;
    private final OrderProcessor orderProcessor;
    private final Cart cart = new Cart();
    private final Scanner scanner;

    private SortKey sortKey;
    private boolean sortAscending = true;
    private ProductType filterType;
    private boolean filterInStockOnly;
    private BigDecimal filterMinPrice;
    private BigDecimal filterMaxPrice;

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
                case "0", "" -> running = false;
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
                0. Exit
                Choose an option:\s""");
    }

    private void browseProducts() {
        List<Product> products = productManager.getProducts();
        if (products.isEmpty()) {
            System.out.println("No products available.");
            return;
        }

        while (true) {
            List<Product> view = buildProductView(products);

            System.out.print("""

                    === Products ===
                    """);
            printSortAndFilterStatus();

            if (view.isEmpty()) {
                System.out.println("No products match the current filter.");
            } else {
                for (int i = 0; i < view.size(); i++) {
                    Product product = view.get(i);
                    System.out.printf("%d. %-14s %-11s %-25s %10s PLN  (stock: %d)%n",
                            i + 1, product.getId(), product.getType(), product.getName(),
                            formatPrice(product.getPrice()), product.getAvailableQuantity());
                }
            }

            System.out.println();
            System.out.print("Enter product number to view details, 's' to sort, 'f' to filter, or 0 to go back: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("s")) {
                openSortMenu();
                continue;
            }
            if (input.equalsIgnoreCase("f")) {
                openFilterMenu();
                continue;
            }
            if (input.isEmpty()) {
                return;
            }

            try {
                int number = Integer.parseInt(input);
                if (number == 0) {
                    return;
                }
                if (number >= 1 && number <= view.size()) {
                    showProductDetails(view.get(number - 1));
                    continue;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the invalid-input message below
            }
            System.out.printf("Invalid input: %s%n", input);
        }
    }

    private List<Product> buildProductView(List<Product> products) {
        List<Product> view = new ArrayList<>(products);

        view.removeIf(product -> filterType != null && product.getType() != filterType);
        view.removeIf(product -> filterInStockOnly && product.getAvailableQuantity() <= 0);
        view.removeIf(product -> filterMinPrice != null && product.getPrice().compareTo(filterMinPrice) < 0);
        view.removeIf(product -> filterMaxPrice != null && product.getPrice().compareTo(filterMaxPrice) > 0);

        if (sortKey != null) {
            Comparator<Product> comparator = switch (sortKey) {
                case NAME -> Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
                case PRICE -> Comparator.comparing(Product::getPrice);
                case TYPE -> Comparator.comparing(Product::getType);
            };
            view.sort(sortAscending ? comparator : comparator.reversed());
        }

        return view;
    }

    private void printSortAndFilterStatus() {
        List<String> parts = new ArrayList<>();

        if (sortKey != null) {
            parts.add("Sorted by %s (%s)".formatted(sortKey.label, sortAscending ? "ascending" : "descending"));
        }
        if (filterType != null) {
            parts.add("Type = " + filterType);
        }
        if (filterInStockOnly) {
            parts.add("In stock only");
        }
        if (filterMinPrice != null || filterMaxPrice != null) {
            parts.add("Price %s - %s PLN".formatted(
                    filterMinPrice != null ? formatPrice(filterMinPrice) : "...",
                    filterMaxPrice != null ? formatPrice(filterMaxPrice) : "..."));
        }

        if (!parts.isEmpty()) {
            System.out.println(String.join(" | ", parts));
        }
    }

    private void openSortMenu() {
        while (true) {
            System.out.print("""

                    === Sort products ===
                    1. Name
                    2. Price
                    3. Type
                    4. Clear sorting
                    0. Cancel
                    Choose an option:\s""");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    applySort(SortKey.NAME);
                    return;
                }
                case "2" -> {
                    applySort(SortKey.PRICE);
                    return;
                }
                case "3" -> {
                    applySort(SortKey.TYPE);
                    return;
                }
                case "4" -> {
                    sortKey = null;
                    return;
                }
                case "0", "" -> {
                    return;
                }
                default -> System.out.println("Unknown option, please choose a number from the menu.");
            }
        }
    }

    private void applySort(SortKey key) {
        sortKey = key;
        sortAscending = askYesNo("Sort ascending?");
    }

    private void openFilterMenu() {
        while (true) {
            System.out.print("""

                    === Filter products ===
                    1. By type
                    2. Toggle in-stock only
                    3. Set price range
                    4. Clear all filters
                    0. Back to product list
                    Choose an option:\s""");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> chooseTypeFilter();
                case "2" -> {
                    filterInStockOnly = !filterInStockOnly;
                    System.out.printf("In-stock only filter is now %s.%n", filterInStockOnly ? "ON" : "OFF");
                }
                case "3" -> choosePriceRangeFilter();
                case "4" -> {
                    clearFilters();
                    System.out.println("All filters cleared.");
                }
                case "0", "" -> {
                    return;
                }
                default -> System.out.println("Unknown option, please choose a number from the menu.");
            }
        }
    }

    private void chooseTypeFilter() {
        ProductType[] types = ProductType.values();
        int clearOption = types.length + 1;

        while (true) {
            System.out.println();
            System.out.println("=== Filter by type ===");
            for (int i = 0; i < types.length; i++) {
                System.out.printf("%d. %s%n", i + 1, types[i]);
            }
            System.out.printf("%d. Clear type filter%n", clearOption);
            System.out.println("0. Cancel");
            System.out.print("Choose an option: ");

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return;
            }
            try {
                int choice = Integer.parseInt(input);
                if (choice == 0) {
                    return;
                }
                if (choice == clearOption) {
                    filterType = null;
                    return;
                }
                if (choice >= 1 && choice <= types.length) {
                    filterType = types[choice - 1];
                    return;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the invalid-option message below
            }
            System.out.println("Unknown option, please choose a number from the menu.");
        }
    }

    private void choosePriceRangeFilter() {
        System.out.print("Enter minimum price (or press Enter for no minimum): ");
        filterMinPrice = readOptionalPrice();

        System.out.print("Enter maximum price (or press Enter for no maximum): ");
        filterMaxPrice = readOptionalPrice();
    }

    private BigDecimal readOptionalPrice() {
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(input);
        } catch (NumberFormatException e) {
            System.out.printf("Invalid price: %s, ignored.%n", input);
            return null;
        }
    }

    private void clearFilters() {
        filterType = null;
        filterInStockOnly = false;
        filterMinPrice = null;
        filterMaxPrice = null;
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
                    0. Back to product list
                    Choose an option:\s""");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    addProductToCart(product);
                    return;
                }
                case "0", "" -> {
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
            System.out.printf("%nOrder placed successfully! Order id: %s%n", order.id());

            if (askYesNo("Generate invoice?")) {
                System.out.printf("%n%s%n", orderProcessor.generateInvoice(order));
            }
        } catch (InsufficientStockException e) {
            System.out.printf("%nOrder could not be processed: %s%n", e.getMessage());
        }
    }

    private boolean askYesNo(String question) {
        while (true) {
            System.out.printf("%s (y/n): ", question);
            String input = scanner.nextLine().trim().toLowerCase();
            switch (input) {
                case "y", "yes" -> {
                    return true;
                }
                case "n", "no" -> {
                    return false;
                }
                default -> System.out.println("Please answer y or n.");
            }
        }
    }

    private Customer readCustomer() {
        System.out.print("""

                Please provide your details for the order.
                Name:\s""");
        String name = readNonBlank();

        System.out.print("Email: ");
        String email = readNonBlank();

        System.out.print("Address: ");
        String address = scanner.nextLine().trim();

        return new Customer(name, email, address);
    }

    private String readNonBlank() {
        String input = scanner.nextLine().trim();
        while (input.isEmpty()) {
            System.out.print("This field cannot be empty, please enter a value: ");
            input = scanner.nextLine().trim();
        }
        return input;
    }

    private String formatPrice(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP).toString();
    }
}
