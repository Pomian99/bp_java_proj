package org.example.model;

public enum ProductType {
    ELECTRONICS("Electronics"),
    COMPUTER("Computer"),
    SMARTPHONE("Smartphone");

    private final String label;

    ProductType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
