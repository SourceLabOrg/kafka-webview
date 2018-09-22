package org.sourcelab.kafka.webview.ui.manager.kafka.dto;

/**
 * DTO class for defining a new Kafka topic.
 */
public class CreateTopic {
    private final String name;
    private final int numberOfPartitions;
    private final short replicaFactor;

    public CreateTopic(final String name, final int numberOfPartitions, final short replicaFactor) {
        this.name = name;
        this.numberOfPartitions = numberOfPartitions;
        this.replicaFactor = replicaFactor;
    }

    public String getName() {
        return name;
    }

    public int getNumberOfPartitions() {
        return numberOfPartitions;
    }

    public short getReplicaFactor() {
        return replicaFactor;
    }

    @Override
    public String toString() {
        return "CreateTopic{"
            + "name='" + name + '\''
            + ", numberOfPartitions=" + numberOfPartitions
            + ", replicaFactor=" + replicaFactor
            + '}';
    }
}
