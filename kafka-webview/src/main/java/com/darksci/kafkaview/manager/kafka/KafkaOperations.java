package com.darksci.kafkaview.manager.kafka;

import com.darksci.kafkaview.manager.kafka.dto.BrokerConfig;
import com.darksci.kafkaview.manager.kafka.dto.ConfigItem;
import com.darksci.kafkaview.manager.kafka.dto.NodeDetails;
import com.darksci.kafkaview.manager.kafka.dto.NodeList;
import com.darksci.kafkaview.manager.kafka.dto.PartitionDetails;
import com.darksci.kafkaview.manager.kafka.dto.TopicConfig;
import com.darksci.kafkaview.manager.kafka.dto.TopicDetails;
import com.darksci.kafkaview.manager.kafka.dto.TopicList;
import org.apache.kafka.clients.admin.AdminClient;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class KafkaOperations implements AutoCloseable {
    private final static Logger logger = LoggerFactory.getLogger(KafkaOperations.class);
    private final static long TIMEOUT = 5000L;

    private final AdminClient adminClient;

    public KafkaOperations(final AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    /**
     * Retrieve all available topics within cluster.
     */
    public TopicList getAvailableTopics() {
        final List<com.darksci.kafkaview.manager.kafka.dto.TopicListing> topicListings = new ArrayList<>();

        try {
            final Collection<TopicListing> results = adminClient.listTopics().listings().get();
            for (final TopicListing entry: results) {
                topicListings.add(
                    new com.darksci.kafkaview.manager.kafka.dto.TopicListing(entry.name(), entry.isInternal())
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
                    partitionDetails.add(
                        new PartitionDetails(
                            topicDescription.name(),
                            partitionInfo.partition(),
                            leaderNode,
                            replicaNodes,
                            isrNodes
                        )
                    );

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
        } catch (InterruptedException | ExecutionException e) {
            // TODO Handle this
            throw new RuntimeException(e.getMessage(), e);
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

    public void createTopic(final String topic, final int numPartitions, final short replicaFactor) {
        final NewTopic newTopic = new NewTopic(topic, numPartitions, replicaFactor);
        final CreateTopicsResult result = adminClient.createTopics(Collections.singletonList(newTopic));
        try {
            result.all().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        adminClient.close();
    }
}
