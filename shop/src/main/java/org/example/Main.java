package org.example;

import org.example.exception.InsufficientStockException;
import org.example.generators.ProductGenerator;
import org.example.model.Cart;
import org.example.model.Configuration;
import org.example.model.Customer;
import org.example.model.Order;
import org.example.model.Product;
import org.example.model.enums.ConfigType;
import org.example.service.OrderProcessor;
import org.example.service.ProductManager;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        ProductManager manager = new ProductManager();
        ProductGenerator.generateProducts().forEach(manager::addProduct);

        System.out.println("Available products:");
        manager.getProducts().forEach(System.out::println);

        Product laptop = manager.getProductById("PROD-COMP-001").orElseThrow();
        Product headphones = manager.getProductById("PROD-ELEC-001").orElseThrow();

        List<Configuration> laptopConfig = laptop.getAvailableConfiguration().stream()
                .filter(configuration -> configuration.type() == ConfigType.RAM
                        && configuration.name().equals("16GB RAM"))
                .toList();

        Cart cart = new Cart();
        cart.addProduct(laptop, laptopConfig, 1);
        cart.addProduct(headphones, List.of(), 2);

        System.out.println("\nCart total: " + cart.getTotal() + " PLN");

        Customer customer = new Customer("Jan Kowalski", "jan.kowalski@example.com", "ul. Przykladowa 1, Warszawa");
        Order order = cart.checkout(customer);

        OrderProcessor orderProcessor = new OrderProcessor();
        try {
            orderProcessor.processOrder(order);
            System.out.println("\n" + orderProcessor.generateInvoice(order));
        } catch (InsufficientStockException e) {
            System.out.println("Order could not be processed: " + e.getMessage());
        }
    }
}
