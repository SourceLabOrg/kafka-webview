/**
 * MIT License
 *
 * Copyright (c) 2017, 2018 SourceLab.org (https://github.com/Crim/kafka-webview/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.sourcelab.kafka.webview.ui.manager.kafka;

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.ClassRule;
import org.junit.Test;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClientConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClusterConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.DeserializerConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.FilterConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.RecordFilterDefinition;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.TopicConfig;
import org.sourcelab.kafka.webview.ui.manager.socket.StartingPosition;
import org.sourcelab.kafka.webview.ui.plugin.filter.RecordFilter;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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
        final KafkaConsumerFactory kafkaConsumerFactory = new KafkaConsumerFactory("not/used", "TestPrefix");

        // Create cluster Config
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(sharedKafkaTestResource.getKafkaConnectString())
            .build();

        // Create Deserializer Config
        final DeserializerConfig deserializerConfig = DeserializerConfig.newBuilder()
            .withKeyDeserializerClass(StringDeserializer.class)
            .withValueDeserializerClass(StringDeserializer.class)
            .build();

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
        final KafkaConsumerFactory kafkaConsumerFactory = new KafkaConsumerFactory("not/used", "TestPrefix");

        // Create cluster Config
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(sharedKafkaTestResource.getKafkaConnectString())
            .build();

        // Create Deserializer Config
        final DeserializerConfig deserializerConfig = DeserializerConfig.newBuilder()
            .withKeyDeserializerClass(StringDeserializer.class)
            .withValueDeserializerClass(StringDeserializer.class)
            .build();

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
     * Simple Smoke Test, using RecordFilter to filter everything from partition 0.
     */
    @Test
    public void testBasicConsumerWithRecordFilter() throws InterruptedException {
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
        final KafkaConsumerFactory kafkaConsumerFactory = new KafkaConsumerFactory("not/used", "TestPrefix");

        // Create cluster Config
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(sharedKafkaTestResource.getKafkaConnectString())
            .build();

        // Create Deserializer Config
        final DeserializerConfig deserializerConfig = DeserializerConfig.newBuilder()
            .withKeyDeserializerClass(StringDeserializer.class)
            .withValueDeserializerClass(StringDeserializer.class)
            .build();

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
            .withMaxResultsPerPartition(maxRecordsPerPoll * 100)
            .withTopicConfig(topicConfig)
            .build();

        // Create consumer
        try (final KafkaConsumer<String, String> consumer = kafkaConsumerFactory.createConsumerAndSubscribe(clientConfig)) {
            // Attempt to consume, should pull first 10
            ConsumerRecords<String, String> records = consumer.poll(10000L);
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
     * Produce messages into topic, tell consumer to start from TAIL.
     * We should get no results.
     */
    @Test
    public void testDeserializerOptions() throws InterruptedException {
        // Reset state on our Test Deserializer
        TestDeserializer.reset();

        // Create a topic with 1 partitions, (partitionId 0)
        final String topicName = "TestTopic" + System.currentTimeMillis();
        sharedKafkaTestResource
            .getKafkaTestServer()
            .createTopic(topicName, 1);

        // Create factory
        final KafkaConsumerFactory kafkaConsumerFactory = new KafkaConsumerFactory("not/used", "TestPrefix");

        // Create cluster Config
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(sharedKafkaTestResource.getKafkaConnectString())
            .build();

        // Create Deserializer Config
        final DeserializerConfig deserializerConfig = DeserializerConfig.newBuilder()
            .withKeyDeserializerClass(TestDeserializer.class)
            .withKeyDeserializerOption("key.option", "key.value")
            .withKeyDeserializerOption("key.option2", "key.value2")

            // Attempt to override a real setting, it should get filtered
            .withKeyDeserializerOption(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "MadeUpValue")

            .withValueDeserializerClass(TestDeserializer.class)
            .withValueDeserializerOption("value.option", "value.value")
            .withValueDeserializerOption("value.option2", "value.value2")

            // Attempt to override a real setting, it should get filtered
            .withValueDeserializerOption(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "MadeUpValue")
            .build();

        // Create Topic Config
        final TopicConfig topicConfig = new TopicConfig(clusterConfig, deserializerConfig, topicName);

        // Create FilterConfig
        final FilterConfig filterConfig = FilterConfig.withNoFilters();

        // Create ClientConfig, instructing to start from tail.
        final ClientConfig clientConfig = ClientConfig.newBuilder()
            .withConsumerId("MyConsumerId")
            .withFilterConfig(filterConfig)
            .withAllPartitions()
            .withStartingPosition(StartingPosition.newTailPosition())
            .withMaxResultsPerPartition(100)
            .withTopicConfig(topicConfig)
            .build();

        // Create consumer
        try (final KafkaConsumer<String, String> consumer = kafkaConsumerFactory.createConsumerAndSubscribe(clientConfig)) {
            // We don't actually care to consume anything.  We want to inspect options passed to deserializer.
            final Map<String, Object> passedConfig = TestDeserializer.getConfig();

            // Validate values got passed
            assertEquals("Should have expected value", "key.value", passedConfig.get("key.option"));
            assertEquals("Should have expected value", "key.value2", passedConfig.get("key.option2"));
            assertEquals("Should have expected value", "value.value", passedConfig.get("value.option"));
            assertEquals("Should have expected value", "value.value2", passedConfig.get("value.option2"));

            // Validate didn't overwrite others
            assertNotEquals(
                "Should not have overridden config",
                "MadeUpValue",
                passedConfig.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
            assertNotEquals(
                "Should not have overridden config",
                "MadeUpValue",
                passedConfig.get(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG));
        }

        // Reset state
        TestDeserializer.reset();
    }

    /**
     * Test Implementation that filters everything from partition 0.
     */
    public static class PartitionFilter implements RecordFilter {

        @Override
        public void configure(final Map<String, ?> consumerConfigs, final Map<String, String> filterOptions) {

        }

        @Override
        public boolean includeRecord(final String topic, final int partition, final long offset, final Object key, final Object value) {
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

    /**
     * Test Deserializer instance for capturing configs.
     */
    public static class TestDeserializer implements Deserializer<String> {
        private static Map<String, Object> configs = new HashMap<>();

        @Override
        public void configure(final Map<String, ?> configs, final boolean isKey) {
            // Save reference
            synchronized (TestDeserializer.class) {
                this.configs.putAll(configs);
            }
        }

        @Override
        public String deserialize(final String topic, final byte[] data) {
            return "Dummy";
        }

        @Override
        public void close() {

        }

        public static Map<String, Object> getConfig() {
            synchronized (TestDeserializer.class) {
                return configs;
            }
        }

        public static void reset() {
            synchronized (TestDeserializer.class) {
                configs.clear();
            }
        }
    }
}