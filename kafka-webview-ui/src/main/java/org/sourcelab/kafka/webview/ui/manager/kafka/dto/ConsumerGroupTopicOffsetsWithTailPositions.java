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

package org.sourcelab.kafka.webview.ui.manager.kafka.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Defines a Consumer's Offsets and tail positions for a single topic it is consuming from.
 */
public class ConsumerGroupTopicOffsetsWithTailPositions {
    private final String topic;
    private final Map<Integer, PartitionOffsetWithTailPosition> offsetMap;

    public ConsumerGroupTopicOffsetsWithTailPositions(final String topic, final Map<Integer, PartitionOffsetWithTailPosition> offsetMap) {
        this.topic = topic;
        this.offsetMap = offsetMap;
    }

    public String getTopic() {
        return topic;
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
     * @return List of offsets.
     */
    public List<PartitionOffsetWithTailPosition> getOffsets() {
        final List<PartitionOffsetWithTailPosition> offsetList = new ArrayList<>(offsetMap.values());

        // Sort by partition
        offsetList.sort((o1, o2) -> Integer.valueOf(o1.getPartition()).compareTo(o2.getPartition()));
        return Collections.unmodifiableList(offsetList);
    }

    /**
     * @return Set of all available partitions.
     */
    public Set<Integer> getPartitions() {
        final TreeSet<Integer> partitions = new TreeSet<>(offsetMap.keySet());
        return Collections.unmodifiableSet(partitions);
    }

    /**
     * Marked private to keep from being serialized in responses.
     */
    private Map<Integer, PartitionOffsetWithTailPosition> getOffsetMap() {
        return offsetMap;
    }

}
