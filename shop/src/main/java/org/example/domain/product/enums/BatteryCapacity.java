package org.example.domain.product.enums;

import lombok.Getter;

@Getter
public enum BatteryCapacity {
    MAH_4000("4000 mAh"),
    MAH_4500("4500 mAh"),
    MAH_5000("5000 mAh");

    private final String displayName;

    BatteryCapacity(String displayName) {
        this.displayName = displayName;
    }
}
