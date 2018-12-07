package org.sourcelab.kafka.webview.ui.manager.kafka;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClusterConfig;

import java.util.HashMap;
import java.util.Map;

public class KafkaClientConfigUtil {
    /**
     * Path on filesystem where keystores are persisted.
     */
    private final String keyStoreRootPath;

    /**
     * Static prefix to pre-pend to all consumerIds.
     */
    private final String consumerIdPrefix;

    /**
     * Request timeout in milliseconds.
     */
    private final int requestTimeoutMs = 15000;

    public KafkaClientConfigUtil(final String keyStoreRootPath, final String consumerIdPrefix) {
        this.keyStoreRootPath = keyStoreRootPath;
        this.consumerIdPrefix = consumerIdPrefix;
    }

    public Map<String, Object> applyCommonSettings(
        final ClusterConfig clusterConfig,
        final String consumerId
    ) {
        return applyCommonSettings(clusterConfig, consumerId, new HashMap<>());
    }

    public Map<String, Object> applyCommonSettings(
        final ClusterConfig clusterConfig,
        final String consumerId,
        final Map<String, Object> config
    ) {
        // Generate consumerId with our configured static prefix.
        final String prefixedConsumerId = consumerIdPrefix.concat("-").concat(consumerId);

        // Set common config items
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, clusterConfig.getConnectString());
        config.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);

        // ClientId and ConsumerGroupId are intended to be unique for each user session.
        // See Issue-57 https://github.com/SourceLabOrg/kafka-webview/issues/57#issuecomment-363508531
        config.put(ConsumerConfig.CLIENT_ID_CONFIG, prefixedConsumerId);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, prefixedConsumerId);

        // Optionally configure SSL
        if (clusterConfig.isUseSsl()) {
            if (clusterConfig.isUseSasl()) {
                config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name);
            } else {
                config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name);
            }
            config.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keyStoreRootPath + "/" + clusterConfig.getKeyStoreFile());
            config.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, clusterConfig.getKeyStorePassword());
            config.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, keyStoreRootPath + "/" + clusterConfig.getTrustStoreFile());
            config.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, clusterConfig.getTrustStorePassword());
        }

        // Optionally configure SASL
        // If we're using SSL, we've already configured everything for SASL too...
        if (clusterConfig.isUseSasl()) {
            if (!clusterConfig.isUseSsl()) {
                config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_PLAINTEXT.name);
            }
            final String jaasConfig = "org.apache.kafka.common.security.plain.PlainLoginModule required\n" +
                "username=\"" + clusterConfig.getSaslPlaintextUsername() + "\"\n" +
                "password=\"" + clusterConfig.getSaslPlaintextPassword() + "\";";

            config.put("sasl.mechanism", "PLAIN");
            config.put("sasl.jaas.config", jaasConfig);
        }

        return config;
    }
}
