package com.darksci.kafka.webview.manager.kafka.dto;

import java.util.List;

public class TopicConfig {
    private final List<ConfigItem> configEntries;

    public TopicConfig(final List<ConfigItem> configEntries) {
        this.configEntries = configEntries;
    }

    public List<ConfigItem> getConfigEntries() {
        return configEntries;
    }

    @Override
    public String toString() {
        return "TopicConfig{" +
            "configEntries=" + configEntries +
            '}';
    }
}
