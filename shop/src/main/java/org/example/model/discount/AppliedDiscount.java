package org.example.model.discount;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * The result of a Discount actually applied to an order - kept separately
 * from Discount (which isn't Serializable) so it can travel inside Order
 * and survive being written to orders.dat.
 */
public record AppliedDiscount(String description, BigDecimal amount) implements Serializable {
}
