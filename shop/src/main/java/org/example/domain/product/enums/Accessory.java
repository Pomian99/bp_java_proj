package org.example.domain.product.enums;

import lombok.Getter;

@Getter
public enum Accessory {
    CHARGER("Charger"),
    SCREEN_PROTECTOR("Screen protector"),
    CASE("Case"),
    TEMPERED_GLASS("Tempered glass");

    private final String displayName;

    Accessory(String displayName) {
        this.displayName = displayName;
    }
}
