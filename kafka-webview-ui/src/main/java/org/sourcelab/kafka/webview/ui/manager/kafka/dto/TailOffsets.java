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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Mapping of partitions on a topic to their tail positions.
 */
public class TailOffsets {
    private final String topic;
    private final Map<Integer, Long> partitionsToOffsets;

    /**
     * Constructor.
     * @param topic Name of the topic.
     * @param partitionsToOffsets Maps partitionId to the current tail offset on that partition.
     */
    public TailOffsets(final String topic, final Map<Integer, Long> partitionsToOffsets) {
        this.topic = topic;
        final Map<Integer, Long> mapping = new HashMap<>(partitionsToOffsets);
        this.partitionsToOffsets = Collections.unmodifiableMap(mapping);
    }

    public String getTopic() {
        return topic;
    }

    public Map<Integer, Long> getPartitionsToOffsets() {
        return partitionsToOffsets;
    }

    /**
     * For a given partition, return the tail offset.
     * @param partitionId The partition to retrieve the tail offset for.
     * @return The current tail offset position.
     *
     * @throws IllegalArgumentException if passed an invalid partitionId.
     */
    public long getTailOffsetForPartition(final int partitionId) {
        if (!partitionsToOffsets.containsKey(partitionId)) {
            throw new IllegalArgumentException("Invalid partitionId " + partitionId);
        }
        return partitionsToOffsets.get(partitionId);
    }

    public Set<Integer> getPartitions() {
        return partitionsToOffsets.keySet();
    }
}
