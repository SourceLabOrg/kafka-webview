package com.darksci.kafka.webview.manager.kafka;

import com.darksci.kafka.webview.manager.encryption.SecretManager;
import com.darksci.kafka.webview.model.Cluster;
import com.darksci.kafka.webview.manager.kafka.config.ClusterConfig;
import org.apache.kafka.clients.admin.AdminClient;

public class KafkaOperationsFactory {
    /**
     * Defines the consumerId.
     */
    private final static String consumerIdPrefix = "KafkaWebView-Operation-UserId";

    private final SecretManager secretManager;
    private final KafkaAdminFactory kafkaAdminFactory;

    public KafkaOperationsFactory(final SecretManager secretManager, final KafkaAdminFactory kafkaAdminFactory) {
        this.secretManager = secretManager;
        this.kafkaAdminFactory = kafkaAdminFactory;
    }

    public KafkaOperations create(final Cluster cluster, final long userId) {
        final String clientId = consumerIdPrefix + userId;

        // Create new Operational Client
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder(cluster, secretManager).build();
        final AdminClient adminClient = kafkaAdminFactory.create(clusterConfig, clientId);

        return new KafkaOperations(adminClient);
    }
}
