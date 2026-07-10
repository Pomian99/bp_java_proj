package org.example.domain.product.enums;

import lombok.Getter;

@Getter
public enum Storage {
    SSD_256_GB("256 GB SSD"),
    SSD_512_GB("512 GB SSD"),
    SSD_1_TB("1 TB SSD"),
    SSD_2_TB("2 TB SSD");

    private final String displayName;

    Storage(String displayName) {
        this.displayName = displayName;
    }
}
