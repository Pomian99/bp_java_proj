package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.model.enums.ProductType;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Getter @Setter @AllArgsConstructor @ToString
public class Product {
    private static final AtomicLong ID_COUNTER = new AtomicLong(1);

    private final String id;
    private final ProductType type;
    private final String name;
    private final List<Configuration> availableConfiguration;
    private BigDecimal basePrice;
    private int availableQuantity;

    public Product(ProductType type, String name, List<Configuration> availableConfiguration, BigDecimal basePrice, int availableQuantity) {
        this(generateId(), type, name, availableConfiguration, basePrice, availableQuantity);
    }

    private static String generateId() {
        return String.format("PROD-%04d", ID_COUNTER.getAndIncrement());
    }

    public Product(String name, ProductType type, BigDecimal basePrice, int availableQuantity) {
        this(type, name, List.of(), basePrice, availableQuantity);
    }

    public BigDecimal getPrice() {
        return basePrice;
    }

    public boolean matchId(String otherId) {
        return id.equals(otherId);
    }
}
