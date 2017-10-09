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
import com.darksci.kafkaview.model.Cluster;
import com.darksci.kafkaview.repository.ClusterRepository;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
    private ClusterRepository clusterRepository;

    /**
     * GET kafka results
     */
    @RequestMapping(path = "/consume", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public KafkaResults consume() {
        // Create the configs
        final String topicName = "TestTopic";
        final String consumerId = "BobsYerAunty";

        // Defines the Cluster
        final ClusterConfig clusterConfig = getClusterConfig();

        // Define our Deserializer
        final DeserializerConfig deserializerConfig = DeserializerConfig.defaultConfig();

        // Defines our Topic
        final TopicConfig topicConfig = new TopicConfig(clusterConfig, deserializerConfig, topicName);

        // Defines any filters
        //final FilterConfig filterConfig = FilterConfig.withNoFilters();
        final FilterConfig filterConfig = FilterConfig.withFilters(AFilter.class);

        // Defines our client
        final ClientConfig clientConfig = new ClientConfig(topicConfig, filterConfig, consumerId);

        // Build a consumer
        final KafkaConsumer kafkaConsumer = new KafkaConsumerFactory(clientConfig).createAndSubscribe();

        // Create consumer
        final KafkaResults results;
        try (final TransactionalKafkaClient transactionalKafkaClient = new TransactionalKafkaClient(kafkaConsumer, clientConfig)) {

            // Poll
            results = transactionalKafkaClient.consume();
        }

        // Debug log
        logger.info("Consumed records: {}", results);

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

    private ClusterConfig getClusterConfig() {
        return new ClusterConfig("localhost:9092");
    }
}
