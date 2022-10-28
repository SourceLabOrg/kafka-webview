/**
 * MIT License
 *
 * Copyright (c) 2017-2022 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

import org.sourcelab.kafka.webview.ui.manager.socket.StartingPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Defines configuration values for consuming from a Kafka cluster.
 */
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

    /**
     * Defines which partitionIds to consume from.
     * An empty set means consume from ALL partitions.
     */
    private final Set<Integer> partitionIds;

    /**
     * Defines where to resume consuming from.
     */
    private final StartingPosition startingPosition;

    /**
     * Defines how many records to retrieve, per partition.
     */
    private final int maxResultsPerPartition;

    /**
     * Turn auto commit on/off.
     */
    private final boolean isAutoCommitEnabled;

    /**
     * How long to timeout poll requests.
     */
    private final long pollTimeoutMs;

    /**
     * Constructor.
     * @param topicConfig Topic configuration values.
     * @param filterConfig Filter configuration values.
     * @param consumerId Consumer identifier.
     * @param startingPosition Defines where to resume consuming from.
     */
    private ClientConfig(
        final TopicConfig topicConfig,
        final FilterConfig filterConfig,
        final String consumerId,
        final StartingPosition startingPosition) {
        this(topicConfig, filterConfig, consumerId, startingPosition, new ArrayList<>(), 10, true, 2000);
    }

    /**
     * Constructor.
     * @param topicConfig Topic configuration values.
     * @param filterConfig Filter configuration values.
     * @param consumerId Consumer identifier.
     * @param startingPosition Defines where to resume consuming from.
     * @param partitionIds List of partitionIds to limit consuming from.
     * @param maxResultsPerPartition How many records to poll per partition.
     * @param isAutoCommitEnabled If the consumer should auto commit state or not.
     * @param pollTimeoutMs poll timeout in milliseconds.
     */
    private ClientConfig(
        final TopicConfig topicConfig,
        final FilterConfig filterConfig,
        final String consumerId,
        final StartingPosition startingPosition,
        final Collection<Integer> partitionIds,
        final int maxResultsPerPartition,
        final boolean isAutoCommitEnabled,
        final long pollTimeoutMs) {

        this.topicConfig = topicConfig;
        this.filterConfig = filterConfig;
        this.consumerId = consumerId;
        this.startingPosition = startingPosition;
        final Set<Integer> tempSet = new HashSet<>();
        tempSet.addAll(partitionIds);
        this.partitionIds = Collections.unmodifiableSet(tempSet);
        this.maxResultsPerPartition = maxResultsPerPartition;
        this.isAutoCommitEnabled = isAutoCommitEnabled;
        this.pollTimeoutMs = pollTimeoutMs;
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
     * Are we limiting what partitions we read from.
     */
    public boolean hasFilteredPartitions() {
        // Empty means allow all.
        return !partitionIds.isEmpty();
    }

    /**
     * Utility method to determine if we should consume from a partition or not.
     * @param partitionId The partition to check.
     * @return True if so, false if not.
     */
    public boolean isPartitionFiltered(final int partitionId) {
        if (!hasFilteredPartitions()) {
            return false;
        }
        return !partitionIds.contains(partitionId);
    }

    public Set<Integer> getPartitionIds() {
        return partitionIds;
    }

    public StartingPosition getStartingPosition() {
        return startingPosition;
    }

    @Override
    public String toString() {
        return "ClientConfig{"
            + "topicConfig=" + topicConfig
            + ", filterConfig=" + filterConfig
            + ", consumerId='" + consumerId + '\''
            + ", partitionIds=" + partitionIds
            + ", startingPosition=" + startingPosition
            + ", maxResultsPerPartition=" + maxResultsPerPartition
            + ", isAutoCommitEnabled=" + isAutoCommitEnabled
            + ", pollTimeoutMs=" + pollTimeoutMs
            + '}';
    }

    /**
     * Create a new Builder instance.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Builder class for createing new ClientConfig instances.
     */
    public static class Builder {
        private TopicConfig topicConfig;
        private FilterConfig filterConfig;
        private String consumerId;
        private Set<Integer> limitPartitions = new HashSet<>();
        private StartingPosition startingPosition = StartingPosition.newResumeFromExistingState();
        private int maxResultsPerPartition = 10;
        private boolean autoCommit = true;
        private long pollTimeoutMs = 2000;

        /**
         * Private constructor.
         */
        private Builder() {

        }

        /**
         * Define topic configuration.
         */
        public Builder withTopicConfig(final TopicConfig topicConfig) {
            this.topicConfig = topicConfig;
            return this;
        }

        /**
         * Define filter configuration.
         */
        public Builder withFilterConfig(final FilterConfig filterConfig) {
            this.filterConfig = filterConfig;
            return this;
        }

        /**
         * Declare there are no filters.
         */
        public Builder withNoFilters() {
            return withFilterConfig(FilterConfig.withNoFilters());
        }

        /**
         * Declare the consumerId to use.
         */
        public Builder withConsumerId(final String consumerId) {
            this.consumerId = consumerId;
            return this;
        }

        /**
         * No limit on what partitions can be consumed.
         */
        public Builder withAllPartitions() {
            limitPartitions.clear();
            return this;
        }

        /**
         * Define a partition that should be consumed.
         */
        public Builder withPartition(final int partitionId) {
            limitPartitions.add(partitionId);
            return this;
        }

        /**
         * Declare one or more partitions that should be consumed.
         */
        public Builder withPartitions(final Collection<Integer> partitionIds) {
            limitPartitions.addAll(partitionIds);
            return this;
        }

        /**
         * Declare how many records should be consumed per partition.
         */
        public Builder withMaxResultsPerPartition(final int maxResultsPerPartition) {
            this.maxResultsPerPartition = maxResultsPerPartition;
            return this;
        }

        /**
         * Declare where to resume consuming from.
         * @param startingPosition Where to resume consuming from.
         * @return Builder instance.
         */
        public Builder withStartingPosition(final StartingPosition startingPosition) {
            this.startingPosition = startingPosition;
            return this;
        }

        /**
         * Declare consumer state should be auto committed.
         */
        public Builder withAutoCommitEnabled() {
            this.autoCommit = true;
            return this;
        }

        /**
         * Declare consumer state should NOT be auto committed.
         */
        public Builder withAutoCommitDisabled() {
            this.autoCommit = false;
            return this;
        }


        /**
         * Declare pollTimeout in milliseconds.
         */
        public Builder withPollTimeoutMs(final long pollTimeoutMs) {
            this.pollTimeoutMs = pollTimeoutMs;
            return this;
        }

        /**
         * Create new ClientConfig instance.
         */
        public ClientConfig build() {
            return new ClientConfig(
                topicConfig, filterConfig, consumerId, startingPosition, limitPartitions, maxResultsPerPartition,
                    autoCommit, pollTimeoutMs);
        }
    }
}
