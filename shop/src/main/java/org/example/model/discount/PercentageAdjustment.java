package org.example.model.discount;

import java.math.BigDecimal;

public class PercentageAdjustment implements AdjustmentStrategy {

    private final BigDecimal percentage;

    public PercentageAdjustment(BigDecimal percentage) {
        this.percentage = percentage;
    }

    @Override
    public BigDecimal apply(BigDecimal base) {
        return base.multiply(percentage).divide(BigDecimal.valueOf(100));
    }
}
