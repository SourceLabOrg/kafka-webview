package com.darksci.kafkaview.manager.kafka;

import com.darksci.kafkaview.manager.kafka.dto.NodeDetails;
import com.darksci.kafkaview.manager.kafka.dto.NodeList;
import com.darksci.kafkaview.manager.kafka.dto.PartitionDetails;
import com.darksci.kafkaview.manager.kafka.dto.TopicDetails;
import com.darksci.kafkaview.manager.kafka.dto.TopicList;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
                nodeDetails.add(new NodeDetails(node.id(), node.host(), node.port()));
            }
            return new NodeList(nodeDetails);
        } catch (InterruptedException | ExecutionException e) {
            // TODO Handle
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Get information about a specific topic.
     */
    public TopicDetails getTopicDetails(final String topic) {
        try {
            final Map<String, TopicDescription> topicDescriptionMap = adminClient.describeTopics(Collections.singleton(topic)).all().get();
            final TopicDescription topicDescription = topicDescriptionMap.get(topic);

            List<PartitionDetails> partitionDetails = new ArrayList<>();

            for (final TopicPartitionInfo partitionInfo: topicDescription.partitions()) {
                final List<NodeDetails> isrNodes = new ArrayList<>();
                final List<NodeDetails> replicaNodes = new ArrayList<>();

                // Translate Leader
                final NodeDetails leaderNode = new NodeDetails(
                    partitionInfo.leader().id(), partitionInfo.leader().host(), partitionInfo.leader().port()
                );

                // Translate ISR nodes
                for (final Node node: partitionInfo.isr()) {
                    isrNodes.add(
                        new NodeDetails(node.id(), node.host(), node.port())
                    );
                }

                // Translate Replicas nodes
                for (final Node node: partitionInfo.replicas()) {
                    replicaNodes.add(
                        new NodeDetails(node.id(), node.host(), node.port())
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
            }

            // Return it
            return new TopicDetails(
                topicDescription.name(),
                topicDescription.isInternal(),
                partitionDetails
            );

        } catch (InterruptedException | ExecutionException e) {
            // TODO Handle this
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void close() {
        adminClient.close();
    }
}
