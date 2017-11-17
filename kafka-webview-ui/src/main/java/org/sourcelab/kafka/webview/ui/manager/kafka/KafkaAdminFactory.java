package org.sourcelab.kafka.webview.ui.manager.kafka;

import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClusterConfig;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.common.config.SslConfigs;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for Creating new KafkaAdmin instances.
 */
public class KafkaAdminFactory {
    private final String keyStoreRootPath;
    private final int requestTimeout = 15000;

    /**
     * Constructor.
     */
    public KafkaAdminFactory(final String keyStoreRootPath) {
        this.keyStoreRootPath = keyStoreRootPath;
    }

    /**
     * Create a new AdminClient instance.
     * @param clusterConfig What cluster to connect to.
     * @param clientId What clientId to associate the connection with.
     */
    public AdminClient create(final ClusterConfig clusterConfig, final String clientId) {
        // Create a map
        final Map<String, Object> config = new HashMap<>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, clusterConfig.getConnectString());
        config.put(AdminClientConfig.CLIENT_ID_CONFIG, clientId);
        config.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeout);

        if (clusterConfig.isUseSsl()) {
            config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
            config.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keyStoreRootPath + "/" + clusterConfig.getKeyStoreFile());
            config.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, clusterConfig.getKeyStorePassword());
            config.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, keyStoreRootPath + "/" + clusterConfig.getTrustStoreFile());
            config.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, clusterConfig.getTrustStorePassword());
        }

        return KafkaAdminClient.create(config);
    }
}
