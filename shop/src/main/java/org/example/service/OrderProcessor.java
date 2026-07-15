package org.example.service;

import org.example.exception.InsufficientStockException;
import org.example.model.Order;
import org.example.model.OrderItem;
import org.example.model.Configuration;
import org.example.model.Product;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class OrderProcessor {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void processOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product.getAvailableQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Not enough stock for product '" + product.getName() + "' (id=" + product.getId() + "): "
                                + "requested " + item.getQuantity() + ", available " + product.getAvailableQuantity());
            }
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setAvailableQuantity(product.getAvailableQuantity() - item.getQuantity());
        }
    }

    public String generateInvoice(Order order) {
        StringBuilder invoice = new StringBuilder();

        invoice.append("INVOICE\n");
        invoice.append("=======\n");
        invoice.append("Order ID: ").append(order.getId()).append('\n');
        invoice.append("Date: ").append(order.getOrderDate().format(DATE_FORMAT)).append('\n');
        invoice.append('\n');

        invoice.append("Customer: ").append(order.getCustomer().name()).append('\n');
        invoice.append("Email: ").append(order.getCustomer().email()).append('\n');
        if (order.getCustomer().address() != null && !order.getCustomer().address().isBlank()) {
            invoice.append("Address: ").append(order.getCustomer().address()).append('\n');
        }
        invoice.append('\n');

        invoice.append("Items:\n");
        invoice.append("------\n");
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            invoice.append(String.format("%-30s x%-3d %10s%n",
                    product.getName(), item.getQuantity(), formatPrice(item.getLineTotal())));

            for (Configuration configuration : item.getSelectedConfigurations()) {
                invoice.append(String.format("    + %-26s %10s%n",
                        configuration.name(), formatPrice(configuration.price())));
            }
        }

        invoice.append('\n');
        invoice.append(String.format("%-30s %14s%n", "TOTAL", formatPrice(order.getTotal())));

        return invoice.toString();
    }

    private static String formatPrice(BigDecimal price) {
        return price.setScale(2, java.math.RoundingMode.HALF_UP) + " PLN";
    }
}