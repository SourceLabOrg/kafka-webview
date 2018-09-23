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

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigsResult;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.BrokerConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.ConfigItem;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.CreateTopic;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.NodeDetails;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.NodeList;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.PartitionDetails;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.TopicConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.TopicDetails;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.TopicList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Wrapper for AdminClient.  For doing various 'operational' type commands
 * against a cluster, or retrieving metadata about a cluster.
 */
public class KafkaOperations implements AutoCloseable {
    private final AdminClient adminClient;

    /**
     * Constructor.
     * @param adminClient The AdminClient to wrap.
     */
    public KafkaOperations(final AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    /**
     * Retrieve all available topics within cluster.
     */
    public TopicList getAvailableTopics() {
        final List<org.sourcelab.kafka.webview.ui.manager.kafka.dto.TopicListing> topicListings = new ArrayList<>();

        try {
            final Collection<TopicListing> results = adminClient.listTopics().listings().get();
            for (final TopicListing entry: results) {
                topicListings.add(
                    new org.sourcelab.kafka.webview.ui.manager.kafka.dto.TopicListing(entry.name(), entry.isInternal())
                );
            }
            return new TopicList(topicListings);
        } catch (InterruptedException | ExecutionException e) {
            // TODO Handle
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Get information about brokers within the cluster.
     */
    public NodeList getClusterNodes() {
        final List<NodeDetails> nodeDetails = new ArrayList<>();

        try {
            final Collection<Node> nodes = adminClient.describeCluster().nodes().get();
            for (final Node node: nodes) {
                nodeDetails.add(new NodeDetails(node.id(), node.host(), node.port(), node.rack()));
            }
            return new NodeList(nodeDetails);
        } catch (InterruptedException | ExecutionException e) {
            // TODO Handle
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Get information about several topics.
     */
    public Map<String, TopicDetails> getTopicDetails(final Collection<String> topics) {
        final Map<String, TopicDetails> results = new HashMap<>();

        try {
            final Map<String, TopicDescription> topicDescriptionMap = adminClient.describeTopics(topics).all().get();

            // Now parse results
            for (final Map.Entry<String, TopicDescription> entry: topicDescriptionMap.entrySet()) {
                final String topic = entry.getKey();
                final TopicDescription topicDescription = entry.getValue();

                final List<PartitionDetails> partitionDetails = new ArrayList<>();

                for (final TopicPartitionInfo partitionInfo: topicDescription.partitions()) {
                    final List<NodeDetails> isrNodes = new ArrayList<>();
                    final List<NodeDetails> replicaNodes = new ArrayList<>();

                    // Translate Leader
                    final Node partitionLeader = partitionInfo.leader();
                    final NodeDetails leaderNode = new NodeDetails(
                        partitionLeader.id(), partitionLeader.host(), partitionLeader.port(), partitionLeader.rack()
                    );

                    // Translate ISR nodes
                    for (final Node node: partitionInfo.isr()) {
                        isrNodes.add(
                            new NodeDetails(node.id(), node.host(), node.port(), node.rack())
                        );
                    }

                    // Translate Replicas nodes
                    for (final Node node: partitionInfo.replicas()) {
                        replicaNodes.add(
                            new NodeDetails(node.id(), node.host(), node.port(), node.rack())
                        );
                    }

                    // Create the details
                    final PartitionDetails partitionDetail = new PartitionDetails(
                        topicDescription.name(),
                        partitionInfo.partition(),
                        leaderNode,
                        replicaNodes,
                        isrNodes
                    );

                    // Add to the list.
                    partitionDetails.add(partitionDetail);

                    // Create new TopicDetails.
                    final TopicDetails topicDetails = new TopicDetails(
                        topicDescription.name(),
                        topicDescription.isInternal(),
                        partitionDetails
                    );
                    results.put(topic, topicDetails);
                }
            }
            // Return it
            return results;
        } catch (final InterruptedException | ExecutionException exception) {
            // TODO Handle this
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    /**
     * Get information about a specific topic.
     */
    public TopicDetails getTopicDetails(final String topic) {
        final Map<String, TopicDetails> results = getTopicDetails(Collections.singleton(topic));
        return results.get(topic);
    }

    /**
     * Get the configuration for topic.
     */
    public TopicConfig getTopicConfig(final String topic) {
        final ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, topic);
        return new TopicConfig(describeResource(configResource));
    }

    /**
     * Get the configuration for topic.
     */
    public BrokerConfig getBrokerConfig(final String brokerId) {
        final ConfigResource configResource = new ConfigResource(ConfigResource.Type.BROKER, brokerId);
        return new BrokerConfig(describeResource(configResource));
    }

    /**
     * Create a new topic on the cluster.
     * @param createTopic defines the new topic
     * @return boolean, true if success.
     */
    public boolean createTopic(final CreateTopic createTopic) {
        final NewTopic newTopic = new NewTopic(
            createTopic.getName(),
            createTopic.getNumberOfPartitions(),
            createTopic.getReplicaFactor()
        );

        try {
            // Create the topic
            final CreateTopicsResult result = adminClient.createTopics(Collections.singleton(newTopic));

            // Wait for the async request to process.
            result.all().get();

            // return true?
            return true;
        } catch (final InterruptedException | ExecutionException exception) {
            // TODO Handle this
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    /**
     * Modify configuration values for a specific topic.
     * @param topic The topic to modify.
     * @param configItems Map of Key => Value to modify.
     * @return boolean
     */
    public TopicConfig alterTopicConfig(final String topic, final Map<String, String> configItems) {
        try {
            // Define the resource we want to modify, the topic.
            final ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, topic);

            final List<ConfigEntry> configEntries = new ArrayList<>();
            for (final Map.Entry<String, String> entry : configItems.entrySet()) {
                configEntries.add(
                    new ConfigEntry(entry.getKey(), entry.getValue())
                );
            }

            // Define the configuration set
            final Config config = new Config(configEntries);

            // Create the topic
            final AlterConfigsResult result = adminClient.alterConfigs(Collections.singletonMap(configResource, config));

            // Wait for the async request to process.
            result.all().get();

            // Lets return updated topic details
            return getTopicConfig(topic);
        } catch (final InterruptedException | ExecutionException exception) {
            // TODO Handle this
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    private List<ConfigItem> describeResource(final ConfigResource configResource) {
        final DescribeConfigsResult result = adminClient.describeConfigs(Collections.singleton(configResource));

        final List<ConfigItem> configItems = new ArrayList<>();

        try {
            final Map<ConfigResource, Config> configMap = result.all().get();

            final Config config = configMap.get(configResource);
            for (final ConfigEntry configEntry : config.entries()) {
                // Skip sensitive entries
                if (configEntry.isSensitive()) {
                    continue;
                }
                configItems.add(
                    new ConfigItem(configEntry.name(), configEntry.value(), configEntry.isDefault())
                );
            }
            return configItems;
        } catch (InterruptedException | ExecutionException e) {
            // TODO Handle this
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Close out the Client.
     */
    public void close() {
        adminClient.close();
    }
}
