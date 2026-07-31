package org.example.cli;

import org.example.model.Cart;
import org.example.model.discount.Discount;
import org.example.service.DiscountManager;
import org.example.service.OrderProcessor;
import org.example.service.ProductManager;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class ShopCli {
    private static final DateTimeFormatter EXPIRY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Scanner scanner;
    private final DiscountManager discountManager;
    private final ProductBrowser productBrowser;
    private final CartMenu cartMenu;

    public ShopCli(ProductManager productManager, DiscountManager discountManager, OrderProcessor orderProcessor, Scanner scanner) {
        this.scanner = scanner;
        this.discountManager = discountManager;

        Cart cart = new Cart();
        CheckoutFlow checkoutFlow = new CheckoutFlow(cart, discountManager, orderProcessor, scanner);
        this.cartMenu = new CartMenu(productManager, discountManager, cart, scanner, checkoutFlow);
        this.productBrowser = new ProductBrowser(productManager, cartMenu, scanner);
    }

    public void run() {
        System.out.println("Welcome to the online shop!");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> productBrowser.browse();
                case "2" -> cartMenu.view();
                case "3" -> viewDiscounts();
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
                3. View current discounts
                0. Exit
                Choose an option:\s""");
    }

    private void viewDiscounts() {
        List<Discount> discounts = discountManager.getActiveDiscounts();

        System.out.print("""

                === Current discounts ===
                """);

        if (discounts.isEmpty()) {
            System.out.println("No discounts are currently active.");
            return;
        }

        for (Discount discount : discounts) {
            System.out.printf("- %s (valid until %s)%n",
                    discount.getDescription(),
                    EXPIRY_FORMAT.format(discount.getExpiresAt().atZone(ZoneId.systemDefault())));
        }
    }
}
