package com.darksci.kafka.webview.controller.api;

import com.darksci.kafka.webview.model.Filter;
import com.darksci.kafka.webview.controller.BaseController;
import com.darksci.kafka.webview.manager.kafka.KafkaOperations;
import com.darksci.kafka.webview.manager.kafka.KafkaOperationsFactory;
import com.darksci.kafka.webview.manager.kafka.WebKafkaConsumer;
import com.darksci.kafka.webview.manager.kafka.WebKafkaConsumerFactory;
import com.darksci.kafka.webview.manager.kafka.dto.ApiErrorResponse;
import com.darksci.kafka.webview.manager.kafka.dto.ConfigItem;
import com.darksci.kafka.webview.manager.kafka.dto.ConsumerState;
import com.darksci.kafka.webview.manager.kafka.dto.KafkaResults;
import com.darksci.kafka.webview.manager.kafka.dto.NodeDetails;
import com.darksci.kafka.webview.manager.kafka.dto.NodeList;
import com.darksci.kafka.webview.manager.kafka.dto.TopicDetails;
import com.darksci.kafka.webview.manager.kafka.dto.TopicList;
import com.darksci.kafka.webview.manager.kafka.dto.TopicListing;
import com.darksci.kafka.webview.model.Cluster;
import com.darksci.kafka.webview.model.View;
import com.darksci.kafka.webview.repository.ClusterRepository;
import com.darksci.kafka.webview.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles API requests.
 */
@Controller
@RequestMapping("/api")
public class ApiController extends BaseController {
    @Autowired
    private ViewRepository viewRepository;

    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private WebKafkaConsumerFactory webKafkaConsumerFactory;

    @Autowired
    private KafkaOperationsFactory kafkaOperationsFactory;

