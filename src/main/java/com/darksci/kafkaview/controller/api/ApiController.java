package com.darksci.kafkaview.controller.api;

import com.darksci.kafkaview.controller.BaseController;
import com.darksci.kafkaview.manager.kafka.KafkaOperations;
import com.darksci.kafkaview.manager.kafka.config.ClientConfig;
import com.darksci.kafkaview.manager.kafka.config.ClusterConfig;
import com.darksci.kafkaview.manager.kafka.config.DeserializerConfig;
import com.darksci.kafkaview.manager.kafka.config.TopicConfig;
import com.darksci.kafkaview.manager.kafka.dto.KafkaResults;
import com.darksci.kafkaview.manager.kafka.dto.TopicList;
import com.darksci.kafkaview.manager.kafka.KafkaConsumerFactory;
import com.darksci.kafkaview.manager.kafka.TransactionalKafkaClient;
import com.darksci.kafkaview.manager.kafka.config.FilterConfig;
import com.darksci.kafkaview.manager.kafka.filter.AFilter;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Handles API requests.
 */
@Controller
@RequestMapping("/api")
public class ApiController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

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
        final TransactionalKafkaClient transactionalKafkaClient = new TransactionalKafkaClient(kafkaConsumer);

        // Poll
        final KafkaResults results = transactionalKafkaClient.consume();

        // and close
        transactionalKafkaClient.close();

        // Debug log
        logger.info("Consumed records: {}", results);

        return results;
    }

    /**
     * GET kafka results
     */
    @RequestMapping(path = "/getTopics", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public TopicList getTopics() {
        final ClusterConfig clusterConfig = getClusterConfig();
        final DeserializerConfig deserializerConfig = DeserializerConfig.defaultConfig();
        final TopicConfig topicConfig = new TopicConfig(clusterConfig, deserializerConfig, "NotUsed");

        final ClientConfig clientConfig = new ClientConfig(topicConfig, FilterConfig.withNoFilters(), "BobsYerAunty");
        final KafkaOperations operations = new KafkaOperations(new KafkaConsumerFactory(clientConfig).create());

        return operations.getAvailableTopics();
    }

    private ClusterConfig getClusterConfig() {
        return new ClusterConfig("localhost:9092");
    }
}
