package org.example.persistence;

import org.example.model.Order;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository extends ListFileRepository<Order> {

    public OrderRepository(Path filePath) {
        super(filePath);
    }

    public void append(Order order) {
        List<Order> orders = new ArrayList<>(load());
        orders.add(order);
        save(orders);
    }
}
