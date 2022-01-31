/**
 * MIT License
 *
 * Copyright (c) 2017-2021 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

package org.sourcelab.kafka.webview.ui.manager.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sourcelab.kafka.webview.ui.manager.encryption.SecretManager;
import org.sourcelab.kafka.webview.ui.manager.sasl.SaslProperties;
import org.sourcelab.kafka.webview.ui.manager.sasl.SaslUtility;
import org.sourcelab.kafka.webview.ui.model.Cluster;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configuration defining cluster specific information.
 */
public class ClusterConfig {
    private final Set<String> brokerHosts;

    /**
     * SSL Configuration Options.
     */
    private final boolean useSsl;
    private final String keyStoreFile;
    private final String keyStorePassword;
    private final String trustStoreFile;
    private final String trustStorePassword;

    /**
     * SASL Configuration Options.
     */
    private final boolean useSasl;
    private final String saslPlaintextUsername;
    private final String saslPlaintextPassword;
    private final String saslMechanism;
    private final String saslJaas;

    /**
     * Client Properties defined on the Cluster configuration.
     */
    private final Map<String, String> clusterClientProperties;

    /**
     * Private constructor for connecting to SSL brokers.
     */
    private ClusterConfig(
        final Set<String> brokerHosts,
        final boolean useSsl,
        final String keyStoreFile,
        final String keyStorePassword,
        final String trustStoreFile,
        final String trustStorePassword,
        final boolean useSasl,
        final String saslPlaintextUsername,
        final String saslPlaintextPassword,
        final String saslMechanism,
        final String saslJaas,
        final Map<String, String> clusterClientProperties
    ) {

        this.brokerHosts = brokerHosts;

        // SSL Options
        this.useSsl = useSsl;
        this.keyStoreFile = keyStoreFile;
        this.keyStorePassword = keyStorePassword;
        this.trustStoreFile = trustStoreFile;
        this.trustStorePassword = trustStorePassword;

        // SASL Options
        this.useSasl = useSasl;
        this.saslPlaintextUsername = saslPlaintextUsername;
        this.saslPlaintextPassword = saslPlaintextPassword;
        this.saslMechanism = saslMechanism;
        this.saslJaas = saslJaas;

        // Shallow copy the cluster client properties.
        this.clusterClientProperties = Collections.unmodifiableMap(
            new HashMap<>(clusterClientProperties)
        );
    }

