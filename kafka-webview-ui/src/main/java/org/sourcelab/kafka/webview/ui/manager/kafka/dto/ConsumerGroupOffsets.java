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
 * Contains details about a consumer group's current offsets for one or more topics they are consuming from.
 */
public class ConsumerGroupOffsets {
    private final String consumerId;
    private final Map<String, ConsumerGroupTopicOffsets> topics;

    public ConsumerGroupOffsets(final String consumerId, final Map<String, ConsumerGroupTopicOffsets> topics) {
        this.consumerId = consumerId;
        this.topics = topics;
    }

    public String getConsumerId() {
        return consumerId;
    }

    /**
     * All topic names consuming from.
     * @return Immutable list of all topic names being consumed.
     */
    public Collection<String> getTopicNames() {
        // Get all topic names sorted.
        return Collections.unmodifiableList(topics.keySet().stream()
            .sorted()
            .collect(Collectors.toList())
        );
    }

    /**
     * Return all offsets per topic as an immutable list.
     * @return Immutable list of offsets for each topic and partition.
     */
    public List<ConsumerGroupTopicOffsets> getTopics() {
        return Collections.unmodifiableList(new ArrayList<>(topics.values()));
    }

    /**
     * For a given topic, return the consumer group offsets for it.
     * @param topicName name of the topic to retrieve offsets for.
     * @return Offsets for the topic.
     */
    public ConsumerGroupTopicOffsets getOffsetsForTopic(final String topicName) {
        if (!topics.containsKey(topicName)) {
            throw new IllegalArgumentException("No topic defined: " + topicName);
        }
        return topics.get(topicName);
    }

    /**
     * Builder instance.
     * @return new builder instance.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Builder instance.
     */
    public static final class Builder {
        private String consumerId;
        private Map<String, List<PartitionOffset>> topicOffsets = new HashMap<>();

        private Builder() {
        }

        public Builder withConsumerId(final String consumerId) {
            this.consumerId = consumerId;
            return this;
        }

        /**
         * Add offsets entry.
         * @param topic name of topic.
         * @param partition which partition on the topic.
         * @param offset The current offset for the topic and partition.
         * @return Builder instance.
         */
        public Builder withOffset(final String topic, final int partition, final long offset) {
            if (!topicOffsets.containsKey(topic)) {
                topicOffsets.put(topic, new ArrayList<>());
            }
            topicOffsets.get(topic).add(new PartitionOffset(
                partition, offset
            ));
            return this;
        }

        /**
         * Build a ConsumerGroupOffsets instance.
         * @return ConsumerGroupOffsets instance.
         */
        public ConsumerGroupOffsets build() {
            final Map<String, ConsumerGroupTopicOffsets> topicOffsetsMap = new HashMap<>();
            for (final Map.Entry<String, List<PartitionOffset>> entry : topicOffsets.entrySet()) {
                topicOffsetsMap.put(
                    entry.getKey(), new ConsumerGroupTopicOffsets(entry.getKey(), entry.getValue())
                );
            }

            return new ConsumerGroupOffsets(consumerId, topicOffsetsMap);
        }
    }
}
