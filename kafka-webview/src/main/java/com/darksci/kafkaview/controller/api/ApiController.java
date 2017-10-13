package com.darksci.kafkaview.controller.api;

import com.darksci.kafkaview.manager.kafka.KafkaAdminFactory;
import com.darksci.kafkaview.manager.kafka.KafkaOperations;
import com.darksci.kafkaview.manager.kafka.config.ClientConfig;
import com.darksci.kafkaview.manager.kafka.config.ClusterConfig;
import com.darksci.kafkaview.manager.kafka.config.DeserializerConfig;
import com.darksci.kafkaview.manager.kafka.config.FilterConfig;
import com.darksci.kafkaview.manager.kafka.config.TopicConfig;
import com.darksci.kafkaview.manager.kafka.dto.ConsumerState;
import com.darksci.kafkaview.manager.kafka.dto.KafkaResults;
import com.darksci.kafkaview.manager.kafka.dto.NodeDetails;
import com.darksci.kafkaview.manager.kafka.dto.NodeList;
import com.darksci.kafkaview.manager.kafka.dto.TopicDetails;
import com.darksci.kafkaview.manager.kafka.dto.TopicList;
import com.darksci.kafkaview.manager.kafka.KafkaConsumerFactory;
import com.darksci.kafkaview.manager.kafka.TransactionalKafkaClient;
import com.darksci.kafkaview.manager.kafka.dto.TopicListing;
import com.darksci.kafkaview.manager.plugin.DeserializerLoader;
import com.darksci.kafkaview.manager.plugin.PluginFactory;
import com.darksci.kafkaview.manager.plugin.exception.LoaderException;
import com.darksci.kafkaview.model.Cluster;
import com.darksci.kafkaview.model.Filter;
import com.darksci.kafkaview.model.MessageFormat;
import com.darksci.kafkaview.model.View;
import com.darksci.kafkaview.plugin.filter.RecordFilter;
import com.darksci.kafkaview.repository.ClusterRepository;
import com.darksci.kafkaview.repository.ViewRepository;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Handles API requests.
 */
@Controller
@RequestMapping("/api")
public class ApiController {
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private ViewRepository viewRepository;

    @Autowired
    private DeserializerLoader deserializerLoader;

    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private PluginFactory<RecordFilter> recordFilterPluginFactory;

    /**
     * GET kafka results
     */
    @RequestMapping(path = "/consumer/view/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public KafkaResults consume(
        final @PathVariable Long id,
        final @RequestParam(name = "action", required = false) String action,
        final @RequestParam(name = "partitions", required = false) String partitions,
        final @RequestParam(name = "filters", required = false) String filters,
        final @RequestParam(name = "results_per_partition", required = false) Integer resultsPerPartition) {

        // Retrieve the view definition
        final View view = viewRepository.findOne(id);
        if (view == null) {
            // TODO Return some kind of error.
            throw new RuntimeException("Unable to find view!");
        }

        // Optionally over ride results per partition
        if (resultsPerPartition != null && resultsPerPartition > 0 && resultsPerPartition < 500) {
            // Override in view
            view.setResultsPerPartition(resultsPerPartition);
        }

        // Determine if we should apply filters over partitions
        // but if the view has enforced partition filtering, don't bypass its logic.
        if (partitions != null && !partitions.isEmpty()) {
            final boolean filterPartitions;
            if (view.getPartitions() == null || view.getPartitions().isEmpty()) {
                filterPartitions = false;
            } else {
                filterPartitions = true;
            }

            // Create a string of partitions
            final Set<Integer> allowedPartitions = view.getPartitionsAsSet();
            final Set<Integer> configuredPartitions = new HashSet<>();

            // Convert the String array into an actual array
            for (final String requestedPartitionStr: partitions.split(",")) {
                try {
                    // If its not an allowed partition skip it
                    final Integer requestedPartition = Integer.parseInt(requestedPartitionStr);
                    if (filterPartitions && !allowedPartitions.contains(requestedPartition)) {
                        continue;
                    }
                    configuredPartitions.add(requestedPartition);
                } catch (NumberFormatException e) {
                    continue;
                }
            }

            // Finally override config if we have something
            if (!configuredPartitions.isEmpty()) {
                view.setPartitions(configuredPartitions.stream().map(Object::toString).collect(Collectors.joining(",")));
            }
        }

        // Determine if we should apply record filters
        // but if the view has enforced record filtering, don't bypass its logic, add onto it.
        final Set<Filter> configuredFilters = new HashSet<>();
        if (filters != null && !filters.isEmpty()) {
            // Retrieve all available filters
            final Map<Long, Filter> allowedFilters = new HashMap<>();

            // Build list of allowed filters
            for (final Filter allowedFilter : view.getOptionalFilters()) {
                allowedFilters.put(allowedFilter.getId(), allowedFilter);
            }

            // Convert the String array into an actual array
            for (final String requestedFilterStr: filters.split(",")) {
                // Convert to a Long
                try {
                    // Convert to a long
                    final Long requestedFilterId = Long.parseLong(requestedFilterStr);

                    // See if its an allowed filter
                    if (!allowedFilters.containsKey(requestedFilterId)) {
                        // Skip not allowed filters
                        continue;
                    }
                    // Configure it
                    configuredFilters.add(allowedFilters.get(requestedFilterId));
                } catch (NumberFormatException e) {
                    // Skip invalid values
                    continue;
                }
            }
        }

        // Create consumer
        final KafkaResults results;
        try (final TransactionalKafkaClient transactionalKafkaClient = setup(view, configuredFilters)) {
            // move directions if needed
            if ("next".equals(action)) {
                // Do nothing!
                //transactionalKafkaClient.next();
            } else if ("previous".equals(action)) {
                transactionalKafkaClient.previous();
            } else if ("head".equals(action)) {
                transactionalKafkaClient.toHead();
            } else if ("tail".equals(action)) {
                transactionalKafkaClient.toTail();
            }

            // Poll
            results = transactionalKafkaClient.consumePerPartition();
        }
        return results;
    }

