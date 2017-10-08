package com.darksci.kafkaview.manager.kafka.dto;

public class KafkaResult {
    private final int partition;
    private final long offset;
    private final Object key;
    private final Object value;

    public KafkaResult(final int partition, final long offset, final Object key, final Object value) {
        this.partition = partition;
        this.offset = offset;
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

    @Override
    public String toString() {
        return "KafkaResult{" +
            "partition=" + partition +
            ", offset=" + offset +
            ", key=" + key +
            ", value=" + value +
            '}';
    }
}
