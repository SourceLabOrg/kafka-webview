/**
 * MIT License
 *
 * Copyright (c) 2017, 2018, 2019 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

import com.salesforce.kafka.test.KafkaTestUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Test;
import org.sourcelab.kafka.webview.ui.manager.encryption.SecretManager;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClientConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClusterConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.DeserializerConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.TopicConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.NodeList;
import org.sourcelab.kafka.webview.ui.model.Cluster;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * This abstract class contains shared tests to be run against Kafka clusters
 * running various listeners, such as SASL_PLAIN, SASL_SSL, SSL.
 */
public abstract class AbstractKafkaClusterTests {
    private final SecretManager secretManager = new SecretManager("dummy-value");

    /**
     * Return a properly configured ClusterConfig.
     * @return ClusterConfig instance.
     */
    abstract protected ClusterConfig buildClusterConfig();

    /**
     * Return a properly configured KafkaAdminFactory instance.
     */
    abstract protected KafkaAdminFactory buildKafkaAdminFactory();

    /**
     * Return expected protocol Kafka cluster should be listening on.
     */
    abstract protected String getExpectedProtocol();

    /**
     * Properly configured KafkaConsumerFactory instance.
     */
    abstract protected KafkaConsumerFactory buildKafkaConsumerFactory();

    /**
     * Properly configured KafkaTestUtils instance.
     */
    abstract protected KafkaTestUtils getKafkaTestUtils();

    /**
     * Properly configured Cluster instance.
     */
    abstract protected Cluster buildCluster();

    /**
     * Test that KafkaAdminFactory can create a working AdminClient when connecting to an SASL_PLAIN enabled cluster.
     */
    @Test
    public void testAdminClient() throws ExecutionException, InterruptedException {

        // Create Cluster config
        final ClusterConfig clusterConfig = buildClusterConfig();

        // Create a factory
        final KafkaAdminFactory kafkaAdminFactory = buildKafkaAdminFactory();

        // Create instance
        try (final AdminClient adminClient = kafkaAdminFactory.create(clusterConfig, "MyClientId")) {

            // Call method to validate things work as expected
            final DescribeClusterResult results = adminClient.describeCluster();
            assertNotNull("Should have a non-null result", results);

            // Request future result
            final Collection<Node> nodes = results.nodes().get();
            assertNotNull("Should have non-null node result", nodes);
            assertFalse("Should have non-empty node", nodes.isEmpty());

            // Now lets get each node's config and make a best effort to verify they are listening on SASL_PLAINTEXT.
            final Collection<ConfigResource> configResourceList = new ArrayList<>();
            for (final Node nodeDetails : nodes) {
                configResourceList.add(
                    new ConfigResource(ConfigResource.Type.BROKER, nodeDetails.idString())
                );
            }

            final Map<ConfigResource, Config> configsResult = adminClient.describeConfigs(configResourceList).all().get();
            for (final Node nodeDetails : nodes) {
                final Config configResults = configsResult.get(
                    new ConfigResource(ConfigResource.Type.BROKER, nodeDetails.idString())
                );
                assertTrue(
                    "Listeners should contain " + getExpectedProtocol() + "://",
                    configResults.get("listeners").value().contains(getExpectedProtocol() + "://")
                );
            }
        }
    }

    /**
     * Simple Smoke Test consuming records using KafkaConsumerFactory.
     */
    @Test
    public void testBasicConsumeWithSaslAuthentication() {
        final String topicName = "test_topic";
        final int maxRecordsPerPoll = 10;

        // Create a topic with 2 partitions, (partitionId 0, 1)
        getKafkaTestUtils().createTopic(topicName, 2, (short) 1);

        // Produce 10 records into partition 0 of topic.
        getKafkaTestUtils().produceRecords(maxRecordsPerPoll, topicName, 0);

        // Produce 10 records into partition 1 of topic.
        getKafkaTestUtils().produceRecords(maxRecordsPerPoll, topicName, 1);

        // Create cluster Config
        final ClusterConfig clusterConfig = buildClusterConfig();

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
        final KafkaConsumerFactory kafkaConsumerFactory = buildKafkaConsumerFactory();
        try (final KafkaConsumer<String, String> consumer = kafkaConsumerFactory.createConsumerAndSubscribe(clientConfig)) {
            // Attempt to consume, should pull first 10
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));
            assertEquals("Should have 10 records", maxRecordsPerPoll, records.count());

            // Attempt to consume, should pull 2nd 10
            records = consumer.poll(Duration.ofSeconds(2));
            assertEquals("Should have 10 records", maxRecordsPerPoll, records.count());
        }
    }

    /**
     * Test that OperationsFactory works.
     */
    @Test
    public void smokeTestOperationsFactory()  {
        // Create dependencies.
        final KafkaAdminFactory kafkaAdminFactory = buildKafkaAdminFactory();

        final KafkaOperationsFactory operationsFactory
            = new KafkaOperationsFactory(getSecretManager(), kafkaAdminFactory);

        // Create cluster model.
        final Cluster cluster = buildCluster();

        // Create instance
        try (final KafkaOperations kafkaOperations = operationsFactory.create(cluster, 1L)) {
            // Call method to validate things work as expected
            final NodeList nodeList = kafkaOperations.getClusterNodes();
            assertNotNull("Should have a non-null result", nodeList);
            assertFalse("Should have non-empty node", nodeList.getNodes().isEmpty());
        }
    }

    /**
     * Accessor for SecretManager instance.
     * @return secret manager instance.
     */
    SecretManager getSecretManager() {
        return secretManager;
    }
}
