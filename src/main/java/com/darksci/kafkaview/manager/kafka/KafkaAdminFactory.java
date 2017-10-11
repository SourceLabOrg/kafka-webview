package com.darksci.kafkaview.manager.kafka;

import com.darksci.kafkaview.manager.kafka.config.ClusterConfig;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.KafkaAdminClient;

import java.util.HashMap;
import java.util.Map;

public class KafkaAdminFactory {
    private final ClusterConfig clusterConfig;
    private final String clientId;

    public KafkaAdminFactory(final ClusterConfig clusterConfig, final String clientId) {
        this.clusterConfig = clusterConfig;
        this.clientId = clientId + "as Admin";
    }

    public AdminClient create() {
        // Create a map
        Map<String, Object> config = new HashMap<>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, clusterConfig.getConnectString());
        config.put(AdminClientConfig.CLIENT_ID_CONFIG, clientId);

        return KafkaAdminClient.create(config);
    }
}
