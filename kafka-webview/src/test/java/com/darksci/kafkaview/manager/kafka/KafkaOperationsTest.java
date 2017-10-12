package com.darksci.kafkaview.manager.kafka;

import com.darksci.kafkaview.manager.kafka.config.ClusterConfig;
import com.darksci.kafkaview.manager.kafka.dto.NodeList;
import com.darksci.kafkaview.manager.kafka.dto.TopicDetails;
import com.darksci.kafkaview.manager.kafka.dto.TopicList;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaOperationsTest {

    private static final Logger logger = LoggerFactory.getLogger(KafkaOperationsTest.class);

    @Test
    public void testGetAvailableTopics() {
        final ClusterConfig clusterConfig = new ClusterConfig("localhost:9092");
        final String clientId = "BobsYerAunty";

        final KafkaOperations operations = new KafkaOperations(new KafkaAdminFactory(clusterConfig, clientId).create());

        final TopicList topics = operations.getAvailableTopics();
        logger.info("{}", topics);
    }

    @Test
    public void testGetClusterNodes() {
        final ClusterConfig clusterConfig = new ClusterConfig("localhost:9092");
        final String clientId = "BobsYerAunty";

        final KafkaOperations operations = new KafkaOperations(new KafkaAdminFactory(clusterConfig, clientId).create());

        final NodeList nodeList = operations.getClusterNodes();
        logger.info("{}", nodeList);
    }

    @Test
    public void testGetTopicDetails() {
        final ClusterConfig clusterConfig = new ClusterConfig("localhost:9092");
        final String clientId = "BobsYerAunty";

        final KafkaOperations operations = new KafkaOperations(new KafkaAdminFactory(clusterConfig, clientId).create());

        final TopicDetails topicDetails = operations.getTopicDetails("A");
        logger.info("{}", topicDetails);
    }
}