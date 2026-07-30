package org.example.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A sellable item: id, type, name, price, available stock and the
 * configuration options (if any) that can be picked when ordering it.
 */
@Getter
@Setter
@ToString
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final AtomicLong ID_COUNTER = new AtomicLong(1);

    private final String id;
    private final ProductType type;
    private final String name;
    private final List<Configuration> availableConfiguration;
    private BigDecimal basePrice;
    private int availableQuantity;

    @Builder
    private Product(ProductType type, String name, @Singular("configuration") List<Configuration> availableConfiguration, BigDecimal basePrice, int availableQuantity) {
        this.id = generateId();
        this.type = type;
        this.name = name;
        this.availableConfiguration = availableConfiguration;
        this.basePrice = basePrice;
        this.availableQuantity = availableQuantity;
    }

    private static String generateId() {
        return String.format("PROD-%04d", ID_COUNTER.getAndIncrement());
    }

    public BigDecimal getPrice() {
        return basePrice;
    }

    public boolean matchId(String otherId) {
        return id.equals(otherId);
    }
}
