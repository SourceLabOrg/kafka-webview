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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class ConsumerGroupOffsets
{
    private final String consumerId;
    private final Map<String, ConsumerGroupTopicOffsets> topics;

    public ConsumerGroupOffsets(final String consumerId, final Map<String, ConsumerGroupTopicOffsets> topics) {
        this.consumerId = consumerId;
        this.topics = topics;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public Collection<String> getTopicNames() {
        // Get all topic names sorted.
        return topics.keySet().stream()
            .sorted()
            .collect(Collectors.toList());
    }

    public ConsumerGroupTopicOffsets getOffsetsForTopic(final String topicName) {
        if (!topics.containsKey(topicName)) {
            throw new IllegalArgumentException("No topic defined: " + topicName);
        }
        return topics.get(topicName);
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }


    public static final class Builder {
        private String consumerId;
        private Map<String, List<PartitionOffset>> topicOffsets = new HashMap<>();

        private Builder() {
        }

        public Builder withConsumerId(String consumerId) {
            this.consumerId = consumerId;
            return this;
        }

        public Builder withOffsets(String topic, int partition, long offset) {
            if (!topicOffsets.containsKey(topic)) {
                topicOffsets.put(topic, new ArrayList<>());
            }
            topicOffsets.get(topic).add(new PartitionOffset(
                partition, offset
            ));
            return this;
        }

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
