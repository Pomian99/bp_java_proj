package org.example.generators;

import org.example.model.Configuration;
import org.example.model.Product;
import org.example.model.enums.ConfigType;
import org.example.model.enums.ProductType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Static factory of sample Product test data.
 * <p>
 * Notes on modelling choices:
 * - Configuration.multiChoice() = false  -> exactly ONE option of that ConfigType
 * may be selected at order time (e.g. you can't have two RAM sizes at once).
 * - Configuration.multiChoice() = true   -> MULTIPLE options of that ConfigType
 * may be selected at order time (e.g. several accessories).
 * - availableConfiguration may contain several Configuration entries that share
 * the same ConfigType (these are the choices offered for that type); the
 * multiChoice flag on each of them indicates how many of that type may
 * ultimately be chosen.
 * - Electronics products have no configuration options at all (per spec), so
 * they always get an empty availableConfiguration list.
 */
public final class ProductGenerator {

    private ProductGenerator() {
    }

    /**
     * Generates the master pool of parts (Configuration options), grouped by ConfigType.
     * generateProducts() pulls from this map to assemble each product's availableConfiguration.
     */
    public static Map<ConfigType, List<Configuration>> generateParts() {
        Map<ConfigType, List<Configuration>> parts = new EnumMap<>(ConfigType.class);

        parts.put(ConfigType.RAM, List.of(
                new Configuration("8GB RAM", ConfigType.RAM, BigDecimal.ZERO),
                new Configuration("16GB RAM", ConfigType.RAM, new BigDecimal("150.00")),
                new Configuration("32GB RAM", ConfigType.RAM, new BigDecimal("350.00"))
        ));

        parts.put(ConfigType.HARD_DRIVE, List.of(
                new Configuration("256GB SSD", ConfigType.HARD_DRIVE, BigDecimal.ZERO),
                new Configuration("512GB SSD", ConfigType.HARD_DRIVE, new BigDecimal("120.00")),
                new Configuration("1TB SSD", ConfigType.HARD_DRIVE, new BigDecimal("250.00"))
        ));

        parts.put(ConfigType.COLOR, List.of(
                new Configuration("Space Grey", ConfigType.COLOR, BigDecimal.ZERO),
                new Configuration("Silver", ConfigType.COLOR, BigDecimal.ZERO),
                new Configuration("Midnight Black", ConfigType.COLOR, BigDecimal.ZERO),
                new Configuration("Ocean Blue", ConfigType.COLOR, BigDecimal.ZERO),
                new Configuration("Rose Gold", ConfigType.COLOR, BigDecimal.ZERO)
        ));

        parts.put(ConfigType.BATTERY_CAPACITY, List.of(
                new Configuration("Standard Battery (4000mAh)", ConfigType.BATTERY_CAPACITY, BigDecimal.ZERO),
                new Configuration("Extended Battery (5000mAh)", ConfigType.BATTERY_CAPACITY, new BigDecimal("40.00"))
        ));

        parts.put(ConfigType.ACCESSORY, List.of(
                new Configuration("Fast Charger", ConfigType.ACCESSORY, new BigDecimal("29.99"), true),
                new Configuration("Screen Protector", ConfigType.ACCESSORY, new BigDecimal("9.99"), true)
        ));

        return parts;
    }

    /**
     * Generates at least two sample products per ProductType, assembled from the
     * shared parts map produced by generateParts(). Each product selects specific
     * parts BY INDEX from the pool for a given ConfigType, so products can differ
     * in how many options they offer for the same type (e.g. one smartphone
     * offering fewer colours than another). Electronics products get no
     * configuration options, as specified.
     */
    public static List<Product> generateProducts() {
        Map<ConfigType, List<Configuration>> parts = generateParts();
        List<Product> products = new ArrayList<>();

        // ---- COMPUTER (2) --------------------------------------------------
        products.add(new Product(
                "PROD-COMP-001",
                ProductType.COMPUTER,
                "UltraBook Pro 14",
                combine(
                        pick(parts, ConfigType.RAM, 0, 1, 2),        // all 3 RAM options
                        pick(parts, ConfigType.HARD_DRIVE, 0, 1, 2)  // all 3 storage options
                ),
                new BigDecimal("1299.00"),
                25
        ));
        products.add(new Product(
                "PROD-COMP-002",
                ProductType.COMPUTER,
                "GameForce X17",
                combine(
                        pick(parts, ConfigType.RAM, 1, 2),           // only 16GB / 32GB (no 8GB)
                        pick(parts, ConfigType.HARD_DRIVE, 1, 2)     // only 512GB / 1TB
                ),
                new BigDecimal("1899.00"),
                10
        ));

        // ---- SMARTPHONE (2) -------------------------------------------------
        products.add(new Product(
                "PROD-PHONE-001",
                ProductType.SMARTPHONE,
                "Nova X12",
                combine(
                        pick(parts, ConfigType.COLOR, 0, 1, 2, 3, 4),      // all 5 colours
                        pick(parts, ConfigType.BATTERY_CAPACITY, 0, 1),     // both battery options
                        pick(parts, ConfigType.ACCESSORY, 0, 1)             // Fast Charger, Screen Protector
                ),
                new BigDecimal("899.00"),
                50
        ));
        products.add(new Product(
                "PROD-PHONE-002",
                ProductType.SMARTPHONE,
                "Nova X12 Mini",
                combine(
                        pick(parts, ConfigType.COLOR, 2, 3),        // only 2 colors (Midnight Black, Ocean Blue)
                        pick(parts, ConfigType.BATTERY_CAPACITY, 0), // standard battery only
                        pick(parts, ConfigType.ACCESSORY, 0)         // Fast Charger only
                ),
                new BigDecimal("699.00"),
                40
        ));

        // ---- ELECTRONICS (2) -- no configuration, per spec -------------------
        products.add(new Product(
                "PROD-ELEC-001",
                ProductType.ELECTRONICS,
                "SoundWave Pro Headphones",
                List.of(),
                new BigDecimal("199.00"),
                100
        ));
        products.add(new Product(
                "PROD-ELEC-002",
                ProductType.ELECTRONICS,
                "SmartHome Speaker",
                List.of(),
                new BigDecimal("129.00"),
                60
        ));

        return products;
    }

    /**
     * Selects specific Configuration entries, by index, from the pool of a given
     * ConfigType. Lets different products offer a different subset/size of parts
     * for the same type (e.g. fewer colors on one product than another).
     */
    private static List<Configuration> pick(Map<ConfigType, List<Configuration>> parts, ConfigType type, int... indexes) {
        List<Configuration> source = parts.getOrDefault(type, List.of());
        List<Configuration> result = new ArrayList<>();
        for (int index : indexes) {
            result.add(source.get(index));
        }
        return result;
    }

    /**
     * Flattens several part selections into a single availableConfiguration list.
     */
    @SafeVarargs
    private static List<Configuration> combine(List<Configuration>... selections) {
        List<Configuration> result = new ArrayList<>();
        for (List<Configuration> selection : selections) {
            result.addAll(selection);
        }
        return result;
    }
}
