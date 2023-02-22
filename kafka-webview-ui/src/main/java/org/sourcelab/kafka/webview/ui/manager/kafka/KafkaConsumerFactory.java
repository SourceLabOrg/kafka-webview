/**
 * MIT License
 *
 * Copyright (c) 2017-2022 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.sourcelab.kafka.webview.ui.manager.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClientConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.RecordFilterDefinition;
import org.sourcelab.kafka.webview.ui.manager.kafka.filter.RecordFilterInterceptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Factory class for creating new KafkaConsumers.
 */
public class KafkaConsumerFactory {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerFactory.class);
    /**
     * Utility class for setting up common kafka client properties.
     */
    private final KafkaClientConfigUtil configUtil;

    /**
     * Constructor.
     * @param configUtil Utility class to DRY out common kafka client settings.
     */
    public KafkaConsumerFactory(final KafkaClientConfigUtil configUtil) {
        if (configUtil == null) {
            throw new RuntimeException("Missing dependency KafkaClientConfigUtil!");
        }
        this.configUtil = configUtil;
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
     * @deprecated To be removed.
     */
    public KafkaConsumer createConsumerAndSubscribe(final ClientConfig clientConfig) {
        final KafkaConsumer kafkaConsumer = createConsumer(clientConfig);

        // Determine which partitions to subscribe to, for now do all
        final List<PartitionInfo> partitionInfos = kafkaConsumer.partitionsFor(clientConfig.getTopicConfig().getTopicName());

        // Pull out partitions, convert to topic partitions
        final Collection<TopicPartition> topicPartitions = new ArrayList<>();
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
    private Map<String, Object> buildConsumerConfig(final ClientConfig clientConfig) {
        // Build config
        final Map<String, Object> configMap = configUtil.applyCommonSettings(
            clientConfig.getTopicConfig().getClusterConfig(),
            clientConfig.getConsumerId()
        );

        // Set deserializer classes.
        configMap.put(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            clientConfig.getTopicConfig().getDeserializerConfig().getKeyDeserializerClass());
        configMap.put(
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            clientConfig.getTopicConfig().getDeserializerConfig().getValueDeserializerClass());

        // Default to reset to earliest
        configMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Enable auto commit
        configMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, clientConfig.isAutoCommitEnabled());

        // How many records to pull
        configMap.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, clientConfig.getMaxResultsPerPartition());

        // If we have any filters
        final List<RecordFilterDefinition> recordFilterDefinitions = clientConfig.getFilterConfig().getFilters();
        if (!recordFilterDefinitions.isEmpty()) {
            // Create interceptor
            configMap.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, RecordFilterInterceptor.class.getName());
            configMap.put(RecordFilterInterceptor.CONFIG_KEY, recordFilterDefinitions);
        }

        // Get the Deserializer options
        final Map<String, String> deserializerOptions = clientConfig.getTopicConfig().getDeserializerConfig().getMergedOptions();

        // Since we basically allow free-form setting options, we want to disallow overwriting already set options
        // with user defined ones. So lets loop through and only set options that are NOT already set.
        for (final Map.Entry<String, String> entry: deserializerOptions.entrySet()) {
            // Skip config items already set.
            if (configMap.containsKey(entry.getKey())) {
                continue;
            }
            logger.warn("Setting property {} value to {}", entry.getKey(), entry.getValue());
            configMap.put(entry.getKey(), entry.getValue());
        }

        return configMap;
    }
}
