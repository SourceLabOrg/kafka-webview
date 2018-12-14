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
