package org.sourcelab.kafka.webview.ui.controller.api;

import org.sourcelab.kafka.webview.ui.controller.BaseController;
import org.sourcelab.kafka.webview.ui.manager.kafka.KafkaOperations;
import org.sourcelab.kafka.webview.ui.manager.kafka.KafkaOperationsFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.SessionIdentifier;
import org.sourcelab.kafka.webview.ui.manager.kafka.ViewCustomizer;
import org.sourcelab.kafka.webview.ui.manager.kafka.WebKafkaConsumer;
import org.sourcelab.kafka.webview.ui.manager.kafka.WebKafkaConsumerFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.FilterDefinition;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.ApiErrorResponse;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.ConfigItem;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.ConsumerState;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.KafkaResults;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.NodeDetails;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.NodeList;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.PartitionDetails;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.TopicDetails;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.TopicList;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.TopicListing;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.model.Filter;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.repository.ClusterRepository;
import org.sourcelab.kafka.webview.ui.repository.FilterRepository;
import org.sourcelab.kafka.webview.ui.repository.ViewRepository;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private FilterRepository filterRepository;

    @Autowired
    private WebKafkaConsumerFactory webKafkaConsumerFactory;

    @Autowired
    private KafkaOperationsFactory kafkaOperationsFactory;

    /**
     * POST kafka results.
     */
    @ResponseBody
    @RequestMapping(path = "/consumer/view/{id}", method = RequestMethod.POST, produces = "application/json")
    public KafkaResults consume(
        @PathVariable final Long id,
        @RequestBody final ConsumeRequest consumeRequest) {

        // How many results per partition to consume
        // Null means use whatever is configured in the view.
        final Integer resultsPerPartition = consumeRequest.getResultsPerPartition();

        // Comma separated list of partitions to consume from.
        // Null or empty string means use whatever is configured in the View.
        final String partitions = consumeRequest.getPartitions();

        // Action describes what to consume 'next', 'prev', 'head', 'tail'
        final String action = consumeRequest.getAction();

        // Any custom configured filters
        final List<ConsumeRequest.Filter> requestFilters = consumeRequest.getFilters();

        // Retrieve the view definition
        final View view = viewRepository.findOne(id);
        if (view == null) {
            throw new NotFoundApiException("Consume", "Unable to find view");
        }

        // Override settings
        final ViewCustomizer viewCustomizer = new ViewCustomizer(view, consumeRequest);
        viewCustomizer.overrideViewSettings();
        final List<FilterDefinition> configuredFilters = viewCustomizer.getFilterDefinitions();

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
    public ConsumerState setConsumerOffsets(@PathVariable final Long id,  @RequestBody final Map<Integer, Long> partitionOffsetMap) {
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
     * POST manually set a consumer's offsets using a timestamp.
     */
    @ResponseBody
    @RequestMapping(path = "/consumer/view/{id}/timestamp/{timestamp}", method = RequestMethod.POST, produces = "application/json")
    public ConsumerState setConsumerOffsetsByTimestamp(@PathVariable final Long id, @PathVariable final Long timestamp) {
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
     * GET all available partitions for a given view.
     */
    @ResponseBody
    @RequestMapping(path = "/view/{id}/partitions", method = RequestMethod.GET, produces = "application/json")
    public Collection<Integer> getPartitionsForView(@PathVariable final Long id) {
        // Retrieve View
        final View view = viewRepository.findOne(id);
        if (view == null) {
            throw new NotFoundApiException("Partitions", "Unable to find view");
        }

        // If the view has defined partitions, we'll return them
        if (!view.getPartitionsAsSet().isEmpty()) {
            return view.getPartitionsAsSet();
        }

        // Otherwise ask the cluster for what partitions.
        // Create new Operational Client
        final Set<Integer> partitionIds = new HashSet<>();
        try (final KafkaOperations operations = createOperationsClient(view.getCluster())) {
            final TopicDetails topicDetails = operations.getTopicDetails(view.getTopic());
            for (final PartitionDetails partitionDetail : topicDetails.getPartitions()) {
                partitionIds.add(partitionDetail.getPartition());
            }
        } catch (final Exception e) {
            throw new ApiException("Topics", e);
        }
        return partitionIds;
    }

    /**
     * GET listing of all available kafka topics for a requested cluster.
     */
    @ResponseBody
    @RequestMapping(path = "/cluster/{id}/topics/list", method = RequestMethod.GET, produces = "application/json")
    public List<TopicListing> getTopics(@PathVariable final Long id) {
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
    public TopicDetails getTopicDetails(@PathVariable final Long id, @PathVariable final String topic) {
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
    public List<ConfigItem> getTopicConfig(@PathVariable final Long id, @PathVariable final String topic) {
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
    public List<ConfigItem> getBrokerConfig(@PathVariable final Long id, @PathVariable final String brokerId) {
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
    public Collection<TopicDetails> getAllTopicsDetails(@PathVariable final Long id) {
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
    public List<NodeDetails> getClusterNodes(@PathVariable final Long id) {
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
     * GET Options for a specific filter.
     */
    @ResponseBody
    @RequestMapping(path = "/filter/{id}/options", method = RequestMethod.GET, produces = "application/json")
    public String[] getFilterOptions(@PathVariable final Long id) {
        // Retrieve Filter
        final Filter filter = filterRepository.findOne(id);
        if (filter == null) {
            throw new NotFoundApiException("FilterOptions", "Unable to find filter");
        }
        final String[] options = filter.getOptions().split(",");

        return options;
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
    private WebKafkaConsumer setup(final View view, final Collection<FilterDefinition> filterDefinitions) {
        final SessionIdentifier sessionIdentifier = new SessionIdentifier(getLoggedInUserId(), getLoggedInUserSessionId());
        return webKafkaConsumerFactory.createWebClient(view, filterDefinitions, sessionIdentifier);
    }

    /**
     * Override parent method.
     */
    @Override
    @ModelAttribute
    public void addAttributes(final Model model) {
        // Do nothing.
    }
}
