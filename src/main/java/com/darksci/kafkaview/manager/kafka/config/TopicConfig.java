package com.darksci.kafkaview.manager.kafka.config;

public class TopicConfig {
    private final ClusterConfig clusterConfig;
    private final DeserializerConfig deserializerConfig;
    private final String topicName;

    public TopicConfig(final ClusterConfig clusterConfig, final DeserializerConfig deserializerConfig, final String topicName) {
        this.clusterConfig = clusterConfig;
        this.deserializerConfig = deserializerConfig;
        this.topicName = topicName;
    }

    public ClusterConfig getClusterConfig() {
        return clusterConfig;
    }

    public DeserializerConfig getDeserializerConfig() {
        return deserializerConfig;
    }

    public String getTopicName() {
        return topicName;
    }

    @Override
    public String toString() {
        return "TopicConfig{" +
            "clusterConfig=" + clusterConfig +
            ", deserializerConfig=" + deserializerConfig +
            ", topicName='" + topicName + '\'' +
            '}';
    }
}
