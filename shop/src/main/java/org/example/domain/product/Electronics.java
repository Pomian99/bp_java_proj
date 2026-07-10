package org.example.domain.product;

import java.math.BigDecimal;

public class Electronics extends Product {
    public Electronics(String id, String name, BigDecimal price, int availableQuantity) {
        super(id, name, price, availableQuantity);
    }
}