    /**
     * GET kafka results
     */
    @ResponseBody
    @RequestMapping(path = "/consumer/view/{id}", method = RequestMethod.GET, produces = "application/json")
    public KafkaResults consume(
        final @PathVariable Long id,
        final @RequestParam(name = "action", required = false) String action,
        final @RequestParam(name = "partitions", required = false) String partitions,
        final @RequestParam(name = "filters", required = false) String filters,
        final @RequestParam(name = "results_per_partition", required = false) Integer resultsPerPartition) {

        // Retrieve the view definition
        final View view = viewRepository.findOne(id);
        if (view == null) {
            throw new NotFoundApiException("Consume", "Unable to find view");
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
                } catch (final NumberFormatException e) {
                    // Skip invalid partitions
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
        try (final WebKafkaConsumer webKafkaConsumer = setup(view, configuredFilters)) {
            // move directions if needed
            if ("next".equals(action)) {
                // Do nothing!
                //webKafkaConsumer.next();
            } else if ("previous".equals(action)) {
                webKafkaConsumer.previous();
            } else if ("head".equals(action)) {
                webKafkaConsumer.toHead();
            } else if ("tail".equals(action)) {
                webKafkaConsumer.toTail();
            }

            // Poll
            return webKafkaConsumer.consumePerPartition();
        } catch (final Exception e) {
            throw new ApiException("Consume", e);
        }
    }

    /**
     * POST manually set a consumer's offsets.
     */
    @ResponseBody
    @RequestMapping(path = "/consumer/view/{id}/offsets", method = RequestMethod.POST, produces = "application/json")
    public ConsumerState setConsumerOffsets(final @PathVariable Long id, final @RequestBody Map<Integer, Long> partitionOffsetMap) {
        // Retrieve the view definition
        final View view = viewRepository.findOne(id);
        if (view == null) {
            throw new NotFoundApiException("Offsets", "Unable to find view");
        }

        // Create consumer
        try (final WebKafkaConsumer webKafkaConsumer = setup(view, new HashSet<>())) {
            return webKafkaConsumer.seek(partitionOffsetMap);
        } catch (final Exception e) {
            throw new ApiException("Offsets", e);
        }
    }

    /**
     * POST manually set a consumer's offsets using a timestamp
     */
    @ResponseBody
    @RequestMapping(path = "/consumer/view/{id}/timestamp/{timestamp}", method = RequestMethod.POST, produces = "application/json")
    public ConsumerState setConsumerOffsetsByTimestamp(final @PathVariable Long id, final @PathVariable Long timestamp) {
        // Retrieve the view definition
        final View view = viewRepository.findOne(id);
        if (view == null) {
            throw new NotFoundApiException("OffsetsByTimestamp", "Unable to find view");
        }

        // Create consumer
        try (final WebKafkaConsumer webKafkaConsumer = setup(view, new HashSet<>())) {
            return webKafkaConsumer.seek(timestamp);
        } catch (final Exception e) {
            throw new ApiException("OffsetsByTimestamp", e);
        }
    }

    /**
     * GET listing of all available kafka topics for a requested cluster.
     */
    @ResponseBody
    @RequestMapping(path = "/cluster/{id}/topics/list", method = RequestMethod.GET, produces = "application/json")
    public List<TopicListing> getTopics(final @PathVariable Long id) {
        // Retrieve cluster
        final Cluster cluster = clusterRepository.findOne(id);
        if (cluster == null) {
            throw new NotFoundApiException("Topics", "Unable to find cluster");
        }

        // Create new Operational Client
        try (final KafkaOperations operations = createOperationsClient(cluster)) {
            final TopicList topics = operations.getAvailableTopics();
            return topics.getTopics();
        } catch (final Exception e) {
            throw new ApiException("Topics", e);
        }
    }

    /**
     * GET Details for a specific Topic.
     */
    @ResponseBody
    @RequestMapping(path = "/cluster/{id}/topic/{topic}/details", method = RequestMethod.GET, produces = "application/json")
    public TopicDetails getTopicDetails(final @PathVariable Long id, final @PathVariable String topic) {
        // Retrieve cluster
        final Cluster cluster = clusterRepository.findOne(id);
        if (cluster == null) {
            throw new NotFoundApiException("TopicDetails", "Unable to find cluster");
        }

        // Create new Operational Client
        try (final KafkaOperations operations = createOperationsClient(cluster)) {
            return operations.getTopicDetails(topic);
        } catch (final Exception e) {
            throw new ApiException("TopicDetails", e);
        }
    }

    /**
     * GET Config for a specific Topic.
     */
    @ResponseBody
    @RequestMapping(path = "/cluster/{id}/topic/{topic}/config", method = RequestMethod.GET, produces = "application/json")
    public List<ConfigItem> getTopicConfig(final @PathVariable Long id, final @PathVariable String topic) {
        // Retrieve cluster
        final Cluster cluster = clusterRepository.findOne(id);
        if (cluster == null) {
            throw new NotFoundApiException("TopicConfig", "Unable to find cluster");
        }

        // Create new Operational Client
        try (final KafkaOperations operations = createOperationsClient(cluster)) {
            return operations.getTopicConfig(topic).getConfigEntries();
        } catch (final Exception e) {
            throw new ApiException("TopicConfig", e);
        }
    }

    /**
     * GET Config for a specific broker.
     */
    @ResponseBody
    @RequestMapping(path = "/cluster/{id}/broker/{brokerId}/config", method = RequestMethod.GET, produces = "application/json")
    public List<ConfigItem> getBrokerConfig(final @PathVariable Long id, final @PathVariable String brokerId) {
        // Retrieve cluster
        final Cluster cluster = clusterRepository.findOne(id);
        if (cluster == null) {
            throw new NotFoundApiException("TopicConfig", "Unable to find cluster");
        }

        // Create new Operational Client
        try (final KafkaOperations operations = createOperationsClient(cluster)) {
            return operations.getBrokerConfig(brokerId).getConfigEntries();
        } catch (final Exception e) {
            throw new ApiException("BrokerConfig", e);
        }
    }

    /**
     * GET Details for all Topics on a cluster.
     */
    @ResponseBody
    @RequestMapping(path = "/cluster/{id}/topics/details", method = RequestMethod.GET, produces = "application/json")
    public Collection<TopicDetails> getAllTopicsDetails(final @PathVariable Long id) {
        // Retrieve cluster
        final Cluster cluster = clusterRepository.findOne(id);
        if (cluster == null) {
            throw new NotFoundApiException("TopicDetails", "Unable to find cluster");
        }

        // Create new Operational Client
        try (final KafkaOperations operations = createOperationsClient(cluster)) {
            // First get all of the topics
            final TopicList topicList = operations.getAvailableTopics();

            // Now get details about all the topics
            final Map<String, TopicDetails> results = operations.getTopicDetails(topicList.getTopicNames());

            // Return just the TopicDetails
            return results.values();
        } catch (final Exception e) {
            throw new ApiException("TopicDetails", e);
        }
    }

    /**
     * GET Nodes within a cluster.
     */
    @ResponseBody
    @RequestMapping(path = "/cluster/{id}/nodes", method = RequestMethod.GET, produces = "application/json")
    public List<NodeDetails> getClusterNodes(final @PathVariable Long id) {
        // Retrieve cluster
        final Cluster cluster = clusterRepository.findOne(id);
        if (cluster == null) {
            throw new NotFoundApiException("ClusterNodes", "Unable to find cluster");
        }

        try (final KafkaOperations operations = createOperationsClient(cluster)) {
            final NodeList nodes = operations.getClusterNodes();
            return nodes.getNodes();
        } catch (final Exception exception) {
            throw new ApiException("ClusterNodes", exception);
        }
    }

    /**
     * Error handler for ApiExceptions.
     */
    @ResponseBody
    @ExceptionHandler(ApiException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleApiException(final ApiException exception) {
        return new ApiErrorResponse(exception.getType(), exception.getMessage());
    }

    /**
     * Create an operations client.
     */
    private KafkaOperations createOperationsClient(final Cluster cluster) {
        return kafkaOperationsFactory.create(cluster, getLoggedInUserId());
    }

    /**
     * Creates a WebKafkaConsumer instance.
     */
    private WebKafkaConsumer setup(final View view, final Collection<Filter> filterList) {
        return webKafkaConsumerFactory.create(view, filterList, getLoggedInUserId());
    }

    /**
     * Override parent method.
     */
    @Override
    @ModelAttribute
    public void addAttributes(Model model) {
        // Do nothing.
    }
}
