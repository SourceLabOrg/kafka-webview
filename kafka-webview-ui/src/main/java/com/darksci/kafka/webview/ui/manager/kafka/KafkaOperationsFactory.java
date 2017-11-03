package com.darksci.kafka.webview.ui.manager.kafka;

import com.darksci.kafka.webview.ui.manager.encryption.SecretManager;
import com.darksci.kafka.webview.ui.manager.kafka.config.ClusterConfig;
import com.darksci.kafka.webview.ui.model.Cluster;
import org.apache.kafka.clients.admin.AdminClient;

/**
 * Factory for creating an AdminClient and wrapping it with KafkaOperations.
 */
public class KafkaOperationsFactory {
    /**
     * Defines the consumerId.
     */
    private static final String consumerIdPrefix = "KafkaWebView-Operation-UserId";

    private final SecretManager secretManager;
    private final KafkaAdminFactory kafkaAdminFactory;

    /**
     * Constructor.
     */
    public KafkaOperationsFactory(final SecretManager secretManager, final KafkaAdminFactory kafkaAdminFactory) {
        this.secretManager = secretManager;
        this.kafkaAdminFactory = kafkaAdminFactory;
    }

    /**
     * Factory method.
     * @param cluster What cluster to connect to.
     * @param userId What userId to associate the connection with.
     * @return KafkaOperations client.
     */
    public KafkaOperations create(final Cluster cluster, final long userId) {
        final String clientId = consumerIdPrefix + userId;

        // Create new Operational Client
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder(cluster, secretManager).build();
        final AdminClient adminClient = kafkaAdminFactory.create(clusterConfig, clientId);

        return new KafkaOperations(adminClient);
    }
}
