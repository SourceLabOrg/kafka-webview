package com.darksci.kafkaview.manager.kafka.dto;

import java.util.Collections;
import java.util.Map;

public class ConsumerState {
    private final String topic;
    private final Map<Integer, Long> offsets;

    public ConsumerState(final String topic, final Map<Integer, Long> offsets) {
        this.topic = topic;
        this.offsets = Collections.unmodifiableMap(offsets);
    }

    public String getTopic() {
        return topic;
    }

    public Map<Integer, Long> getOffsets() {
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
