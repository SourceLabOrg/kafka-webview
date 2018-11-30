package org.sourcelab.kafka.webview.ui.manager.kafka.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents details about a consumer group offset positions.
 */
public class ConsumerGroupOffsets {
    private final String consumerGroupId;
    private final String topic;
    private final Map<Integer, PartitionOffset> offsetMap;

    /**
     * Constructor.
     * @param consumerGroupId id of consumer group.
     * @param topic name of the topic.
     * @param offsets details about each partition and offset.
     */
    public ConsumerGroupOffsets(final String consumerGroupId, final String topic, final Collection<PartitionOffset> offsets) {
        this.consumerGroupId = consumerGroupId;
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

    public String getConsumerGroupId() {
        return consumerGroupId;
    }

    public String getTopic() {
        return topic;
    }

    public Map<Integer, PartitionOffset> getOffsetMap() {
        return offsetMap;
    }

    public List<PartitionOffset> getOffsets() {
        final List<PartitionOffset> offsetList = new ArrayList<>(offsetMap.values());

        // Sort by partition
        offsetList.sort((o1, o2) -> Integer.valueOf(o1.getPartition()).compareTo(o2.getPartition()));
        return Collections.unmodifiableList(offsetList);
    }

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

    @Override
    public String toString() {
        return "ConsumerGroupOffsets{"
            + "consumerGroupId='" + consumerGroupId + '\''
            + ", topic='" + topic + '\''
            + ", offsetMap=" + offsetMap
            + '}';
    }
}
