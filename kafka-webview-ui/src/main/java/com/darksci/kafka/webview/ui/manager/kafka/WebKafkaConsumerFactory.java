package com.darksci.kafka.webview.ui.manager.kafka;

import com.darksci.kafka.webview.ui.manager.encryption.SecretManager;
import com.darksci.kafka.webview.ui.manager.kafka.dto.KafkaResult;
import com.darksci.kafka.webview.ui.model.Cluster;
import com.darksci.kafka.webview.ui.model.Filter;
import com.darksci.kafka.webview.ui.manager.kafka.config.ClientConfig;
import com.darksci.kafka.webview.ui.manager.kafka.config.ClusterConfig;
import com.darksci.kafka.webview.ui.manager.kafka.config.DeserializerConfig;
import com.darksci.kafka.webview.ui.manager.kafka.config.FilterConfig;
import com.darksci.kafka.webview.ui.manager.kafka.config.TopicConfig;
import com.darksci.kafka.webview.ui.manager.plugin.PluginFactory;
import com.darksci.kafka.webview.ui.manager.plugin.exception.LoaderException;
import com.darksci.kafka.webview.ui.model.MessageFormat;
import com.darksci.kafka.webview.ui.model.View;
import com.darksci.kafka.webview.ui.plugin.filter.RecordFilter;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;

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

    public WebKafkaConsumer createWebClient(final View view, final Collection<Filter> filterList, final long userId) {
        // Create client config builder
        final ClientConfig clientConfig = createClientConfig(view, filterList, userId).build();

        // Create kafka consumer
        final KafkaConsumer kafkaConsumer = createKafkaConsumer(clientConfig);

        // Create consumer
        return new WebKafkaConsumer(kafkaConsumer, clientConfig);
    }

    public SocketKafkaConsumer createWebSocketClient(final View view, final Collection<Filter> filterList, final long userId, final Queue<KafkaResult> kafkaResultQueue) {
        // Create client config builder
        final ClientConfig clientConfig = createClientConfig(view, filterList, userId).build();

        // Create kafka consumer
        final KafkaConsumer kafkaConsumer = createKafkaConsumer(clientConfig);

        // Create consumer
        final SocketKafkaConsumer socketKafkaConsumer =  new SocketKafkaConsumer(kafkaConsumer, clientConfig, kafkaResultQueue);

        // TODO Reset to tail
        // for now go to head
        socketKafkaConsumer.toHead();

        return socketKafkaConsumer;
    }

    private ClientConfig.Builder createClientConfig(final View view, final Collection<Filter> filterList, final long userId) {
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
        return clientConfigBuilder;
    }

    private KafkaConsumer createKafkaConsumer(final ClientConfig clientConfig) {
        return kafkaConsumerFactory.createConsumerAndSubscribe(clientConfig);
    }
}
