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
    public ConsumerGroupOffsetsWithTailPositions(final String consumerId, final String topic, final Collection<PartitionOffsetWithTailPosition> offsets) {
        this.consumerId = consumerId;
        this.topic = topic;

        final Map<Integer, PartitionOffsetWithTailPosition> offsetMap = new HashMap<>();
        for (final PartitionOffsetWithTailPosition offset : offsets) {
            offsetMap.put(
                offset.getPartition(),
                offset
            );
        }
        this.offsetMap = Collections.unmodifiableMap(offsetMap);
    }

    public String getConsumerId() {
        return consumerId;
    }

    public String getTopic() {
        return topic;
    }

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
            return offsetOptional.get().getTailOffset();
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
