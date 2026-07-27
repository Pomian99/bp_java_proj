package org.example.cli;

import org.example.exception.InsufficientStockException;
import org.example.model.Cart;
import org.example.model.Customer;
import org.example.model.Order;
import org.example.service.OrderProcessor;

import java.util.Scanner;

class CheckoutFlow {
    private final Cart cart;
    private final OrderProcessor orderProcessor;
    private final CartMenu cartMenu;
    private final Scanner scanner;

    CheckoutFlow(Cart cart, OrderProcessor orderProcessor, CartMenu cartMenu, Scanner scanner) {
        this.cart = cart;
        this.orderProcessor = orderProcessor;
        this.cartMenu = cartMenu;
        this.scanner = scanner;
    }

    void checkout() {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty, nothing to order.");
            return;
        }

        cartMenu.printCartContents();
        Customer customer = readCustomer();
        Order order = cart.toOrder(customer);

        try {
            orderProcessor.processOrder(order);
            cart.clear();
            System.out.printf("%nOrder placed successfully! Order id: %s%n", order.id());

            if (CliUtils.askYesNo(scanner, "Generate invoice?")) {
                System.out.printf("%n%s%n", orderProcessor.generateInvoice(order));
            }
        } catch (InsufficientStockException e) {
            System.out.printf("%nOrder could not be processed: %s%n", e.getMessage());
        }
    }

    private Customer readCustomer() {
        System.out.print("""

                Please provide your details for the order.
                Name:\s""");
        String name = CliUtils.readNonBlank(scanner);

        System.out.print("Email: ");
        String email = CliUtils.readNonBlank(scanner);

        System.out.print("Address: ");
        String address = scanner.nextLine().trim();

        return new Customer(name, email, address);
    }
}
