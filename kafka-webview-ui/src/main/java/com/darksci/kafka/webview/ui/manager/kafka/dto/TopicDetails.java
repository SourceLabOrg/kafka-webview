package com.darksci.kafka.webview.ui.manager.kafka.dto;

import java.util.Collections;
import java.util.List;

/**
 * Represents a Topic along with associated metadata.
 */
public class TopicDetails {
    private final String name;
    private final boolean isInternal;
    private final List<PartitionDetails> partitions;

    /**
     * Constructor.
     */
    public TopicDetails(final String name, final boolean isInternal, final List<PartitionDetails> partitions) {
        this.name = name;
        this.isInternal = isInternal;
        this.partitions = Collections.unmodifiableList(partitions);
    }

    public String getName() {
        return name;
    }

    public List<PartitionDetails> getPartitions() {
        return partitions;
    }

    public boolean isInternal() {
        return isInternal;
    }

    @Override
    public String toString() {
        return "TopicDetails{"
            + "+ name='" + name + '\''
            + ", + isInternal=" + isInternal
            + ", + partitions=" + partitions
            + '}';
    }
}
