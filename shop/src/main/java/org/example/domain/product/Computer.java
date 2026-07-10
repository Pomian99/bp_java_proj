package org.example.domain.product;

import lombok.Getter;
import org.example.domain.product.enums.Cpu;
import org.example.domain.product.enums.RamSize;
import org.example.domain.product.enums.Storage;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class Computer extends Product implements Configurable {
    private final Cpu cpu;
    private final RamSize ram;
    private final Storage storage;

    public Computer(String id, String name, BigDecimal price, int availableQuantity,
                    Cpu cpu, RamSize ram, Storage storage) {
        super(id, name, price, availableQuantity);
        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
    }

    @Override
    public List<ConfigOption<?>> getConfigurationOptions() {
        return List.of(
                new ConfigOption<>("CPU", Cpu.values()),
                new ConfigOption<>("RAM", RamSize.values()),
                new ConfigOption<>("Hard Drive", Storage.values())
        );
    }

    @Override
    public String toString() {
        return String.format("%s [%s, %s, %s] - %.2f PLN, available: %d",
                name, cpu.getDisplayName(), ram.getDisplayName(), storage.getDisplayName(), price, availableQuantity);
    }
}
