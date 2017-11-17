package org.sourcelab.kafka.webview.ui.manager.kafka;

import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClusterConfig;
import com.salesforce.kafka.test.junit.SharedKafkaTestResource;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.Node;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class KafkaAdminFactoryTest {

    @ClassRule
    public static SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

    /**
     * Test that KafkaAdminFactory can create a working AdminClient when connecting to a non-ssl cluster.
     */
    @Test
    public void testCreateNonSslAdminClient() throws ExecutionException, InterruptedException {
        // Create Cluster config
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(sharedKafkaTestResource.getKafkaConnectString())
            .build();

        final KafkaAdminFactory kafkaAdminFactory = new KafkaAdminFactory("NotUsed");

        // Create instance
        try (final AdminClient adminClient = kafkaAdminFactory.create(clusterConfig, "MyClientId")) {

            // Call method to validate things work as expected
            final DescribeClusterResult results = adminClient.describeCluster();
            assertNotNull("Should have a non-null result", results);

            // Request future result
            final Collection<Node> nodes = results.nodes().get();
            assertNotNull("Should have non-null node result", nodes);
            assertFalse("Should have non-empty node", nodes.isEmpty());
        }
    }
}