    public Set<String> getBrokerHosts() {
        return brokerHosts;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public String getConnectString() {
        return brokerHosts.stream().collect(Collectors.joining(","));
    }

    public String getKeyStoreFile() {
        return keyStoreFile;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getTrustStoreFile() {
        return trustStoreFile;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public boolean isUseSasl() {
        return useSasl;
    }

    public String getSaslPlaintextUsername() {
        return saslPlaintextUsername;
    }

    public String getSaslPlaintextPassword() {
        return saslPlaintextPassword;
    }

    public String getSaslMechanism() {
        return saslMechanism;
    }

    public String getSaslJaas() {
        return saslJaas;
    }

    public Map<String, String> getClusterClientProperties() {
        return clusterClientProperties;
    }

    @Override
    public String toString() {
        return "ClusterConfig{"
            + "brokerHosts=" + brokerHosts
            + ", useSsl=" + useSsl
            + ", keyStoreFile='" + keyStoreFile + '\''
            + ", trustStoreFile='" + trustStoreFile + '\''
            + ", useSasl=" + useSasl
            + ", saslMechanism='" + saslMechanism + '\''
            + '}';
    }

    /**
     * @return New empty builder instance.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Create a new Builder instance using a Cluster model entity and SecretManager
     * for decrypting secrets.
     *
     * @param cluster Cluster entity to build config off of.
     * @param secretManager SecretManager to decrypt secrets with.
     * @return Builder instance.
     */
    public static Builder newBuilder(final Cluster cluster, final SecretManager secretManager) {
        final ClusterConfig.Builder builder = ClusterConfig.newBuilder()
            .withBrokerHosts(cluster.getBrokerHosts());

        if (cluster.isSslEnabled()) {
            builder
                .withUseSsl(cluster.isSslEnabled())
                .withKeyStoreFile(cluster.getKeyStoreFile())
                .withKeyStorePassword(secretManager.decrypt(cluster.getKeyStorePassword()))
                .withTrustStoreFile(cluster.getTrustStoreFile())
                .withTrustStorePassword(secretManager.decrypt(cluster.getTrustStorePassword()));
        } else {
            builder.withUseSsl(false);
        }

        if (cluster.isSaslEnabled()) {
            // Parse Properties.
            final SaslUtility saslUtility = new SaslUtility(secretManager);
            final SaslProperties saslProperties = saslUtility.decodeProperties(cluster);

            builder
                .withUseSasl(true)
                .withSaslMechanism(saslProperties.getMechanism())
                .withSaslPlaintextUsername(saslProperties.getPlainUsername())
                .withSaslPlaintextPassword(saslProperties.getPlainPassword())
                .withSaslJaas(saslProperties.getJaas());
        } else {
            builder.withUseSasl(false);
        }

        // If we have defined cluster client options, decode and set them.
        if (cluster.getOptionParameters() != null) {
            final ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> customOptions;
            try {
                customOptions = objectMapper.readValue(cluster.getOptionParameters(), Map.class);
            } catch (final IOException e) {
                // Fail safe?
                customOptions = new HashMap<>();
            }
            builder.withClusterClientConfig(customOptions);
        }

        return builder;
    }

    /**
     * Builder instance for ClusterConfig.
     */
    public static final class Builder {
        private Set<String> brokerHosts;

        /**
         * SSL Configuration Options.
         */
        private boolean useSsl = false;
        private String keyStoreFile;
        private String keyStorePassword;
        private String trustStoreFile;
        private String trustStorePassword;

        /**
         * SASL Configuration Options.
         */
        private boolean useSasl = false;
        private String saslPlaintextUsername;
        private String saslPlaintextPassword;
        private String saslMechanism;
        private String saslJaas;

        /**
         * Override properties defined from the cluster.option_parameters field.
         * These should be applied LAST ontop of all the other config options.
         */
        private Map<String, String> clusterOverrideProperties = new HashMap<>();

        private Builder() {
        }

        /**
         * Set broker hosts.
         */
        public Builder withBrokerHosts(final Set<String> brokerHosts) {
            this.brokerHosts = brokerHosts;
            return this;
        }

        /**
         * Set broker hosts.
         */
        public Builder withBrokerHosts(final String... brokerHosts) {
            this.brokerHosts = new HashSet<>();
            this.brokerHosts.addAll(Arrays.asList(brokerHosts));
            return this;
        }

        /**
         * Declare if the brokers use SSL.
         */
        public Builder withUseSsl(final boolean useSsl) {
            this.useSsl = useSsl;
            return this;
        }

        /**
         * Declare keystore file if using SSL.
         */
        public Builder withKeyStoreFile(final String keyStoreFile) {
            this.keyStoreFile = keyStoreFile;
            return this;
        }

        /**
         * Declare keystore password if using SSL.
         */
        public Builder withKeyStorePassword(final String keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
            return this;
        }

        /**
         * Declare truststore file if using SSL.
         */
        public Builder withTrustStoreFile(final String trustStoreFile) {
            this.trustStoreFile = trustStoreFile;
            return this;
        }

        /**
         * Declare truststore password if using SSL.
         */
        public Builder withTrustStorePassword(final String trustStorePassword) {
            this.trustStorePassword = trustStorePassword;
            return this;
        }

        /**
         * Declare if the brokers use SASL.
         */
        public Builder withUseSasl(final boolean useSasl) {
            this.useSasl = useSasl;
            return this;
        }

        /**
         * Declare SASL plaintext username.
         */
        public Builder withSaslPlaintextUsername(final String saslPlaintextUsername) {
            this.saslPlaintextUsername = saslPlaintextUsername;
            return this;
        }

        /**
         * Declare SASL plaintext password.
         */
        public Builder withSaslPlaintextPassword(final String saslPlaintextPassword) {
            this.saslPlaintextPassword = saslPlaintextPassword;
            return this;
        }

        /**
         * Declare SASL mechanism.
         */
        public Builder withSaslMechanism(final String saslMechanism) {
            this.saslMechanism = saslMechanism;
            return this;
        }

        /**
         * Declare SASL JAAS configuration.
         */
        public Builder withSaslJaas(final String saslJaas) {
            this.saslJaas = saslJaas;
            return this;
        }

        /**
         * Declare Override Properties defined on the cluster.
         */
        public Builder withClusterClientConfig(final Map<String, String> clusterClientConfig) {
            this.clusterOverrideProperties.putAll(clusterClientConfig);
            return this;
        }

        /**
         * Declare Override Properties defined on the cluster.
         */
        public Builder withClusterClientConfig(final String key, final String value) {
            this.clusterOverrideProperties.put(key, value);
            return this;
        }

        /**
         * Create ClusterConfig instance from builder values.
         */
        public ClusterConfig build() {
            return new ClusterConfig(
                brokerHosts,
                // SSL Options.
                useSsl,
                keyStoreFile,
                keyStorePassword,
                trustStoreFile,
                trustStorePassword,
                // SASL Options
                useSasl,
                saslPlaintextUsername,
                saslPlaintextPassword,
                saslMechanism,
                saslJaas,
                // Cluster client properties
                clusterOverrideProperties
            );
        }
    }
}
