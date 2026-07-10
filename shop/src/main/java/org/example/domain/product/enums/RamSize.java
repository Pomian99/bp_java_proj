package org.example.domain.product.enums;

import lombok.Getter;

@Getter
public enum RamSize {
    GB_8("8 GB"),
    GB_16("16 GB"),
    GB_32("32 GB"),
    GB_64("64 GB");

    private final String displayName;

    RamSize(String displayName) {
        this.displayName = displayName;
    }
}
