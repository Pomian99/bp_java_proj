package org.example.model.discount;

import java.math.BigDecimal;

public class FixedAdjustment implements AdjustmentStrategy {

    private final BigDecimal amount;

    public FixedAdjustment(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public BigDecimal apply(BigDecimal base) {
        return amount;
    }
}
