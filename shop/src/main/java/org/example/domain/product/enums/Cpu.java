package org.example.domain.product.enums;

import lombok.Getter;

@Getter
public enum Cpu {
    I5("Intel Core i5"),
    I7("Intel Core i7"),
    I9("Intel Core i9");

    private final String displayName;

    Cpu(String displayName) {
        this.displayName = displayName;
    }
}
