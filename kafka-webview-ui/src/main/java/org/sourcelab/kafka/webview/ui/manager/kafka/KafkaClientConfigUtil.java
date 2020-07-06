/**
 * MIT License
 *
 * Copyright (c) 2017, 2018, 2019 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.sourcelab.kafka.webview.ui.manager.kafka;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClusterConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class to DRY out common Kafka client configuration options that apply to multiple client types.
 */
public class KafkaClientConfigUtil {
    private static final Logger logger = LoggerFactory.getLogger(KafkaClientConfigUtil.class);

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

    /**
     * Constructor.
     * @param keyStoreRootPath Path to where keystore files are persisted on the file system.
     * @param consumerIdPrefix Application configuration value for a standard prefix to apply to all consumerIds.
     */
    public KafkaClientConfigUtil(final String keyStoreRootPath, final String consumerIdPrefix) {
        this.keyStoreRootPath = keyStoreRootPath;
        this.consumerIdPrefix = consumerIdPrefix;
    }

    /**
     * Builds a map of all common Kafka client configuration settings.
     * @param clusterConfig ClusterConfig instance to use as basis for configuration/
     * @param consumerId Id of consumer to use.  This will be prefixed with consumerIdPrefix property.
     * @return a new Map containing the configuration options.
     */
    public Map<String, Object> applyCommonSettings(final ClusterConfig clusterConfig, final String consumerId) {
        return applyCommonSettings(clusterConfig, consumerId, new HashMap<>());
    }

