package org.example.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A single line in an Order (or Cart): a product, the configurations chosen
 * for it, and the ordered quantity.
 */
public record OrderItem(Product product, List<Configuration> selectedConfigurations,
                        int quantity) implements Serializable {
    private static final long serialVersionUID = 1L;

    public OrderItem(Product product, List<Configuration> selectedConfigurations, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product must not be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        List<Configuration> configurations = selectedConfigurations == null ? List.of() : selectedConfigurations;
        validateConfigurations(product, configurations);

        this.product = product;
        this.selectedConfigurations = List.copyOf(configurations);
        this.quantity = quantity;
    }

    private static void validateConfigurations(Product product, List<Configuration> selected) {
        List<Configuration> available = product.getAvailableConfiguration();

        for (Configuration configuration : selected) {
            if (available == null || !available.contains(configuration)) {
                throw new IllegalArgumentException(
                        "Configuration '" + configuration.name() + "' is not available for product " + product.getId());
            }
        }

        Map<ConfigType, List<Configuration>> selectedByType =
                selected.stream().collect(Collectors.groupingBy(Configuration::type));

        for (Map.Entry<ConfigType, List<Configuration>> entry : selectedByType.entrySet()) {
            boolean allowsMultiple = entry.getValue().stream().anyMatch(Configuration::multipleChoice);
            if (!allowsMultiple && entry.getValue().size() > 1) {
                throw new IllegalArgumentException(
                        "Only one configuration of type " + entry.getKey() + " may be selected for product " + product.getId());
            }
        }
    }

    public BigDecimal getLineTotal() {
        BigDecimal unitPrice = product.getPrice()
                .add(selectedConfigurations.stream()
                        .map(Configuration::price)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public OrderItem withQuantity(int newQuantity) {
        return new OrderItem(product, selectedConfigurations, newQuantity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrderItem other)) {
            return false;
        }
        return product.getId().equals(other.product.getId())
                && new HashSet<>(selectedConfigurations).equals(new HashSet<>(other.selectedConfigurations));
    }

    @Override
    public int hashCode() {
        return Objects.hash(product.getId(), new HashSet<>(selectedConfigurations));
    }
}