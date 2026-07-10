package org.example.domain.product;

import java.util.List;

public interface Configurable {
    List<ConfigOption<?>> getConfigurationOptions();
}
