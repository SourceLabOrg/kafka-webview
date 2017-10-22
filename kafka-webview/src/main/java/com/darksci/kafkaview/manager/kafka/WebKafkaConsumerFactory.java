package com.darksci.kafkaview.manager.kafka;

import com.darksci.kafkaview.manager.encryption.SecretManager;
import com.darksci.kafkaview.manager.kafka.config.ClientConfig;
import com.darksci.kafkaview.manager.kafka.config.ClusterConfig;
import com.darksci.kafkaview.manager.kafka.config.DeserializerConfig;
import com.darksci.kafkaview.manager.kafka.config.FilterConfig;
import com.darksci.kafkaview.manager.kafka.config.TopicConfig;
import com.darksci.kafkaview.manager.plugin.PluginFactory;
import com.darksci.kafkaview.manager.plugin.exception.LoaderException;
import com.darksci.kafkaview.model.Cluster;
import com.darksci.kafkaview.model.Filter;
import com.darksci.kafkaview.model.MessageFormat;
import com.darksci.kafkaview.model.View;
import com.darksci.kafkaview.plugin.filter.RecordFilter;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WebKafkaConsumerFactory {
    /**
     * Defines the consumerId.
     */
    private final static String consumerIdPrefix = "KafkaWebView-Consumer-UserId";

    private final PluginFactory<Deserializer> deserializerPluginFactory;
    private final PluginFactory<RecordFilter> recordFilterPluginFactory;
    private final SecretManager secretManager;
    private final KafkaConsumerFactory kafkaConsumerFactory;

    public WebKafkaConsumerFactory(
        final PluginFactory<Deserializer> deserializerPluginFactory,
        final PluginFactory<RecordFilter> recordFilterPluginFactory,
        final SecretManager secretManager,
        final KafkaConsumerFactory kafkaConsumerFactory) {
        this.deserializerPluginFactory = deserializerPluginFactory;
        this.recordFilterPluginFactory = recordFilterPluginFactory;
        this.secretManager = secretManager;
        this.kafkaConsumerFactory = kafkaConsumerFactory;
    }

    public WebKafkaConsumer create(final View view, final Collection<Filter> filterList, final long userId) {
        // Construct a consumerId based on user
        final String consumerId = consumerIdPrefix + userId;

        // Grab our relevant bits
        final Cluster cluster = view.getCluster();
        final MessageFormat keyMessageFormat = view.getKeyMessageFormat();
        final MessageFormat valueMessageFormat = view.getValueMessageFormat();

        final Class keyDeserializerClass;
        try {
            if (keyMessageFormat.isDefaultFormat()) {
                keyDeserializerClass = deserializerPluginFactory.getPluginClass(keyMessageFormat.getClasspath());
            } else {
                keyDeserializerClass = deserializerPluginFactory.getPluginClass(keyMessageFormat.getJar(), keyMessageFormat.getClasspath());
            }
        } catch (final LoaderException exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }

        final Class valueDeserializerClass;
        try {
            if (valueMessageFormat.isDefaultFormat()) {
                valueDeserializerClass = deserializerPluginFactory.getPluginClass(valueMessageFormat.getClasspath());
            } else {
                valueDeserializerClass = deserializerPluginFactory.getPluginClass(valueMessageFormat.getJar(), valueMessageFormat.getClasspath());
            }
        } catch (final LoaderException exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }


        final ClusterConfig clusterConfig = ClusterConfig.newBuilder(cluster, secretManager).build();
        final DeserializerConfig deserializerConfig = new DeserializerConfig(keyDeserializerClass, valueDeserializerClass);
        final TopicConfig topicConfig = new TopicConfig(clusterConfig, deserializerConfig, view.getTopic());

        final ClientConfig.Builder clientConfigBuilder = ClientConfig.newBuilder()
            .withTopicConfig(topicConfig)
            .withConsumerId(consumerId)
            .withPartitions(view.getPartitionsAsSet())
            .withMaxResultsPerPartition(view.getResultsPerPartition());

        // Add enforced filters to our filterList
        filterList.addAll(view.getEnforcedFilters());

        if (filterList.isEmpty()) {
            clientConfigBuilder.withNoFilters();
        } else {
            final List<RecordFilter> recordFilters = new ArrayList<>();
            // Build filter list
            for (final Filter filter: filterList) {
                // Build it
                try {
                    final RecordFilter recordFilter = recordFilterPluginFactory.getPlugin(filter.getJar(), filter.getClasspath());
                    recordFilters.add(recordFilter);
                } catch (LoaderException e) {
                    throw new RuntimeException(e);
                }
            }
            clientConfigBuilder.withFilterConfig(FilterConfig.withFilters(recordFilters));
        }

        // Create the damn consumer
        final ClientConfig clientConfig = clientConfigBuilder.build();
        final KafkaConsumer kafkaConsumer = kafkaConsumerFactory.createConsumerAndSubscribe(clientConfig);

        // Create consumer
        return new WebKafkaConsumer(kafkaConsumer, clientConfig);
    }
}
