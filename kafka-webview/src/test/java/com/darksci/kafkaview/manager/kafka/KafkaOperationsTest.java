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

    private final KafkaAdminFactory kafkaAdminFactory = new KafkaAdminFactory("./uploads");

    @Test
    public void testGetAvailableTopics() {
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder().withBrokerHosts("localhost:9092").build();
        final String clientId = "BobsYerAunty";

        final KafkaOperations operations = new KafkaOperations(kafkaAdminFactory.create(clusterConfig, clientId));

        final TopicList topics = operations.getAvailableTopics();
        logger.info("{}", topics);
    }

    @Test
    public void testGetClusterNodes() {
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder().withBrokerHosts("localhost:9092").build();
        final String clientId = "BobsYerAunty";

        final KafkaOperations operations = new KafkaOperations(kafkaAdminFactory.create(clusterConfig, clientId));

        final NodeList nodeList = operations.getClusterNodes();
        logger.info("{}", nodeList);
    }

    @Test
    public void testGetTopicDetails() {
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder().withBrokerHosts("localhost:9092").build();
        final String clientId = "BobsYerAunty";

        final KafkaOperations operations = new KafkaOperations(kafkaAdminFactory.create(clusterConfig, clientId));

        final TopicDetails topicDetails = operations.getTopicDetails("A");
        logger.info("{}", topicDetails);
    }
}