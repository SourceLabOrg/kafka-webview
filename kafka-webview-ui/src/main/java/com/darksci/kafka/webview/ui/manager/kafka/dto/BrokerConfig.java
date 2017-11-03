package com.darksci.kafka.webview.ui.manager.kafka.dto;

import java.util.Collections;
import java.util.List;

/**
 * Represents configuration values as defined on a broker.
 */
public class BrokerConfig {
    private final List<ConfigItem> configEntries;

    public BrokerConfig(final List<ConfigItem> configEntries) {
        this.configEntries = Collections.unmodifiableList(configEntries);
    }

    public List<ConfigItem> getConfigEntries() {
        return configEntries;
    }

    @Override
    public String toString() {
        return "BrokerConfig{"
            + "configEntries=" + configEntries
            + '}';
    }
}
