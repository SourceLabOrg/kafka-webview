package com.darksci.kafkaview.manager.kafka;

import com.darksci.kafkaview.manager.encryption.SecretManager;
import com.darksci.kafkaview.manager.kafka.config.ClusterConfig;
import com.darksci.kafkaview.model.Cluster;
import org.apache.kafka.clients.admin.AdminClient;

public class KafkaOperationsFactory {
    private final SecretManager secretManager;
    private final KafkaAdminFactory kafkaAdminFactory;

    public KafkaOperationsFactory(final SecretManager secretManager, final KafkaAdminFactory kafkaAdminFactory) {
        this.secretManager = secretManager;
        this.kafkaAdminFactory = kafkaAdminFactory;
    }

    public KafkaOperations createOperationsClient(final Cluster cluster, final long userId) {
        final String clientId = "User" + userId;

        // Create new Operational Client
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder(cluster, secretManager).build();
        final AdminClient adminClient = kafkaAdminFactory.create(clusterConfig, clientId);

        return new KafkaOperations(adminClient);
    }
}
