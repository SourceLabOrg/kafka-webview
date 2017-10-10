package com.darksci.kafkaview.manager.kafka;

import com.darksci.kafkaview.manager.kafka.config.ClientConfig;
import com.darksci.kafkaview.manager.kafka.filter.FilterInterceptor;
import com.darksci.kafkaview.model.View;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KafkaConsumerFactory {
    private final static Logger logger = LoggerFactory.getLogger(KafkaConsumerFactory.class);

    private final ClientConfig clientConfig;

    public KafkaConsumerFactory(final ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    public KafkaConsumer create() {
        // Create consumer
        return new KafkaConsumer<>(buildConsumerConfig());
    }

    public KafkaConsumer createAndSubscribe() {
        final KafkaConsumer kafkaConsumer = create();

        // Determine which partitions to subscribe to, for now do all
        final List<PartitionInfo> partitionInfos = kafkaConsumer.partitionsFor(clientConfig.getTopicConfig().getTopicName());

        // Pull out partitions, convert to browser partitions
        final List<TopicPartition> topicPartitions = new ArrayList<>();
        for (final PartitionInfo partitionInfo: partitionInfos) {
            topicPartitions.add(new TopicPartition(partitionInfo.topic(), partitionInfo.partition()));
        }

        // Assign them.
        kafkaConsumer.assign(topicPartitions);

        // Return the kafka consumer.
        return kafkaConsumer;
    }

    private Map<String, Object> buildConsumerConfig() {
        // Build config
        final Map<String, Object> configMap = new HashMap<>();
        configMap.put(ConsumerConfig.CLIENT_ID_CONFIG, clientConfig.getConsumerId());
        configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, clientConfig.getTopicConfig().getClusterConfig().getConnectString());
        configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, clientConfig.getTopicConfig().getDeserializerConfig().getKeyDeserializerClass());
        configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, clientConfig.getTopicConfig().getDeserializerConfig().getValueDeserializerClass());

        // Enable auto commit
        configMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, clientConfig.isAutoCommitEnabled());

        // How many records to pull
        configMap.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, clientConfig.getMaxRecords());

        // If we have any filters
        if (!clientConfig.getFilterConfig().getFilters().isEmpty()) {
            // Create interceptor
            configMap.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, FilterInterceptor.class.getName());
            configMap.put(FilterInterceptor.CONFIG_KEY, clientConfig.getFilterConfig().getFilters());
        }

        return configMap;
    }
}
