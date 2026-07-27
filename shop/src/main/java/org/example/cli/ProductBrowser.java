package org.example.cli;

import org.example.model.Configuration;
import org.example.model.Product;
import org.example.model.enums.ConfigType;
import org.example.model.enums.ProductType;
import org.example.service.ProductManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

class ProductBrowser {
    private enum SortKey {
        NAME("Name"), PRICE("Price"), TYPE("Type");

        private final String label;

        SortKey(String label) {
            this.label = label;
        }
    }

    private final ProductManager productManager;
    private final CartMenu cartMenu;
    private final Scanner scanner;

    private SortKey sortKey;
    private boolean sortAscending = true;
    private ProductType filterType;
    private boolean filterInStockOnly;
    private BigDecimal filterMinPrice;
    private BigDecimal filterMaxPrice;

    ProductBrowser(ProductManager productManager, CartMenu cartMenu, Scanner scanner) {
        this.productManager = productManager;
        this.cartMenu = cartMenu;
        this.scanner = scanner;
    }

    void browse() {
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
                            CliUtils.formatPrice(product.getPrice()), product.getAvailableQuantity());
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
                    filterMinPrice != null ? CliUtils.formatPrice(filterMinPrice) : "...",
                    filterMaxPrice != null ? CliUtils.formatPrice(filterMaxPrice) : "..."));
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
        sortAscending = CliUtils.askYesNo(scanner, "Sort ascending?");
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
                CliUtils.formatPrice(product.getPrice()), product.getAvailableQuantity());

        List<Configuration> configurations = product.getAvailableConfiguration();
        if (configurations != null && !configurations.isEmpty()) {
            System.out.println("Available configurations:");
            Map<ConfigType, List<Configuration>> byType = configurations.stream()
                    .collect(Collectors.groupingBy(Configuration::type, LinkedHashMap::new, Collectors.toList()));
            for (Map.Entry<ConfigType, List<Configuration>> entry : byType.entrySet()) {
                System.out.printf("  %s:%n", entry.getKey());
                for (Configuration configuration : entry.getValue()) {
                    System.out.printf("    - %-20s +%s PLN%n", configuration.name(), CliUtils.formatPrice(configuration.price()));
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
                    cartMenu.addProductToCart(product);
                    return;
                }
                case "0", "" -> {
                    return;
                }
                default -> System.out.println("Unknown option, please choose a number from the menu.");
            }
        }
    }
}
