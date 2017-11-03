package com.darksci.kafka.webview.ui.manager.kafka;

import com.darksci.kafka.webview.ui.manager.kafka.config.ClientConfig;
import com.darksci.kafka.webview.ui.manager.kafka.config.ClusterConfig;
import com.darksci.kafka.webview.ui.manager.kafka.filter.RecordFilterInterceptor;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SslConfigs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory class for creating new KafkaConsumers.
 */
public class KafkaConsumerFactory {

    private final String keyStoreRootPath;

    /**
     * Constructor.
     */
    public KafkaConsumerFactory(final String keyStoreRootPath) {
        this.keyStoreRootPath = keyStoreRootPath;
    }

    /**
     * Create a new KafkaConsumer based on the passed in ClientConfig.
     */
    public KafkaConsumer createConsumer(final ClientConfig clientConfig) {
        // Create consumer
        return new KafkaConsumer<>(buildConsumerConfig(clientConfig));
    }

    /**
     * Create a new KafkaConsumer based on the passed in ClientConfig, and subscribe to the appropriate
     * partitions.
     */
    public KafkaConsumer createConsumerAndSubscribe(final ClientConfig clientConfig) {
        final KafkaConsumer kafkaConsumer = createConsumer(clientConfig);

        // Determine which partitions to subscribe to, for now do all
        final List<PartitionInfo> partitionInfos = kafkaConsumer.partitionsFor(clientConfig.getTopicConfig().getTopicName());

        // Pull out partitions, convert to topic partitions
        final List<TopicPartition> topicPartitions = new ArrayList<>();
        for (final PartitionInfo partitionInfo: partitionInfos) {
            // Skip filtered partitions
            if (!clientConfig.isPartitionFiltered(partitionInfo.partition())) {
                topicPartitions.add(new TopicPartition(partitionInfo.topic(), partitionInfo.partition()));
            }
        }

        // Assign them.
        kafkaConsumer.assign(topicPartitions);

        // Return the kafka consumer.
        return kafkaConsumer;
    }

    /**
     * Build an appropriate configuration based on the passed in ClientConfig.
     */
    public Map<String, Object> buildConsumerConfig(final ClientConfig clientConfig) {
        // Build config
        final Map<String, Object> configMap = new HashMap<>();
        configMap.put(ConsumerConfig.CLIENT_ID_CONFIG, clientConfig.getConsumerId());
        configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, clientConfig.getTopicConfig().getClusterConfig().getConnectString());
        configMap.put(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            clientConfig.getTopicConfig().getDeserializerConfig().getKeyDeserializerClass());
        configMap.put(
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            clientConfig.getTopicConfig().getDeserializerConfig().getValueDeserializerClass());

        // Enable auto commit
        configMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, clientConfig.isAutoCommitEnabled());

        // How many records to pull
        configMap.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, clientConfig.getMaxResultsPerPartition());

        // If we have any filters
        if (!clientConfig.getFilterConfig().getFilters().isEmpty()) {
            // Create interceptor
            configMap.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, RecordFilterInterceptor.class.getName());
            configMap.put(RecordFilterInterceptor.CONFIG_KEY, clientConfig.getFilterConfig().getFilters());
        }

        // Use SSL?
        final ClusterConfig clusterConfig = clientConfig.getTopicConfig().getClusterConfig();
        if (clusterConfig.isUseSsl()) {
            configMap.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
            configMap.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
            configMap.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keyStoreRootPath + "/" + clusterConfig.getKeyStoreFile());
            configMap.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, clusterConfig.getKeyStorePassword());
            configMap.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, keyStoreRootPath + "/" + clusterConfig.getTrustStoreFile());
            configMap.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, clusterConfig.getTrustStorePassword());
        }

        return configMap;
    }
}
