package com.darksci.kafkaview.manager.kafka;

import com.darksci.kafkaview.manager.kafka.config.TopicConfig;
import com.darksci.kafkaview.manager.kafka.config.ClientConfig;
import com.darksci.kafkaview.manager.kafka.config.ClusterConfig;
import com.darksci.kafkaview.manager.kafka.config.DeserializerConfig;
import com.darksci.kafkaview.manager.kafka.config.FilterConfig;
import com.darksci.kafkaview.manager.kafka.dto.TopicList;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaOperationsTest {

    private static final Logger logger = LoggerFactory.getLogger(KafkaOperationsTest.class);

    @Test
    public void test() {
        final ClusterConfig clusterConfig = new ClusterConfig("localhost:9092");
        final DeserializerConfig deserializerConfig = new DeserializerConfig(StringDeserializer.class, StringDeserializer.class);
        final TopicConfig topicConfig = new TopicConfig(clusterConfig, deserializerConfig, "NotUsed");

        final ClientConfig clientConfig = new ClientConfig(topicConfig, FilterConfig.withNoFilters(), "BobsYerAunty");
        final KafkaOperations operations = new KafkaOperations(new KafkaConsumerFactory(clientConfig).create());

        final TopicList topics = operations.getAvailableTopics();
        logger.info("{}", topics);
    }

}