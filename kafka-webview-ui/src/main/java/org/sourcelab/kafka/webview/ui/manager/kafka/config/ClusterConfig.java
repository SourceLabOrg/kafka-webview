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

package org.sourcelab.kafka.webview.ui.manager.kafka.config;

import org.sourcelab.kafka.webview.ui.manager.encryption.SecretManager;
import org.sourcelab.kafka.webview.ui.manager.sasl.SaslProperties;
import org.sourcelab.kafka.webview.ui.manager.sasl.SaslUtility;
import org.sourcelab.kafka.webview.ui.model.Cluster;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configuration defining cluster specific information.
 */
public class ClusterConfig {
    private final Set<String> brokerHosts;
    private final String defaultConsumerGroupId;

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
     * Private constructor for connecting to SSL brokers.
     */
    private ClusterConfig(
        final Set<String> brokerHosts,
        final String defaultConsumerGroupId,
        final boolean useSsl,
        final String keyStoreFile,
        final String keyStorePassword,
        final String trustStoreFile,
        final String trustStorePassword,
        final boolean useSasl,
        final String saslPlaintextUsername,
        final String saslPlaintextPassword,
        final String saslMechanism,
        final String saslJaas) {

        this.brokerHosts = brokerHosts;
        this.defaultConsumerGroupId = defaultConsumerGroupId;

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
    }

    public Set<String> getBrokerHosts() {
        return brokerHosts;
    }

    public String getDefaultConsumerGroupId() {
        return defaultConsumerGroupId;
    }

    /**
     * Is a default consumer group id configured for this cluster?
     * @return True if one is defined, false if not.
     */
    public boolean hasDefaultConsumerGroupId() {
        if (defaultConsumerGroupId == null || defaultConsumerGroupId.trim().isEmpty()) {
            return false;
        }
        return true;
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

        return builder;
    }

    /**
     * Builder instance for ClusterConfig.
     */
    public static final class Builder {
        private Set<String> brokerHosts;

        private String defaultConsumerGroupId = null;

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

        public Builder withDefaultConsumerGroupId(final String defaultConsumerGroupId) {
            this.defaultConsumerGroupId = defaultConsumerGroupId;
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
         * Create ClusterConfig instance from builder values.
         */
        public ClusterConfig build() {
            return new ClusterConfig(
                brokerHosts,
                defaultConsumerGroupId,
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
                saslJaas
            );
        }
    }
}
