package org.example.repository;

import org.example.model.Order;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileOrderRepository extends ListFileRepository<Order> implements OrderRepository {

    public FileOrderRepository(Path filePath) {
        super(filePath);
    }

    @Override
    public void append(Order order) {
        List<Order> orders = new ArrayList<>(load());
        orders.add(order);
        save(orders);
    }
}
