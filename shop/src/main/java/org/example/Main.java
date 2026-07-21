package org.example;

import org.example.exception.InsufficientStockException;
import org.example.generators.ProductGenerator;
import org.example.model.Cart;
import org.example.model.Configuration;
import org.example.model.Customer;
import org.example.model.Order;
import org.example.model.OrderItem;
import org.example.model.Product;
import org.example.model.enums.ConfigType;
import org.example.model.enums.ProductType;
import org.example.model.OrderRepository;
import org.example.model.ProductRepository;
import org.example.service.OrderProcessor;
import org.example.service.ProductManager;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws Exception {
        Path dataDir = Path.of("shop-data");
        ProductRepository productRepository = new ProductRepository(dataDir.resolve("products.dat"));
        OrderRepository orderRepository = new OrderRepository(dataDir.resolve("orders.dat"));

        ProductManager manager = new ProductManager(productRepository);
        if (manager.getProducts().isEmpty()) {
            System.out.println("No persisted products found, generating sample catalog.");
            ProductGenerator.generateProducts().forEach(manager::addProduct);
        } else {
            System.out.println("Loaded persisted products from " + dataDir.resolve("products.dat"));
        }

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

        OrderProcessor orderProcessor = new OrderProcessor(manager, orderRepository);
        try {
            orderProcessor.processOrder(order);
            System.out.println("\n" + orderProcessor.generateInvoice(order));
        } catch (InsufficientStockException e) {
            System.out.println("Order could not be processed: " + e.getMessage());
        }

        runConcurrencyDemo();
    }

    private static void runConcurrencyDemo() throws Exception {
        System.out.println("\n=== Concurrency demo: concurrent orders + admin restock ===");

        Path demoDir = Files.createTempDirectory("shop-concurrency-demo");
        ProductManager demoManager = new ProductManager(new ProductRepository(demoDir.resolve("products.dat")));
        OrderRepository demoOrderRepository = new OrderRepository(demoDir.resolve("orders.dat"));
        OrderProcessor demoProcessor = new OrderProcessor(demoManager, demoOrderRepository);

        int initialStock = 100;
        Product demoProduct = new Product(ProductType.ELECTRONICS, "Demo Widget", List.of(), BigDecimal.TEN, initialStock);
        demoManager.addProduct(demoProduct);

        int orderThreads = 40;
        int adminThreads = 5;
        int restockPerAdmin = 10;
        int totalThreads = orderThreads + adminThreads;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalThreads);
        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        AtomicInteger failedOrders = new AtomicInteger();

        for (int i = 0; i < orderThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    Customer customer = new Customer("Demo Customer", "demo@example.com", null);
                    Order order = new Order(customer, List.of(new OrderItem(demoProduct, List.of(), 1)));
                    demoProcessor.processOrder(order);
                } catch (InsufficientStockException e) {
                    failedOrders.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        for (int i = 0; i < adminThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    demoManager.adjustStock(demoProduct.getId(), restockPerAdmin);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        System.out.println("Starting " + orderThreads + " order threads and " + adminThreads
                + " admin restock threads at the same time...");
        startLatch.countDown();
        boolean finished = doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        if (!finished) {
            System.out.println("Demo threads did not finish in time.");
            return;
        }

        int successfulOrders = orderThreads - failedOrders.get();
        int expectedStock = initialStock - successfulOrders + adminThreads * restockPerAdmin;
        int actualStock = demoManager.getProductById(demoProduct.getId()).orElseThrow().getAvailableQuantity();
        int persistedOrders = demoOrderRepository.load().size();

        System.out.println("Initial stock:       " + initialStock);
        System.out.println("Orders placed:       " + orderThreads + " (failed: " + failedOrders.get() + ")");
        System.out.println("Admin restocks:      " + adminThreads + " x +" + restockPerAdmin);
        System.out.println("Expected final stock:" + expectedStock);
        System.out.println("Actual final stock:  " + actualStock);
        System.out.println("Orders persisted:    " + persistedOrders + " (expected " + successfulOrders + ")");
        System.out.println(expectedStock == actualStock && persistedOrders == successfulOrders
                ? "OK: no lost updates."
                : "MISMATCH: race condition detected!");
    }
}
