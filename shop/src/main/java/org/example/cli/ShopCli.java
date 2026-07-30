package org.example.cli;

import org.example.model.Cart;
import org.example.service.OrderProcessor;
import org.example.service.ProductManager;

import java.util.Scanner;

public class ShopCli {
    private final Scanner scanner;
    private final ProductBrowser productBrowser;
    private final CartMenu cartMenu;
    private final CheckoutFlow checkoutFlow;

    public ShopCli(ProductManager productManager, OrderProcessor orderProcessor, Scanner scanner) {
        this.scanner = scanner;

        Cart cart = new Cart();
        this.cartMenu = new CartMenu(productManager, cart, scanner);
        this.productBrowser = new ProductBrowser(productManager, cartMenu, scanner);
        this.checkoutFlow = new CheckoutFlow(cart, orderProcessor, cartMenu, scanner);
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
                case "3" -> checkoutFlow.checkout();
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
}
