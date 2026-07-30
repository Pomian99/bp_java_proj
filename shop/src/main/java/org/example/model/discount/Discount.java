package org.example.model.discount;

import lombok.Getter;
import org.example.model.OrderItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;

/**
 * A cart-wide price reduction. Never persisted - it only lives in
 * ProductManager's in-memory list - so condition is a plain Predicate rather
 * than something modelled as serializable data.
 */
@Getter
public class Discount {
    private final String description;
    private final Predicate<List<OrderItem>> condition;
    private final AdjustmentType type;
    private final BigDecimal value;
    private final Instant expiresAt;

    public Discount(String description, Predicate<List<OrderItem>> condition, AdjustmentType type,
                     BigDecimal value, Instant expiresAt) {
        this.description = description;
        this.condition = condition;
        this.type = type;
        this.value = value;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public BigDecimal reduce(BigDecimal base) {
        return type == AdjustmentType.PERCENTAGE
                ? base.multiply(value).divide(BigDecimal.valueOf(100))
                : value;
    }
}
