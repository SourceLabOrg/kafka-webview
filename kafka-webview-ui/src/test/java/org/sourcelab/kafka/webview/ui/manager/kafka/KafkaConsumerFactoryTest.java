package org.sourcelab.kafka.webview.ui.manager.kafka;

import com.salesforce.kafka.test.junit.SharedKafkaTestResource;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.ClassRule;
import org.junit.Test;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClientConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClusterConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.DeserializerConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.FilterConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.RecordFilterDefinition;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.TopicConfig;
import org.sourcelab.kafka.webview.ui.plugin.filter.RecordFilter;

import java.util.HashMap;
import java.util.Map;

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

            for (final ConsumerRecord<String, String> record: records) {
                assertEquals("Should be from parittion 1 only", 1, record.partition());
            }

            // Attempt to consume, should come up empty
            records = consumer.poll(2000L);
            assertTrue("Should be empty", records.isEmpty());
        }
    }

    /**
     * Simple Smoke Test, using RecordFilter.
     */
    @Test
    public void testBasicConsumerWithRecordFilter() {
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

        // Create FilterConfig
        final FilterConfig filterConfig = FilterConfig.withFilters(
            new RecordFilterDefinition(new PartitionFilter(), new HashMap<>())
        );

        // Create ClientConfig
        final ClientConfig clientConfig = ClientConfig.newBuilder()
            .withConsumerId("MyConsumerId")
            .withFilterConfig(filterConfig)
            .withAllPartitions()
            .withMaxResultsPerPartition(10000)
            .withTopicConfig(topicConfig)
            .build();

        // Create consumer
        try (final KafkaConsumer<String, String> consumer = kafkaConsumerFactory.createConsumerAndSubscribe(clientConfig)) {
            // Attempt to consume, should pull first 10
            ConsumerRecords<String, String> records = consumer.poll(2000L);
            assertEquals("Should have found " + maxRecordsPerPoll + " records", maxRecordsPerPoll, records.count());

            for (final ConsumerRecord<String, String> record: records) {
                assertEquals("Should be from parittion 1 only", 1, record.partition());
            }

            // Attempt to consume, should come up empty
            records = consumer.poll(2000L);
            assertTrue("Should be empty", records.isEmpty());
        }
    }

    /**
     * Test Implementation that filters everything from partition 0.
     */
    public static class PartitionFilter implements RecordFilter {

        @Override
        public void configure(final Map<String, ?> consumerConfigs, final Map<String, String> filterOptions) {

        }

        @Override
        public boolean filter(final String topic, final int partition, final long offset, final Object key, final Object value) {
            // Filter partition 0 records
            if (partition == 0) {
                return false;
            }
            return true;
        }

        @Override
        public void close() {

        }
    }
}