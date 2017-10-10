package com.darksci.kafkaview.controller.api;

import com.darksci.kafkaview.controller.BaseController;
import com.darksci.kafkaview.manager.kafka.KafkaOperations;
import com.darksci.kafkaview.manager.kafka.config.ClientConfig;
import com.darksci.kafkaview.manager.kafka.config.ClusterConfig;
import com.darksci.kafkaview.manager.kafka.config.DeserializerConfig;
import com.darksci.kafkaview.manager.kafka.config.TopicConfig;
import com.darksci.kafkaview.manager.kafka.dto.KafkaResults;
import com.darksci.kafkaview.manager.kafka.dto.TopicDetails;
import com.darksci.kafkaview.manager.kafka.dto.TopicList;
import com.darksci.kafkaview.manager.kafka.KafkaConsumerFactory;
import com.darksci.kafkaview.manager.kafka.TransactionalKafkaClient;
import com.darksci.kafkaview.manager.kafka.config.FilterConfig;
import com.darksci.kafkaview.manager.kafka.filter.AFilter;
import com.darksci.kafkaview.manager.plugin.DeserializerLoader;
import com.darksci.kafkaview.manager.plugin.exception.LoaderException;
import com.darksci.kafkaview.manager.ui.FlashMessage;
import com.darksci.kafkaview.model.Cluster;
import com.darksci.kafkaview.model.MessageFormat;
import com.darksci.kafkaview.model.View;
import com.darksci.kafkaview.repository.ClusterRepository;
import com.darksci.kafkaview.repository.ViewRepository;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles API requests.
 */
@Controller
@RequestMapping("/api")
public class ApiController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private ViewRepository viewRepository;

    @Autowired
    private DeserializerLoader deserializerLoader;

    @Autowired
    private ClusterRepository clusterRepository;

    /**
     * GET kafka results
     */
    @RequestMapping(path = "/consume/view/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public KafkaResults consume(
        final @PathVariable Long id,
        final @RequestParam(name = "action", required = false) String action) {

        // Retrieve the view definition
        final View view = viewRepository.findOne(id);
        if (view == null) {
            // TODO Return some kind of error.
        }

        // Create consumer
        final KafkaResults results;
        try (final TransactionalKafkaClient transactionalKafkaClient = setup(view)) {
            // move directions if needed
            if ("n".equals(action)) {
                transactionalKafkaClient.next();
            } else if ("p".equals(action)) {
                transactionalKafkaClient.previous();
            } else if ("r".equals(action)) {
                transactionalKafkaClient.toHead();
            }

            // Poll
            results = transactionalKafkaClient.consumePerPartition();
        }
        return results;
    }

    /**
     * GET kafka topics for a requested cluster.
     */
    @RequestMapping(path = "/cluster/{id}/topics/list", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<TopicDetails> getTopics(final @PathVariable Long id) {
        // Retrieve cluster
        final Cluster cluster = clusterRepository.findOne(id);
        if (cluster == null) {
            // Handle error by returning empty list?
            new ArrayList<>();
        }

        try (final KafkaOperations operations = KafkaOperations.newKafkaOperationalInstance(cluster.getBrokerHosts())) {
            final TopicList topics = operations.getAvailableTopics();
            return topics.getAllTopics();
        }
    }

    private TransactionalKafkaClient setup(final View view) {
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
        final ClientConfig clientConfig = new ClientConfig(topicConfig, FilterConfig.withNoFilters(), consumerId);

        // Create the damn consumer
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
