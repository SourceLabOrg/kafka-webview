/**
 * MIT License
 *
 * Copyright (c) 2017-2021 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.manager.encryption.SecretManager;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClientConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClusterConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.DeserializerConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.FilterConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.FilterDefinition;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.RecordFilterDefinition;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.TopicConfig;
import org.sourcelab.kafka.webview.ui.manager.plugin.PluginFactory;
import org.sourcelab.kafka.webview.ui.manager.plugin.exception.LoaderException;
import org.sourcelab.kafka.webview.ui.manager.socket.StartingPosition;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.model.Filter;
import org.sourcelab.kafka.webview.ui.model.MessageFormat;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.model.ViewToFilterEnforced;
import org.sourcelab.kafka.webview.ui.plugin.filter.RecordFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Factory class for creating new Kafka Consumers to be used by WebRequests.
 */
public class WebKafkaConsumerFactory {
    private static final Logger logger = LoggerFactory.getLogger(WebKafkaConsumerFactory.class);

    /**
     * Defines the consumerId prefix pre-pended to all consumers.
     */
    private static final String consumerIdPrefix = "UserId";

    private final PluginFactory<Deserializer> deserializerPluginFactory;
    private final PluginFactory<RecordFilter> recordFilterPluginFactory;
    private final SecretManager secretManager;
    private final KafkaConsumerFactory kafkaConsumerFactory;

    //  Multi-threaded consumer
    private final ExecutorService multiThreadedConsumerThreadPool;

    // For parsing json options
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructor.
     */
    public WebKafkaConsumerFactory(
        final PluginFactory<Deserializer> deserializerPluginFactory,
        final PluginFactory<RecordFilter> recordFilterPluginFactory,
        final SecretManager secretManager,
        final KafkaConsumerFactory kafkaConsumerFactory,
        final ExecutorService multiThreadedConsumerThreadPool) {
        this.deserializerPluginFactory = deserializerPluginFactory;
        this.recordFilterPluginFactory = recordFilterPluginFactory;
        this.secretManager = secretManager;
        this.kafkaConsumerFactory = kafkaConsumerFactory;
        this.multiThreadedConsumerThreadPool = multiThreadedConsumerThreadPool;
    }

    /**
     * Create a Web Consumer Client.  These instances are not intended to live beyond
     * the length of the web request using it.
     * @param view What view to consume from.
     * @param filterDefinitions Any additional filters to apply.
     * @param sessionIdentifier An identifier for the consumer.
     */
    public WebKafkaConsumer createWebClient(
        final View view,
        final Collection<FilterDefinition> filterDefinitions,
        final SessionIdentifier sessionIdentifier) {

        // Create client config builder
        final ClientConfig clientConfig = createClientConfig(view, filterDefinitions, sessionIdentifier)
            // Always resume from existing state.
            .withStartingPosition(StartingPosition.newResumeFromExistingState())
            .build();

        // If we've been passed an executor service
        if (multiThreadedConsumerThreadPool != null) {
            // Assume we want to use multi-threaded consumer.
            return new ParallelWebKafkaConsumer(kafkaConsumerFactory, clientConfig, multiThreadedConsumerThreadPool);
        } else {
            // Create single threaded kafka consumer
            final KafkaConsumer kafkaConsumer = createKafkaConsumer(clientConfig);
            return new DefaultWebKafkaConsumer(kafkaConsumer, clientConfig);
        }
    }

    /**
     * Create a WebSocket Consumer Client.  These instances are intended to be long lived
     * and run in the background, streaming consumed records to a Web Socket.
     * @param view What view to consume from.
     * @param filterDefinitions Any additional filters to apply.
     * @param startingPosition Defines where the Socket consumer should resume from.
     * @param sessionIdentifier An identifier for the consumer.
     */
    public SocketKafkaConsumer createWebSocketClient(
        final View view,
        final Collection<FilterDefinition> filterDefinitions,
        final StartingPosition startingPosition,
        final SessionIdentifier sessionIdentifier) {
        // Create client config builder
        final ClientConfig clientConfig = createClientConfig(view, filterDefinitions, sessionIdentifier)
            .withStartingPosition(startingPosition)
            .build();

        // Create kafka consumer
        final KafkaConsumer kafkaConsumer = createKafkaConsumer(clientConfig);

        // Create consumer
        return new SocketKafkaConsumer(kafkaConsumer, clientConfig);
    }

