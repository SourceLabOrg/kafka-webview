package com.darksci.kafka.webview.ui.manager.kafka.dto;

import java.util.Collections;
import java.util.List;

/**
 * Represents a collection of metadata about how a topic is configured.
 */
public class TopicConfig {
    private final List<ConfigItem> configEntries;

    /**
     * Constructor.
     */
    public TopicConfig(final List<ConfigItem> configEntries) {
        this.configEntries = configEntries;
    }

    public List<ConfigItem> getConfigEntries() {
        return Collections.unmodifiableList(configEntries);
    }

    @Override
    public String toString() {
        return "TopicConfig{"
            + "+ configEntries=" + configEntries
            + '}';
    }
}
