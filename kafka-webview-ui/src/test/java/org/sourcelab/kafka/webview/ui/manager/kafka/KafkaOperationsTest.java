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
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClientConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClusterConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.DeserializerConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.FilterConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.BrokerConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.ConfigItem;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.ConsumerGroupDetails;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.ConsumerGroupIdentifier;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.ConsumerGroupOffsets;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.CreateTopic;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.NodeDetails;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.NodeList;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.PartitionDetails;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.PartitionOffset;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.TopicConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.TopicDetails;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.TopicList;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.TopicListing;
import org.sourcelab.kafka.webview.ui.manager.socket.StartingPosition;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class KafkaOperationsTest {
    private static final Logger logger = LoggerFactory.getLogger(KafkaOperationsTest.class);

    @ClassRule
    public static SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

    private final static KafkaAdminFactory kafkaAdminFactory = new KafkaAdminFactory("./uploads");
    private static KafkaOperations kafkaOperations = null;

    @Before
    public void setup() {
        if (kafkaOperations != null) {
            return;
        }
        // Setup client once.

        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(sharedKafkaTestResource.getKafkaConnectString())
            .build();
        final String clientId = "BobsYerAunty";

        // Create one operations client
        kafkaOperations = new KafkaOperations(
            kafkaAdminFactory.create(clusterConfig, clientId),
            kafkaAdminFactory.createConsumer(clusterConfig, clientId)
        );
    }

    @AfterClass
    public static void shutdown() {
        if (kafkaOperations == null) {
            return;
        }
        kafkaOperations.close();
    }

    /**
     * Test getting topic listing.
     */
    @Test
    public void testGetAvailableTopics() {
        final String topic1 = "MyTopic1" + System.currentTimeMillis();
        final String topic2 = "MyTopic2" + System.currentTimeMillis();

        // Create two topics
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(topic1, 2, (short) 1);

        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(topic2, 1, (short) 1);


        // List topics
        final TopicList topics = kafkaOperations.getAvailableTopics();

        // Should have our two topics
        assertTrue("Should have topic1", topics.getTopicNames().contains(topic1));
        assertTrue("Should have topic2", topics.getTopicNames().contains(topic2));

        boolean foundTopic1 = false;
        boolean foundTopic2 = false;
        for (final TopicListing topicList: topics.getTopics()) {
            if (topicList.getName().equals(topic1)) {
                assertFalse("Haven't seen topic1 yet", foundTopic1);
                assertFalse("Shouldn't be an internal topic", topicList.isInternal());
                foundTopic1 = true;
            } else if (topicList.getName().equals(topic2)) {
                assertFalse("Haven't seen topic2 yet", foundTopic2);
                assertFalse("Shouldn't be an internal topic", topicList.isInternal());
                foundTopic2 = true;
            }
        }
        assertTrue("Found topic1", foundTopic1);
        assertTrue("Found topic1", foundTopic2);
    }

    /**
     * Test retrieving cluster node details.
     */
    @Test
    public void testGetClusterNodes() {
        final String[] brokerHostBits = sharedKafkaTestResource.getKafkaConnectString().split(":");
        final String brokerHost = brokerHostBits[0];
        final int brokerPort = Integer.valueOf(brokerHostBits[1]);

        final NodeList nodeList = kafkaOperations.getClusterNodes();
        logger.info("{}", nodeList);
        assertEquals("Should have single node", 1, nodeList.getNodes().size());

        final NodeDetails node = nodeList.getNodes().get(0);
        validateNode(node, 1, brokerHost, brokerPort);
    }

    /**
     * Test retrieving topic details.
     */
    @Test
    public void testGetTopicDetails() {
        final String[] brokerHostBits = sharedKafkaTestResource.getKafkaConnectString().split(":");
        final String brokerHost = brokerHostBits[0];
        final int brokerPort = Integer.valueOf(brokerHostBits[1]);

        final String topic1 = "MyTopic1" + System.currentTimeMillis();
        final String topic2 = "MyTopic2" + System.currentTimeMillis();

        // Create two topics
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(topic1, 2, (short) 1);

        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(topic2, 1, (short) 1);

        final TopicDetails topicDetails = kafkaOperations.getTopicDetails(topic1);

        assertEquals("Has correct name", topic1, topicDetails.getName());
        assertEquals("Is not internal", false, topicDetails.isInternal());
        assertEquals("Has 2 partitions", 2, topicDetails.getPartitions().size());

        final PartitionDetails partition0 = topicDetails.getPartitions().get(0);
        assertEquals("Has correct topic name", topic1, partition0.getTopic());
        assertEquals("Has correct partition", 0, partition0.getPartition());
        assertEquals("Has one replica", 1, partition0.getReplicas().size());
        assertEquals("Has one isr", 1, partition0.getIsr().size());
        assertNotNull("Has a leader", partition0.getLeader());

        // Validate leader, isr, replica
        validateNode(partition0.getLeader(), 1, brokerHost, brokerPort);
        validateNode(partition0.getIsr().get(0), 1, brokerHost, brokerPort);
        validateNode(partition0.getReplicas().get(0), 1, brokerHost, brokerPort);

        final PartitionDetails partition1 = topicDetails.getPartitions().get(1);
        assertEquals("Has correct topic name", topic1, partition1.getTopic());
        assertEquals("Has correct partition", 1, partition1.getPartition());
        assertEquals("Has one replica", 1, partition1.getReplicas().size());
        assertEquals("Has one isr", 1, partition1.getIsr().size());
        assertNotNull("Has a leader", partition1.getLeader());

        // Validate leader, isr, replica
        validateNode(partition1.getLeader(), 1, brokerHost, brokerPort);
        validateNode(partition1.getIsr().get(0), 1, brokerHost, brokerPort);
        validateNode(partition1.getReplicas().get(0), 1, brokerHost, brokerPort);

    }

    /**
     * Test retrieving configuration values for a topic
     */
    @Test
    public void testGetTopicConfig() {
        final String topic1 = "MyTopic1" + System.currentTimeMillis();
        final String topic2 = "MyTopic2" + System.currentTimeMillis();

        // Create two topics
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(topic1, 2, (short) 1);

        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(topic2, 1, (short) 1);

        final TopicConfig topicConfig = kafkaOperations.getTopicConfig(topic1);

        assertNotNull("non-null response", topicConfig);
        assertFalse("Should not be empty", topicConfig.getConfigEntries().isEmpty());

        for (final ConfigItem configItem: topicConfig.getConfigEntries()) {
            assertNotNull(configItem.getName());
        }
    }

    /**
     * Test retrieving configuration values for a broker
     */
    @Test
    public void testGetBrokerConfig() {
        final String brokerId = "1";

        final BrokerConfig brokerConfig = kafkaOperations.getBrokerConfig(brokerId);

        assertNotNull("non-null response", brokerConfig);
        assertFalse("Should not be empty", brokerConfig.getConfigEntries().isEmpty());

        for (final ConfigItem configItem: brokerConfig.getConfigEntries()) {
            assertNotNull(configItem.getName());
        }
    }

    /**
     * Test creating a new topic.
     */
    @Test
    public void testCreateTopic() {
        final String newTopic = "TestTopic-" + System.currentTimeMillis();

        // Sanity test to validate our topic doesn't exist
        TopicList topicsList = kafkaOperations.getAvailableTopics();
        assertFalse("Should not contain our topic yet", topicsList.getTopicNames().contains(newTopic));

        // Create our topic
        final boolean result = kafkaOperations.createTopic(new CreateTopic(newTopic, 1, (short) 1));
        assertTrue("Should have true return result", result);

        // Validate topic exists now.
        topicsList = kafkaOperations.getAvailableTopics();
        assertTrue("Should contain our topic now", topicsList.getTopicNames().contains(newTopic));
    }

    /**
     * Test altering the configuration of a topic.
     */
    @Test
    public void testModifyingATopic() {
        final String topicName = "TestTopic-" + System.currentTimeMillis();

        // Define the values we want to modify
        final String configName1 = "cleanup.policy";
        final String newConfigValue1 = "compact";

        final String configName2 = "max.message.bytes";
        final String newConfigValue2 = "1024";


        // Create our topic
        final boolean result = kafkaOperations.createTopic(new CreateTopic(topicName, 1, (short) 1));
        assertTrue("Should have true return result", result);

        // Validate topic exists now.
        final TopicList topicsList = kafkaOperations.getAvailableTopics();
        assertTrue("Should contain our topic now", topicsList.getTopicNames().contains(topicName));

        // Get configuration for topic
        TopicConfig topicConfig = kafkaOperations.getTopicConfig(topicName);

        // Sanity test, these keys should exist and be set to default values.
        assertTrue("Should be set to default", topicConfig.getConfigItemByName(configName1).get().isDefault());
        assertTrue("Should be set to default", topicConfig.getConfigItemByName(configName2).get().isDefault());

        // Now lets modify them
        final Map<String, String> alteredConfigs = new HashMap<>();
        alteredConfigs.put(configName1, newConfigValue1);
        alteredConfigs.put(configName2, newConfigValue2);

        // Alter them and get back modified topicConfig
        topicConfig = kafkaOperations.alterTopicConfig(topicName, alteredConfigs);

        // Validate our entries were modified
        assertNotNull(topicConfig);

        // Validation, these keys should exist and be set to new values.
        ConfigItem configItem = topicConfig.getConfigItemByName(configName1).get();
        assertFalse("Should no longer be default", configItem.isDefault());
        assertEquals("Should have our value", newConfigValue1, configItem.getValue());

        configItem = topicConfig.getConfigItemByName(configName2).get();
        assertFalse("Should no longer be default", configItem.isDefault());
        assertEquals("Should have our value", newConfigValue2, configItem.getValue());
    }

    /**
     * Test listing consumer group ids.
     */
    @Test
    public void testListConsumers() {
        // First need to create a topic.
        final String topicName = "AnotherTestTopic-" + System.currentTimeMillis();

        // Create topic
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(topicName, 1, (short) 1);

        // Publish data into the topic
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .produceRecords(10, topicName, 0);

        final String consumerId1 = "ConsumerA";
        final String consumerId2 = "ConsumerB";
        final String consumerPrefix = "TestConsumer";

        consumeFromTopicAndClose(topicName, consumerId1, consumerPrefix);
        consumeFromTopicAndClose(topicName, consumerId2, consumerPrefix);


        // Ask for list of consumers.
        final List<ConsumerGroupIdentifier> consumerIds = kafkaOperations.listConsumers();

        // We should have two
        assertTrue("Should have at least 2 consumers listed", consumerIds.size() >= 2);

        // Results may contain other consumer groups, look for ours.
        boolean foundGroup1 = false;
        boolean foundGroup2 = false;
        for (final ConsumerGroupIdentifier foundConsumerGroupIds : consumerIds) {
            if (foundConsumerGroupIds.getId().equals(consumerPrefix + "-" + consumerId1)) {
                foundGroup1 = true;
            } else if (foundConsumerGroupIds.getId().equals(consumerPrefix + "-" + consumerId2)) {
                foundGroup2 = true;
            }
        }
        assertTrue("Found consumer group 1", foundGroup1);
        assertTrue("Found consumer group 2", foundGroup2);
    }

    /**
     * Test remove a consumer group id.
     */
    @Test
    public void testRemoveConsumer() {
        // First need to create a topic.
        final String topicName = "AnotherTestTopic-" + System.currentTimeMillis();

        // Create topic
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(topicName, 1, (short) 1);

        // Publish data into the topic
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .produceRecords(10, topicName, 0);

        final String consumerId1 = "ConsumerA";
        final String consumerId2 = "ConsumerB";
        final String consumerPrefix = "TestConsumer";

        consumeFromTopicAndClose(topicName, consumerId1, consumerPrefix);
        consumeFromTopicAndClose(topicName, consumerId2, consumerPrefix);

        // Ask for list of consumers.
        List<ConsumerGroupIdentifier> consumerIds = kafkaOperations.listConsumers();

        // We should have two
        assertTrue("Should have at least 2 consumers listed", consumerIds.size() >= 2);

        // Results may contain other consumer groups, look for ours.
        boolean foundGroup1 = false;
        boolean foundGroup2 = false;
        for (final ConsumerGroupIdentifier foundConsumerGroupIds : consumerIds) {
            if (foundConsumerGroupIds.getId().equals(consumerPrefix + "-" + consumerId1)) {
                foundGroup1 = true;
            } else if (foundConsumerGroupIds.getId().equals(consumerPrefix + "-" + consumerId2)) {
                foundGroup2 = true;
            }
        }
        assertTrue("Found consumer group 1", foundGroup1);
        assertTrue("Found consumer group 2", foundGroup2);

        // Now attempt to remove consumer2
        final boolean result = kafkaOperations.removeConsumerGroup(consumerPrefix + "-" + consumerId2);
        assertTrue("Should have returned true.", result);

        // Verify only one consumer group remains
        consumerIds = kafkaOperations.listConsumers();

        // We should have consumer id 1, but not consumer id 2
        assertTrue("Should have atleast 1 consumers listed", consumerIds.size() >= 1);

        // Results may contain other consumer groups, look for ours.
        foundGroup1 = false;
        foundGroup2 = false;
        for (final ConsumerGroupIdentifier foundConsumerGroupIds : consumerIds) {
            if (foundConsumerGroupIds.getId().equals(consumerPrefix + "-" + consumerId1)) {
                foundGroup1 = true;
            } else if (foundConsumerGroupIds.getId().equals(consumerPrefix + "-" + consumerId2)) {
                foundGroup2 = true;
            }
        }
        assertTrue("Found consumer group 1", foundGroup1);
        assertFalse("Should not have found consumer group 2", foundGroup2);
    }

    /**
     * Test getting details about a consumer.
     */
    @Test
    public void testGetConsumerGroupDetails() {
        // First need to create a topic.
        final String topicName = "AnotherTestTopic-" + System.currentTimeMillis();

        // Create topic
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(topicName, 1, (short) 1);

        // Publish data into the topic
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .produceRecords(10, topicName, 0);

        final String consumerId1 = "ConsumerA-" + System.currentTimeMillis();
        final String consumerPrefix = "TestConsumer";
        final String finalConsumerId = consumerPrefix + "-" + consumerId1;

        // Create consumer, consume from topic, keep alive.
        try (final KafkaConsumer consumer = consumeFromTopic(topicName, consumerId1, consumerPrefix)) {

            // Ask for list of consumers.
            final ConsumerGroupDetails consumerGroupDetails = kafkaOperations.getConsumerGroupDetails(finalConsumerId);

            // We should have one
            assertNotNull(consumerGroupDetails);

            // Validate bits
            assertEquals(finalConsumerId, consumerGroupDetails.getConsumerId());
            assertFalse(consumerGroupDetails.isSimple());
            assertEquals("range", consumerGroupDetails.getPartitionAssignor());
            assertEquals("Stable", consumerGroupDetails.getState());
            assertEquals(1, consumerGroupDetails.getMembers().size());

            final ConsumerGroupDetails.Member memberDetails = consumerGroupDetails.getMembers().get(0);
            assertNotNull(memberDetails);
            assertEquals("/127.0.0.1", memberDetails.getHost());
            assertEquals(finalConsumerId, memberDetails.getClientId());
            assertTrue(memberDetails.getMemberId().startsWith(finalConsumerId));
        }
    }

    /**
     * Test getting details about a consumer.
     */
    @Test
    public void testGetConsumerGroupOffsets() {
        // First need to create a topic.
        final String topicName = "AnotherTestTopic-" + System.currentTimeMillis();

        // Create topic
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .createTopic(topicName, 2, (short) 1);

        // Publish data into the topic
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .produceRecords(10, topicName, 0);
        sharedKafkaTestResource
            .getKafkaTestUtils()
            .produceRecords(10, topicName, 1);

        final String consumerId1 = "ConsumerA-" + System.currentTimeMillis();
        final String consumerPrefix = "TestConsumer";
        final String finalConsumerId = consumerPrefix + "-" + consumerId1;

        // Create consumer, consume from topic, keep alive.
        try (final KafkaConsumer consumer = consumeFromTopic(topicName, consumerId1, consumerPrefix)) {

            // Ask for list of offsets.
            final ConsumerGroupOffsets consumerGroupOffsets = kafkaOperations.getConsumerGroupOffsets(finalConsumerId);

            // We should have one
            assertNotNull(consumerGroupOffsets);

            // Validate bits
            assertEquals(topicName, consumerGroupOffsets.getTopic());
            assertEquals(finalConsumerId, consumerGroupOffsets.getConsumerId());
            assertEquals(2, consumerGroupOffsets.getOffsets().size());
            assertEquals(10, consumerGroupOffsets.getOffsetForPartition(0));
            assertEquals(10, consumerGroupOffsets.getOffsetForPartition(1));

            final PartitionOffset offsetsPartition0 = consumerGroupOffsets.getOffsets().get(0);
            assertNotNull(offsetsPartition0);
            assertEquals(0, offsetsPartition0.getPartition());
            assertEquals(10, offsetsPartition0.getOffset());

            final PartitionOffset offsetsPartition1 = consumerGroupOffsets.getOffsets().get(1);
            assertNotNull(offsetsPartition1);
            assertEquals(1, offsetsPartition1.getPartition());
            assertEquals(10, offsetsPartition1.getOffset());
        }
    }

    /**
     * Utility method for validating NodeDetails record.
     */
    private void validateNode(final NodeDetails node, final int expectedId, final String expectedHost, final int expectedPort) {
        assertEquals("Incorrect id", expectedId, node.getId());
        assertEquals("Should have hostname", expectedHost, node.getHost());
        assertEquals("Should have port", expectedPort, node.getPort());
    }

    /**
     * Helper method to consumer records from a topic.
     *  @param topic topic to consume from.
     * @param consumerId Consumer's consumerId
     * @param consumerPrefix Any consumer Id prefix.
     */
    private KafkaConsumer<String, String> consumeFromTopic(final String topic, final String consumerId, final String consumerPrefix) {
        // Create cluster config.
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(sharedKafkaTestResource.getKafkaConnectString())
            .build();

        // Create Deserializer Config
        final DeserializerConfig deserializerConfig = DeserializerConfig.newBuilder()
            .withKeyDeserializerClass(KafkaConsumerFactoryTest.TestDeserializer.class)
            .withKeyDeserializerOption("key.option", "key.value")
            .withKeyDeserializerOption("key.option2", "key.value2")

            // Attempt to override a real setting, it should get filtered
            .withKeyDeserializerOption(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "MadeUpValue")

            .withValueDeserializerClass(KafkaConsumerFactoryTest.TestDeserializer.class)
            .withValueDeserializerOption("value.option", "value.value")
            .withValueDeserializerOption("value.option2", "value.value2")

            // Attempt to override a real setting, it should get filtered
            .withValueDeserializerOption(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "MadeUpValue")
            .build();

        // Create Topic Config
        final org.sourcelab.kafka.webview.ui.manager.kafka.config.TopicConfig topicConfig = new org.sourcelab.kafka.webview.ui.manager.kafka.config.TopicConfig(clusterConfig, deserializerConfig, topic);

        // Create FilterConfig
        final FilterConfig filterConfig = FilterConfig.withNoFilters();

        // Create ClientConfig, instructing to start from tail.
        final ClientConfig clientConfig = ClientConfig.newBuilder()
            .withConsumerId(consumerId)
            .withFilterConfig(filterConfig)
            .withAllPartitions()
            .withStartingPosition(StartingPosition.newHeadPosition())
            .withMaxResultsPerPartition(100)
            .withTopicConfig(topicConfig)
            .build();

        // Create consumer and consume the entries, storing state in Kafka.
        final KafkaConsumerFactory kafkaConsumerFactory = new KafkaConsumerFactory("not/used", consumerPrefix);
        final KafkaConsumer<String, String> consumer = kafkaConsumerFactory.createConsumerAndSubscribe(clientConfig);

        // "Subscribe" to topic.
        consumer.unsubscribe();
        consumer.subscribe(Collections.singletonList(topicConfig.getTopicName()));


        // consume and commit offsets.
        // Wait for assignment to complete.
        for (int attempts = 0; attempts < 10; attempts++) {
            consumer.poll(Duration.ofMillis(1000L));
            final Set<TopicPartition> assignmentSet = consumer.assignment();
            if (!assignmentSet.isEmpty()) {
                break;
            }
        }

        // Commit offsets.
        consumer.commitSync();

        return consumer;
    }

    /**
     * Helper method to consumer records from a topic.
     *
     * @param topic topic to consume from.
     * @param consumerId Consumer's consumerId
     * @param consumerPrefix Any consumer Id prefix.
     */
    private void consumeFromTopicAndClose(final String topic, final String consumerId, final String consumerPrefix) {
        final KafkaConsumer consumer = consumeFromTopic(topic, consumerId, consumerPrefix);
        consumer.close();
    }
}