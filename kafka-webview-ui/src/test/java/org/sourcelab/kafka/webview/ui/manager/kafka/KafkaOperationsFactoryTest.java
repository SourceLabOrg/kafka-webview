package org.sourcelab.kafka.webview.ui.manager.kafka;

import com.salesforce.kafka.test.junit.SharedKafkaTestResource;
import org.junit.ClassRule;
import org.junit.Test;
import org.sourcelab.kafka.webview.ui.manager.encryption.SecretManager;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.NodeList;
import org.sourcelab.kafka.webview.ui.model.Cluster;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class KafkaOperationsFactoryTest {

    @ClassRule
    public static SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

    /**
     * Test that KafkaAdminFactory can create a working AdminClient when connecting to a non-ssl cluster.
     */
    @Test
    public void smokeTestNonSslOperationsClient() throws ExecutionException, InterruptedException {
        // Create dependencies.
        final SecretManager secretManager = new SecretManager("notused");
        final KafkaAdminFactory kafkaAdminFactory = new KafkaAdminFactory("NotUsed");
        final KafkaOperationsFactory operationsFactory = new KafkaOperationsFactory(secretManager, kafkaAdminFactory);

        // Create cluster model.
        final Cluster cluster = new Cluster();
        cluster.setBrokerHosts(sharedKafkaTestResource.getKafkaConnectString());
        cluster.setSslEnabled(false);

        // Create instance
        try (final KafkaOperations kafkaOperations = operationsFactory.create(cluster, 1L)) {

            // Call method to validate things work as expected
            final NodeList nodeList = kafkaOperations.getClusterNodes();
            assertNotNull("Should have a non-null result", nodeList);
            assertFalse("Should have non-empty node", nodeList.getNodes().isEmpty());
        }
    }
}