package com.darksci.kafkaview.manager.kafka.dto;

public class KafkaResult {
    private final int partition;
    private final long offset;
    private final long timestamp;
    private final Object key;
    private final Object value;

    public KafkaResult(final int partition, final long offset, final long timestamp, final Object key, final Object value) {
        this.partition = partition;
        this.offset = offset;
        this.timestamp = timestamp;
        this.key = key;
        this.value = value;
    }

    public int getPartition() {
        return partition;
    }

    public long getOffset() {
        return offset;
    }

    public Object getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "KafkaResult{" +
            "partition=" + partition +
            ", offset=" + offset +
            ", timestamp=" + timestamp +
            ", key=" + key +
            ", value=" + value +
            '}';
    }
}
