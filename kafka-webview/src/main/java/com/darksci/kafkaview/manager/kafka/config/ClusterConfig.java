package com.darksci.kafkaview.manager.kafka.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ClusterConfig {
    private final Set<String> brokerHosts;

    public ClusterConfig(final Set<String> brokerHosts) {
        this.brokerHosts = brokerHosts;
    }

    public ClusterConfig(final String... brokerHosts) {
        this.brokerHosts = new HashSet<>();
        this.brokerHosts.addAll(Arrays.asList(brokerHosts));
    }

    private Set<String> getBrokerHosts() {
        return brokerHosts;
    }

    @Override
    public String toString() {
        return "ClusterConfig{" +
            "brokerHosts=" + brokerHosts +
            '}';
    }

    public String getConnectString() {
        return brokerHosts.stream().collect(Collectors.joining(","));
    }
}
