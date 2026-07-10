package org.example.domain.product;

import lombok.Getter;
import org.example.domain.product.enums.Accessory;
import org.example.domain.product.enums.BatteryCapacity;
import org.example.domain.product.enums.Color;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Getter
public class Smartphone extends Product implements Configurable {
    private final Color color;
    private final BatteryCapacity battery;
    private final Set<Accessory> accessories;

    public Smartphone(String id, String name, BigDecimal price, int availableQuantity,
                      Color color, BatteryCapacity battery, Set<Accessory> accessories) {
        super(id, name, price, availableQuantity);
        this.color = color;
        this.battery = battery;
        this.accessories = Set.copyOf(accessories);
    }

    @Override
    public List<ConfigOption<?>> getConfigurationOptions() {
        return List.of(
                new ConfigOption<>("Colour", Color.values()),
                new ConfigOption<>("Battery", BatteryCapacity.values()),
                new ConfigOption<>("Accessories", Accessory.values(), true)
        );
    }

    @Override
    public String toString() {
        return String.format("%s [%s, %s, %s] - %.2f PLN, available: %d",
                name, color.getDisplayName(), battery.getDisplayName(), accessories, price, availableQuantity);
    }
}
