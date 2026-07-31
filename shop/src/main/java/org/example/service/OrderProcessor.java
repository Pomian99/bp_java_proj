package org.example.service;

import org.example.model.Order;
import org.example.model.OrderItem;
import org.example.model.Configuration;
import org.example.model.Product;
import org.example.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class OrderProcessor {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final ZoneId DISPLAY_ZONE = ZoneId.systemDefault();

    private final ProductManager productManager;
    private final OrderRepository orderRepository;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public OrderProcessor(ProductManager productManager, OrderRepository orderRepository) {
        this.productManager = productManager;
        this.orderRepository = orderRepository;
        Order.seedIdCounterFrom(orderRepository.load());
    }

    public synchronized void processOrder(Order order) {
        productManager.reserveStockForOrder(order.getItems());
        orderRepository.append(order);
    }

    public CompletableFuture<Order> processOrderAsync(Order order) {
        return CompletableFuture.supplyAsync(() -> {
            processOrder(order);
            return order;
        }, executor);
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public String generateInvoice(Order order) {
        StringBuilder invoice = new StringBuilder();

        invoice.append("INVOICE\n");
        invoice.append("=======\n");
        invoice.append("Order ID: ").append(order.getId()).append('\n');
        invoice.append("Date: ").append(DATE_FORMAT.format(order.getOrderDate().atZone(DISPLAY_ZONE))).append('\n');
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
            Product product = item.product();
            invoice.append(String.format("%-30s x%-3d %10s%n",
                    product.getName(), item.quantity(), formatPrice(item.getLineTotal())));

            for (Configuration configuration : item.selectedConfigurations()) {
                invoice.append(String.format("    + %-26s %10s%n",
                        configuration.name(), formatPrice(configuration.price())));
            }
        }

        order.getAppliedDiscount().ifPresent(discount -> invoice.append(String.format("%n%-30s -%14s%n",
                "Discount: " + discount.description(), formatPrice(discount.amount()))));

        invoice.append('\n');
        invoice.append(String.format("%-30s %14s%n", "TOTAL", formatPrice(order.getTotal())));

        return invoice.toString();
    }

    private static String formatPrice(BigDecimal price) {
        return price.setScale(2, java.math.RoundingMode.HALF_UP) + " PLN";
    }
}