package com.darksci.kafkaview.manager.kafka.config;

import com.darksci.kafkaview.model.Cluster;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(final Cluster cluster) {
        // Create new Operational Client
        return ClusterConfig.newBuilder()
            .withBrokerHosts(cluster.getBrokerHosts())
            .withUseSSL(cluster.isSslEnabled())
            .withKeyStoreFile(cluster.getKeyStoreFile())
            .withKeyStorePassword(cluster.getKeyStorePassword())
            .withTrustStoreFile(cluster.getTrustStoreFile())
            .withTrustStorePassword(cluster.getTrustStorePassword());
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
