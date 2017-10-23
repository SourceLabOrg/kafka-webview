package com.darksci.kafka.webview.manager.kafka.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClientConfig {
    /**
     * Holds details about what topic we're consuming from.
     */
    private final TopicConfig topicConfig;

    /**
     * Holds details about any filters applied.
     */
    private final FilterConfig filterConfig;

    /**
     * Defines the id of the consumer, which is where offsets/state is stored under.
     */
    private final String consumerId;

    private final Set<Integer> partitionIds;

    /**
     * Defines how many records to retrieve, per partition.
     */
    private final int maxResultsPerPartition;

    /**
     * Turn auto commit on/off.
     */
    private boolean isAutoCommitEnabled = false;

    /**
     * How long to timeout poll requests.
     */
    private long pollTimeoutMs = 2000;

    public ClientConfig(final TopicConfig topicConfig, final FilterConfig filterConfig, final String consumerId) {
        this(topicConfig, filterConfig, consumerId, new ArrayList<>(), 10);
    }

    public ClientConfig(
        final TopicConfig topicConfig,
        final FilterConfig filterConfig,
        final String consumerId,
        final Collection<Integer> partitionIds,
        final int maxResultsPerPartition) {
        this.topicConfig = topicConfig;
        this.filterConfig = filterConfig;
        this.consumerId = consumerId;
        final Set<Integer> tempSet = new HashSet<>();
        tempSet.addAll(partitionIds);
        this.partitionIds = Collections.unmodifiableSet(tempSet);
        this.maxResultsPerPartition = maxResultsPerPartition;
    }

    public TopicConfig getTopicConfig() {
        return topicConfig;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public FilterConfig getFilterConfig() {
        return filterConfig;
    }

    public boolean isAutoCommitEnabled() {
        return isAutoCommitEnabled;
    }

    public int getMaxResultsPerPartition() {
        return maxResultsPerPartition;
    }

    public long getPollTimeoutMs() {
        return pollTimeoutMs;
    }

    /**
     * Should we limit what partitions we read from?
     */
    public boolean hasFilteredPartitions() {
        // Empty means allow all.
        return !partitionIds.isEmpty();
    }

    public boolean isPartitionFiltered(final int partitionId) {
        if (!hasFilteredPartitions()) {
            return false;
        }
        return !partitionIds.contains(partitionId);
    }

    public Set<Integer> getPartitionIds() {
        return partitionIds;
    }

    @Override
    public String toString() {
        return "ClientConfig{" +
            "topicConfig=" + topicConfig +
            ", filterConfig=" + filterConfig +
            ", consumerId='" + consumerId + '\'' +
            '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private TopicConfig topicConfig;
        private FilterConfig filterConfig;
        private String consumerId;
        private Set<Integer> limitPartitions = new HashSet<>();
        private int maxResultsPerPartition = 10;

        private Builder() {

        }

        public Builder withTopicConfig(final TopicConfig topicConfig) {
            this.topicConfig = topicConfig;
            return this;
        }

        public Builder withFilterConfig(final FilterConfig filterConfig) {
            this.filterConfig = filterConfig;
            return this;
        }

        public Builder withNoFilters() {
            return withFilterConfig(FilterConfig.withNoFilters());
        }

        public Builder withConsumerId(final String consumerId) {
            this.consumerId = consumerId;
            return this;
        }

        public Builder withPartition(final int partitionId) {
            limitPartitions.add(partitionId);
            return this;
        }

        public Builder withPartitions(final Collection<Integer> partitionIds) {
            limitPartitions.addAll(partitionIds);
            return this;
        }

        public Builder withMaxResultsPerPartition(final int maxResultsPerPartition) {
            this.maxResultsPerPartition = maxResultsPerPartition;
            return this;
        }

        public ClientConfig build() {
            return new ClientConfig(topicConfig, filterConfig, consumerId, limitPartitions, maxResultsPerPartition);
        }
    }
}
