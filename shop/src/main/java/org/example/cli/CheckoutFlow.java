package org.example.cli;

import org.example.exception.InsufficientStockException;
import org.example.model.Cart;
import org.example.model.Customer;
import org.example.model.Order;
import org.example.model.OrderItem;
import org.example.model.discount.AppliedDiscount;
import org.example.service.OrderProcessor;
import org.example.service.ProductManager;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletionException;

class CheckoutFlow {
    private final Cart cart;
    private final ProductManager productManager;
    private final OrderProcessor orderProcessor;
    private final Scanner scanner;

    CheckoutFlow(Cart cart, ProductManager productManager, OrderProcessor orderProcessor, Scanner scanner) {
        this.cart = cart;
        this.productManager = productManager;
        this.orderProcessor = orderProcessor;
        this.scanner = scanner;
    }

    void checkout() {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty, nothing to order.");
            return;
        }

        Customer customer = readCustomer();

        List<OrderItem> items = cart.viewCart();
        AppliedDiscount appliedDiscount = productManager.getBestDiscount(items).orElse(null);
        Order order = new Order(customer, items, appliedDiscount);

        try {
            orderProcessor.processOrderAsync(order).join();
            cart.clear();
            System.out.printf("%nOrder placed successfully! Order id: %s%n", order.getId());

            if (CliUtils.askYesNo(scanner, "Generate invoice?")) {
                System.out.printf("%n%s%n", orderProcessor.generateInvoice(order));
            }
        } catch (CompletionException e) {
            if (e.getCause() instanceof InsufficientStockException cause) {
                System.out.printf("%nOrder could not be processed: %s%n", cause.getMessage());
            } else {
                throw e;
            }
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
