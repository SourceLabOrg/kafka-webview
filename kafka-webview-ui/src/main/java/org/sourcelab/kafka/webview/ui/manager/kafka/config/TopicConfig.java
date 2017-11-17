package org.sourcelab.kafka.webview.ui.manager.kafka.config;

/**
 * Configuration defining which Cluster to connect to, which topic, and how to deserialize values.
 */
public class TopicConfig {
    private final ClusterConfig clusterConfig;
    private final DeserializerConfig deserializerConfig;
    private final String topicName;

    /**
     * Constructor.
     * @param clusterConfig Cluster configuration.
     * @param deserializerConfig Deserializer configuration.
     * @param topicName Topic definition.
     */
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
        return "TopicConfig{"
            + "clusterConfig=" + clusterConfig
            + ", deserializerConfig=" + deserializerConfig
            + ", topicName='" + topicName + '\''
            + '}';
    }
}
