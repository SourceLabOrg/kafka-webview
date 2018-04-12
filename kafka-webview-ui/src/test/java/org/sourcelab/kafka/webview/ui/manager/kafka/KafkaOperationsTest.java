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
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClusterConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.BrokerConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.ConfigItem;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.NodeDetails;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.NodeList;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.PartitionDetails;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.TopicConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.TopicDetails;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.TopicList;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.TopicListing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class KafkaOperationsTest {
    private static final Logger logger = LoggerFactory.getLogger(KafkaOperationsTest.class);

    @ClassRule
    public static SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

    private final KafkaAdminFactory kafkaAdminFactory = new KafkaAdminFactory("./uploads");

    /**
     * Test getting topic listing.
     */
    @Test
    public void testGetAvailableTopics() {
        final String topic1 = "MyTopic1" + System.currentTimeMillis();
        final String topic2 = "MyTopic2" + System.currentTimeMillis();

        // Create two topics
        sharedKafkaTestResource
            .getKafkaTestServer()
            .createTopic(topic1, 2);

        sharedKafkaTestResource
            .getKafkaTestServer()
            .createTopic(topic2, 1);

        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(sharedKafkaTestResource.getKafkaConnectString())
            .build();
        final String clientId = "BobsYerAunty";

        // Create operations client.
        try (final KafkaOperations operations = new KafkaOperations(kafkaAdminFactory.create(clusterConfig, clientId))) {
            final TopicList topics = operations.getAvailableTopics();

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
    }

    /**
     * Test retrieving cluster node details.
     */
    @Test
    public void testGetClusterNodes() {
        final String[] brokerHostBits = sharedKafkaTestResource.getKafkaConnectString().split(":");
        final String brokerHost = brokerHostBits[0];
        final int brokerPort = Integer.valueOf(brokerHostBits[1]);

        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(sharedKafkaTestResource.getKafkaConnectString())
            .build();
        final String clientId = "BobsYerAunty";

        // Create operations client.
        try (final KafkaOperations operations = new KafkaOperations(kafkaAdminFactory.create(clusterConfig, clientId))) {
            final NodeList nodeList = operations.getClusterNodes();
            logger.info("{}", nodeList);
            assertEquals("Should have single node", 1, nodeList.getNodes().size());

            final NodeDetails node = nodeList.getNodes().get(0);
            validateNode(node, 1, brokerHost, brokerPort);
        }
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
            .getKafkaTestServer()
            .createTopic(topic1, 2);

        sharedKafkaTestResource
            .getKafkaTestServer()
            .createTopic(topic2, 1);

        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(sharedKafkaTestResource.getKafkaConnectString())
            .build();
        final String clientId = "BobsYerAunty";

        // Create operations client.
        try (final KafkaOperations operations = new KafkaOperations(kafkaAdminFactory.create(clusterConfig, clientId))) {
            final TopicDetails topicDetails = operations.getTopicDetails(topic1);

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
            .getKafkaTestServer()
            .createTopic(topic1, 2);

        sharedKafkaTestResource
            .getKafkaTestServer()
            .createTopic(topic2, 1);

        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(sharedKafkaTestResource.getKafkaConnectString())
            .build();
        final String clientId = "BobsYerAunty";

        // Create operations client.
        try (final KafkaOperations operations = new KafkaOperations(kafkaAdminFactory.create(clusterConfig, clientId))) {
            final TopicConfig topicConfig = operations.getTopicConfig(topic1);

            assertNotNull("non-null response", topicConfig);
            assertFalse("Should not be empty", topicConfig.getConfigEntries().isEmpty());

            for (final ConfigItem configItem: topicConfig.getConfigEntries()) {
                assertNotNull(configItem.getName());
            }
        }
    }

    /**
     * Test retrieving configuration values for a broker
     */
    @Test
    public void testGetBrokerConfig() {
        final String brokerId = "1";

        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(sharedKafkaTestResource.getKafkaConnectString())
            .build();
        final String clientId = "BobsYerAunty";

        // Create operations client.
        try (final KafkaOperations operations = new KafkaOperations(kafkaAdminFactory.create(clusterConfig, clientId))) {
            final BrokerConfig brokerConfig = operations.getBrokerConfig(brokerId);

            assertNotNull("non-null response", brokerConfig);
            assertFalse("Should not be empty", brokerConfig.getConfigEntries().isEmpty());

            for (final ConfigItem configItem: brokerConfig.getConfigEntries()) {
                assertNotNull(configItem.getName());
            }
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
}