    /**
     * Builds a map of all common Kafka client configuration settings.
     * @param clusterConfig ClusterConfig instance to use as basis for configuration/
     * @param consumerId Id of consumer to use.  This will be prefixed with consumerIdPrefix property.
     * @param config Apply configuration to existing map.
     * @return a new Map containing the configuration options.
     */
    private Map<String, Object> applyCommonSettings(
        final ClusterConfig clusterConfig,
        final String consumerId,
        final Map<String, Object> config
    ) {
        // Generate groupId with our configured static prefix.
        final String prefixedGroupId = consumerIdPrefix.concat("-").concat(consumerId);

        // Generate consumerId, which should be unique per user and thread.
        final String prefixedConsumerId = prefixedGroupId.concat("-") + Thread.currentThread().getId();

        // Set common config items
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, clusterConfig.getConnectString());
        config.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);

        // groupId is intended to be unique for each user session.
        // clientId is intended to be unique per user session and thread.
        // See Issue-57 https://github.com/SourceLabOrg/kafka-webview/issues/57#issuecomment-363508531
        // See Issue-175 https://github.com/SourceLabOrg/kafka-webview/issues/175
        config.put(ConsumerConfig.CLIENT_ID_CONFIG, prefixedConsumerId);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, prefixedGroupId);

        // Optionally configure SSL
        applySslSettings(clusterConfig, config);

        // Optionally configure SASL
        applySaslSettings(clusterConfig, config);

        // Apply cluster client properties if defined.
        // Note: This should always be applied last.
        applyClusterClientProperties(clusterConfig, config);

        return config;
    }

    /**
     * If client properties are defined on the cluster, they get applied last ontop of everything else.
     * @param clusterConfig configuration properties.
     * @param config config to be applied to.
     */
    private void applyClusterClientProperties(final ClusterConfig clusterConfig, final Map<String, Object> config) {
        if (clusterConfig.getClusterClientProperties().isEmpty()) {
            return;
        }

        for (final Map.Entry<String, String> entry : clusterConfig.getClusterClientProperties().entrySet()) {
            if (config.containsKey(entry.getKey())) {
                // Log a warning as behavior may be altered in a way that causes Kafka WebView to no longer function.
                logger.warn(
                    "Client property defined on the cluster replaced property '{}'. "
                    + "The original value of '{}' was replaced with with '{}'. "
                    + "Overriding of configuration properties in this way may cause Kafka Webview to not function correctly.",
                    entry.getKey(), config.get(entry.getKey()), entry.getValue()
                );
            }
            // Set value.
            config.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * If SSL is configured for this cluster, apply the settings.
     * @param clusterConfig Cluster configuration definition to source values from.
     * @param config Config map to apply settings to.
     */
    private void applySslSettings(final ClusterConfig clusterConfig, final Map<String, Object> config) {
        // Optionally configure SSL
        if (!clusterConfig.isUseSsl()) {
            return;
        }
        if (clusterConfig.isUseSasl()) {
            config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name);
        } else {
            config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name);

            // KeyStore and KeyStore password only needed if NOT using SASL
            config.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keyStoreRootPath + "/" + clusterConfig.getKeyStoreFile());
            config.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, clusterConfig.getKeyStorePassword());
        }
        // Only put Trust properties if one is defined
        if (clusterConfig.getTrustStoreFile() != null) {
            config.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, keyStoreRootPath + "/" + clusterConfig.getTrustStoreFile());
            config.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, clusterConfig.getTrustStorePassword());
        }
    }

    /**
     * If SASL is configured for this cluster, apply the settings.
     * @param clusterConfig Cluster configuration definition to source values from.
     * @param config Config map to apply settings to.
     */
    private void applySaslSettings(final ClusterConfig clusterConfig, final Map<String, Object> config) {
        // If we're using SSL, we've already configured everything for SASL too...
        if (!clusterConfig.isUseSasl()) {
            return;
        }

        // If not using SSL
        if (clusterConfig.isUseSsl()) {
            // SASL+SSL
            config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name);

            // Keystore and keystore password not required if using SASL+SSL
            config.remove(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG);
            config.remove(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG);
        } else {
            // Just SASL PLAINTEXT
            config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_PLAINTEXT.name);
        }
        config.put(SaslConfigs.SASL_MECHANISM, clusterConfig.getSaslMechanism());
        config.put(SaslConfigs.SASL_JAAS_CONFIG, clusterConfig.getSaslJaas());
    }

    /**
     * Utility method to get all defined Kafka Consumer properties from the upstream Kafka library.
     * @return Sorted list of property names.
     */
    public static List<KafkaSettings> getAllKafkaConsumerProperties() {
        // Likely not the most graceful way to group these properties...

        // Our return value
        final List<KafkaSettings> kafkaSettings = new ArrayList<>();

        // Keep a running set of all the keys we've collected so far.
        final Set<String> allPreviousKeys = new HashSet<>();

        // Add SSL Settings
        ConfigDef configDef = new ConfigDef();
        SslConfigs.addClientSslSupport(configDef);
        kafkaSettings.add(new KafkaSettings(
            "SSL", configDef.names()
        ));

        // Add SASL Settings
        configDef = new ConfigDef();
        SaslConfigs.addClientSaslSupport(configDef);
        kafkaSettings.add(new KafkaSettings(
            "SASL", configDef.names()
        ));

        // Collect all keys.
        kafkaSettings
            .forEach((entry) -> allPreviousKeys.addAll(entry.getKeys()));

        // Add basic consumer properties, removing entries from the previous categories
        kafkaSettings.add(new KafkaSettings(
            "Consumer",
            ConsumerConfig.configNames().stream()
                .filter((entry) -> !allPreviousKeys.contains(entry))
                .collect(Collectors.toSet())
        ));

        // Sort our list and return
        return kafkaSettings.stream()
            .sorted(Comparator.comparing(KafkaSettings::getGroup))
            .collect(Collectors.toList());
    }

    /**
     * Abstracted list of Kafka keys categorized.
     */
    public static class KafkaSettings {
        private final String group;
        private final List<String> keys;

        /**
         * Constructor.
         * @param group Category name.
         * @param keys Available keys.
         */
        private KafkaSettings(final String group, final Set<String> keys) {
            this.group = group;

            // Sort the list of keys
            this.keys = keys.stream()
                .sorted()
                .collect(Collectors.toList());
        }

        public String getGroup() {
            return group;
        }

        public List<String> getKeys() {
            return keys;
        }

        @Override
        public String toString() {
            return "KafkaSettings{"
                + "group='" + group + '\''
                + ", keys=" + keys
                + '}';
        }
    }
}
