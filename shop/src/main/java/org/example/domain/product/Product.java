package org.example.domain.product;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public abstract class Product {
    protected final String id;
    protected String name;
    protected BigDecimal price;
    protected int availableQuantity;

    public Product(String id, String name, BigDecimal price, int availableQuantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.availableQuantity = availableQuantity;
    }
}
