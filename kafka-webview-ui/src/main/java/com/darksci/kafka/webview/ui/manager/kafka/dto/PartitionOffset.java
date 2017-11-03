package com.darksci.kafka.webview.ui.manager.kafka.dto;

/**
 * Represents metadata about an offset stored on a particular partition.
 */
public class PartitionOffset {
    private final int partition;
    private final long offset;

    /**
     * Constructor.
     */
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
        return "PartitionOffset{"
            + "partition=" + partition
            + ", offset=" + offset
            + '}';
    }
}