    private ClientConfig.Builder createClientConfig(
        final View view,
        final Collection<FilterDefinition> filterDefinitions,
        final SessionIdentifier sessionIdentifier) {

        // Construct a consumerId based on user
        final String consumerId = consumerIdPrefix + sessionIdentifier.toString();

        // Grab our relevant bits
        final Cluster cluster = view.getCluster();

        // Build cluster config
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder(cluster, secretManager).build();

        // Build Deserializer config.
        final DeserializerConfig deserializerConfig = buildDeserializerConfig(view);

        // Put together the topic config.
        final TopicConfig topicConfig = new TopicConfig(clusterConfig, deserializerConfig, view.getTopic());

        // Build the client config.
        final ClientConfig.Builder clientConfigBuilder = ClientConfig.newBuilder()
            .withTopicConfig(topicConfig)
            .withConsumerId(consumerId)
            .withPartitions(view.getPartitionsAsSet())
            .withMaxResultsPerPartition(view.getResultsPerPartition());

        final List<RecordFilterDefinition> recordFilterDefinitions = new ArrayList<>();

        // Add enforced filters to our filterList
        final Set<ViewToFilterEnforced> enforcedFilters = view.getEnforcedFilters();
        for (final ViewToFilterEnforced enforcedFilter: enforcedFilters) {
            // Grab filter, add to list
            final RecordFilterDefinition recordFilterDefinition =
                buildRecordFilterDefinition(enforcedFilter.getFilter(), enforcedFilter.getOptionParameters());
            recordFilterDefinitions.add(recordFilterDefinition);
        }

        // Loop over each passed in filter.
        for (final FilterDefinition filterDefinition: filterDefinitions) {
            // Build it
            final RecordFilterDefinition recordFilterDefinition =
                buildRecordFilterDefinition(filterDefinition.getFilter(), filterDefinition.getOptions());
            recordFilterDefinitions.add(recordFilterDefinition);
        }
        clientConfigBuilder.withFilterConfig(FilterConfig.withFilters(recordFilterDefinitions));

        if (recordFilterDefinitions.isEmpty()) {
            clientConfigBuilder.withNoFilters();
        } else {
            clientConfigBuilder.withFilterConfig(FilterConfig.withFilters(recordFilterDefinitions));
        }

        // Create the damn consumer
        return clientConfigBuilder;
    }

    private RecordFilterDefinition buildRecordFilterDefinition(final Filter filter, final Map<String, String> options) {
        // Build it
        try {
            // Create instance.
            final RecordFilter recordFilter = recordFilterPluginFactory.getPlugin(filter.getJar(), filter.getClasspath());

            // Create definition
            return new RecordFilterDefinition(recordFilter, options);
        } catch (LoaderException e) {
            throw new RuntimeException(e);
        }
    }

    private RecordFilterDefinition buildRecordFilterDefinition(final Filter filter, final String optionParametersJsonStr) {
        // Build it
        try {
            // Parse options from string
            final Map<String, String> options = mapper.readValue(optionParametersJsonStr, Map.class);

            // Create definition
            return buildRecordFilterDefinition(filter, options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Using the supplied view, build the DeserializerConfig.
     * @param view View to build the DeserializerConfig for.
     * @return Properly configured DeserializerConfig.
     */
    private DeserializerConfig buildDeserializerConfig(final View view) {
        final DeserializerConfig.Builder builder = DeserializerConfig.newBuilder();

        // Construct properties for message formats / deserializers
        final MessageFormat keyMessageFormat = view.getKeyMessageFormat();
        final MessageFormat valueMessageFormat = view.getValueMessageFormat();

        // Grab class instances for Deserializers
        builder
            .withKeyDeserializerClass(getDeserializerClass(keyMessageFormat))
            .withValueDeserializerClass(getDeserializerClass(valueMessageFormat));

        // Load Key Deserializer options
        try {
            final Map<String, String> options = mapper.readValue(keyMessageFormat.getOptionParameters(), Map.class);
            builder.withKeyDeserializerOptions(options);
        } catch (final IOException e) {
            // Swallow?
            logger.error(
                "Failed to deserialize options for key deserializer: {} with error: {}",
                keyMessageFormat.getName(), e.getMessage());
        }

        // Load Value Deserializer options
        try {
            final Map<String, String> options = mapper.readValue(valueMessageFormat.getOptionParameters(), Map.class);
            builder.withValueDeserializerOptions(options);
        } catch (final IOException e) {
            // Swallow?
            logger.error(
                "Failed to deserialize options for value deserializer: {} with error: {}",
                valueMessageFormat.getName(), e.getMessage());
        }

        // Build deserializer config and return
        return builder.build();
    }

    private Class<? extends Deserializer> getDeserializerClass(final MessageFormat messageFormat) {
        try {
            if (messageFormat.isDefaultFormat()) {
                return deserializerPluginFactory.getPluginClass(messageFormat.getClasspath());
            } else {
                return deserializerPluginFactory.getPluginClass(messageFormat.getJar(), messageFormat.getClasspath());
            }
        } catch (final LoaderException exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    private KafkaConsumer createKafkaConsumer(final ClientConfig clientConfig) {
        return kafkaConsumerFactory.createConsumerAndSubscribe(clientConfig);
    }
}
