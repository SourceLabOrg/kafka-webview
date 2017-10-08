package com.darksci.kafkaview.manager.kafka.config;

public class ClientConfig {
    private final TopicConfig topicConfig;
    private final FilterConfig filterConfig;
    private final String consumerId;

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

    @Override
    public String toString() {
        return "ClientConfig{" +
            "topicConfig=" + topicConfig +
            ", filterConfig=" + filterConfig +
            ", consumerId='" + consumerId + '\'' +
            '}';
    }
}
