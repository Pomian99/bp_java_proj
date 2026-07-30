package org.example.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A single configurable option for a Product (e.g. "16GB RAM"). multipleChoice
 * controls whether more than one Configuration of the same type may be
 * selected on a single OrderItem.
 */
public record Configuration(String name, ConfigType type, BigDecimal price,
                            boolean multipleChoice) implements Serializable {
    public Configuration(String name, ConfigType type, BigDecimal price) {
        this(name, type, price, false);
    }
}
