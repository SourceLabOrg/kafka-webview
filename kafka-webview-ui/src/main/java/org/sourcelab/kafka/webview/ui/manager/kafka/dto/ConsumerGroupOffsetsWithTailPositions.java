/**
 * MIT License
 *
 * Copyright (c) 2017, 2018 SourceLab.org (https://github.com/Crim/kafka-webview/)
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents details about a consumer group offset positions, including the current tail offset positions for
 * each partition.
 */
public class ConsumerGroupOffsetsWithTailPositions {
    private final String consumerId;
    private final String topic;
    private final Map<Integer, PartitionOffsetWithTailPosition> offsetMap;

    /**
     * Constructor.
     * @param consumerId id of consumer group.
     * @param topic name of the topic.
     * @param offsets details about each partition and offset.
     */
    public ConsumerGroupOffsetsWithTailPositions(
        final String consumerId,
        final String topic,
        final Iterable<PartitionOffsetWithTailPosition> offsets
    ) {
        this.consumerId = consumerId;
        this.topic = topic;

        final Map<Integer, PartitionOffsetWithTailPosition> copiedMap = new HashMap<>();
        for (final PartitionOffsetWithTailPosition offset : offsets) {
            copiedMap.put(
                offset.getPartition(),
                offset
            );
        }
        this.offsetMap = Collections.unmodifiableMap(copiedMap);
    }

    public String getConsumerId() {
        return consumerId;
    }

    public String getTopic() {
        return topic;
    }

    /**
     * Marked private to keep from being serialized in responses.
     */
    private Map<Integer, PartitionOffsetWithTailPosition> getOffsetMap() {
        return offsetMap;
    }

    /**
     * @return List of offsets.
     */
    public List<PartitionOffsetWithTailPosition> getOffsets() {
        final List<PartitionOffsetWithTailPosition> offsetList = new ArrayList<>(offsetMap.values());

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
        final Optional<PartitionOffsetWithTailPosition> offsetOptional = getOffsetMap()
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
     * Get offset for the requested partition.
     * @param partition id of partition.
     * @return offset stored
     * @throws RuntimeException if requested invalid partition.
     */
    public long getTailOffsetForPartition(final int partition) {
        final Optional<PartitionOffsetWithTailPosition> offsetOptional = getOffsetMap()
            .values()
            .stream()
            .filter((offset) -> offset.getPartition() == partition)
            .findFirst();

        if (offsetOptional.isPresent()) {
            return offsetOptional.get().getTail();
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
        return "ConsumerGroupOffsetsWithTailPositions{"
            + "consumerId='" + consumerId + '\''
            + ", topic='" + topic + '\''
            + ", offsetMap=" + offsetMap
            + '}';
    }
}
