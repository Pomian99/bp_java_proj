package org.example.service;

import org.example.model.Order;
import org.example.model.OrderItem;
import org.example.model.Configuration;
import org.example.model.Product;
import org.example.model.OrderRepository;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class OrderProcessor {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ProductManager productManager;
    private final OrderRepository orderRepository;

    public OrderProcessor(ProductManager productManager, OrderRepository orderRepository) {
        this.productManager = productManager;
        this.orderRepository = orderRepository;
        Order.seedIdCounterFrom(orderRepository.load());
    }

    public synchronized void processOrder(Order order) {
        productManager.reserveStockForOrder(order.items());
        orderRepository.append(order);
    }

    public String generateInvoice(Order order) {
        StringBuilder invoice = new StringBuilder();

        invoice.append("INVOICE\n");
        invoice.append("=======\n");
        invoice.append("Order ID: ").append(order.id()).append('\n');
        invoice.append("Date: ").append(order.orderDate().format(DATE_FORMAT)).append('\n');
        invoice.append('\n');

        invoice.append("Customer: ").append(order.customer().name()).append('\n');
        invoice.append("Email: ").append(order.customer().email()).append('\n');
        if (order.customer().address() != null && !order.customer().address().isBlank()) {
            invoice.append("Address: ").append(order.customer().address()).append('\n');
        }
        invoice.append('\n');

        invoice.append("Items:\n");
        invoice.append("------\n");
        for (OrderItem item : order.items()) {
            Product product = item.product();
            invoice.append(String.format("%-30s x%-3d %10s%n",
                    product.getName(), item.quantity(), formatPrice(item.getLineTotal())));

            for (Configuration configuration : item.selectedConfigurations()) {
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