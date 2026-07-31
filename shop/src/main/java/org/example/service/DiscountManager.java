package org.example.service;

import org.example.exception.NegativePriceException;
import org.example.model.OrderItem;
import org.example.model.OrderItems;
import org.example.model.discount.AppliedDiscount;
import org.example.model.discount.Discount;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Discounts are kept in memory only (not persisted alongside products).
 */
public class DiscountManager {
    private final List<Discount> discounts = new ArrayList<>();

    public synchronized void addDiscount(Discount discount) {
        Objects.requireNonNull(discount, "Discount cannot be null");
        discounts.add(discount);
    }

    public synchronized void removeDiscount(Discount discount) {
        discounts.remove(discount);
    }

    public synchronized List<Discount> getActiveDiscounts() {
        Instant now = Instant.now();
        return discounts.stream()
                .filter(discount -> discount.getExpiresAt().isAfter(now))
                .toList();
    }

    /**
     * Picks the best qualifying Discount for the given cart contents (lowest
     * resulting total). A candidate that would push the total below zero is
     * skipped with a console message instead of being applied.
     */
    public synchronized Optional<AppliedDiscount> getBestDiscount(List<OrderItem> items) {
        BigDecimal itemsTotal = OrderItems.total(items);

        AppliedDiscount best = null;
        BigDecimal bestResultingTotal = null;

        for (Discount discount : getActiveDiscounts()) {
            if (!discount.getCondition().test(items)) {
                continue;
            }

            BigDecimal amount = discount.reduce(itemsTotal);
            BigDecimal resultingTotal = itemsTotal.subtract(amount);

            try {
                requireNonNegative(resultingTotal, discount.getDescription());
            } catch (NegativePriceException e) {
                System.out.println("Skipped discount: " + e.getMessage());
                continue;
            }

            if (best == null || resultingTotal.compareTo(bestResultingTotal) < 0) {
                best = new AppliedDiscount(discount.getDescription(), amount);
                bestResultingTotal = resultingTotal;
            }
        }

        return Optional.ofNullable(best);
    }

    private void requireNonNegative(BigDecimal resultingTotal, String description) {
        if (resultingTotal.signum() < 0) {
            throw new NegativePriceException(
                    "Discount '" + description + "' would result in a negative price and was not applied.");
        }
    }
}
