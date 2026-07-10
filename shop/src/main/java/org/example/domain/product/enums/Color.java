package org.example.domain.product.enums;

import lombok.Getter;

@Getter
public enum Color {
    BLACK("Black"),
    WHITE("White"),
    BLUE("Blue"),
    SILVER("Silver");

    private final String displayName;

    Color(String displayName) {
        this.displayName = displayName;
    }
}
