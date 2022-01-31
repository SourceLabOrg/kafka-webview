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

package org.sourcelab.kafka.webview.ui.manager.kafka.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents details about a consumer group offset positions for 1 or more topics the consumer is subscribed to.
 */
public class ConsumerGroupTopicOffsets {
    private final String topic;
    private final Map<Integer, PartitionOffset> offsetMap;

    /**
     * Constructor.  Preferred to use Builder instance.
     * @param topic name of the topic.
     * @param offsets details about each partition and offset.
     */
    public ConsumerGroupTopicOffsets(final String topic, final Collection<PartitionOffset> offsets) {
        this.topic = topic;

        final Map<Integer, PartitionOffset> offsetMap = new HashMap<>();
        for (final PartitionOffset offset : offsets) {
            offsetMap.put(
                offset.getPartition(),
                offset
            );
        }
        this.offsetMap = Collections.unmodifiableMap(offsetMap);
    }

    public String getTopic() {
        return topic;
    }

    /**
     * Marked private to keep from being serialized in responses.
     */
    private Map<Integer, PartitionOffset> getOffsetMap() {
        return offsetMap;
    }

    /**
     * @return List of offsets.
     */
    public List<PartitionOffset> getOffsets() {
        final List<PartitionOffset> offsetList = new ArrayList<>(offsetMap.values());

        // Sort by partition
        offsetList.sort((o1, o2) -> Integer.valueOf(o1.getPartition()).compareTo(o2.getPartition()));
        return Collections.unmodifiableList(offsetList);
    }

    /**
     * Get offset for the requested partition.
     * @param partition id of partition.
     * @return offset stored
     * @throws RuntimeException if requested invalid partition.
     */
    public long getOffsetForPartition(final int partition) {
        final Optional<PartitionOffset> offsetOptional = getOffsetMap()
            .values()
            .stream()
            .filter((offset) -> offset.getPartition() == partition)
            .findFirst();

        if (offsetOptional.isPresent()) {
            return offsetOptional.get().getOffset();
        }
        throw new RuntimeException("Unable to find partition " + partition);
    }

    /**
     * @return Set of all available partitions.
     */
    public Set<Integer> getPartitions() {
        final TreeSet<Integer> partitions = new TreeSet<>(offsetMap.keySet());
        return Collections.unmodifiableSet(partitions);
    }

    @Override
    public String toString() {
        return "ConsumerGroupOffsets{"
            + ", topic='" + topic + '\''
            + ", offsetMap=" + offsetMap
            + '}';
    }
}
