package com.darksci.kafkaview.manager.kafka.config;

import com.darksci.kafkaview.manager.encryption.SecretManager;
import com.darksci.kafkaview.model.Cluster;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configuration defining cluster specific information.
 */
public class ClusterConfig {
    private final Set<String> brokerHosts;
    private final boolean useSSL;
    private final String keyStoreFile;
    private final String keyStorePassword;
    private final String trustStoreFile;
    private final String trustStorePassword;

    private ClusterConfig(final Set<String> brokerHosts) {
        this(brokerHosts, false, null, null, null, null);
    }

    private ClusterConfig(
        final Set<String> brokerHosts,
        final boolean useSSL,
        final String keyStoreFile,
        final String keyStorePassword,
        final String trustStoreFile,
        final String trustStorePassword) {

        this.brokerHosts = brokerHosts;
        this.useSSL = useSSL;
        this.keyStoreFile = keyStoreFile;
        this.keyStorePassword = keyStorePassword;
        this.trustStoreFile = trustStoreFile;
        this.trustStorePassword = trustStorePassword;
    }

    public Set<String> getBrokerHosts() {
        return brokerHosts;
    }

    public boolean isUseSSL() {
        return useSSL;
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

    @Override
    public String toString() {
        return "ClusterConfig{" +
            "brokerHosts=" + brokerHosts +
            ", useSSL=" + useSSL +
            ", keyStoreFile='" + keyStoreFile + '\'' +
            ", trustStoreFile='" + trustStoreFile + '\'' +
            '}';
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
        // Create new Operational Client
        final ClusterConfig.Builder builder = ClusterConfig.newBuilder()
            .withBrokerHosts(cluster.getBrokerHosts());

        if (cluster.isSslEnabled()) {
            builder
                .withUseSSL(cluster.isSslEnabled())
                .withKeyStoreFile(cluster.getKeyStoreFile())
                .withKeyStorePassword(secretManager.decrypt(cluster.getKeyStorePassword()))
                .withTrustStoreFile(cluster.getTrustStoreFile())
                .withTrustStorePassword(secretManager.decrypt(cluster.getTrustStorePassword()));
        } else {
            builder.withUseSSL(false);
        }

        return builder;
    }


    public static final class Builder {
        private Set<String> brokerHosts;
        private boolean useSSL = false;
        private String keyStoreFile;
        private String keyStorePassword;
        private String trustStoreFile;
        private String trustStorePassword;

        private Builder() {
        }

        public Builder withBrokerHosts(Set<String> brokerHosts) {
            this.brokerHosts = brokerHosts;
            return this;
        }

        public Builder withBrokerHosts(final String... brokerHosts) {
            this.brokerHosts = new HashSet<>();
            this.brokerHosts.addAll(Arrays.asList(brokerHosts));
            return this;
        }

        public Builder withUseSSL(boolean useSSL) {
            this.useSSL = useSSL;
            return this;
        }

        public Builder withKeyStoreFile(String keyStoreFile) {
            this.keyStoreFile = keyStoreFile;
            return this;
        }

        public Builder withKeyStorePassword(String keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
            return this;
        }

        public Builder withTrustStoreFile(String trustStoreFile) {
            this.trustStoreFile = trustStoreFile;
            return this;
        }

        public Builder withTrustStorePassword(String trustStorePassword) {
            this.trustStorePassword = trustStorePassword;
            return this;
        }

        public ClusterConfig build() {
            if (!useSSL) {
                return new ClusterConfig(brokerHosts);
            }
            return new ClusterConfig(brokerHosts, true, keyStoreFile, keyStorePassword, trustStoreFile, trustStorePassword);
        }
    }
}
