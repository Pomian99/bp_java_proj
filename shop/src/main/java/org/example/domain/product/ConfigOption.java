package org.example.domain.product;

public record ConfigOption<T extends Enum<T>>(
        String categoryName,
        T[] options,
        boolean isMultiChoice
) {
    public ConfigOption(String categoryName, T[] options) {
        this(categoryName, options, false);
    }
}
