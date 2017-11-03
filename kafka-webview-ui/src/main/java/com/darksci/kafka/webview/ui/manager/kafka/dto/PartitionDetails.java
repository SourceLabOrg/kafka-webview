package com.darksci.kafka.webview.ui.manager.kafka.dto;

import java.util.Collections;
import java.util.List;

/**
 * Represent details about a specific partition.
 */
public class PartitionDetails {
    private final String topic;
    private final int partition;
    private final NodeDetails leader;
    private final List<NodeDetails> replicas;
    private final List<NodeDetails> isr;

    /**
     * Constructor.
     */
    public PartitionDetails(
        final String topic,
        final int partition,
        final NodeDetails leader,
        final List<NodeDetails> replicas,
        final List<NodeDetails> isr
    ) {
        this.topic = topic;
        this.partition = partition;
        this.leader = leader;
        this.replicas = Collections.unmodifiableList(replicas);
        this.isr = Collections.unmodifiableList(isr);
    }

    public String getTopic() {
        return topic;
    }

    public int getPartition() {
        return partition;
    }

    public NodeDetails getLeader() {
        return leader;
    }

    public List<NodeDetails> getReplicas() {
        return replicas;
    }

    public List<NodeDetails> getIsr() {
        return isr;
    }

    @Override
    public String toString() {
        return "PartitionDetails{"
            + "topic='" + topic + '\''
            + ", partition=" + partition
            + ", leader=" + leader
            + ", replicas=" + replicas
            + ", isr=" + isr
            + '}';
    }
}
