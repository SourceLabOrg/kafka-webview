package com.darksci.kafka.webview.ui.manager.kafka;

import com.darksci.kafka.webview.ui.manager.kafka.config.ClientConfig;
import com.darksci.kafka.webview.ui.manager.kafka.config.ClusterConfig;
import com.darksci.kafka.webview.ui.manager.kafka.config.DeserializerConfig;
import com.darksci.kafka.webview.ui.manager.kafka.config.TopicConfig;
import com.salesforce.kafka.test.junit.SharedKafkaTestResource;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KafkaConsumerFactoryTest {

    @ClassRule
    public static SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

    /**
     * Simple Smoke Test.
     */
    @Test
    public void testBasicConsumerMultiplePartitions() {
        final int maxRecordsPerPoll = 10;

        // Create a topic with 2 partitions, (partitionId 0, 1)
        final String topicName = "TestTopic";
        sharedKafkaTestResource
            .getKafkaTestServer()
            .createTopic(topicName, 2);

        // Produce 10 records into partition 0 of topic.
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .produceRecords(maxRecordsPerPoll, topicName, 0);

        // Produce 10 records into partition 1 of topic.
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .produceRecords(maxRecordsPerPoll, topicName, 1);

        // Create factory
        final KafkaConsumerFactory kafkaConsumerFactory = new KafkaConsumerFactory("not/used");

        // Create cluster Config
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(sharedKafkaTestResource.getKafkaConnectString())
            .build();

        // Create Deserializer Config
        final DeserializerConfig deserializerConfig = new DeserializerConfig(StringDeserializer.class, StringDeserializer.class);

        // Create Topic Config
        final TopicConfig topicConfig = new TopicConfig(clusterConfig, deserializerConfig, topicName);

        // Create ClientConfig
        final ClientConfig clientConfig = ClientConfig.newBuilder()
            .withConsumerId("MyConsumerId")
            .withNoFilters()
            .withAllPartitions()
            .withMaxResultsPerPartition(maxRecordsPerPoll)
            .withTopicConfig(topicConfig)
            .build();

        // Create consumer
        try (final KafkaConsumer<String, String> consumer = kafkaConsumerFactory.createConsumerAndSubscribe(clientConfig)) {
            // Attempt to consume, should pull first 10
            ConsumerRecords<String, String> records = consumer.poll(2000L);
            assertEquals("Should have found " + maxRecordsPerPoll + " records", maxRecordsPerPoll, records.count());

            // Attempt to consume, should pull 2nd 10
            records = consumer.poll(2000L);
            assertEquals("Should have found " + maxRecordsPerPoll + " records", maxRecordsPerPoll, records.count());
        }
    }

    /**
     * Simple Smoke Test, exclude a partition.
     */
    @Test
    public void testBasicConsumerExcludePartitions() {
        final int maxRecordsPerPoll = 10;

        // Create a topic with 2 partitions, (partitionId 0, 1)
        final String topicName = "TestTopic";
        sharedKafkaTestResource
            .getKafkaTestServer()
            .createTopic(topicName, 2);

        // Produce 10 records into partition 0 of topic.
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .produceRecords(maxRecordsPerPoll, topicName, 0);

        // Produce 10 records into partition 1 of topic.
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .produceRecords(maxRecordsPerPoll, topicName, 1);

        // Create factory
        final KafkaConsumerFactory kafkaConsumerFactory = new KafkaConsumerFactory("not/used");

        // Create cluster Config
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(sharedKafkaTestResource.getKafkaConnectString())
            .build();

        // Create Deserializer Config
        final DeserializerConfig deserializerConfig = new DeserializerConfig(StringDeserializer.class, StringDeserializer.class);

        // Create Topic Config
        final TopicConfig topicConfig = new TopicConfig(clusterConfig, deserializerConfig, topicName);

        // Create ClientConfig
        final ClientConfig clientConfig = ClientConfig.newBuilder()
            .withConsumerId("MyConsumerId")
            .withNoFilters()
            .withPartition(1)
            .withMaxResultsPerPartition(maxRecordsPerPoll)
            .withTopicConfig(topicConfig)
            .build();

        // Create consumer
        try (final KafkaConsumer<String, String> consumer = kafkaConsumerFactory.createConsumerAndSubscribe(clientConfig)) {
            // Attempt to consume, should pull first 10
            ConsumerRecords<String, String> records = consumer.poll(2000L);
            assertEquals("Should have found " + maxRecordsPerPoll + " records", maxRecordsPerPoll, records.count());

            // Attempt to consume, should come up empty
            records = consumer.poll(2000L);
            assertTrue("Should be empty", records.isEmpty());
        }
    }
}