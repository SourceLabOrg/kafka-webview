/**
 * MIT License
 *
 * Copyright (c) 2017, 2018 SourceLab.org (https://github.com/Crim/kafka-webview/)
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
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClusterConfig;

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
                "username=\"kafkaclient\"\n" +
                "password=\"client-secret\";";

            config.put("sasl.mechanism", "PLAIN");
            config.put("sasl.jaas.config", jaasConfig);

        }

        // TODO Handle SSL + SASL

        return KafkaAdminClient.create(config);
    }
}