    /**
     * POST manually set a consumer's offsets.
     */
    @RequestMapping(path = "/consumer/view/{id}/offsets", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ConsumerState setConsumerOffsets(final @PathVariable Long id, final @RequestBody Map<Integer, Long> partitionOffsetMap) {
        // Retrieve the view definition
        final View view = viewRepository.findOne(id);
        if (view == null) {
            // TODO Return some kind of error.
        }

        // Create consumer
        try (final TransactionalKafkaClient transactionalKafkaClient = setup(view, new HashSet<>())) {
            return transactionalKafkaClient.seek(partitionOffsetMap);
        }
    }

    /**
     * GET listing of all available kafka topics for a requested cluster.
     */
    @RequestMapping(path = "/cluster/{id}/topics/list", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<TopicListing> getTopics(final @PathVariable Long id) {
        // Retrieve cluster
        final Cluster cluster = clusterRepository.findOne(id);
        if (cluster == null) {
            // Handle error by returning empty list?
            new ArrayList<>();
        }

        // Create new Operational Client
        try (final KafkaOperations operations = createOperationsClient(cluster)) {
            final TopicList topics = operations.getAvailableTopics();
            return topics.getTopics();
        }
    }

    /**
     * GET Topic Details
     */
    @RequestMapping(path = "/cluster/{id}/topic/{topic}/details", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public TopicDetails getTopicDetails(final @PathVariable Long id, final @PathVariable String topic) {
        // Retrieve cluster
        final Cluster cluster = clusterRepository.findOne(id);
        if (cluster == null) {
            // Handle error by returning empty list?
            new ArrayList<>();
        }

        // Create new Operational Client
        try (final KafkaOperations operations = createOperationsClient(cluster)) {
            final TopicDetails topicDetails = operations.getTopicDetails(topic);
            return topicDetails;
        }
    }

    /**
     * GET Nodes within a cluster.
     */
    @RequestMapping(path = "/cluster/{id}/nodes", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<NodeDetails> getClusterNodes(final @PathVariable Long id) {
        // Retrieve cluster
        final Cluster cluster = clusterRepository.findOne(id);
        if (cluster == null) {
            // Handle error by returning empty list?
            new ArrayList<>();
        }

        try (final KafkaOperations operations = createOperationsClient(cluster)) {
            final NodeList nodes = operations.getClusterNodes();
            return nodes.getNodes();
        }
    }

    private KafkaOperations createOperationsClient(final Cluster cluster) {
        // TODO use a clientId unique to the client + cluster + topic
        final String clientId = "MyUser on MyTopic at MyCluster";

        // Create new Operational Client
        final ClusterConfig clusterConfig = new ClusterConfig(cluster.getBrokerHosts());
        final AdminClient adminClient = new KafkaAdminFactory(clusterConfig, clientId).create();

        return new KafkaOperations(adminClient);
    }

    private TransactionalKafkaClient setup(final View view, final Collection<Filter> filterList) {
        // Construct a consumerId based on user
        final String consumerId = "MyUserId1";

        // Grab our relevant bits
        final Cluster cluster = view.getCluster();
        final MessageFormat keyMessageFormat = view.getKeyMessageFormat();
        final MessageFormat valueMessageFormat = view.getValueMessageFormat();

        final Class keyDeserializerClass;
        try {
            if (keyMessageFormat.isDefaultFormat()) {
                keyDeserializerClass = deserializerLoader.getDeserializerClass(keyMessageFormat.getClasspath());
            } else {
                keyDeserializerClass = deserializerLoader.getDeserializerClass(keyMessageFormat.getJar(), keyMessageFormat.getClasspath());
            }
        } catch (final LoaderException exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }

        final Class valueDeserializerClass;
        try {
            if (valueMessageFormat.isDefaultFormat()) {
                valueDeserializerClass = deserializerLoader.getDeserializerClass(valueMessageFormat.getClasspath());
            } else {
                valueDeserializerClass = deserializerLoader.getDeserializerClass(valueMessageFormat.getJar(), valueMessageFormat.getClasspath());
            }
        } catch (final LoaderException exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }


        final ClusterConfig clusterConfig = new ClusterConfig(cluster.getBrokerHosts());
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
        final KafkaConsumerFactory kafkaConsumerFactory = new KafkaConsumerFactory(clientConfig);
        final KafkaConsumer kafkaConsumer = kafkaConsumerFactory.createAndSubscribe();

        // Create consumer
        final KafkaResults results;
        return new TransactionalKafkaClient(kafkaConsumer, clientConfig);
    }

    private ClusterConfig getClusterConfig() {
        return new ClusterConfig("localhost:9092");
    }
}
