package org.example.model.discount;

import java.math.BigDecimal;

public interface AdjustmentStrategy {

    BigDecimal apply(BigDecimal base);
}
