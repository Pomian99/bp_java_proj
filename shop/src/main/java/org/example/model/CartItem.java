package org.example.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class CartItem {
    private final Product product;
    private final List<Configuration> productConfiguration;
    private int quantity;

    public CartItem(Product product, List<Configuration> productConfiguration, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        this.product = product;
        this.productConfiguration = productConfiguration;
        this.quantity = quantity;
    }

    public void increaseQuantity(int quantityToAdd) {
        if (quantityToAdd <= 0) {
            throw new IllegalArgumentException("Quantity to add must be greater than zero");
        }

        quantity += quantityToAdd;
    }

    public void changeQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public String toString() {
        return String.format("%s x%d = %.2f PLN", product.getName(), quantity, getSubtotal());
    }
}
