package com.darksci.kafkaview.manager.kafka.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KafkaResults {
    private final List<KafkaResult> results;
    private final Map<Integer, Long> startOffsets;
    private final Map<Integer, Long> endOffsets;

    public KafkaResults(final List<KafkaResult> results, final Map<Integer, Long> startOffsets, final Map<Integer, Long> endOffsets) {
        this.results = Collections.unmodifiableList(results);
        this.startOffsets = Collections.unmodifiableMap(startOffsets);
        this.endOffsets = Collections.unmodifiableMap(endOffsets);
    }

    public List<KafkaResult> getResults() {
        return results;
    }

    public Map<Integer, Long> getStartOffsets() {
        return startOffsets;
    }

    public Map<Integer, Long> getEndOffsets() {
        return endOffsets;
    }

    @Override
    public String toString() {
        return "KafkaResults{" +
            "results=" + results +
            ", startOffsets=" + startOffsets +
            ", endOffsets=" + endOffsets +
            '}';
    }

    public static KafkaResults newInstance(final List<KafkaResult> kafkaResults) {
        // Find first & last offsets
        final Map<Integer, Long> firstOffsets = new HashMap<>();
        final Map<Integer, Long> lastOffsets = new HashMap<>();

        // Loop over the results and build map
        for (final KafkaResult kafkaResult: kafkaResults) {
            final int partition = kafkaResult.getPartition();
            final long offset = kafkaResult.getOffset();

            if (firstOffsets.getOrDefault(partition, Long.MAX_VALUE) > offset) {
                firstOffsets.put(partition, offset);
            }
            if (lastOffsets.getOrDefault(partition, -1L) < offset) {
                lastOffsets.put(partition, offset);
            }
        }

        return new KafkaResults(kafkaResults, firstOffsets, lastOffsets);
    }
}
