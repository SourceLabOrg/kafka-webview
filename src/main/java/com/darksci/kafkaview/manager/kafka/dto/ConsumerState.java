package com.darksci.kafkaview.manager.kafka.dto;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConsumerState {
    private final String topic;
    private final List<PartitionOffset> offsets;

    public ConsumerState(final String topic, final List<PartitionOffset> offsets) {
        this.topic = topic;
        this.offsets = Collections.unmodifiableList(offsets);
    }

    public String getTopic() {
        return topic;
    }

    public List<PartitionOffset> getOffsets() {
        return offsets;
    }

    @Override
    public String toString() {
        return "ConsumerState{" +
            "topic='" + topic + '\'' +
            ", offsets=" + offsets +
            '}';
    }
}
