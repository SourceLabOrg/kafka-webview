package com.darksci.kafkaview.manager.kafka.dto;

import java.util.List;

public class BrokerConfig {
    private final List<ConfigItem> configEntries;

    public BrokerConfig(final List<ConfigItem> configEntries) {
        this.configEntries = configEntries;
    }

    public List<ConfigItem> getConfigEntries() {
        return configEntries;
    }

    @Override
    public String toString() {
        return "BrokerConfig{" +
            "configEntries=" + configEntries +
            '}';
    }
}
