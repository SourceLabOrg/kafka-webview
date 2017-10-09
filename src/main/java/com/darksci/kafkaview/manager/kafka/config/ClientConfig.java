package com.darksci.kafkaview.manager.kafka.config;

public class ClientConfig {
    /**
     * Holds details about what topic we're consuming from.
     */
    private final TopicConfig topicConfig;

    /**
     * Holds details about any filters applied.
     */
    private final FilterConfig filterConfig;

    /**
     * Defines the id of the consumer, which is where offsets/state is stored under.
     */
    private final String consumerId;

    /**
     * Defines how many records to retrieve.
     */
    private int maxRecords = 10;

    private boolean isAutoCommitEnabled = false;

    public ClientConfig(final TopicConfig topicConfig, final FilterConfig filterConfig, final String consumerId) {
        this.topicConfig = topicConfig;
        this.filterConfig = filterConfig;
        this.consumerId = consumerId;
    }

    public TopicConfig getTopicConfig() {
        return topicConfig;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public FilterConfig getFilterConfig() {
        return filterConfig;
    }

    public int getMaxRecords() {
        return maxRecords;
    }

    public boolean isAutoCommitEnabled() {
        return isAutoCommitEnabled;
    }

    @Override
    public String toString() {
        return "ClientConfig{" +
            "topicConfig=" + topicConfig +
            ", filterConfig=" + filterConfig +
            ", consumerId='" + consumerId + '\'' +
            '}';
    }
}
