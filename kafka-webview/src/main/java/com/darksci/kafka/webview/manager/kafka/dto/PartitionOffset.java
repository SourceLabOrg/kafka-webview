package com.darksci.kafka.webview.manager.kafka.dto;

public class PartitionOffset {
    private final int partition;
    private final long offset;

    public PartitionOffset(final int partition, final long offset) {
        this.partition = partition;
        this.offset = offset;
    }

    public int getPartition() {
        return partition;
    }

    public long getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return "PartitionOffset{" +
            "partition=" + partition +
            ", offset=" + offset +
            '}';
    }
}
