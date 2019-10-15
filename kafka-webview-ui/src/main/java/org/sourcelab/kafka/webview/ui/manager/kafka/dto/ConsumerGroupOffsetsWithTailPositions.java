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

package org.sourcelab.kafka.webview.ui.manager.kafka.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents details about a consumer group offset positions, including the current tail offset positions for
 * each partition.
 */
public class ConsumerGroupOffsetsWithTailPositions {
    private final String consumerId;
    private final Map<String, ConsumerGroupTopicOffsetsWithTailPositions> topicToOffsetMap;
    private final long timestamp;

    /**
     * Constructor.
     * @param consumerId id of consumer group.
     * @param topicToOffsets details about each partition and offset mapped by topic.
     * @param timestamp Timestamp offsets were retrieved.
     */
    public ConsumerGroupOffsetsWithTailPositions(
        final String consumerId,
        final Map<String, List<PartitionOffsetWithTailPosition>> topicToOffsets,
        final long timestamp) {
        this.consumerId = consumerId;
        this.timestamp = timestamp;

        final Map<String, ConsumerGroupTopicOffsetsWithTailPositions> tempMap = new HashMap<>();

        for (final Map.Entry<String, List<PartitionOffsetWithTailPosition>> entry : topicToOffsets.entrySet()) {
            final String topic = entry.getKey();
            final Iterable<PartitionOffsetWithTailPosition> offsets = entry.getValue();

            final Map<Integer, PartitionOffsetWithTailPosition> copiedMap = new HashMap<>();
            for (final PartitionOffsetWithTailPosition offset : offsets) {
                copiedMap.put(
                    offset.getPartition(),
                    offset
                );
            }
            tempMap.put(topic,  new ConsumerGroupTopicOffsetsWithTailPositions(topic, Collections.unmodifiableMap(copiedMap)));
        }
        this.topicToOffsetMap = Collections.unmodifiableMap(tempMap);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getConsumerId() {
        return consumerId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Collection<String> getTopicNames() {
        return topicToOffsetMap.keySet().stream()
            .sorted()
            .collect(Collectors.toList());
    }

    public ConsumerGroupTopicOffsetsWithTailPositions getOffsetsForTopic(final String topic) {
        if (!topicToOffsetMap.containsKey(topic)) {
            throw new IllegalArgumentException("No topic exists: " + topic);
        }
        return topicToOffsetMap.get(topic);
    }

    public Map<String, ConsumerGroupTopicOffsetsWithTailPositions> getTopicToOffsetMap() {
        return topicToOffsetMap;
    }

    @Override
    public String toString() {
        return "ConsumerGroupOffsetsWithTailPositions{"
            + "consumerId='" + consumerId + '\''
            + ", timestamp='" + timestamp + '\''
            + ", offsetMap=" + topicToOffsetMap
            + '}';
    }

    public static final class Builder {
        private String consumerId;
        private Map<String, List<PartitionOffsetWithTailPosition>> topicToOffsetMap = new HashMap<>();
        private long timestamp;

        private Builder() {
        }

        public Builder withConsumerId(String consumerId) {
            this.consumerId = consumerId;
            return this;
        }

        public Builder withTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withOffsetsForTopic(final String topic, PartitionOffsetWithTailPosition partitionOffsetWithTailPosition) {
            if (!topicToOffsetMap.containsKey(topic)) {
                topicToOffsetMap.put(topic, new ArrayList<>());
            }
            topicToOffsetMap.get(topic).add(partitionOffsetWithTailPosition);
            return this;
        }



        public ConsumerGroupOffsetsWithTailPositions build() {
            return new ConsumerGroupOffsetsWithTailPositions(
                consumerId,
                topicToOffsetMap,
                timestamp
            );
        }
    }
}
