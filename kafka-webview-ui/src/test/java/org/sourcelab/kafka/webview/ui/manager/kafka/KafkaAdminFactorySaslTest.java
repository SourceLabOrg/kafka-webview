package org.sourcelab.kafka.webview.ui.manager.kafka;

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.Node;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClusterConfig;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Tests currently disabled until we can get an embedded SASL enabled cluster setup.
 * Tests currently assume that there's a local Kafka cluster running at 127.0.0.1:9092 with SASL plain.
 */
public class KafkaAdminFactorySaslTest {

    private final String brokerHost = "localhost:9092";
    private final String saslUser = "kafkaclient";
    private final String saslPass = "client-secret";

    /**
     * Setup broker without SSL and WITH SASL support.
     */
    //@ClassRule
    public static SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

    /**
     * Test that KafkaAdminFactory can create a working AdminClient when connecting to a non-ssl SASL cluster.
     */
    //@Test
    public void testCreateSaslAdminClient() throws ExecutionException, InterruptedException {
        // Create Cluster config
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(brokerHost)
            .withUseSasl(true)
            .withSaslPlaintextUsername(saslUser)
            .withSaslPlaintextPassword(saslPass)
            .build();

        final KafkaAdminFactory kafkaAdminFactory = new KafkaAdminFactory(
            new KafkaClientConfigUtil("not/used", "MyPrefix")
        );

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
        //assertFalse("Finish writing this test", true);
    }
}
