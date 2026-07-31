package org.example.generators;

import org.example.model.OrderItem;
import org.example.model.OrderItems;
import org.example.model.ProductType;
import org.example.model.discount.Discount;
import org.example.model.discount.FixedAdjustment;
import org.example.model.discount.PercentageAdjustment;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Static factory of sample Discount test data, analogous to ProductGenerator.
 */
public final class DiscountGenerator {

    private DiscountGenerator() {
    }

    public static List<Discount> generateDiscounts() {
        List<Discount> discounts = new ArrayList<>();
        Instant oneYearFromNow = Instant.now().plus(365, ChronoUnit.DAYS);

        discounts.add(new Discount(
                "10% off orders over 1000 PLN",
                items -> OrderItems.total(items).compareTo(new BigDecimal("1000")) >= 0,
                new PercentageAdjustment(new BigDecimal("10")),
                oneYearFromNow
        ));

        discounts.add(new Discount(
                "50 PLN off orders over 2000 PLN",
                items -> OrderItems.total(items).compareTo(new BigDecimal("2000")) >= 0,
                new FixedAdjustment(new BigDecimal("50")),
                oneYearFromNow
        ));

        discounts.add(new Discount(
                "15% off when buying at least 2 smartphones",
                items -> quantityOfType(items, ProductType.SMARTPHONE) >= 2,
                new PercentageAdjustment(new BigDecimal("15")),
                oneYearFromNow
        ));

        return discounts;
    }

    private static int quantityOfType(List<OrderItem> items, ProductType type) {
        return items.stream()
                .filter(item -> item.product().getType() == type)
                .mapToInt(OrderItem::quantity)
                .sum();
    }
}
