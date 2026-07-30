package org.example.repository;

import org.example.model.Order;

import java.util.List;

public interface OrderRepository {

    List<Order> load();

    void save(List<Order> items);

    void append(Order order);
}
