package org.example.model;

import org.example.model.enums.ConfigType;

import java.math.BigDecimal;

public record Configuration(String name, ConfigType type, BigDecimal price, boolean multipleChoice) {
    public Configuration(String name, ConfigType type, BigDecimal price) {
        this(name, type, price, false);
    }
}
