package org.sourcelab.kafka.webview.ui.manager.kafka.dto;

public class PartitionOffsetWithTailPosition {
    private final int partition;
    private final long offset;
    private final long tailOffset;

    /**
     * Constructor.
     */
    public PartitionOffsetWithTailPosition(final int partition, final long offset, final long tailOffset) {
        this.partition = partition;
        this.offset = offset;
        this.tailOffset = tailOffset;
    }

    public int getPartition() {
        return partition;
    }

    public long getOffset() {
        return offset;
    }

    public long getTailOffset() {
        return tailOffset;
    }

    @Override
    public String toString() {
        return "PartitionOffsetWithTailPosition{"
            + "partition=" + partition
            + ", offset=" + offset
            + ", tailOffset=" + tailOffset
            + '}';
    }
}
